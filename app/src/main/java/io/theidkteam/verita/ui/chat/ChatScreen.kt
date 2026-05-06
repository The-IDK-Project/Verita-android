package io.theidkteam.verita.ui.chat

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import io.theidkteam.verita.utils.resolveMatrixUrl
import io.theidkteam.verita.utils.resolveFullMatrixUrl
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import com.ramcosta.composedestinations.annotation.Destination
import io.theidkteam.verita.VeritaApp
import org.matrix.android.sdk.api.session.room.model.message.MessageStickerContent
import org.matrix.android.sdk.api.session.room.timeline.TimelineEvent
import org.matrix.android.sdk.api.session.room.timeline.getLastMessageContent

@OptIn(ExperimentalMaterial3Api::class)
@Destination
@Composable
fun ChatScreen(
    roomId: String,
    onBack: () -> Unit,
    onNavigateToVerification: () -> Unit,
    viewModel: ChatViewModel = hiltViewModel()
) {
    LaunchedEffect(roomId) {
        viewModel.initRoom(roomId)
    }

    val events by viewModel.timelineEvents.collectAsState()
    val roomSummary by viewModel.roomSummary.collectAsState()
    val testMessages by viewModel.testMessages.collectAsState()
    val myUserId = viewModel.myUserId
    val contentUrlResolver = viewModel.getContentUrlResolver()
    var messageText by remember { mutableStateOf("") }

    val context = LocalContext.current
    val settingsManager = (context.applicationContext as? VeritaApp)?.settingsManager
    val backgroundUri = settingsManager?.chatBackgroundUri

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            settingsManager?.saveChatBackground(it.toString())
        }
    }

    Scaffold(
        modifier = Modifier.pointerInput(Unit) {
            detectHorizontalDragGestures { _, dragAmount ->
                // "Swipe left" (moving finger from right to left)
                if (dragAmount < -50) {
                    onBack()
                }
            }
        },
        topBar = {
            val roomAvatarUrl = roomSummary?.avatarUrl.resolveMatrixUrl(contentUrlResolver)

            Column {
                TopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (roomAvatarUrl != null) {
                                AsyncImage(
                                    model = roomAvatarUrl,
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                                Spacer(Modifier.width(8.dp))
                            }
                            Text(roomSummary?.displayName ?: "Chat")
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    },
                    actions = {
                        IconButton(onClick = { launcher.launch("image/*") }) {
                            Icon(Icons.Default.Wallpaper, contentDescription = "Set Background")
                        }
                        if (backgroundUri?.isNotEmpty() == true) {
                            IconButton(onClick = { settingsManager?.saveChatBackground("") }) {
                                Icon(Icons.Default.LayersClear, contentDescription = "Clear Background")
                            }
                        }
                    }
                )
                if (viewModel.isRoomEncrypted()) {
                    Surface(
                        color = MaterialTheme.colorScheme.tertiaryContainer,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "Encrypted Chat. Tap to verify device.",
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.weight(1f)
                            )
                            Button(
                                onClick = onNavigateToVerification,
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text("Verify", style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                }
            }
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (backgroundUri?.isNotEmpty() == true) {
                Image(
                    painter = rememberAsyncImagePainter(backgroundUri),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                    alpha = 0.4f
                )
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                reverseLayout = true
            ) {
                if (events.isEmpty() && testMessages.isEmpty()) {
                    item(key = "alice1") {
                        TestMessageItem(sender = "Alice", body = "Hello! This is a test message.", modifier = Modifier.animateItem())
                    }
                    item(key = "bob1") {
                        TestMessageItem(sender = "Bob", body = "Hi Alice! I see the test messages.", modifier = Modifier.animateItem())
                    }
                    item(key = "alice2") {
                        TestMessageItem(sender = "Alice", body = "Great, the UI is working!", modifier = Modifier.animateItem())
                    }
                } else {
                    items(
                        items = testMessages.reversed(),
                        key = { it.id }
                    ) { msg ->
                        TestMessageItem(
                            sender = msg.sender,
                            body = msg.body,
                            isMe = msg.sender == "Me" || msg.sender == myUserId,
                            modifier = Modifier.animateItem()
                        )
                    }
                    items(
                        items = events,
                        key = { it.localId }
                    ) { event ->
                        MessageItem(
                            event = event,
                            contentUrlResolver = contentUrlResolver,
                            isMe = event.root.senderId == myUserId,
                            modifier = Modifier.animateItem()
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TestMessageItem(sender: String, body: String, isMe: Boolean = false, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalAlignment = if (isMe) Alignment.End else Alignment.Start
    ) {
        Text(
            text = sender,
            style = MaterialTheme.typography.labelSmall,
            color = if (isMe) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary
        )
        Card(
            colors = CardDefaults.cardColors(
                containerColor = if (isMe) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
            ),
            modifier = Modifier.padding(top = 4.dp)
        ) {
            Text(
                text = body,
                modifier = Modifier.padding(8.dp)
            )
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
                placeholder = { Text("Message") },
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences
                )
            )
            IconButton(onClick = onSendText, enabled = text.isNotBlank()) {
                Icon(Icons.Default.Send, contentDescription = "Send")
            }
        }
    }
}

@Composable
fun MessageItem(
    event: TimelineEvent,
    contentUrlResolver: org.matrix.android.sdk.api.session.content.ContentUrlResolver?,
    isMe: Boolean,
    modifier: Modifier = Modifier
) {
    val messageContent = event.getLastMessageContent()
    val isSticker = messageContent is MessageStickerContent

    // Filter for Telegram "Premium" or unwanted content if identified
    val body = messageContent?.body ?: ""
    val isUnwantedTelegramContent = body.contains("pinned a message", ignoreCase = true) || 
                                   body.contains("joined the group", ignoreCase = true) ||
                                   body.contains("Telegram Premium", ignoreCase = true) ||
                                   body.contains("Gift", ignoreCase = true)

    if (isUnwantedTelegramContent && !isMe) return

    val senderAvatarUrl = event.senderInfo.avatarUrl.resolveMatrixUrl(contentUrlResolver, 128, 128)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start
    ) {
        if (!isMe) {
            if (senderAvatarUrl != null) {
                AsyncImage(
                    model = senderAvatarUrl,
                    contentDescription = null,
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = (event.senderInfo.displayName ?: event.root.senderId ?: "?").take(1),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
            Spacer(Modifier.width(8.dp))
        }

        Column(
            horizontalAlignment = if (isMe) Alignment.End else Alignment.Start
        ) {
            Text(
                text = event.senderInfo.displayName ?: event.root.senderId ?: "",
                style = MaterialTheme.typography.labelSmall,
                color = if (isMe) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary
            )

            if (isSticker) {
                val stickerContent = messageContent as MessageStickerContent
                val resolvedUrl = stickerContent.url.resolveFullMatrixUrl(contentUrlResolver)

                Column(horizontalAlignment = if (isMe) Alignment.End else Alignment.Start) {
                    AsyncImage(
                        model = resolvedUrl ?: stickerContent.url,
                        contentDescription = stickerContent.body,
                        modifier = Modifier
                            .size(150.dp)
                            .padding(4.dp)
                    )
                    Text(
                        text = stickerContent.body,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
            } else {
                val displayBody = messageContent?.body
                    ?: if (event.root.isEncrypted()) "[Encrypted - Verify Device to read]"
                    else "[Unsupported event: ${event.root.getClearType()}]"

                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (isMe) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                    ),
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    Text(
                        text = displayBody,
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }
        }

        if (isMe) {
            Spacer(Modifier.width(8.dp))
            if (senderAvatarUrl != null) {
                AsyncImage(
                    model = senderAvatarUrl,
                    contentDescription = null,
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.secondaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = (event.senderInfo.displayName ?: event.root.senderId ?: "?").take(1),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }
        }
    }
}
