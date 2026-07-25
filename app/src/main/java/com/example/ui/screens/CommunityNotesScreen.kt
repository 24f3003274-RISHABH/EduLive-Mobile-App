package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CommunityPost
import com.example.ui.theme.AccentAmber
import com.example.ui.theme.SuccessGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommunityNotesScreen(
    posts: List<CommunityPost>,
    onBookmarkNote: (String, String) -> Unit
) {
    var selectedSection by remember { mutableStateOf(0) } // 0: Forum, 1: PDF Notes & Mind Maps

    val pdfNotesList = listOf(
        Pair("Rotational Motion Formula Sheet PDF", "Physics • 12 Pages • 2.4 MB"),
        Pair("NCERT Biology Mind Maps 2026", "Botany & Zoology • 45 Pages • 8.1 MB"),
        Pair("Indian Polity Articles & Landmark Cases", "UPSC IAS • 28 Pages • 5.0 MB"),
        Pair("Calculus High-Yield Question Bank", "Mathematics • 32 Pages • 4.2 MB")
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Top Tab Row
        TabRow(selectedTabIndex = selectedSection) {
            Tab(
                selected = selectedSection == 0,
                onClick = { selectedSection = 0 },
                text = { Text("💬 Discussion Forum") }
            )
            Tab(
                selected = selectedSection == 1,
                onClick = { selectedSection = 1 },
                text = { Text("📑 PDF Notes & Mind Maps") }
            )
        }

        if (selectedSection == 0) {
            // COMMUNITY DISCUSSION FORUM
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(posts) { post ->
                    var upvotes by remember { mutableStateOf(post.upvotes) }
                    var isUpvoted by remember { mutableStateOf(false) }

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.AccountCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Column {
                                        Text(post.authorName, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        Text(post.authorRole, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                                Text(post.timestamp, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(post.title, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(post.content, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)

                            if (post.verifiedAnswer != null) {
                                Spacer(modifier = Modifier.height(10.dp))
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = SuccessGreen.copy(alpha = 0.12f)),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Row(modifier = Modifier.padding(10.dp)) {
                                        Icon(Icons.Default.Verified, contentDescription = null, tint = SuccessGreen, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(post.verifiedAnswer, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(onClick = {
                                        if (isUpvoted) { upvotes--; isUpvoted = false } else { upvotes++; isUpvoted = true }
                                    }) {
                                        Icon(
                                            imageVector = Icons.Default.ThumbUp,
                                            contentDescription = "Upvote",
                                            tint = if (isUpvoted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Text("$upvotes", fontSize = 12.sp, fontWeight = FontWeight.Bold)

                                    Spacer(modifier = Modifier.width(16.dp))

                                    Icon(Icons.Default.Comment, contentDescription = "Comments", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("${post.commentCount} replies", fontSize = 12.sp)
                                }

                                IconButton(onClick = { onBookmarkNote(post.title, post.content) }) {
                                    Icon(Icons.Default.BookmarkBorder, contentDescription = "Bookmark")
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // PDF NOTES & MIND MAPS
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(pdfNotesList) { (title, subtitle) ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PictureAsPdf,
                                    contentDescription = "PDF",
                                    tint = AccentAmber,
                                    modifier = Modifier.size(32.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Text(subtitle, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }

                            IconButton(onClick = { onBookmarkNote(title, subtitle) }) {
                                Icon(Icons.Default.Download, contentDescription = "Download PDF", tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }
            }
        }
    }
}
