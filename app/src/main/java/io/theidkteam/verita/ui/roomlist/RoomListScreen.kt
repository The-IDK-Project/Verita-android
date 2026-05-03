package io.theidkteam.verita.ui.roomlist

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Login
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import org.matrix.android.sdk.api.session.room.timeline.getLastMessageContent
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ramcosta.composedestinations.annotation.Destination

import androidx.hilt.navigation.compose.hiltViewModel
import org.matrix.android.sdk.api.session.room.model.RoomSummary
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue

@OptIn(ExperimentalMaterial3Api::class)
@Destination
@Composable
fun RoomListScreen(
    onNavigateToSettings: () -> Unit,
    onNavigateToLogin: () -> Unit,
    onNavigateToChat: (String) -> Unit,
    viewModel: RoomListViewModel = hiltViewModel()
) {
    val rooms by viewModel.rooms?.collectAsState(initial = emptyList()) ?: remember { mutableStateOf(emptyList<RoomSummary>()) }
    
    var selectedFolder by remember { mutableStateOf("All") }
    val folders = listOf("All", "Personal", "Groups", "Channels", "Bots")

    val filteredRooms = remember(rooms, selectedFolder) {
        when (selectedFolder) {
            "Personal" -> rooms.filter { it.isDirect }
            "Groups" -> rooms.filter { !it.isDirect }
            "Channels" -> rooms.filter { it.roomId.contains("channel") } // Heuristic
            "Bots" -> rooms.filter { it.displayName.contains("bot", ignoreCase = true) }
            else -> rooms
        }
    }
    
    // Demo data for design purposes if no real rooms
    val demoRooms = listOf(
        DemoRoom("1", "Android Devs", "Hello world! 🔥 5", "12:45", isDirect = false),
        DemoRoom("2", "Verita Support", "How can I help you? 👍 2", "11:20", isDirect = true),
        DemoRoom("3", "Telegram Bot", "How to use bridge? 🤖", "10:05", isDirect = true)
    )

    val filteredDemoRooms = remember(selectedFolder) {
        when (selectedFolder) {
            "Personal" -> demoRooms.filter { it.isDirect && !it.name.contains("Bot") }
            "Groups" -> demoRooms.filter { !it.isDirect }
            "Bots" -> demoRooms.filter { it.name.contains("Bot") }
            else -> demoRooms
        }
    }

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = { Text("Verita", fontWeight = FontWeight.Bold) },
                    actions = {
                        IconButton(onClick = onNavigateToSettings) {
                            Icon(Icons.Default.Settings, contentDescription = "Settings")
                        }
                        IconButton(onClick = {
                            viewModel.logout()
                            onNavigateToLogin()
                        }) {
                            Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Logout")
                        }
                    }
                )
                
                // Folder Tabs (Horizontal Scrollable)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    folders.forEach { folder ->
                        val isSelected = selectedFolder == folder
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedFolder = folder },
                            label = { Text(folder) },
                            leadingIcon = if (isSelected) {
                                { Icon(Icons.Default.Folder, contentDescription = null, modifier = Modifier.size(16.dp)) }
                            } else null
                        )
                    }
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Special login button if not logged in or as a shortcut
            if (selectedFolder == "All") {
                item {
                    ListItem(
                        headlineContent = { Text("LOGIN TO YOUR ACCOUNT", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold) },
                        supportingContent = { Text("Tap here to sign in with Matrix") },
                        leadingContent = { Icon(Icons.Default.Login, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                        modifier = Modifier.clickable { onNavigateToLogin() }
                    )
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), thickness = 0.5.dp)
                }
            }

            if (rooms.isEmpty()) {
                item {
                    Text(
                        "No real rooms found for category '$selectedFolder'. Showing demo data:",
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray
                    )
                }
                // Render demo chats
                items(filteredDemoRooms) { room ->
                    DemoRoomItem(room = room, onClick = { onNavigateToChat(room.id) })
                }
            } else {
                items(filteredRooms) { room ->
                    RealRoomItem(room = room, onClick = { onNavigateToChat(room.roomId) })
                }
            }
        }
    }
}

@Composable
fun RealRoomItem(room: RoomSummary, onClick: () -> Unit) {
    ListItem(
        headlineContent = { 
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(room.displayName, fontWeight = FontWeight.SemiBold)
                Text(room.latestPreviewableEvent?.root?.originServerTs?.toString() ?: "", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            }
        },
        supportingContent = { Text(room.latestPreviewableEvent?.getLastMessageContent()?.body ?: "No messages", maxLines = 1, fontSize = 14.sp) },
        leadingContent = {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(room.displayName.take(1), color = MaterialTheme.colorScheme.onPrimaryContainer, fontWeight = FontWeight.Bold)
            }
        },
        modifier = Modifier.clickable { onClick() }
    )
}

data class DemoRoom(val id: String, val name: String, val lastMsg: String, val time: String, val isDirect: Boolean = false)

@Composable
fun DemoRoomItem(room: DemoRoom, onClick: () -> Unit) {
    ListItem(
        headlineContent = { 
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(room.name, fontWeight = FontWeight.SemiBold)
                Text(room.time, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            }
        },
        supportingContent = { Text(room.lastMsg, maxLines = 1, fontSize = 14.sp) },
        leadingContent = {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(room.name.take(1), color = MaterialTheme.colorScheme.onPrimaryContainer, fontWeight = FontWeight.Bold)
            }
        },
        modifier = Modifier.clickable { onClick() }
    )
}
