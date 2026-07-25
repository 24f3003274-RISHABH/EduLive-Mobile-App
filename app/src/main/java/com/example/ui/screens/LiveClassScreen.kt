package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ChatMessage
import com.example.data.model.LivePoll
import com.example.data.model.LiveSession
import com.example.ui.theme.AccentAmber
import com.example.ui.theme.AiCyan
import com.example.ui.theme.LiveRed

@Composable
fun LiveClassScreen(
    session: LiveSession?,
    chatMessages: List<ChatMessage>,
    poll: LivePoll,
    isLowBandwidth: Boolean,
    isAudioOnly: Boolean,
    onSendMessage: (String, Boolean) -> Unit,
    onVotePoll: (Int) -> Unit,
    onToggleLowBandwidth: () -> Unit,
    onToggleAudioOnly: () -> Unit
) {
    var messageText by remember { mutableStateOf("") }
    var isDoubtFlagged by remember { mutableStateOf(false) }
    var handRaised by remember { mutableStateOf(false) }
    var showPollSheet by remember { mutableStateOf(true) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // 1. Live Video Stream Player Area
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(230.dp)
                .background(Color(0xFF0F172A)),
            contentAlignment = Alignment.Center
        ) {
            if (isAudioOnly) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.GraphicEq,
                        contentDescription = "Audio Stream",
                        tint = AiCyan,
                        modifier = Modifier.size(56.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "🎧 Audio-Only Mode Active (32kbps)",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            } else {
                // Interactive Whiteboard & Faculty Simulation
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = LiveRed
                            ) {
                                Text(
                                    text = "LIVE",
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "👀 ${session?.viewerCount ?: 14850} Students",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Quality & Bandwidth Toggles
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            IconButton(
                                onClick = onToggleLowBandwidth,
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Speed,
                                    contentDescription = "Data Saver Mode",
                                    tint = if (isLowBandwidth) AccentAmber else Color.White
                                )
                            }

                            IconButton(
                                onClick = onToggleAudioOnly,
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Headphones,
                                    contentDescription = "Audio Only Mode",
                                    tint = if (isAudioOnly) AiCyan else Color.White
                                )
                            }
                        }
                    }

                    // Simulated Whiteboard Formulas / Diagram
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B).copy(alpha = 0.9f)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "📐 Whiteboard: ${session?.title ?: "Rotational Dynamics"}",
                                color = AiCyan,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Torque τ = I * α = dL/dt  |  Rolling Condition: v_cm = ω * R",
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Faculty Info Bar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Faculty: ${session?.instructorName ?: "Prof. Alok Pandey"}",
                            color = Color.White.copy(alpha = 0.9f),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )

                        // Raise Hand Button
                        Button(
                            onClick = { handRaised = !handRaised },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (handRaised) AccentAmber else Color.White.copy(alpha = 0.2f)
                            ),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.PanTool, contentDescription = "Raise Hand", modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (handRaised) "Hand Raised ✋" else "Raise Hand 🖐️",
                                fontSize = 11.sp,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }

        // 2. Interactive Live Poll Banner
        AnimatedVisibility(visible = poll.isActive && showPollSheet) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "⚡ Live Class Poll (${poll.totalVotes} votes)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        IconButton(
                            onClick = { showPollSheet = false },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Close Poll")
                        }
                    }

                    Text(
                        text = poll.question,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )

                    poll.options.forEachIndexed { idx, opt ->
                        val pct = poll.votesPercent.getOrElse(idx) { 0 }
                        val isUserOption = poll.activeOptionIndex == idx

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable { onVotePoll(idx) },
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "${('A' + idx)}. $opt",
                                fontSize = 12.sp,
                                fontWeight = if (isUserOption) FontWeight.Bold else FontWeight.Normal,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = "$pct%",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }

        // 3. Real-time Live Chat Stream
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 12.dp)
        ) {
            Text(
                text = "💬 Live Student Chat",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(vertical = 6.dp)
            )

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(chatMessages) { msg ->
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = when {
                            msg.isTeacher -> MaterialTheme.colorScheme.primaryContainer
                            msg.isDoubtQuestion -> AccentAmber.copy(alpha = 0.15f)
                            else -> MaterialTheme.colorScheme.surface
                        },
                        tonalElevation = 1.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = if (msg.isTeacher) "👨‍🏫 ${msg.senderName}" else msg.senderName,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = if (msg.isTeacher) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                )
                                Text(text = msg.timestamp, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = msg.text,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurface
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
                shape = RoundedCornerShape(16.dp),
                tonalElevation = 4.dp
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
                            tint = if (isDoubtFlagged) AccentAmber else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    OutlinedTextField(
                        value = messageText,
                        onValueChange = { messageText = it },
                        placeholder = { Text(if (isDoubtFlagged) "Ask doubt to faculty..." else "Type message...") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.width(4.dp))

                    IconButton(
                        onClick = {
                            onSendMessage(messageText, isDoubtFlagged)
                            messageText = ""
                            isDoubtFlagged = false
                        }
                    ) {
                        Icon(Icons.Default.Send, contentDescription = "Send", tint = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }
    }
}
