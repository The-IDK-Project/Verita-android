package io.theidkteam.verita.ui.roomlist

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Login
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import org.matrix.android.sdk.api.session.room.timeline.getLastMessageContent
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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
    
    // Demo data for design purposes if no real rooms
    val demoRooms = listOf(
        DemoRoom("1", "Android Devs", "Hello world! 🔥 5", "12:45"),
        DemoRoom("2", "Verita Support", "How can I help you? 👍 2", "11:20")
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Verita", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                    IconButton(onClick = onNavigateToLogin) {
                        Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Logout")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Special login button if not logged in or as a shortcut
            item {
                ListItem(
                    headlineContent = { Text("LOGIN TO YOUR ACCOUNT", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold) },
                    supportingContent = { Text("Tap here to sign in with Matrix") },
                    leadingContent = { Icon(Icons.Default.Login, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                    modifier = Modifier.clickable { onNavigateToLogin() }
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), thickness = 0.5.dp)
            }

            if (rooms.isEmpty()) {
                item {
                    Text(
                        "No real rooms found. Showing demo data:",
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray
                    )
                }
                // Render demo chats
                items(demoRooms) { room ->
                    DemoRoomItem(room = room, onClick = { onNavigateToChat(room.id) })
                }
            } else {
                items(rooms) { room ->
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

data class DemoRoom(val id: String, val name: String, val lastMsg: String, val time: String)

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
