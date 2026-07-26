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
