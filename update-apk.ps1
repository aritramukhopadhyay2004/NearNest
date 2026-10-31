<#
.SYNOPSIS
    NearNest (FamTrack AI) - In-Place APK Generator & 20-Day Change Sync Script

.DESCRIPTION
    Checks if any project source code, layouts, dependencies, or assets have been
    modified within the last 20 days (or since the last APK build). If changes are detected,
    it automatically compiles a fresh APK using Gradle, replaces 'apk/NearNest-latest.apk'
    in-place, updates 'apk/build_info.json', and optionally commits & pushes to GitHub.

.PARAMETER Force
    Rebuilds and updates the APK in-place immediately, bypassing the 20-day change check.

.PARAMETER Push
    Automatically stages, commits, and pushes the updated APK to GitHub origin/main.
#>

[CmdletBinding()]
param(
    [switch]$Force,
    [switch]$Push
)

$ErrorActionPreference = "Stop"
$ProjectRoot = $PSScriptRoot
Set-Location $ProjectRoot

Write-Host "==========================================================" -ForegroundColor Cyan
Write-Host "  NearNest: In-Place APK Generator & 20-Day Change Sync   " -ForegroundColor Cyan
Write-Host "==========================================================" -ForegroundColor Cyan

$ApkDir = Join-Path $ProjectRoot "apk"
$ApkTarget = Join-Path $ApkDir "NearNest-latest.apk"
$BuildInfoFile = Join-Path $ApkDir "build_info.json"
$Gradblew = Join-Path $ProjectRoot "gradlew.bat"

# Ensure target apk directory exists
if (-not (Test-Path $ApkDir)) {
    New-Item -ItemType Directory -Path $ApkDir -Force | Out-Null
}

$CutoffDays = 20
$CutoffDate = (Get-Date).AddDays(-$CutoffDays)
$NeedsRebuild = $false
$Reason = ""

# 1. Check if APK exists
if (-not (Test-Path $ApkTarget)) {
    $NeedsRebuild = $true
    $Reason = "Target APK 'apk/NearNest-latest.apk' does not exist yet."
}
elseif ($Force) {
    $NeedsRebuild = $true
    $Reason = "Force build requested (-Force)."
}
else {
    # 2. Check Git commits and previous build info
    $currentGitCommit = try { (git rev-parse --short HEAD 2>$null).Trim() } catch { "" }
    $lastBuildInfo = $null
    if (Test-Path $BuildInfoFile) {
        try {
            $lastBuildInfo = Get-Content $BuildInfoFile -Raw | ConvertFrom-Json
        } catch {}
    }

    if ($lastBuildInfo -and $lastBuildInfo.gitCommit -and ($lastBuildInfo.gitCommit -ne $currentGitCommit)) {
        $recentCommits = try { git log --since="$CutoffDays days ago" --oneline -n 1 2>$null } catch { "" }
        if ($recentCommits) {
            $NeedsRebuild = $true
            $Reason = "New git commit detected ($currentGitCommit, previous build was on $($lastBuildInfo.gitCommit))."
        }
    }

    # 3. Check filesystem changes in app/src or gradle configs within 20 days
    if (-not $NeedsRebuild) {
        $watchedPaths = @(
            (Join-Path $ProjectRoot "app\src"),
            (Join-Path $ProjectRoot "app\build.gradle.kts"),
            (Join-Path $ProjectRoot "build.gradle.kts"),
            (Join-Path $ProjectRoot "gradle\libs.versions.toml")
        )

        $lastApkWrite = (Get-Item $ApkTarget).LastWriteTime
        $changedFiles = Get-ChildItem -Path $watchedPaths -Recurse -File -ErrorAction SilentlyContinue |
            Where-Object { $_.LastWriteTime -gt $CutoffDate -and $_.LastWriteTime -gt $lastApkWrite }

        if ($changedFiles -and $changedFiles.Count -gt 0) {
            $NeedsRebuild = $true
            $Reason = "$($changedFiles.Count) source file(s) modified within the last $CutoffDays days (e.g. $($changedFiles[0].Name))."
        }
    }
}

# 4. If no rebuild needed, report status and exit cleanly
if (-not $NeedsRebuild) {
    $currentApk = Get-Item $ApkTarget
    $apkSizeMb = [math]::Round($currentApk.Length / 1MB, 2)
    Write-Host "[OK] No code changes detected in the last $CutoffDays days." -ForegroundColor Green
    Write-Host "     Current APK: $ApkTarget ($apkSizeMb MB, last updated: $($currentApk.LastWriteTime))" -ForegroundColor Gray
    Write-Host "     Tip: Use '.\update-apk.ps1 -Force' to rebuild anyway." -ForegroundColor Yellow
    exit 0
}

# 5. Build Fresh APK
Write-Host "[+] $Reason" -ForegroundColor Yellow
Write-Host "[+] Compiling fresh debug APK with Gradle..." -ForegroundColor Cyan

$buildStart = Get-Date
& $Gradblew assembleDebug --no-daemon

if ($LASTEXITCODE -ne 0) {
    Write-Host "[ERROR] Gradle build failed with exit code $LASTEXITCODE." -ForegroundColor Red
    exit $LASTEXITCODE
}

# 6. Copy and Overwrite In-Place
$SourceApk = Join-Path $ProjectRoot "app\build\outputs\apk\debug\app-debug.apk"
if (-not (Test-Path $SourceApk)) {
    Write-Host "[ERROR] Compiled APK not found at: $SourceApk" -ForegroundColor Red
    exit 1
}

Copy-Item -Path $SourceApk -Destination $ApkTarget -Force
(Get-Item $ApkTarget).LastWriteTime = Get-Date
$buildDuration = [math]::Round(((Get-Date) - $buildStart).TotalSeconds, 1)

$newApk = Get-Item $ApkTarget
$newApkMb = [math]::Round($newApk.Length / 1MB, 2)

# 7. Write build metadata record (apk/build_info.json)
$gitHash = try { (git rev-parse --short HEAD 2>$null) } catch { "unknown" }
$gitMessage = try { (git log -1 --pretty=%B 2>$null).Trim() } catch { "Local Build" }

$buildInfo = [PSCustomObject]@{
    apkFileName       = "NearNest-latest.apk"
    buildDate         = (Get-Date).ToString("o")
    fileSizeBytes     = $newApk.Length
    fileSizeMb        = $newApkMb
    buildDurationSecs = $buildDuration
    triggerReason     = $Reason
    gitCommit         = $gitHash
    gitMessage        = $gitMessage
    checkWindowDays   = $CutoffDays
}

$buildInfo | ConvertTo-Json -Depth 4 | Set-Content -Path $BuildInfoFile -Encoding utf8

Write-Host "==========================================================" -ForegroundColor Green
Write-Host "  SUCCESS: Updated APK In-Place!                         " -ForegroundColor Green
Write-Host "==========================================================" -ForegroundColor Green
Write-Host "  File:     $ApkTarget" -ForegroundColor White
Write-Host "  Size:     $newApkMb MB" -ForegroundColor White
Write-Host "  Duration: $buildDuration seconds" -ForegroundColor White
Write-Host "  Commit:   $gitHash" -ForegroundColor White
Write-Host "  Metadata: $BuildInfoFile" -ForegroundColor White

# 8. Optional Git Push
if ($Push) {
    Write-Host "`n[+] Pushing updated APK and metadata to GitHub..." -ForegroundColor Cyan
    git add "apk/NearNest-latest.apk" "apk/build_info.json"
    git commit -m "chore(apk): update NearNest-latest.apk in-place ($gitHash) [skip ci]"
    git push origin main
    Write-Host "[OK] Pushed latest APK to GitHub origin/main." -ForegroundColor Green
}
