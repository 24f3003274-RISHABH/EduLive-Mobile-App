package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Course
import com.example.data.model.VideoLecture
import com.example.ui.theme.AccentAmber
import com.example.ui.theme.SuccessGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CourseExplorerScreen(
    courses: List<Course>,
    selectedCourse: Course?,
    lectures: List<VideoLecture>,
    enrolledCourseIds: Set<String>,
    onSelectCourse: (Course) -> Unit,
    onEnrollCourse: (String) -> Unit,
    onDownloadLecture: (VideoLecture) -> Unit
) {
    var showCheckoutModal by remember { mutableStateOf(false) }
    var couponInput by remember { mutableStateOf("") }
    var couponApplied by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Course Selector Header Bar
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    text = "Explore & Enroll Batches",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            // List of Courses
            items(courses) { course ->
                val isEnrolled = enrolledCourseIds.contains(course.id)
                val isSelected = selectedCourse?.id == course.id

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelectCourse(course) },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = course.title,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                modifier = Modifier.weight(1f)
                            )
                            if (isEnrolled) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = SuccessGreen.copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        text = "✓ ENROLLED",
                                        color = SuccessGreen,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "👨‍🏫 Faculty: ${course.instructorName}",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.AccessTime, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("${course.durationHours} hrs total", fontSize = 11.sp)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.VideoLibrary, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("${course.totalLectures} lectures", fontSize = 11.sp)
                            }
                        }

                        if (!isEnrolled) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = {
                                    onSelectCourse(course)
                                    showCheckoutModal = true
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("Enroll Now - ₹${course.priceDiscounted}", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Selected Course Detailed Syllabus & Lectures
            if (selectedCourse != null) {
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "📚 Syllabus & Recorded Lectures for '${selectedCourse.title}'",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }

                items(lectures) { lecture ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PlayCircle,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = lecture.title,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 13.sp
                                    )
                                    Text(
                                        text = "${lecture.durationMinutes} mins • High Quality HLS",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            IconButton(onClick = { onDownloadLecture(lecture) }) {
                                Icon(
                                    imageVector = Icons.Default.Download,
                                    contentDescription = "Download",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Checkout Modal Sheet
    if (showCheckoutModal && selectedCourse != null) {
        val finalPrice = if (couponApplied) (selectedCourse.priceDiscounted * 0.9).toInt() else selectedCourse.priceDiscounted
        val gstAmount = (finalPrice * 0.18).toInt()
        val totalPayable = finalPrice + gstAmount

        ModalBottomSheet(onDismissRequest = { showCheckoutModal = false }) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Text(text = "Checkout Order Summary", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text(text = selectedCourse.title, fontSize = 14.sp, color = MaterialTheme.colorScheme.primary)

                Spacer(modifier = Modifier.height(16.dp))

                // Coupon Code Input
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = couponInput,
                        onValueChange = { couponInput = it },
                        placeholder = { Text("Enter Coupon (e.g. EDULIVE10)") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    Button(onClick = {
                        if (couponInput.trim().uppercase() == "EDULIVE10") {
                            couponApplied = true
                        }
                    }) {
                        Text(if (couponApplied) "Applied ✓" else "Apply")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Price Breakdown
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Course Price")
                    Text("₹$finalPrice")
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("GST (18%)")
                    Text("₹$gstAmount")
                }
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Total Payable Amount", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text("₹$totalPayable", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.primary)
                }

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = {
                        onEnrollCourse(selectedCourse.id)
                        showCheckoutModal = false
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Pay ₹$totalPayable via Razorpay / UPI", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
