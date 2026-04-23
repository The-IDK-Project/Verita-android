package io.theidkteam.verita.ui.chat

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import coil.compose.AsyncImage
import org.matrix.android.sdk.api.session.room.model.message.MessageStickerContent
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ramcosta.composedestinations.annotation.Destination
import org.matrix.android.sdk.api.session.room.timeline.TimelineEvent
import org.matrix.android.sdk.api.session.room.timeline.getLastMessageContent
import androidx.compose.material3.ExperimentalMaterial3Api

@OptIn(ExperimentalMaterial3Api::class)
@Destination
@Composable
fun ChatScreen(
    roomId: String,
    onBack: () -> Unit,
    viewModel: ChatViewModel = hiltViewModel()
) {
    LaunchedEffect(roomId) {
        viewModel.initRoom(roomId)
    }

    val events by viewModel.timelineEvents.collectAsState()
    val contentUrlResolver = viewModel.getContentUrlResolver()
    var messageText by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Chat") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        bottomBar = {
            ChatBottomBar(
                text = messageText,
                onTextChange = { messageText = it },
                onSendText = {
                    viewModel.sendMessage(messageText)
                    messageText = ""
                },
                onSendVoice = { /* Open voice recorder */ },
                onSendCircle = { /* Open circle camera */ }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            reverseLayout = true
        ) {
            items(events) { event ->
                MessageItem(event, contentUrlResolver)
            }
        }
    }
}

@Composable
fun ChatBottomBar(
    text: String,
    onTextChange: (String) -> Unit,
    onSendText: () -> Unit,
    onSendVoice: () -> Unit,
    onSendCircle: () -> Unit
) {
    Surface(tonalElevation = 2.dp) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onSendCircle) {
                Icon(Icons.Default.Videocam, contentDescription = "Video Circle")
            }
            IconButton(onClick = onSendVoice) {
                Icon(Icons.Default.Mic, contentDescription = "Voice Message")
            }
            TextField(
                value = text,
                onValueChange = onTextChange,
                modifier = Modifier.weight(1f),
                placeholder = { Text("Message") }
            )
            IconButton(onClick = onSendText, enabled = text.isNotBlank()) {
                Icon(Icons.Default.Send, contentDescription = "Send")
            }
        }
    }
}

@Composable
fun MessageItem(event: TimelineEvent, contentUrlResolver: org.matrix.android.sdk.api.session.content.ContentUrlResolver?) {
    val messageContent = event.getLastMessageContent()
    val isSticker = messageContent is MessageStickerContent
    
    Column(modifier = Modifier.padding(8.dp)) {
        Text(
            text = event.senderInfo.displayName ?: event.root.senderId ?: "",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary
        )
        
        if (isSticker) {
            val stickerContent = messageContent as MessageStickerContent
            val resolvedUrl = contentUrlResolver?.resolveFullSize(stickerContent.url)
            
            Column {
                AsyncImage(
                    model = resolvedUrl ?: stickerContent.url,
                    contentDescription = stickerContent.body,
                    modifier = Modifier.size(150.dp).padding(4.dp)
                )
                Text(
                    text = stickerContent.body,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
        } else {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Text(
                    text = messageContent?.body ?: "",
                    modifier = Modifier.padding(8.dp)
                )
            }
        }
    }
}
