package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.UserRole
import com.example.ui.theme.*

data class NavigationTabItem(
    val index: Int,
    val title: String,
    val activeIcon: ImageVector,
    val inactiveIcon: ImageVector,
    val isLiveBadge: Boolean = false,
    val isAiSpark: Boolean = false
)

@Composable
fun EduBottomBar(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    userRole: UserRole
) {
    val tabs = listOf(
        NavigationTabItem(0, "Home", Icons.Filled.Home, Icons.Outlined.Home),
        NavigationTabItem(1, "Batches", Icons.Filled.VideoLibrary, Icons.Outlined.VideoLibrary),
        NavigationTabItem(2, "Live", Icons.Filled.LiveTv, Icons.Outlined.LiveTv, isLiveBadge = true),
        NavigationTabItem(3, "EduAI", Icons.Filled.AutoAwesome, Icons.Outlined.AutoAwesome, isAiSpark = true),
        NavigationTabItem(4, "Tests", Icons.Filled.Quiz, Icons.Outlined.Quiz),
        NavigationTabItem(5, "Forum", Icons.Filled.Forum, Icons.Outlined.Forum),
        NavigationTabItem(
            6,
            when (userRole) {
                UserRole.STUDENT -> "More"
                UserRole.TEACHER -> "Educator"
                UserRole.PARENT -> "Parent"
                UserRole.ADMIN -> "ERP Admin"
            },
            Icons.Filled.Dashboard,
            Icons.Outlined.Dashboard
        )
    )

    NavigationBar(
        containerColor = GeoSurfaceVariant,
        tonalElevation = 0.dp
    ) {
        tabs.forEach { tab ->
            val isSelected = selectedTab == tab.index

            NavigationBarItem(
                selected = isSelected,
                onClick = { onTabSelected(tab.index) },
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = GeoPrimaryContainer,
                    selectedIconColor = GeoOnPrimaryContainer,
                    selectedTextColor = GeoOnPrimaryContainer,
                    unselectedIconColor = GeoTextMuted,
                    unselectedTextColor = GeoTextMuted
                ),
                icon = {
                    Box(contentAlignment = Alignment.TopEnd) {
                        Icon(
                            imageVector = if (isSelected) tab.activeIcon else tab.inactiveIcon,
                            contentDescription = tab.title
                        )

                        if (tab.isLiveBadge) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .offset(x = 4.dp, y = (-2).dp)
                                    .clip(CircleShape)
                                    .background(GeoLiveRed)
                            )
                        }
                    }
                },
                label = {
                    Text(
                        text = tab.title,
                        fontSize = 10.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    )
                }
            )
        }
    }
}
