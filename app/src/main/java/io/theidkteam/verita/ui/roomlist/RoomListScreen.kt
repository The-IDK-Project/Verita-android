package io.theidkteam.verita.ui.roomlist

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import org.matrix.android.sdk.api.session.room.model.RoomSummary
import org.matrix.android.sdk.api.session.room.timeline.getLastMessageContent

@OptIn(ExperimentalMaterial3Api::class)
@Destination
@Composable
fun RoomListScreen(
    navigator: DestinationsNavigator,
    viewModel: RoomListViewModel = hiltViewModel()
) {
    val rooms = viewModel.rooms?.collectAsState(initial = emptyList())?.value ?: return

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Verita") },
                actions = {
                    IconButton(onClick = { viewModel.logout() }) {
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
            items(rooms) { room ->
                RoomItem(room = room) {
                    // navigator.navigate(ChatScreenDestination(room.roomId))
                }
            }
        }
    }
}

@Composable
fun RoomItem(room: RoomSummary, onClick: () -> Unit) {
    val lastMessage = room.latestPreviewableEvent?.getLastMessageContent()?.body ?: "No messages"
    
    ListItem(
        headlineContent = { Text(room.displayName) },
        supportingContent = { Text(lastMessage, maxLines = 1) },
        modifier = Modifier.clickable { onClick() }
    )
}
