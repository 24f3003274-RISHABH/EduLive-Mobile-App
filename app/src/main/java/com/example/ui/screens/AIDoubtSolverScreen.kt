package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import com.example.ui.theme.AccentAmber
import com.example.ui.theme.AiCyan

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AIDoubtSolverScreen(
    doubtInput: String,
    subject: String,
    aiResult: String?,
    isLoading: Boolean,
    flashcards: List<Pair<String, String>>,
    onDoubtInputChange: (String) -> Unit,
    onSubjectChange: (String) -> Unit,
    onAskClick: () -> Unit
) {
    val sampleQuestions = listOf(
        "Explain Moment of Inertia for a thin uniform rod about its center",
        "Derive Nernst Equation for electrochemical cell EMF",
        "What is the significance of Kesavananda Bharati case in Indian Polity?",
        "Solve x² - 7x + 12 = 0 step by step",
        "Explain the process of Double Fertilization in Angiosperms"
    )

    val subjects = listOf("Physics", "Chemistry", "Mathematics", "Biology", "Polity & History", "General Aptitude")

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // AI Tutor Header Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(AiCyan.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = "AI", tint = AiCyan, modifier = Modifier.size(24.dp))
                        }
                        Column {
                            Text(
                                text = "EduLive+ AI Tutor",
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                            Text(
                                text = "Instant Doubt Resolution & Smart Flashcards",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Subject Selector Chips
                    Text(text = "Select Subject Domain:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(6.dp))

                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(subjects) { sub ->
                            FilterChip(
                                selected = subject == sub,
                                onClick = { onSubjectChange(sub) },
                                label = { Text(sub, fontSize = 11.sp) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Multimodal Input Field
                    OutlinedTextField(
                        value = doubtInput,
                        onValueChange = onDoubtInputChange,
                        placeholder = { Text("Ask any question or type doubt here...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 100.dp),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Action buttons: Voice, Image, Ask
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            IconButton(onClick = { /* Voice question simulation */ }) {
                                Icon(Icons.Default.Mic, contentDescription = "Voice Input", tint = MaterialTheme.colorScheme.primary)
                            }
                            IconButton(onClick = { /* Image question simulation */ }) {
                                Icon(Icons.Default.AddPhotoAlternate, contentDescription = "Upload Image", tint = MaterialTheme.colorScheme.primary)
                            }
                        }

                        Button(
                            onClick = onAskClick,
                            enabled = !isLoading && doubtInput.isNotBlank(),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp)
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp)
                            } else {
                                Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Solve Doubt")
                            }
                        }
                    }
                }
            }
        }

        // Quick Sample Doubt Chips
        item {
            Column {
                Text(
                    text = "💡 Frequently Asked Doubts",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(8.dp))

                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(sampleQuestions) { sample ->
                        SuggestionChip(
                            onClick = {
                                onDoubtInputChange(sample)
                                onAskClick()
                            },
                            label = { Text(sample, fontSize = 12.sp, maxLines = 1) }
                        )
                    }
                }
            }
        }

        // AI Answer Output Card
        if (aiResult != null) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Psychology, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "AI Faculty Step-by-Step Solution",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = aiResult,
                            fontSize = 14.sp,
                            lineHeight = 20.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }

        // Auto-Generated AI Flashcards
        if (flashcards.isNotEmpty()) {
            item {
                Text(
                    text = "🎴 Auto-Generated AI Flashcards",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            items(flashcards) { (q, a) ->
                var isFlipped by remember { mutableStateOf(false) }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { isFlipped = !isFlipped },
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isFlipped) AccentAmber.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surface
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = if (isFlipped) "💡 Answer:" else "❓ Question (Tap to Flip):",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isFlipped) AccentAmber else MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (isFlipped) a else q,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}
