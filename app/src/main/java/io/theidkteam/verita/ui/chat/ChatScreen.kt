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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import io.theidkteam.verita.utils.resolveMatrixUrl
import io.theidkteam.verita.utils.resolveFullMatrixUrl
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import com.ramcosta.composedestinations.annotation.Destination
import io.theidkteam.verita.VeritaApp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.supervisorScope
import org.matrix.android.sdk.api.session.room.model.message.MessageAudioContent
import org.matrix.android.sdk.api.session.room.model.message.MessageStickerContent
import org.matrix.android.sdk.api.session.room.model.message.MessageImageContent
import org.matrix.android.sdk.api.session.room.model.message.MessageVideoContent
import org.matrix.android.sdk.api.session.room.model.message.MessageWithAttachmentContent
import org.matrix.android.sdk.api.session.room.timeline.TimelineEvent
import org.matrix.android.sdk.api.session.room.timeline.getLastMessageContent

@OptIn(ExperimentalMaterial3Api::class)
@Destination
@Composable
fun ChatScreen(
    roomId: String,
    onBack: () -> Unit,
    onNavigateToVerification: () -> Unit,
    onNavigateToSettings: () -> Unit,
    viewModel: ChatViewModel = hiltViewModel()
) {
    LaunchedEffect(roomId) {
        viewModel.initRoom(roomId)
    }

    val events by viewModel.timelineEvents.collectAsState()
    val roomSummary by viewModel.roomSummary.collectAsState()
    val testMessages by viewModel.testMessages.collectAsState()
    val myUserId = viewModel.myUserId
    val myAvatarUrl by viewModel.myAvatarUrl.collectAsState()
    val myDisplayName by viewModel.myDisplayName.collectAsState()
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
                        
                        val myResolvedAvatarUrl = myAvatarUrl.resolveMatrixUrl(contentUrlResolver)
                        if (myResolvedAvatarUrl != null) {
                            AsyncImage(
                                model = myResolvedAvatarUrl,
                                contentDescription = "My Avatar",
                                modifier = Modifier
                                    .padding(end = 8.dp)
                                    .size(32.dp)
                                    .clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .padding(end = 8.dp)
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.secondaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = (myUserId ?: "?").take(1).uppercase(),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                        }
                    }
                )
                val isVerified by viewModel.isVerified.collectAsState()
                val isBackupEnabled = viewModel.isKeysBackupEnabled()
                
                if (viewModel.isRoomEncrypted() && (!isVerified || !isBackupEnabled)) {
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
                                if (!isVerified) "Verify device to read messages." else "Restore keys to read old messages.",
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.weight(1f)
                            )
                            if (!isVerified) {
                                Button(
                                    onClick = onNavigateToVerification,
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text("Verify", style = MaterialTheme.typography.labelSmall)
                                }
                            }
                            if (isVerified && !isBackupEnabled) {
                                Button(
                                    onClick = onNavigateToSettings,
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text("Restore", style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        }
                    }
                }
                
                // Top Audio Player Placeholder
                Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.8f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.AudioFile, contentDescription = null, modifier = Modifier.size(24.dp))
                        Spacer(Modifier.width(12.dp))
                        Text(
                            "Voice message player placeholder",
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = { /* Close player */ }) {
                            Icon(Icons.Default.Close, contentDescription = "Close", modifier = Modifier.size(16.dp))
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
                            fileService = viewModel.getFileService(),
                            isMe = event.root.senderId == myUserId,
                            myUserId = myUserId,
                            myDisplayName = myDisplayName,
                            modifier = Modifier.animateItem()
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MatrixImage(
    messageContent: MessageWithAttachmentContent,
    contentUrlResolver: org.matrix.android.sdk.api.session.content.ContentUrlResolver?,
    fileService: org.matrix.android.sdk.api.session.file.FileService?,
    modifier: Modifier = Modifier
) {
    val imageSource by produceState<Any?>(initialValue = null, messageContent) {
        withContext(Dispatchers.IO) {
            supervisorScope {
                try {
                    when (messageContent) {
                        is MessageImageContent -> {
                            if (messageContent.encryptedFileInfo != null) {
                                val file = fileService?.downloadFile(messageContent)
                                value = file?.absolutePath
                            } else {
                                value = messageContent.url?.let { contentUrlResolver?.resolveFullSize(it) } ?: messageContent.url
                            }
                        }
                        is MessageStickerContent -> {
                            value = messageContent.url?.let { contentUrlResolver?.resolveFullSize(it) } ?: messageContent.url
                        }
                        is MessageVideoContent -> {
                            if (messageContent.encryptedFileInfo != null) {
                                val file = fileService?.downloadFile(messageContent)
                                value = file?.absolutePath
                            } else {
                                value = messageContent.videoInfo?.thumbnailUrl?.let { contentUrlResolver?.resolveFullSize(it) }
                            }
                        }
                        else -> {
                            value = null
                        }
                    }
                } catch (_: Throwable) {
                    // Fallback to non-encrypted URL if possible, or just null
                    value = messageContent.url?.let { contentUrlResolver?.resolveFullSize(it) } ?: messageContent.url
                }
            }
        }
    }

    AsyncImage(
        model = imageSource,
        contentDescription = messageContent.body,
        modifier = modifier,
        contentScale = ContentScale.Fit
    )
}

@Composable
fun VoiceMessagePlayer(
    messageContent: MessageAudioContent,
    fileService: org.matrix.android.sdk.api.session.file.FileService?,
    modifier: Modifier = Modifier
) {
    var isPlaying by remember { mutableStateOf(false) }
    
    Card(
        modifier = modifier.padding(4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { isPlaying = !isPlaying }) {
                Icon(
                    if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (isPlaying) "Pause" else "Play"
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                LinearProgressIndicator(
                    progress = { if (isPlaying) 0.5f else 0f },
                    modifier = Modifier.fillMaxWidth().height(4.dp),
                )
                Text(
                    text = "Voice message (${messageContent.audioInfo?.duration?.let { "${it/1000}s" } ?: "0s"})",
                    style = MaterialTheme.typography.labelSmall
                )
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
                    capitalization = KeyboardCapitalization.Sentences,
                    imeAction = ImeAction.Send
                ),
                keyboardActions = KeyboardActions(
                    onSend = {
                        if (text.isNotBlank()) onSendText()
                    }
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
    fileService: org.matrix.android.sdk.api.session.file.FileService?,
    isMe: Boolean,
    myUserId: String?,
    myDisplayName: String?,
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
    
    // Ping check
    val isPing = !isMe && (
        (myDisplayName?.isNotEmpty() == true && body.contains(myDisplayName, ignoreCase = true)) ||
        (myUserId?.isNotEmpty() == true && body.contains(myUserId, ignoreCase = true))
    )

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

            val bubbleColor = when {
                isMe -> MaterialTheme.colorScheme.primaryContainer
                isPing -> MaterialTheme.colorScheme.errorContainer // Highlight pings with a different color
                else -> MaterialTheme.colorScheme.surfaceVariant
            }

            when {
                isSticker -> {
                    val stickerContent = messageContent as MessageStickerContent
                    Column(horizontalAlignment = if (isMe) Alignment.End else Alignment.Start) {
                        MatrixImage(
                            messageContent = stickerContent,
                            contentUrlResolver = contentUrlResolver,
                            fileService = fileService,
                            modifier = Modifier.size(150.dp).padding(4.dp)
                        )
                        Text(
                            text = stickerContent.body,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                    }
                }
                messageContent is MessageImageContent -> {
                    val imageContent = messageContent as MessageImageContent
                    Card(
                        modifier = Modifier.padding(top = 4.dp),
                        shape = MaterialTheme.shapes.medium,
                        colors = CardDefaults.cardColors(containerColor = bubbleColor)
                    ) {
                        Column {
                            MatrixImage(
                                messageContent = imageContent,
                                contentUrlResolver = contentUrlResolver,
                                fileService = fileService,
                                modifier = Modifier
                                    .fillMaxWidth(0.7f)
                                    .heightIn(max = 300.dp)
                            )
                            if (imageContent.body.isNotEmpty() && !imageContent.body.startsWith("mxc://")) {
                                Text(
                                    text = imageContent.body,
                                    modifier = Modifier.padding(8.dp),
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                }
                messageContent is MessageVideoContent -> {
                    val videoContent = messageContent as MessageVideoContent
                    Card(
                        modifier = Modifier.padding(top = 4.dp),
                        shape = MaterialTheme.shapes.medium,
                        colors = CardDefaults.cardColors(containerColor = bubbleColor)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            MatrixImage(
                                messageContent = videoContent,
                                contentUrlResolver = contentUrlResolver,
                                fileService = fileService,
                                modifier = Modifier
                                    .fillMaxWidth(0.7f)
                                    .heightIn(max = 300.dp)
                            )
                            Icon(
                                Icons.Default.PlayCircle,
                                contentDescription = "Play Video",
                                modifier = Modifier.size(48.dp),
                                tint = Color.White.copy(alpha = 0.8f)
                            )
                        }
                    }
                }
                messageContent is MessageAudioContent -> {
                    VoiceMessagePlayer(
                        messageContent = messageContent,
                        fileService = fileService,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
                else -> {
                    val displayBody = messageContent?.body
                        ?: if (event.root.isEncrypted()) "[Encrypted - Verify Device to read]"
                        else "[Unsupported event: ${event.root.getClearType()}]"

                    Card(
                        colors = CardDefaults.cardColors(containerColor = bubbleColor),
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        Text(
                            text = displayBody,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }
            }
            
            // Indications (Status)
            if (isMe) {
                val statusText = when {
                    event.root.sendState.isSent() -> "✓"
                    event.root.sendState.hasFailed() -> "⚠"
                    else -> "..."
                }
                Text(
                    text = statusText,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.padding(top = 2.dp)
                )
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
