package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PinDrop
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.assistant.AiModelType
import com.example.data.model.ChatMessage
import com.example.data.model.MessageSender
import com.example.ui.theme.AccentAmber
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.AccentEmerald
import com.example.ui.theme.AccentRose
import com.example.ui.theme.BackgroundDark
import com.example.ui.theme.SurfaceBorderDark
import com.example.ui.theme.SurfaceCardDark
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun AssistantChatView(
    messages: List<ChatMessage>,
    isAiThinking: Boolean,
    isRawJsonVisible: Boolean,
    rawJsonContext: String,
    selectedModelType: AiModelType = AiModelType.GEMINI_3_8_FLASH,
    isMapsGroundingEnabled: Boolean = true,
    isRecordingVoice: Boolean = false,
    onSelectModel: (AiModelType) -> Unit = {},
    onToggleMapsGrounding: () -> Unit = {},
    onStartVoiceRecording: () -> Unit = {},
    onStopVoiceRecording: () -> Unit = {},
    onLaunchLiveVoice: () -> Unit = {},
    onClearHistory: () -> Unit = {},
    onSendMessage: (String) -> Unit,
    onTriggerPromptInjectionTest: () -> Unit,
    onTriggerRateLimitTest: () -> Unit,
    onToggleRawJson: () -> Unit,
    modifier: Modifier = Modifier
) {
    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    // Auto-scroll to latest message
    LaunchedEffect(messages.size, isAiThinking) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    val suggestionChips = listOf(
        "Did John go to the grocery store today?",
        "When did Emma leave school?",
        "Where was Michael at 2 PM?",
        "Did Sarah visit the pharmacy today?",
        "Where did John go today?",
        "Where was David at 4 PM?" // Tests Rule 1: No data available
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .imePadding()
    ) {
        // --- 1. Assistant Header & Controls ---
        Surface(
            color = SurfaceCardDark,
            border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorderDark),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(AccentCyan.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = "AI Assistant",
                                tint = AccentCyan,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "AI Movement Assistant",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.VerifiedUser,
                                    contentDescription = "Verified",
                                    tint = AccentCyan,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Multi-Turn Gemini • Zero Hallucinations",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = AccentCyan,
                                        fontWeight = FontWeight.Medium
                                    )
                                )
                            }
                        }
                    }

                    // Action Header: Live Voice button & View JSON
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        // Live Voice Button (gemini-3.8-live)
                        Button(
                            onClick = onLaunchLiveVoice,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = AccentEmerald,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                            modifier = Modifier
                                .height(38.dp)
                                .testTag("launch_live_voice_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.RecordVoiceOver,
                                contentDescription = "Live Voice",
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Live Voice",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Toggle Raw JSON Context Inspector
                        Button(
                            onClick = onToggleRawJson,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isRawJsonVisible) AccentCyan else Color(0xFF334155),
                                contentColor = if (isRawJsonVisible) Color(0xFF00363A) else Color.White
                            ),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                            modifier = Modifier
                                .height(38.dp)
                                .testTag("toggle_raw_json_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Code,
                                contentDescription = "Inspect JSON",
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }

                // Collapsible Raw JSON Data Inspector
                AnimatedVisibility(visible = isRawJsonVisible) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF0A0F1D)),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E293B))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Raw Trajectory Telemetry (Rule 1 & 5 Verified)",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = AccentAmber
                                    )
                                )
                                Text(
                                    text = "Coordinates masked for privacy in chat",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = Color.LightGray,
                                        fontSize = 10.sp
                                    )
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = rawJsonContext,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 10.sp,
                                    color = Color(0xFF94A3B8)
                                ),
                                maxLines = 10
                            )
                        }
                    }
                }
            }
        }

        // --- 2. Model Selector & Google Maps Grounding Bar ---
        Surface(
            color = Color(0xFF0B132B),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E293B)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Models list
                val models = listOf(
                    AiModelType.GEMINI_3_8_FLASH,
                    AiModelType.GEMINI_3_1_FLASH_LITE,
                    AiModelType.GEMINI_4_ARGON
                )

                models.forEach { model ->
                    val isSelected = selectedModelType == model
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isSelected) AccentCyan else Color(0xFF1E293B),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isSelected) AccentCyan else Color(0xFF334155)
                        ),
                        modifier = Modifier
                            .clickable { onSelectModel(model) }
                            .testTag("model_chip_${model.modelId}")
                    ) {
                        Text(
                            text = when (model) {
                                AiModelType.GEMINI_3_8_FLASH -> "3.8 Flash (General)"
                                AiModelType.GEMINI_3_1_FLASH_LITE -> "3.1 Flash-Lite (Fast)"
                                AiModelType.GEMINI_4_ARGON -> "4 Argon (Complex)"
                                else -> model.displayName
                            },
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) Color(0xFF00363A) else Color.White
                            ),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }

                // Google Maps Grounding Switch Chip
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (isMapsGroundingEnabled) AccentEmerald.copy(alpha = 0.2f) else Color(0xFF1E293B),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isMapsGroundingEnabled) AccentEmerald else Color(0xFF334155)
                    ),
                    modifier = Modifier
                        .clickable { onToggleMapsGrounding() }
                        .testTag("maps_grounding_toggle")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.PinDrop,
                            contentDescription = "Maps Grounding",
                            tint = if (isMapsGroundingEnabled) AccentEmerald else Color.Gray,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isMapsGroundingEnabled) "Google Maps Grounding ON" else "Maps Grounding OFF",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = if (isMapsGroundingEnabled) AccentEmerald else Color.Gray,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }

                // Clear history button
                IconButton(
                    onClick = onClearHistory,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Clear History",
                        tint = Color.LightGray,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        // --- 3. Quick Suggestion & Test Chips Bar ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(SurfaceCardDark.copy(alpha = 0.5f))
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Test Case 6: Injection Attack Test Chip
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = AccentRose.copy(alpha = 0.15f),
                border = androidx.compose.foundation.BorderStroke(1.dp, AccentRose),
                modifier = Modifier
                    .clickable { onTriggerPromptInjectionTest() }
                    .testTag("test_injection_chip")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = "Security Test",
                        tint = AccentRose,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "TC06 Injection Test",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = AccentRose,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }

            // Test Case 5: Rate Limiting Stress Test Chip
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = AccentAmber.copy(alpha = 0.15f),
                border = androidx.compose.foundation.BorderStroke(1.dp, AccentAmber),
                modifier = Modifier
                    .clickable { onTriggerRateLimitTest() }
                    .testTag("test_ratelimit_chip")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Timer,
                        contentDescription = "Rate Limit Test",
                        tint = AccentAmber,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "TC05 Rate Limit Test",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = AccentAmber,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }

            // Ground Truth Questions
            suggestionChips.forEach { query ->
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFF1E293B),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155)),
                    modifier = Modifier
                        .clickable { onSendMessage(query) }
                        .testTag("chip_${query.take(15).replace(" ", "_")}")
                ) {
                    Text(
                        text = query,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = TextSecondary,
                            fontWeight = FontWeight.Medium
                        )
                    )
                }
            }
        }

        // --- 4. Chat Messages Stream ---
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(messages, key = { it.id }) { message ->
                ChatMessageItem(message = message)
            }

            if (isAiThinking) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(AccentCyan.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp,
                                color = AccentCyan
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Consulting ${selectedModelType.displayName}...",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = TextSecondary,
                                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                            )
                        )
                    }
                }
            }
        }

        // Audio Recording Active Banner
        if (isRecordingVoice) {
            Surface(
                color = AccentRose.copy(alpha = 0.2f),
                border = androidx.compose.foundation.BorderStroke(1.dp, AccentRose),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.GraphicEq,
                            contentDescription = "Recording",
                            tint = AccentRose,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Listening... tap stop to transcribe with gemini-3.5-transcribe",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }

                    Button(
                        onClick = onStopVoiceRecording,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AccentRose,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Stop,
                            contentDescription = "Stop",
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Stop", fontSize = 11.sp)
                    }
                }
            }
        }

        // --- 5. Accessible Large Input Bar with Speech Transcription ---
        Surface(
            color = SurfaceCardDark,
            border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorderDark),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    placeholder = {
                        Text(
                            text = "Ask where family members have been...",
                            color = Color(0xFF64748B),
                            fontSize = 14.sp
                        )
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("chat_input_field"),
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFF0F172A),
                        unfocusedContainerColor = Color(0xFF0F172A),
                        focusedBorderColor = AccentCyan,
                        unfocusedBorderColor = Color(0xFF334155),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                    keyboardActions = KeyboardActions(
                        onSend = {
                            if (inputText.isNotBlank()) {
                                onSendMessage(inputText)
                                inputText = ""
                            }
                        }
                    ),
                    trailingIcon = {
                        // Microphone button for gemini-3.5-transcribe
                        IconButton(
                            onClick = {
                                if (isRecordingVoice) {
                                    onStopVoiceRecording()
                                } else {
                                    onStartVoiceRecording()
                                }
                            },
                            modifier = Modifier
                                .size(48.dp)
                                .testTag("mic_transcribe_button")
                        ) {
                            Icon(
                                imageVector = if (isRecordingVoice) Icons.Default.Stop else Icons.Default.Mic,
                                contentDescription = "Transcribe Audio",
                                tint = if (isRecordingVoice) AccentRose else AccentCyan
                            )
                        }
                    }
                )

                Spacer(modifier = Modifier.width(8.dp))

                // High-contrast, large touch target send button (>= 48dp)
                Button(
                    onClick = {
                        if (inputText.isNotBlank()) {
                            onSendMessage(inputText)
                            inputText = ""
                        }
                    },
                    enabled = inputText.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AccentCyan,
                        contentColor = Color(0xFF00363A),
                        disabledContainerColor = Color(0xFF1E293B),
                        disabledContentColor = Color(0xFF475569)
                    ),
                    shape = CircleShape,
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("send_message_button"),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send Message",
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

/**
 * Converts markdown-style bullets and bold tags into high-contrast formatted text for mobile display
 */
fun formatChatMarkdown(text: String): AnnotatedString {
    return buildAnnotatedString {
        val lines = text.lines()
        lines.forEachIndexed { lineIdx, rawLine ->
            var line = rawLine.trim()

            // Check for bullet prefixes (*, -, •)
            val isBullet = line.startsWith("* ") || line.startsWith("- ") || line.startsWith("• ")
            if (isBullet) {
                withStyle(SpanStyle(color = AccentCyan, fontWeight = FontWeight.Bold)) {
                    append("  •  ")
                }
                line = line.drop(2).trim()
            }

            // Split by ** to extract bold segments without complex regex ranges
            val boldParts = line.split("**")
            boldParts.forEachIndexed { idx, part ->
                if (part.isNotEmpty()) {
                    if (idx % 2 == 1) {
                        // Odd indices are inside **...**
                        withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = Color.White)) {
                            append(part)
                        }
                    } else {
                        // Even indices are normal text
                        append(part)
                    }
                }
            }

            if (lineIdx < lines.size - 1) {
                append("\n")
            }
        }
    }
}

@Composable
fun ChatMessageItem(message: ChatMessage) {
    val isUser = message.sender == MessageSender.USER
    val annotatedText = remember(message.text) { formatChatMarkdown(message.text) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .testTag("message_${message.id}"),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        if (!isUser) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(
                        when {
                            message.isCoolingDownAlert -> AccentAmber.copy(alpha = 0.2f)
                            message.isSecurityRefusal -> AccentRose.copy(alpha = 0.2f)
                            else -> AccentCyan.copy(alpha = 0.2f)
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when {
                        message.isCoolingDownAlert -> Icons.Default.Timer
                        message.isSecurityRefusal -> Icons.Default.Lock
                        else -> Icons.Default.AutoAwesome
                    },
                    contentDescription = "Assistant",
                    tint = when {
                        message.isCoolingDownAlert -> AccentAmber
                        message.isSecurityRefusal -> AccentRose
                        else -> AccentCyan
                    },
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
        }

        Card(
            shape = RoundedCornerShape(
                topStart = 18.dp,
                topEnd = 18.dp,
                bottomStart = if (isUser) 18.dp else 4.dp,
                bottomEnd = if (isUser) 4.dp else 18.dp
            ),
            colors = CardDefaults.cardColors(
                containerColor = when {
                    isUser -> Color(0xFF0284C7)
                    message.isCoolingDownAlert -> Color(0xFF451A03)
                    message.isSecurityRefusal -> Color(0xFF4C0519)
                    else -> Color(0xFF1E293B)
                }
            ),
            border = androidx.compose.foundation.BorderStroke(
                1.dp,
                when {
                    isUser -> Color(0xFF38BDF8).copy(alpha = 0.5f)
                    message.isCoolingDownAlert -> AccentAmber
                    message.isSecurityRefusal -> AccentRose
                    else -> Color(0xFF334155)
                }
            ),
            modifier = Modifier.widthIn(min = 100.dp, max = 340.dp)
        ) {
            Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)) {
                // Header tag for Assistant
                if (!isUser) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = AccentCyan,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "NearNest AI",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = AccentCyan,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )
                            )
                        }

                        if (message.isGroundingVerified) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Verified Grounding",
                                    tint = AccentEmerald,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = "Maps Grounded",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = AccentEmerald,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 10.sp
                                    )
                                )
                            }
                        }
                    }
                }

                if (message.isCoolingDownAlert) {
                    Text(
                        text = "TC05: Rate Limit Active",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = AccentAmber,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                }

                if (message.isSecurityRefusal) {
                    Text(
                        text = "TC06: System Security Defense",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = AccentRose,
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                }

                // Styled Markdown Message Content with High Contrast (Rule 6)
                Text(
                    text = annotatedText,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        color = if (isUser) Color.White else Color(0xFFF1F5F9),
                        fontSize = 15.sp,
                        lineHeight = 22.sp
                    )
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = message.timestamp12h,
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = if (isUser) Color(0xFFE0F2FE) else Color(0xFF94A3B8),
                        fontSize = 10.sp
                    ),
                    modifier = Modifier.align(Alignment.End)
                )
            }
        }
    }
}

