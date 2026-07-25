package com.example.ui.screens

import androidx.compose.foundation.background
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
import com.example.data.model.*
import com.example.ui.theme.AccentAmber
import com.example.ui.theme.AiCyan
import com.example.ui.theme.SuccessGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    user: UserProfile,
    role: UserRole,
    downloads: List<DownloadEntity>,
    bookmarks: List<BookmarkEntity>,
    testAttempts: List<AttemptEntity>,
    invoices: List<ERPInvoice>,
    parentReport: ParentAnalyticsReport,
    onDeleteDownload: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        when (role) {
            UserRole.TEACHER -> {
                // TEACHER DASHBOARD
                item {
                    Text("👨‍🏫 Educator Control Center", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        StatCard("Active Batches", "4 Batches", Icons.Default.Class, modifier = Modifier.weight(1f))
                        StatCard("Monthly Revenue", "₹2,48,500", Icons.Default.Payments, modifier = Modifier.weight(1f))
                    }
                }

                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("🗓️ Schedule Live Class", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Spacer(modifier = Modifier.height(10.dp))
                            OutlinedTextField(
                                value = "",
                                onValueChange = {},
                                placeholder = { Text("Topic Title (e.g. Organic Isomerism)") },
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Button(onClick = {}, modifier = Modifier.fillMaxWidth()) {
                                Icon(Icons.Default.Add, contentDescription = null)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Publish & Notify Students")
                            }
                        }
                    }
                }
            }

            UserRole.PARENT -> {
                // PARENT ANALYTICS VIEW
                item {
                    Text("👨‍👩‍👧 Parent Monitoring Hub", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                }

                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Student: ${parentReport.studentName}", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.primary)
                            Text("Target Exam: ${parentReport.targetExam}", fontSize = 12.sp)

                            Spacer(modifier = Modifier.height(16.dp))

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                                MetricColumn("Attendance", "${parentReport.attendancePercentage}%")
                                MetricColumn("Avg Score", "${parentReport.avgScorePercent}%")
                                MetricColumn("Study Hours", "${parentReport.studyHoursThisWeek}h")
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Text("💬 Faculty Remark:", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text(parentReport.teacherFeedback, fontSize = 12.sp)
                        }
                    }
                }
            }

            UserRole.ADMIN -> {
                // ADMIN ERPNEXT PANEL
                item {
                    Text("🏛️ ERPNext Educational Admin", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                }

                item {
                    Text("Recent Fee Invoices & GST Audit Logs:", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }

                items(invoices) { inv ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(inv.invoiceId, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text("${inv.studentName} • ${inv.courseTitle}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("Base: ₹${inv.baseAmount} + GST (18%): ₹${inv.gstAmount}", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Text("₹${inv.totalPaid}", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = SuccessGreen)
                        }
                    }
                }
            }

            UserRole.STUDENT -> {
                // STUDENT PROFILE & ROOM OFFLINE DOWNLOADS
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.AccountCircle, contentDescription = null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(user.name, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                                    Text(user.email, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text("🎯 ${user.targetExam.displayName}", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                                }
                            }
                        }
                    }
                }

                item {
                    Text("📥 Offline Encrypted Downloads (Room DB)", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }

                if (downloads.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Text("No lectures downloaded for offline viewing yet.", modifier = Modifier.padding(16.dp), fontSize = 13.sp)
                        }
                    }
                } else {
                    items(downloads) { dl ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(dl.title, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Text("${dl.durationMinutes} mins • ${dl.sizeMB} MB", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                IconButton(onClick = { onDeleteDownload(dl.videoId) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red)
                                }
                            }
                        }
                    }
                }

                item {
                    Text("📊 Recent Test Attempts (Room Persistence)", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }

                if (testAttempts.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Text("Take a mock test in the Tests tab to record scores here.", modifier = Modifier.padding(16.dp), fontSize = 13.sp)
                        }
                    }
                } else {
                    items(testAttempts) { att ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(att.testTitle, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Text("Correct: ${att.correctCount} | Wrong: ${att.wrongCount}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Text("${att.scoreObtained} / ${att.maxScore}", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatCard(title: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(8.dp))
            Text(value, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Text(title, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
