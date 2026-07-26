package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.TargetExam
import com.example.data.model.UserProfile
import com.example.data.model.UserRole
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EduTopAppBar(
    user: UserProfile,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    onExamSelected: (TargetExam) -> Unit,
    onRoleSelected: (UserRole) -> Unit,
    onOpenAuth: () -> Unit = {},
    toastMessage: String?,
    onClearToast: () -> Unit
) {
    var showExamMenu by remember { mutableStateOf(false) }
    var showRoleMenu by remember { mutableStateOf(false) }
    var isSearching by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .statusBarsPadding()
    ) {
        // Notification Toast
        AnimatedVisibility(
            visible = toastMessage != null,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            if (toastMessage != null) {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = toastMessage,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            fontWeight = FontWeight.SemiBold
                        )
                        IconButton(
                            onClick = onClearToast,
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Close Toast", tint = MaterialTheme.colorScheme.onPrimaryContainer)
                        }
                    }
                }
            }
        }

        // Main Header Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Brand & Exam Selector Dropdown
            Column(
                modifier = Modifier.clickable { showExamMenu = true }
            ) {
                Text(
                    text = "GOOD MORNING",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = GeoTextSecondary,
                    letterSpacing = 1.sp
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "EduLive+",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 22.sp,
                        color = GeoTextPrimary
                    )
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = "Select Exam",
                        tint = GeoPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            DropdownMenu(
                expanded = showExamMenu,
                onDismissRequest = { showExamMenu = false }
            ) {
                Text(
                    text = "  Select Target Exam",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = GeoTextSecondary,
                    modifier = Modifier.padding(vertical = 4.dp, horizontal = 12.dp)
                )
                HorizontalDivider()
                TargetExam.values().forEach { exam ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                exam.displayName,
                                fontWeight = if (exam == user.targetExam) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        onClick = {
                            onExamSelected(exam)
                            showExamMenu = false
                        },
                        leadingIcon = {
                            if (exam == user.targetExam) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = GeoPrimary)
                            }
                        }
                    )
                }
            }

            // Right Action Controls: Streak, Role Switch, Search, Notifications
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Streak Badge
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = AccentAmber.copy(alpha = 0.15f),
                    modifier = Modifier.height(32.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocalFireDepartment,
                            contentDescription = "Streak",
                            tint = AccentAmber,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "${user.streakDays}d",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = AccentAmber
                        )
                    }
                }

                // Role Switcher Badge
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                    modifier = Modifier
                        .height(32.dp)
                        .clickable { showRoleMenu = true }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = when (user.role) {
                                UserRole.STUDENT -> Icons.Default.Person
                                UserRole.TEACHER -> Icons.Default.CoPresent
                                UserRole.PARENT -> Icons.Default.FamilyRestroom
                                UserRole.ADMIN -> Icons.Default.AdminPanelSettings
                            },
                            contentDescription = "Role",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = user.role.label,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                DropdownMenu(
                    expanded = showRoleMenu,
                    onDismissRequest = { showRoleMenu = false }
                ) {
                    Text(
                        text = "  Switch Dashboard View",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 4.dp, horizontal = 12.dp)
                    )
                    HorizontalDivider()
                    UserRole.values().forEach { role ->
                        DropdownMenuItem(
                            text = { Text(role.label) },
                            onClick = {
                                onRoleSelected(role)
                                showRoleMenu = false
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = when (role) {
                                        UserRole.STUDENT -> Icons.Default.Person
                                        UserRole.TEACHER -> Icons.Default.CoPresent
                                        UserRole.PARENT -> Icons.Default.FamilyRestroom
                                        UserRole.ADMIN -> Icons.Default.AdminPanelSettings
                                    },
                                    contentDescription = null
                                )
                            }
                        )
                    }
                }

                IconButton(
                    onClick = { isSearching = !isSearching },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = if (isSearching) Icons.Default.Close else Icons.Outlined.Search,
                        contentDescription = "Search",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }

                // Auth / Account Profile Button
                IconButton(
                    onClick = onOpenAuth,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AccountCircle,
                        contentDescription = "Account & Sign In",
                        tint = GeoPrimary
                    )
                }
            }
        }

        // Expandable Search Bar
        AnimatedVisibility(visible = isSearching) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                placeholder = { Text("Search courses, faculties, topics, tests...") },
                leadingIcon = { Icon(Icons.Outlined.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { onSearchQueryChange("") }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear search")
                        }
                    }
                },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                shape = RoundedCornerShape(12.dp)
            )
        }
    }
}
