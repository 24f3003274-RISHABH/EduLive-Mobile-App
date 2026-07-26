package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ChatMessage
import com.example.data.model.LivePoll
import com.example.data.model.LiveSession
import com.example.data.model.TargetExam
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Random

data class FloatingReaction(
    val id: Long,
    val emoji: String,
    val startOffsetX: Float
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LiveClassScreen(
    session: LiveSession?,
    allSessions: List<LiveSession> = emptyList(),
    chatMessages: List<ChatMessage>,
    poll: LivePoll,
    isLowBandwidth: Boolean,
    isAudioOnly: Boolean,
    onSendMessage: (String, Boolean) -> Unit,
    onVotePoll: (Int) -> Unit,
    onToggleLowBandwidth: () -> Unit,
    onToggleAudioOnly: () -> Unit,
    onSelectSession: (LiveSession) -> Unit = {},
    onJoinByLink: (String) -> Unit = {},
    onScheduleClass: (String, String, String, TargetExam, String, String, Int, String) -> Unit = { _, _, _, _, _, _, _, _ -> },
    showToast: (String) -> Unit = {}
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val chatListState = rememberLazyListState()

    var messageText by remember { mutableStateOf("") }
    var isDoubtFlagged by remember { mutableStateOf(false) }
    var handRaised by remember { mutableStateOf(false) }
    var handPosition by remember { mutableStateOf(3) }
    var showPollSheet by remember { mutableStateOf(true) }
    var selectedQuality by remember { mutableStateOf("1080p HD 60fps") }
    var isPlaying by remember { mutableStateOf(true) }
    var isFullscreen by remember { mutableStateOf(false) }
    var isPipMode by remember { mutableStateOf(false) }

    // Broadcaster vs Student Watcher Mode (0 = Student Watcher, 1 = Teacher Studio)
    var activeViewMode by remember { mutableIntStateOf(0) }
    var isBroadcastingLive by remember { mutableStateOf(false) }
    var broadcastTimerSeconds by remember { mutableIntStateOf(0) }
    var isMicMuted by remember { mutableStateOf(false) }
    var isCameraFront by remember { mutableStateOf(true) }
    var isWhiteboardActive by remember { mutableStateOf(false) }
    var showTeacherGuideDialog by remember { mutableStateOf(false) }

    // Live Broadcast Timer Effect
    LaunchedEffect(isBroadcastingLive) {
        if (isBroadcastingLive) {
            while (true) {
                delay(1000)
                broadcastTimerSeconds++
            }
        } else {
            broadcastTimerSeconds = 0
        }
    }

    // Dialog States
    var showJoinLinkDialog by remember { mutableStateOf(false) }
    var showScheduleClassDialog by remember { mutableStateOf(false) }
    var showQualityMenu by remember { mutableStateOf(false) }
    var showSessionPicker by remember { mutableStateOf(false) }

    // Input States
    var joinLinkInput by remember { mutableStateOf("") }

    // Floating Emoji Animations
    val floatingReactions = remember { mutableStateListOf<FloatingReaction>() }

    fun triggerReaction(emoji: String) {
        val reaction = FloatingReaction(
            id = System.currentTimeMillis() + Random().nextInt(1000),
            emoji = emoji,
            startOffsetX = (Random().nextFloat() * 0.6f + 0.2f)
        )
        floatingReactions.add(reaction)
        coroutineScope.launch {
            delay(2500)
            floatingReactions.remove(reaction)
        }
    }

    // Auto-scroll chat to latest message
    LaunchedEffect(chatMessages.size) {
        if (chatMessages.isNotEmpty()) {
            chatListState.animateScrollToItem(chatMessages.size - 1)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(GeoBackground)
    ) {
        // --- 0. Role Mode Switcher (Student Watcher vs Teacher Studio) ---
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 2.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = activeViewMode == 0,
                    onClick = { activeViewMode = 0 },
                    label = { Text("📺 Watch Stream (Student)", fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                    leadingIcon = { Icon(Icons.Default.LiveTv, contentDescription = null, modifier = Modifier.size(16.dp)) },
                    modifier = Modifier.weight(1f)
                )

                FilterChip(
                    selected = activeViewMode == 1,
                    onClick = { activeViewMode = 1 },
                    label = { Text("🎥 Teacher Studio (Host Live)", fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                    leadingIcon = { Icon(Icons.Default.Videocam, contentDescription = null, modifier = Modifier.size(16.dp)) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = GeoLiveRed,
                        selectedLabelColor = Color.White,
                        selectedLeadingIconColor = Color.White
                    ),
                    modifier = Modifier.weight(1f)
                )

                IconButton(
                    onClick = { showTeacherGuideDialog = true },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.HelpOutline,
                        contentDescription = "How Live Streaming Works Guide",
                        tint = GeoPrimary
                    )
                }
            }
        }

        if (activeViewMode == 1) {
            // --- TEACHER MOBILE BROADCAST STUDIO MODE ---
            TeacherBroadcastStudioView(
                session = session,
                isBroadcastingLive = isBroadcastingLive,
                broadcastTimerSeconds = broadcastTimerSeconds,
                isMicMuted = isMicMuted,
                isCameraFront = isCameraFront,
                isWhiteboardActive = isWhiteboardActive,
                onToggleBroadcast = {
                    isBroadcastingLive = !isBroadcastingLive
                    if (isBroadcastingLive) {
                        showToast("🔴 YOU ARE NOW LIVE! 198 Students connected.")
                    } else {
                        showToast("⏹️ Live broadcast ended.")
                    }
                },
                onToggleMic = {
                    isMicMuted = !isMicMuted
                    showToast(if (isMicMuted) "🎙️ Microphone Muted" else "🎙️ Microphone Active")
                },
                onToggleCamera = {
                    isCameraFront = !isCameraFront
                    showToast(if (isCameraFront) "📷 Switched to Front Camera" else "📷 Switched to Back Camera")
                },
                onToggleWhiteboard = {
                    isWhiteboardActive = !isWhiteboardActive
                    showToast(if (isWhiteboardActive) "✏️ Digital Whiteboard Overlay ON" else "✏️ Digital Whiteboard OFF")
                },
                onOpenGuide = { showTeacherGuideDialog = true },
                showToast = showToast
            )
        } else {
            // --- STUDENT LIVE CLASSROOM WATCHER MODE ---
        // --- 1. Top Link & Schedule Quick Bar ---
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = GeoSurfaceVariant,
            tonalElevation = 1.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Active Stream Selector Switcher
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { showSessionPicker = true },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(if (session?.status == "LIVE NOW") GeoLiveRed else GeoSecondary)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = session?.title ?: "Select Live Stream",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = GeoTextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = "Switch Stream",
                        tint = GeoPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Hotstar Action Buttons
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    // 🔗 Join via Link
                    IconButton(
                        onClick = { showJoinLinkDialog = true },
                        modifier = Modifier
                            .size(34.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(GeoPrimaryContainer)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Link,
                            contentDescription = "Join Class via Link",
                            tint = GeoOnPrimaryContainer,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // 📅 Schedule Class
                    IconButton(
                        onClick = { showScheduleClassDialog = true },
                        modifier = Modifier
                            .size(34.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(GeoSecondaryContainer)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = "Schedule Class",
                            tint = GeoOnSecondaryContainer,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // 📋 Copy Share Link
                    IconButton(
                        onClick = {
                            val link = session?.shareLink ?: "https://edulive.app/class/${session?.id ?: "live_101"}"
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("Class Link", link))
                            showToast("📋 Class link copied to clipboard! Share with students.")

                            // Share Intent
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_SUBJECT, "Join Live Class: ${session?.title}")
                                putExtra(Intent.EXTRA_TEXT, "🎓 Join my live online seminar on EduLive+: $link")
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "Share Class Link"))
                        },
                        modifier = Modifier
                            .size(34.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(AccentAmber.copy(alpha = 0.2f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share Class Link",
                            tint = AccentAmber,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        // --- 2. Hotstar / IPL Style Live Streaming Player Area ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(if (isFullscreen) 320.dp else 240.dp)
                .background(Color(0xFF090D16)),
            contentAlignment = Alignment.Center
        ) {
            if (isAudioOnly) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.GraphicEq,
                        contentDescription = "Audio Stream",
                        tint = AiCyan,
                        modifier = Modifier.size(52.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "🎧 Ultra Data Saver Audio Stream (32 kbps)",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Text(
                        text = "Consuming 90% less mobile data",
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 11.sp
                    )
                }
            } else {
                // Video Surface Mock Background
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    Color(0xFF1E293B),
                                    Color(0xFF0F172A),
                                    Color(0xFF020617)
                                )
                            )
                        )
                )

                // Hotstar Overhead Live HUD Stats
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(10.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    // Top HUD Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = GeoLiveRed
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .clip(CircleShape)
                                            .background(Color.White)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "LIVE",
                                        color = Color.White,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color.Black.copy(alpha = 0.6f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.People, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "${session?.viewerCount ?: 198} / ${session?.maxStudentsCapacity ?: 200} Online",
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        // Stream Quality & Audio Toggles
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            // Bitrate & Quality Selector
                            Surface(
                                modifier = Modifier.clickable { showQualityMenu = true },
                                shape = RoundedCornerShape(12.dp),
                                color = Color.Black.copy(alpha = 0.6f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Hd, contentDescription = null, tint = AiCyan, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = selectedQuality.take(5),
                                        color = Color.White,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                DropdownMenu(
                                    expanded = showQualityMenu,
                                    onDismissRequest = { showQualityMenu = false }
                                ) {
                                    listOf("1080p HD 60fps", "720p HD", "480p SD", "360p Low Data", "Data Saver (240p)").forEach { qual ->
                                        DropdownMenuItem(
                                            text = { Text(qual, fontSize = 12.sp) },
                                            onClick = {
                                                selectedQuality = qual
                                                showQualityMenu = false
                                                showToast("Stream quality set to $qual")
                                            }
                                        )
                                    }
                                }
                            }

                            IconButton(
                                onClick = onToggleLowBandwidth,
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Speed,
                                    contentDescription = "Data Saver Mode",
                                    tint = if (isLowBandwidth) AccentAmber else Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            IconButton(
                                onClick = onToggleAudioOnly,
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Headphones,
                                    contentDescription = "Audio Only Mode",
                                    tint = if (isAudioOnly) AiCyan else Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }

                    // Center Interactive Whiteboard / Faculty Live Screen
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A).copy(alpha = 0.85f)),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.15f))
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "📐 Interactive Whiteboard Stream",
                                    color = AiCyan,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "3.2 Mbps • 1.1s Latency",
                                    color = Color.White.copy(alpha = 0.6f),
                                    fontSize = 10.sp
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = session?.title ?: "Rotational Mechanics Seminar",
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = session?.description ?: "Live problem solving session with instant student chat and polls.",
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 11.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    // Bottom HUD Controls
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = { isPlaying = !isPlaying },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                    contentDescription = "Play Pause",
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Text(
                                text = session?.instructorName ?: "Prof. Alok Pandey",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            // Raise Hand Button
                            Button(
                                onClick = {
                                    handRaised = !handRaised
                                    if (handRaised) {
                                        showToast("🖐️ Hand Raised! Position #$handPosition in faculty queue")
                                    } else {
                                        showToast("Hand lowered")
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (handRaised) AccentAmber else Color.White.copy(alpha = 0.2f)
                                ),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.height(28.dp)
                            ) {
                                Icon(Icons.Default.PanTool, contentDescription = "Raise Hand", modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (handRaised) "Raised #$handPosition" else "Raise Hand",
                                    fontSize = 10.sp,
                                    color = Color.White
                                )
                            }

                            IconButton(
                                onClick = { isFullscreen = !isFullscreen },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = if (isFullscreen) Icons.Default.FullscreenExit else Icons.Default.Fullscreen,
                                    contentDescription = "Fullscreen Toggle",
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }

                // Floating Reactions Overlay (Hotstar Style)
                floatingReactions.forEach { reaction ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(bottom = 40.dp),
                        contentAlignment = Alignment.BottomStart
                    ) {
                        Text(
                            text = reaction.emoji,
                            fontSize = 28.sp,
                            modifier = Modifier
                                .offset(x = (reaction.startOffsetX * 250).dp)
                        )
                    }
                }
            }
        }

        // --- 3. Hotstar Floating Emoji Reaction Bar ---
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color(0xFF0F172A)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "React Live:",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White.copy(alpha = 0.7f)
                )

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    listOf("❤️", "🔥", "👏", "🎯", "💡", "❓").forEach { emoji ->
                        Surface(
                            shape = CircleShape,
                            color = Color.White.copy(alpha = 0.15f),
                            modifier = Modifier
                                .clickable { triggerReaction(emoji) }
                                .size(32.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(text = emoji, fontSize = 16.sp)
                            }
                        }
                    }
                }
            }
        }

        // --- 4. Live Poll Banner ---
        AnimatedVisibility(visible = poll.isActive && showPollSheet) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                colors = CardDefaults.cardColors(containerColor = GeoPrimaryContainer),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Poll, contentDescription = null, tint = GeoOnPrimaryContainer, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Live Class Quick Poll (${poll.totalVotes} votes)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = GeoOnPrimaryContainer
                            )
                        }
                        IconButton(
                            onClick = { showPollSheet = false },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Close Poll", tint = GeoOnPrimaryContainer)
                        }
                    }

                    Text(
                        text = poll.question,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = GeoOnPrimaryContainer,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )

                    poll.options.forEachIndexed { idx, opt ->
                        val pct = poll.votesPercent.getOrElse(idx) { 0 }
                        val isUserOption = poll.activeOptionIndex == idx

                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 3.dp)
                                .clickable { onVotePoll(idx) },
                            shape = RoundedCornerShape(10.dp),
                            color = if (isUserOption) GeoPrimary else MaterialTheme.colorScheme.surface
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "${('A' + idx)}. $opt",
                                    fontSize = 12.sp,
                                    fontWeight = if (isUserOption) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isUserOption) Color.White else GeoTextPrimary,
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    text = "$pct%",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isUserOption) Color.White else GeoPrimary
                                )
                            }
                        }
                    }
                }
            }
        }

        // --- 5. Real-time Student Chat Feed (200+ Students Capacity) ---
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "💬 Live Student Chat",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = GeoTextPrimary
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = GeoSecondaryContainer
                    ) {
                        Text(
                            text = "${chatMessages.size} messages",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = GeoOnSecondaryContainer,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Text(
                    text = "High-speed 200+ Room",
                    fontSize = 11.sp,
                    color = GeoTextSecondary
                )
            }

            LazyColumn(
                state = chatListState,
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(chatMessages) { msg ->
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = when {
                            msg.isTeacher -> GeoPrimaryContainer
                            msg.isDoubtQuestion -> AccentAmber.copy(alpha = 0.15f)
                            else -> MaterialTheme.colorScheme.surface
                        },
                        border = BorderStroke(
                            1.dp,
                            when {
                                msg.isTeacher -> GeoPrimary.copy(alpha = 0.3f)
                                msg.isDoubtQuestion -> AccentAmber.copy(alpha = 0.4f)
                                else -> GeoBorder
                            }
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = if (msg.isTeacher) "👨‍🏫 ${msg.senderName}" else msg.senderName,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = if (msg.isTeacher) GeoOnPrimaryContainer else GeoTextPrimary
                                    )
                                    if (msg.isDoubtQuestion) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = AccentAmber
                                        ) {
                                            Text(
                                                text = "DOUBT",
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.Black,
                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                            )
                                        }
                                    }
                                }
                                Text(text = msg.timestamp, fontSize = 10.sp, color = GeoTextSecondary)
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = msg.text,
                                fontSize = 13.sp,
                                color = GeoTextPrimary
                            )
                        }
                    }
                }
            }

            // Chat Input Bar
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, GeoBorder)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { isDoubtFlagged = !isDoubtFlagged }) {
                        Icon(
                            imageVector = Icons.Default.Help,
                            contentDescription = "Flag as Doubt",
                            tint = if (isDoubtFlagged) AccentAmber else GeoTextSecondary
                        )
                    }

                    OutlinedTextField(
                        value = messageText,
                        onValueChange = { messageText = it },
                        placeholder = { Text(if (isDoubtFlagged) "Ask doubt to faculty..." else "Type message...") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp)
                    )

                    Spacer(modifier = Modifier.width(4.dp))

                    IconButton(
                        onClick = {
                            if (messageText.isNotBlank()) {
                                onSendMessage(messageText, isDoubtFlagged)
                                messageText = ""
                                isDoubtFlagged = false
                            }
                        }
                    ) {
                        Icon(Icons.Default.Send, contentDescription = "Send", tint = GeoPrimary)
                    }
                }
            }
        }
        } // End of activeViewMode == 0 (Student Watcher Mode)
    } // End of main Column

    // --- TEACHER GUIDE DIALOG: How Live Streaming & Student Joining Works ---
    if (showTeacherGuideDialog) {
        AlertDialog(
            onDismissRequest = { showTeacherGuideDialog = false },
            icon = { Icon(Icons.Default.LiveTv, contentDescription = null, tint = GeoLiveRed) },
            title = { Text("📡 How Live Streaming Works", fontWeight = FontWeight.Bold) },
            text = {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    item {
                        Text(
                            text = "Welcome Teacher! Here is how your students join your live classes smoothly without technical issues:",
                            fontSize = 13.sp,
                            color = GeoTextSecondary
                        )
                    }

                    item {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = GeoPrimaryContainer.copy(alpha = 0.5f),
                            border = BorderStroke(1.dp, GeoPrimary.copy(alpha = 0.3f))
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text("🎥 Method 1: Mobile Camera Broadcast", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = GeoPrimary)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("1. Switch to 'Teacher Studio' tab at the top.\n2. Tap '🔴 START LIVE CAMERA STREAM'.\n3. Tap 'Copy & Share WhatsApp Link'.\n4. Students tap the link to join directly in the app!", fontSize = 11.sp, color = GeoTextPrimary)
                            }
                        }
                    }

                    item {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = GeoSecondaryContainer.copy(alpha = 0.5f),
                            border = BorderStroke(1.dp, GeoSecondary.copy(alpha = 0.3f))
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text("💻 Method 2: OBS Studio / PC Streaming", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = GeoSecondary)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("1. Copy the RTMP Server & Stream Key provided in Teacher Studio.\n2. Paste them into OBS Studio or Streamlabs on PC.\n3. Click 'Start Streaming' in OBS to broadcast 4K 60fps HD live video!", fontSize = 11.sp, color = GeoTextPrimary)
                            }
                        }
                    }

                    item {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = AccentAmber.copy(alpha = 0.15f),
                            border = BorderStroke(1.dp, AccentAmber.copy(alpha = 0.4f))
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text("👥 Method 3: 200+ Students Joining via Link", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = AccentAmber)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Students do not need complex logins! They can paste the link under 'Join via Link' or open the link directly from WhatsApp or Telegram.", fontSize = 11.sp, color = GeoTextPrimary)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { showTeacherGuideDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = GeoPrimary),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Got It, Let's Stream!")
                }
            }
        )
    }

    // --- DIALOG 1: Join Class via Link / Code ---
    if (showJoinLinkDialog) {
        AlertDialog(
            onDismissRequest = { showJoinLinkDialog = false },
            icon = { Icon(Icons.Default.Link, contentDescription = null, tint = GeoPrimary) },
            title = { Text("🔗 Join Live Class via Link", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text(
                        text = "Paste any seminar link or class access code to join the live stream instantly:",
                        fontSize = 13.sp,
                        color = GeoTextSecondary
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = joinLinkInput,
                        onValueChange = { joinLinkInput = it },
                        placeholder = { Text("e.g. https://edulive.app/class/jee_physics_101 or room_101") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "💡 Quick Try Examples:",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = GeoTextPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    listOf("jee_physics_101", "neet_bio_202", "upsc_polity_303").forEach { code ->
                        Text(
                            text = "• edulive.app/class/$code",
                            fontSize = 12.sp,
                            color = GeoPrimary,
                            modifier = Modifier
                                .clickable { joinLinkInput = code }
                                .padding(vertical = 2.dp)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showJoinLinkDialog = false
                        onJoinByLink(joinLinkInput)
                        joinLinkInput = ""
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GeoPrimary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Join Stream")
                }
            },
            dismissButton = {
                TextButton(onClick = { showJoinLinkDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // --- DIALOG 2: Schedule Online Class & Generate Link ---
    if (showScheduleClassDialog) {
        var newTitle by remember { mutableStateOf("") }
        var newSubject by remember { mutableStateOf("") }
        var newInstructor by remember { mutableStateOf("Prof. Alok Pandey") }
        var newTime by remember { mutableStateOf("Today at 06:00 PM") }
        var newUrl by remember { mutableStateOf("https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4") }
        var newCapacity by remember { mutableStateOf("200") }
        var newDesc by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showScheduleClassDialog = false },
            icon = { Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = GeoPrimary) },
            title = { Text("📅 Schedule Class & Share Link", fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Configure your live seminar room for up to 500 students:", fontSize = 12.sp, color = GeoTextSecondary)
                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = newTitle,
                        onValueChange = { newTitle = it },
                        label = { Text("Class / Seminar Title") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = newSubject,
                        onValueChange = { newSubject = it },
                        label = { Text("Subject / Batch Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = newTime,
                        onValueChange = { newTime = it },
                        label = { Text("Scheduled Time (e.g. Today 6 PM / LIVE NOW)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = newCapacity,
                        onValueChange = { newCapacity = it },
                        label = { Text("Max Student Capacity (e.g. 200)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = newUrl,
                        onValueChange = { newUrl = it },
                        label = { Text("HLS / Video Stream URL") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newTitle.isBlank()) {
                            showToast("Please enter a title for the class")
                            return@Button
                        }
                        val cap = newCapacity.toIntOrNull() ?: 200
                        onScheduleClass(
                            newTitle,
                            if (newSubject.isNotBlank()) newSubject else "Live Seminar",
                            newInstructor,
                            TargetExam.JEE_MAIN,
                            newTime,
                            newUrl,
                            cap,
                            newDesc
                        )
                        showScheduleClassDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GeoPrimary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Schedule & Get Link")
                }
            },
            dismissButton = {
                TextButton(onClick = { showScheduleClassDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // --- DIALOG 3: Switch Active Stream Room ---
    if (showSessionPicker) {
        AlertDialog(
            onDismissRequest = { showSessionPicker = false },
            icon = { Icon(Icons.Default.LiveTv, contentDescription = null, tint = GeoPrimary) },
            title = { Text("📺 Select Active Live Stream Room", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Select a scheduled online class or seminar stream:", fontSize = 12.sp, color = GeoTextSecondary)
                    Spacer(modifier = Modifier.height(10.dp))

                    allSessions.forEach { sess ->
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable {
                                    onSelectSession(sess)
                                    showSessionPicker = false
                                },
                            shape = RoundedCornerShape(12.dp),
                            color = if (sess.id == session?.id) GeoPrimaryContainer else MaterialTheme.colorScheme.surface,
                            border = BorderStroke(1.dp, GeoBorder)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = sess.title,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = GeoTextPrimary,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = if (sess.status == "LIVE NOW") GeoLiveRed else GeoSecondary
                                    ) {
                                        Text(
                                            text = sess.status,
                                            fontSize = 9.sp,
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Instructor: ${sess.instructorName} • Capacity: ${sess.maxStudentsCapacity}",
                                    fontSize = 11.sp,
                                    color = GeoTextSecondary
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showSessionPicker = false }) {
                    Text("Close")
                }
            }
        )
    }
}

@Composable
fun TeacherBroadcastStudioView(
    session: LiveSession?,
    isBroadcastingLive: Boolean,
    broadcastTimerSeconds: Int,
    isMicMuted: Boolean,
    isCameraFront: Boolean,
    isWhiteboardActive: Boolean,
    onToggleBroadcast: () -> Unit,
    onToggleMic: () -> Unit,
    onToggleCamera: () -> Unit,
    onToggleWhiteboard: () -> Unit,
    onOpenGuide: () -> Unit,
    showToast: (String) -> Unit
) {
    val context = LocalContext.current
    val formatTime = { sec: Int ->
        val m = sec / 60
        val s = sec % 60
        String.format("%02d:%02d", m, s)
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // --- 1. Camera Studio Viewport Frame ---
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                border = BorderStroke(2.dp, if (isBroadcastingLive) GeoLiveRed else GeoBorder)
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    // Camera Simulation Graphic Canvas
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    listOf(
                                        Color(0xFF1E293B),
                                        Color(0xFF0F172A),
                                        Color(0xFF020617)
                                    )
                                )
                            )
                    ) {
                        // Camera framing crosshair icon / Whiteboard overlay
                        if (isWhiteboardActive) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Brush,
                                    contentDescription = "Whiteboard",
                                    tint = AiCyan,
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "📐 DIGITAL WHITEBOARD BROADCAST OVERLAY",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = AiCyan
                                )
                                Text(
                                    text = "Equations & Notes are being recorded live for 200+ students",
                                    fontSize = 10.sp,
                                    color = Color.White.copy(alpha = 0.7f)
                                )
                            }
                        } else {
                            // Camera Feed Indicator
                            Column(
                                modifier = Modifier.align(Alignment.Center),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = Color.White.copy(alpha = 0.15f),
                                    modifier = Modifier.size(72.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Videocam,
                                        contentDescription = "Camera Feed",
                                        tint = if (isBroadcastingLive) GeoLiveRed else Color.White,
                                        modifier = Modifier
                                            .padding(16.dp)
                                            .fillMaxSize()
                                    )
                                }
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = if (isCameraFront) "📷 Front HD Camera Active" else "📷 Back Wide Lens Active",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                                Text(
                                    text = "1080p 60FPS • Auto Focus & Noise Suppression ON",
                                    color = Color.White.copy(alpha = 0.7f),
                                    fontSize = 11.sp
                                )
                            }
                        }

                        // Top Live HUD Controls
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp)
                                .align(Alignment.TopStart),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isBroadcastingLive) GeoLiveRed else Color.DarkGray
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(Color.White)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (isBroadcastingLive) "🔴 LIVE BROADCAST (${formatTime(broadcastTimerSeconds)})" else "READY TO BROADCAST",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = Color.White
                                    )
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color.Black.copy(alpha = 0.6f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Group, contentDescription = null, tint = AccentAmber, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (isBroadcastingLive) "198 Students Connected" else "0 / 200 Max",
                                        fontSize = 11.sp,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }

                        // Bottom Quick Controls on Camera Overlay
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp)
                                .align(Alignment.BottomCenter),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = onToggleCamera,
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.25f))
                            ) {
                                Icon(Icons.Default.FlipCameraAndroid, contentDescription = "Flip Camera", tint = Color.White)
                            }

                            IconButton(
                                onClick = onToggleMic,
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(if (isMicMuted) GeoLiveRed else Color.White.copy(alpha = 0.25f))
                            ) {
                                Icon(
                                    imageVector = if (isMicMuted) Icons.Default.MicOff else Icons.Default.Mic,
                                    contentDescription = "Mute Mic",
                                    tint = Color.White
                                )
                            }

                            IconButton(
                                onClick = onToggleWhiteboard,
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(if (isWhiteboardActive) AiCyan else Color.White.copy(alpha = 0.25f))
                            ) {
                                Icon(Icons.Default.Brush, contentDescription = "Whiteboard", tint = if (isWhiteboardActive) Color.Black else Color.White)
                            }
                        }
                    }
                }
            }
        }

        // --- 2. Main Broadcast Action Trigger ---
        item {
            Button(
                onClick = onToggleBroadcast,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isBroadcastingLive) Color(0xFFDC2626) else GeoPrimary
                ),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Icon(
                    imageVector = if (isBroadcastingLive) Icons.Default.StopCircle else Icons.Default.RadioButtonChecked,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isBroadcastingLive) "⏹️ END LIVE BROADCAST NOW" else "🔴 START LIVE MOBILE CAMERA STREAM NOW",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        }

        // --- 3. Class Link Sharing Hub (WhatsApp & Telegram) ---
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = GeoSurfaceVariant),
                border = BorderStroke(1.dp, GeoBorder)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("🔗 Class Direct Access Link", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = GeoTextPrimary)
                        Surface(shape = RoundedCornerShape(6.dp), color = AccentAmber) {
                            Text("Share for 200+ Students", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.Black, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    val shareUrl = session?.shareLink ?: "https://edulive.app/class/${session?.id ?: "jee_physics_101"}"

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, GeoBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = shareUrl,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = GeoPrimary,
                            modifier = Modifier.padding(10.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                clipboard.setPrimaryClip(ClipData.newPlainText("Class Link", shareUrl))
                                showToast("📋 Link Copied! Send it on WhatsApp / Telegram.")

                                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_SUBJECT, "Join Live Class: ${session?.title ?: "Physics Seminar"}")
                                    putExtra(Intent.EXTRA_TEXT, "🎓 Teacher is LIVE! Click link to join class: $shareUrl")
                                }
                                context.startActivity(Intent.createChooser(shareIntent, "Share Class Link"))
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = GeoSecondary),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Share on WhatsApp", fontSize = 12.sp)
                        }

                        OutlinedButton(
                            onClick = onOpenGuide,
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Help, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Guide", fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // --- 4. OBS Studio / PC Live Broadcaster Settings ---
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, GeoBorder)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("💻 Want to Stream from PC / OBS Studio?", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = GeoTextPrimary)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Copy these RTMP credentials into OBS Studio -> Settings -> Stream:", fontSize = 11.sp, color = GeoTextSecondary)

                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Server RTMP URL:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = GeoTextPrimary)
                    Text("rtmp://live.edulive.app/app/", fontSize = 11.sp, color = GeoSecondary)

                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Stream Key:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = GeoTextPrimary)
                    Text("live_key_${session?.id ?: "jee_physics_101"}", fontSize = 11.sp, color = GeoLiveRed)
                }
            }
        }
    }
}
