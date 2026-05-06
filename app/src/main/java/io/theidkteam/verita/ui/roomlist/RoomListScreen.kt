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
import io.theidkteam.verita.deltachat.DeltaChatRoom
import androidx.compose.material.icons.filled.Email
import io.theidkteam.verita.utils.resolveMatrixUrl
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale

@OptIn(ExperimentalMaterial3Api::class)
@Destination
@Composable
fun RoomListScreen(
    onNavigateToSettings: () -> Unit,
    onNavigateToLogin: () -> Unit,
    onNavigateToChat: (String) -> Unit,
    viewModel: RoomListViewModel = hiltViewModel()
) {
    val rooms by viewModel.rooms.collectAsState(initial = emptyList())
    val deltaRooms by viewModel.deltaRooms.collectAsState(initial = emptyList())
    val syncState by viewModel.syncState.collectAsState()
    val contentUrlResolver = viewModel.getContentUrlResolver()
    
    var selectedFolder by remember { mutableStateOf("All") }
    val folders = listOf("All", "Personal", "Delta Chat (Beta)", "Groups", "Channels", "Bots")

    val filteredRooms = remember(rooms, selectedFolder) {
        when (selectedFolder) {
            "Personal" -> rooms.filter { it.isDirect }
            "Groups" -> rooms.filter { !it.isDirect }
            "Channels" -> rooms.filter { it.roomId.contains("channel") || it.roomId.contains("space") }
            "Bots" -> rooms.filter { it.displayName.contains("bot", ignoreCase = true) }
            else -> rooms
        }
    }

    val filteredDeltaRooms = remember(deltaRooms, selectedFolder) {
        if (selectedFolder == "All" || selectedFolder == "Delta Chat (Beta)") {
            deltaRooms
        } else {
            emptyList()
        }
    }

    val isSyncing = syncState is org.matrix.android.sdk.api.session.sync.SyncState.Running
    val isEmpty = filteredRooms.isEmpty() && filteredDeltaRooms.isEmpty()
    
    // Demo data for design purposes if no real rooms
    val demoRooms = listOf(
        DemoRoom("1", "Android Devs", "Hello world!", "12:45", isDirect = false),
        DemoRoom("2", "Verita Support", "How can I help you?", "11:20", isDirect = true),
        DemoRoom("3", "Telegram Bot", "How to use bridge?", "10:05", isDirect = true)
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
                
                // Folder Tabs
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    folders.forEach { folder ->
                        val isSelected = selectedFolder == folder
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedFolder = folder },
                            label = { 
                                Text(if (folder == "Delta Chat (Beta)") "Delta Chat 🧪" else folder) 
                            },
                            leadingIcon = if (isSelected) {
                                { Icon(Icons.Default.Folder, contentDescription = null, modifier = Modifier.size(16.dp)) }
                            } else null
                        )
                    }
                    
                    if (isSyncing) {
                        Spacer(Modifier.width(8.dp))
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
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
            if (selectedFolder == "Delta Chat (Beta)" && deltaRooms.isEmpty()) {
                item {
                    DeltaChatBetaPlaceholder(onEnable = { viewModel.enableDeltaChat() })
                }
            }

            // Special login button if not logged in
            if (selectedFolder == "All" && !viewModel.isLogged) {
                item {
                    ListItem(
                        headlineContent = { Text("CONNECT MATRIX ACCOUNT", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold) },
                        supportingContent = { Text("Sign in to see your Matrix rooms") },
                        leadingContent = { Icon(Icons.Default.Login, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                        modifier = Modifier.clickable { onNavigateToLogin() }
                    )
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), thickness = 0.5.dp)
                }
            }

            if (isEmpty) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            if (isSyncing) "Synchronizing chats..." else "No chats found in '$selectedFolder'",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray
                        )
                        if (!isSyncing && selectedFolder == "All") {
                            Text(
                                "Showing demo chats for preview:",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.LightGray,
                                modifier = Modifier.padding(top = 16.dp)
                            )
                        }
                    }
                }
                
                if (selectedFolder == "All" || selectedFolder == "Personal" || selectedFolder == "Groups") {
                    items(filteredDemoRooms) { room ->
                        DemoRoomItem(room = room, onClick = { onNavigateToChat(room.id) })
                    }
                }
            } else {
                items(filteredDeltaRooms) { room ->
                    DeltaChatRoomItem(room = room, onClick = { /* Future */ })
                }
                items(filteredRooms) { room ->
                    RealRoomItem(
                        room = room,
                        contentUrlResolver = contentUrlResolver,
                        onClick = { onNavigateToChat(room.roomId) }
                    )
                }
            }
        }
    }
}

@Composable
fun DeltaChatBetaPlaceholder(onEnable: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Delta Chat Support (BETA)", fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Text(
                "Delta Chat uses your existing email account as a chat server. " +
                "We are currently implementing the core engine. Stay tuned!",
                style = MaterialTheme.typography.bodySmall
            )
            Spacer(Modifier.height(12.dp))
            Button(onClick = onEnable, modifier = Modifier.align(Alignment.End)) {
                Text("Enable Demo")
            }
        }
    }
}

@Composable
fun DeltaChatRoomItem(room: DeltaChatRoom, onClick: () -> Unit) {
    ListItem(
        headlineContent = {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(room.name, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.width(8.dp))
                    SuggestionChip(
                        onClick = {},
                        label = { Text("Beta", fontSize = 10.sp) },
                        modifier = Modifier.height(20.dp)
                    )
                }
                Text(room.time, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            }
        },
        supportingContent = { Text(room.lastMessage, maxLines = 1, fontSize = 14.sp) },
        leadingContent = {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFE1F5FE)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Email, contentDescription = null, tint = Color(0xFF0288D1))
            }
        },
        modifier = Modifier.clickable { onClick() }
    )
}

@Composable
fun RealRoomItem(
    room: RoomSummary,
    contentUrlResolver: org.matrix.android.sdk.api.session.content.ContentUrlResolver?,
    onClick: () -> Unit
) {
    val avatarUrl = room.avatarUrl.resolveMatrixUrl(contentUrlResolver)

    ListItem(
        headlineContent = { 
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(room.displayName, fontWeight = FontWeight.SemiBold)
                Text(room.latestPreviewableEvent?.root?.originServerTs?.toString() ?: "", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            }
        },
        supportingContent = { Text(room.latestPreviewableEvent?.getLastMessageContent()?.body ?: "No messages", maxLines = 1, fontSize = 14.sp) },
        leadingContent = {
            if (avatarUrl != null) {
                AsyncImage(
                    model = avatarUrl,
                    contentDescription = null,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Text(room.displayName.take(1), color = MaterialTheme.colorScheme.onPrimaryContainer, fontWeight = FontWeight.Bold)
                }
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
