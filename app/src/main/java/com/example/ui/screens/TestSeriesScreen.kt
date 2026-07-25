package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
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
import com.example.data.model.TestSeries
import com.example.ui.theme.AccentAmber
import com.example.ui.theme.LiveRed
import com.example.ui.theme.SuccessGreen

@Composable
fun TestSeriesScreen(
    testData: TestSeries,
    currentQuestionIndex: Int,
    userAnswers: Map<Int, Int>,
    isTestSubmitted: Boolean,
    lastTestScore: Pair<Int, Int>?,
    onSelectOption: (Int, Int) -> Unit,
    onQuestionIndexChange: (Int) -> Unit,
    onSubmitTest: () -> Unit,
    onResetTest: () -> Unit
) {
    val currentQuestion = testData.questions.getOrNull(currentQuestionIndex) ?: testData.questions.first()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Header Bar with Title and Timer
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 4.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = testData.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Marks: +4 / -1  |  Exam: ${testData.targetExam.displayName}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = AccentAmber.copy(alpha = 0.15f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Timer, contentDescription = "Timer", tint = AccentAmber, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "02:45:10", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = AccentAmber)
                    }
                }
            }
        }

        if (isTestSubmitted && lastTestScore != null) {
            // TEST RESULT SCORECARD SCREEN
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("🎉 TEST SUBMITTED SUCCESSFULLY!", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = "${lastTestScore.first} / ${lastTestScore.second}",
                                fontSize = 36.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text("Total Score Obtained", fontSize = 13.sp, color = MaterialTheme.colorScheme.onPrimaryContainer)

                            Spacer(modifier = Modifier.height(16.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                MetricColumn("Rank", "14 / 24,000")
                                MetricColumn("Percentile", "99.42 %ile")
                                MetricColumn("Accuracy", "80 %")
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Button(onClick = onResetTest, shape = RoundedCornerShape(12.dp)) {
                                Text("Re-Take Test")
                            }
                        }
                    }
                }

                item {
                    Text("📖 Question-by-Question Solution Analysis", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }

                itemsIndexed(testData.questions) { idx, q ->
                    val userAns = userAnswers[idx]
                    val isCorrect = userAns == q.correctOptionIndex

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Q${idx + 1}. ${q.topic}", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text(
                                    text = if (userAns == null) "Unattempted" else if (isCorrect) "✓ Correct (+4)" else "✗ Incorrect (-1)",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = if (userAns == null) Color.Gray else if (isCorrect) SuccessGreen else LiveRed
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(q.questionText, fontSize = 13.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Correct Answer: ${('A' + q.correctOptionIndex)}. ${q.options[q.correctOptionIndex]}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = SuccessGreen
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "💡 Explanation: ${q.explanation}",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        } else {
            // LIVE TEST TAKING CBT INTERFACE
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(16.dp)
            ) {
                // Question Palette Scroll Strip
                Text("Question Palette:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(6.dp))

                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    itemsIndexed(testData.questions) { idx, _ ->
                        val isAnswered = userAnswers.containsKey(idx)
                        val isCurrent = currentQuestionIndex == idx

                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(
                                    when {
                                        isCurrent -> MaterialTheme.colorScheme.primary
                                        isAnswered -> SuccessGreen
                                        else -> MaterialTheme.colorScheme.surfaceVariant
                                    }
                                )
                                .clickable { onQuestionIndexChange(idx) },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${idx + 1}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = if (isCurrent || isAnswered) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Question Box
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = MaterialTheme.colorScheme.primaryContainer
                            ) {
                                Text(
                                    text = "Question ${currentQuestionIndex + 1} of ${testData.questions.size}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                            Text("Topic: ${currentQuestion.topic}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = currentQuestion.questionText,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Options List
                        currentQuestion.options.forEachIndexed { optIdx, optText ->
                            val isSelected = userAnswers[currentQuestionIndex] == optIdx

                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clickable { onSelectOption(currentQuestionIndex, optIdx) },
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                                )
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(
                                        selected = isSelected,
                                        onClick = { onSelectOption(currentQuestionIndex, optIdx) }
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "${('A' + optIdx)}. $optText",
                                        fontSize = 13.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                // Bottom Controls: Previous, Next, Submit
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = { if (currentQuestionIndex > 0) onQuestionIndexChange(currentQuestionIndex - 1) },
                        enabled = currentQuestionIndex > 0
                    ) {
                        Text("Previous")
                    }

                    if (currentQuestionIndex == testData.questions.size - 1) {
                        Button(
                            onClick = onSubmitTest,
                            colors = ButtonDefaults.buttonColors(containerColor = LiveRed)
                        ) {
                            Text("Submit Test", fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Button(
                            onClick = { if (currentQuestionIndex < testData.questions.size - 1) onQuestionIndexChange(currentQuestionIndex + 1) }
                        ) {
                            Text("Next Question")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MetricColumn(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = MaterialTheme.colorScheme.primary)
        Text(label, fontSize = 11.sp, color = MaterialTheme.colorScheme.onPrimaryContainer)
    }
}
