package com.example.data.telemetry

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Genuine Android device battery telemetry model.
 */
data class BatteryInfo(
    val percentage: Int, // 0..100, or -1 if unavailable
    val isCharging: Boolean = false
) {
    val displayString: String
        get() = if (percentage in 0..100) "$percentage%" else "--%"

    val isAvailable: Boolean
        get() = percentage in 0..100
}

/**
 * Reads genuine local device battery levels via Android BatteryManager & ACTION_BATTERY_CHANGED.
 * Never fabricates or generates random battery percentages.
 */
class DeviceBatteryManager(private val context: Context) {

    private val _batteryInfo = MutableStateFlow(readCurrentBattery())
    val batteryInfo: StateFlow<BatteryInfo> = _batteryInfo.asStateFlow()

    fun readCurrentBattery(): BatteryInfo {
        return try {
            val bm = context.getSystemService(Context.BATTERY_SERVICE) as? BatteryManager
            val capacity = bm?.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY) ?: -1

            val intent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
            val isCharging = intent?.let {
                val status = it.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
                status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL
            } ?: false

            val finalPercent = if (capacity in 0..100) {
                capacity
            } else if (intent != null) {
                val rawLevel = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
                val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
                if (rawLevel >= 0 && scale > 0) {
                    (rawLevel * 100) / scale
                } else {
                    -1
                }
            } else {
                -1
            }
            BatteryInfo(percentage = finalPercent, isCharging = isCharging)
        } catch (_: Throwable) {
            BatteryInfo(percentage = -1, isCharging = false)
        }
    }

    fun refresh() {
        _batteryInfo.value = readCurrentBattery()
    }
}

