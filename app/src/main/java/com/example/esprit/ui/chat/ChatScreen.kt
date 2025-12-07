package com.example.esprit.ui.chat

import android.Manifest
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.esprit.model.chat.MessageDto
import com.example.esprit.util.AudioRecorder
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    navController: NavController,
    clubId: String? = null,
    partnerId: String? = null,
    initialTitle: String = "Chat",
    viewModel: ChatViewModel = hiltViewModel()
) {
    val messages by viewModel.messages.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val isTyping by viewModel.isTyping.collectAsState()
    var messageText by remember { mutableStateOf("") }
    var replyingTo by remember { mutableStateOf<MessageDto?>(null) }
    
    val listState = rememberLazyListState()
    val context = LocalContext.current

    // Observe changes
    LaunchedEffect(clubId, partnerId) {
        viewModel.loadMessages(clubId, partnerId)
    }

    // Auto-scroll to bottom
    LaunchedEffect(messages) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }
    
    // Typing indicator logic
    LaunchedEffect(messageText) {
        viewModel.sendTyping(messageText.isNotEmpty())
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            val file = uriToFile(context, it)
            if (file != null) {
                viewModel.sendImage(file, replyingTo)
                replyingTo = null
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text(initialTitle)
                        if (partnerId != null) {
                            // Presence handling would go here (e.g. "Online")
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (clubId != null) {
                        var showMembers by remember { mutableStateOf(false) }
                        val members by viewModel.clubMembers.collectAsState()
                        
                        IconButton(onClick = { showMembers = true }) {
                            Icon(Icons.Default.Info, contentDescription = "Members")
                        }
                        
                        if (showMembers) {
                            AlertDialog(
                                onDismissRequest = { showMembers = false },
                                title = { Text("Club Members") },
                                text = {
                                    LazyColumn(modifier = Modifier.heightIn(max = 300.dp)) {
                                        items(members) { member ->
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clickable {
                                                        showMembers = false
                                                        if (member.id != currentUser?.id) {
                                                            navController.navigate(com.example.esprit.ui.nav.Destinations.privateChatRoute(member.id, member.name))
                                                        }
                                                    }
                                                    .padding(8.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                AsyncImage(
                                                    model = member.avatar ?: "https://via.placeholder.com/40",
                                                    contentDescription = null,
                                                    modifier = Modifier
                                                        .size(40.dp)
                                                        .clip(CircleShape)
                                                        .background(Color.Gray)
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Column {
                                                    Text(member.name, fontWeight = FontWeight.Bold)
                                                    Text(member.role, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                                                }
                                            }
                                        }
                                    }
                                },
                                confirmButton = {
                                    TextButton(onClick = { showMembers = false }) {
                                        Text("Close")
                                    }
                                }
                            )
                        }
                    }
                    IconButton(onClick = { /* Menu */ }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Menu")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(messages) { message ->
                    val isMe = message.senderId.id == (currentUser?.id ?: "")
                    MessageItem(
                        message = message,
                        isMe = isMe,
                        onEdit = { /* Edit logic */ },
                        onDelete = { viewModel.deleteMessage(message.id) },
                        onReact = { emoji -> viewModel.addReaction(message.id, emoji) },
                        onReply = { replyingTo = message }
                    )
                }
                if (isTyping) {
                    item {
                        Text(
                            text = "Typing...",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.Gray,
                            modifier = Modifier.padding(start = 16.dp, bottom = 4.dp)
                        )
                    }
                }
            }

            ChatInput(
                value = messageText,
                onValueChange = { messageText = it },
                onSend = {
                    if (messageText.isNotBlank()) {
                        viewModel.sendMessage(messageText, "TEXT", replyingTo)
                        messageText = ""
                        replyingTo = null
                    }
                },
                onSendVoice = { file -> 
                    viewModel.sendVoice(file, replyingTo)
                    replyingTo = null
                },
                onAttach = { launcher.launch("image/*") },
                replyingTo = replyingTo,
                onCancelReply = { replyingTo = null }
            )
        }
    }
}

@Composable
fun MessageItem(
    message: MessageDto, 
    isMe: Boolean,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onReact: (String) -> Unit,
    onReply: () -> Unit
) {
    val alignment = if (isMe) Alignment.End else Alignment.Start
    val bubbleColor = if (isMe) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
    val textColor = if (isMe) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
    
    var showMenu by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = alignment
    ) {
        if (!isMe) {
            Text(
                text = "${message.senderId.firstName} ${message.senderId.lastName}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outline,
                modifier = Modifier.padding(start = 8.dp, bottom = 2.dp)
            )
        }
        
        Row(verticalAlignment = Alignment.Bottom) {
                     if (!isMe) {
                         val baseUrl = com.example.esprit.util.Constants.BASE_URL.replace(Regex("/+$"), "")
                         val avatarUrl = message.senderId.imageUrl
                         val fullAvatarUrl = if (avatarUrl != null) {
                             if (avatarUrl.startsWith("http")) avatarUrl
                             else "$baseUrl/${avatarUrl.replace(Regex("^/+"), "")}"
                         } else "https://via.placeholder.com/40"

                         AsyncImage(
                             model = fullAvatarUrl,
                             contentDescription = null,
                             modifier = Modifier
                                 .size(32.dp)
                                 .clip(CircleShape)
                                 .background(Color.Gray)
                         )
                         Spacer(modifier = Modifier.width(8.dp))
                     }

             Box {
                 Surface(
                     shape = RoundedCornerShape(12.dp),
                     color = bubbleColor,
                     modifier = Modifier
                         .widthIn(max = 280.dp)
                         .pointerInput(Unit) {
                             detectTapGestures(
                                 onLongPress = { showMenu = true }
                             )
                         }
                 ) {
                     Column(modifier = Modifier.padding(12.dp)) {
                         // Reply Context
                         if (message.replyTo != null) {
                             Surface(
                                 shape = RoundedCornerShape(8.dp),
                                 color = MaterialTheme.colorScheme.surface.copy(alpha = 0.3f),
                                 modifier = Modifier
                                     .padding(bottom = 4.dp)
                                     .fillMaxWidth()
                             ) {
                                 Column(modifier = Modifier.padding(6.dp)) {
                                     Text(
                                         text = message.replyTo.senderId.firstName ?: "",
                                         style = MaterialTheme.typography.labelSmall,
                                         color = textColor.copy(alpha = 0.8f),
                                         fontWeight = FontWeight.Bold
                                     )
                                     Text(
                                         text = if (message.replyTo.type == "IMAGE") "Sent an image" else if (message.replyTo.type == "VOICE") "Sent a voice message" else message.replyTo.content,
                                         style = MaterialTheme.typography.bodySmall,
                                         color = textColor.copy(alpha = 0.8f),
                                         maxLines = 1,
                                         overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                     )
                                 }
                             }
                         }

                         if (message.isDeleted) {
                             Text(
                                 text = "Message deleted",
                                 color = textColor.copy(alpha = 0.6f),
                                 fontStyle = FontStyle.Italic,
                                 style = MaterialTheme.typography.bodyMedium
                             )
                         } else {
                             if (message.type == "IMAGE" && message.attachmentUrl != null) {
                                 val baseUrl = com.example.esprit.util.Constants.BASE_URL.replace(Regex("/+$"), "")
                                 val relativePath = message.attachmentUrl.replace(Regex("^/+"), "")
                                 val fullUrl = if (message.attachmentUrl.startsWith("http")) {
                                     message.attachmentUrl
                                 } else {
                                     "$baseUrl/$relativePath"
                                 }
                                 
                                 AsyncImage(
                                     model = fullUrl,
                                     contentDescription = "Image",
                                     modifier = Modifier
                                         .fillMaxWidth()
                                         .height(200.dp)
                                         .clip(RoundedCornerShape(8.dp))
                                         .background(Color.LightGray)
                                 )
                                 if (message.content.isNotEmpty() && message.content != "Sent an image") {
                                     Spacer(modifier = Modifier.height(4.dp))
                                     Text(text = message.content, color = textColor)
                                 }
                             } else if (message.type == "VOICE" && message.attachmentUrl != null) {
                                 val baseUrl = com.example.esprit.util.Constants.BASE_URL.replace(Regex("/+$"), "")
                                 val relativePath = message.attachmentUrl.replace(Regex("^/+"), "")
                                 val fullUrl = if (message.attachmentUrl.startsWith("http")) {
                                     message.attachmentUrl 
                                 } else {
                                     "$baseUrl/$relativePath"
                                 }
                                 
                                 VoiceMessagePlayer(url = fullUrl, isMe = isMe, textColor = textColor)
                                 
                             } else {
                                 Text(
                                     text = message.content,
                                     color = textColor,
                                     style = MaterialTheme.typography.bodyMedium
                                 )
                             }
                         }
                        
                        Row(
                             modifier = Modifier.align(Alignment.End),
                             verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (message.isEdited && !message.isDeleted) {
                                Text(
                                    text = "(edited)",
                                    fontSize = 10.sp,
                                    color = textColor.copy(alpha = 0.7f),
                                    modifier = Modifier.padding(end = 4.dp)
                                )
                            }
                            Text(
                                text = formatTime(message.createdAt),
                                color = textColor.copy(alpha = 0.7f),
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                }
                
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false },
                    modifier = Modifier.background(MaterialTheme.colorScheme.surface)
                ) {
                    DropdownMenuItem(
                        text = { Text("Reply") },
                        onClick = {
                            onReply()
                            showMenu = false
                        },
                        leadingIcon = { Icon(Icons.Default.Send, null) }
                    )

                    if (isMe && !message.isDeleted) {
                         DropdownMenuItem(
                            text = { Text("Edit") },
                            onClick = { 
                                onEdit()
                                showMenu = false
                            },
                            leadingIcon = { Icon(Icons.Default.Edit, null) }
                        )
                        DropdownMenuItem(
                            text = { Text("Delete") },
                            onClick = { 
                                onDelete()
                                showMenu = false
                            },
                             leadingIcon = { Icon(Icons.Default.Delete, null) }
                        )
                    }
                }
            }
        }
        
        if (message.reactions.isNotEmpty()) {
             Row(
                 modifier = Modifier.padding(top = 4.dp, start = if (isMe) 0.dp else 40.dp),
                 horizontalArrangement = Arrangement.spacedBy(4.dp)
             ) {
                 message.reactions.groupBy { it.emoji }.forEach { (emoji, people) ->
                     Surface(
                         shape = RoundedCornerShape(12.dp),
                         color = MaterialTheme.colorScheme.surfaceVariant,
                         modifier = Modifier.clickable { }
                     ) {
                         Text(
                             text = "$emoji ${people.size}",
                             fontSize = 12.sp,
                             modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                         )
                     }
                 }
             }
        }
    }
}

@Composable
fun VoiceMessagePlayer(url: String, isMe: Boolean, textColor: Color) {
    var isPlaying by remember { mutableStateOf(false) }
    var progress by remember { mutableStateOf(0f) }
    val waveform = remember { List(20) { (10..40).random().dp } }
    
    LaunchedEffect(isPlaying) {
        if (isPlaying) {
             try {
                val player = MediaPlayer().apply {
                    setDataSource(url)
                    prepare()
                    start()
                    setOnCompletionListener { 
                        isPlaying = false
                        progress = 0f
                        release()
                    }
                }
                val duration = player.duration
                while (player.isPlaying) {
                    progress = player.currentPosition.toFloat() / duration.toFloat()
                    kotlinx.coroutines.delay(100)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                isPlaying = false
                progress = 0f
            }
        }
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 4.dp)
    ) {
        IconButton(
            onClick = { isPlaying = !isPlaying },
            modifier = Modifier.size(32.dp)
        ) {
            Icon(
                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                contentDescription = "Play",
                tint = textColor
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(2.dp),
            modifier = Modifier.height(40.dp)
        ) {
            waveform.forEachIndexed { index, height ->
                val isPlayed = (index.toFloat() / waveform.size.toFloat()) < progress
                val barColor = if (isPlayed) textColor else textColor.copy(alpha = 0.5f)
                
                Box(
                    modifier = Modifier
                        .width(3.dp)
                        .height(height)
                        .background(barColor, RoundedCornerShape(2.dp))
                )
            }
        }
    }
}

@Composable
fun ChatInput(
    value: String,
    onValueChange: (String) -> Unit,
    onSend: () -> Unit,
    onSendVoice: (File) -> Unit,
    onAttach: () -> Unit,
    replyingTo: MessageDto?,
    onCancelReply: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            if (replyingTo != null) {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Replying to ${replyingTo.senderId.firstName ?: ""}",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = if (replyingTo.type == "IMAGE") "Sent an image" else if (replyingTo.type == "VOICE") "Sent a voice message" else replyingTo.content,
                                maxLines = 1,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        IconButton(onClick = onCancelReply) {
                            Icon(Icons.Default.Close, "Cancel Reply")
                        }
                    }
                }
            }

            Row(
                modifier = Modifier
                    .padding(8.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val context = LocalContext.current
                var isRecording by remember { mutableStateOf(false) }
                var recordingTime by remember { mutableStateOf(0L) }
                val recorder = remember { AudioRecorder(context) }
                
                LaunchedEffect(isRecording) {
                    if (isRecording) {
                        val startTime = System.currentTimeMillis()
                        while (isRecording) {
                            recordingTime = (System.currentTimeMillis() - startTime) / 1000
                            kotlinx.coroutines.delay(1000)
                        }
                    } else {
                        recordingTime = 0L
                    }
                }

                if (isRecording) {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = null,
                        tint = Color.Red,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text(
                        text = "Recording... ${String.format("%02d:%02d", recordingTime / 60, recordingTime % 60)}",
                        color = Color.Red,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                } else {
                    IconButton(onClick = onAttach) {
                        Icon(Icons.Default.Add, contentDescription = "Attach", tint = MaterialTheme.colorScheme.primary)
                    }
                    
                    TextField(
                        value = value,
                        onValueChange = onValueChange,
                        placeholder = { Text("Type a message...") },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(24.dp),
                        colors = TextFieldDefaults.colors(
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }

                if (value.isNotBlank() && !isRecording) {
                    IconButton(onClick = onSend) {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = "Send",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                } else {
                    val permissionLauncher = rememberLauncherForActivityResult(
                        ActivityResultContracts.RequestPermission()
                    ) { /* Check result if needed */ }

                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .pointerInput(Unit) {
                                detectTapGestures(
                                    onPress = {
                                        if (ContextCompat.checkSelfPermission(
                                                context, 
                                                Manifest.permission.RECORD_AUDIO
                                            ) == PackageManager.PERMISSION_GRANTED
                                        ) {
                                            try {
                                                isRecording = true
                                                val file = File(context.cacheDir, "voice_msg_${System.currentTimeMillis()}.m4a")
                                                val startTime = System.currentTimeMillis()
                                                recorder.startRecording(file)
                                                
                                                tryAwaitRelease() 
                                                
                                                recorder.stopRecording()
                                                isRecording = false
                                                
                                                if (System.currentTimeMillis() - startTime < 500) {
                                                     android.widget.Toast.makeText(context, "Hold to record", android.widget.Toast.LENGTH_SHORT).show()
                                                } else {
                                                    onSendVoice(file)
                                                }
                                            } catch (e: Exception) {
                                                e.printStackTrace()
                                                isRecording = false
                                            }
                                        } else {
                                            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                        }
                                    }
                                )
                            }
                            .background(if(isRecording) Color.Red.copy(alpha=0.1f) else Color.Transparent)
                            .scale(if(isRecording) 1.2f else 1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = "Record",
                            tint = if (isRecording) Color.Red else MaterialTheme.colorScheme.secondary
                        )
                    }
                }
            }
        }
    }
}

fun uriToFile(context: android.content.Context, uri: Uri): File? {
    val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
    val file = File(context.cacheDir, "temp_image_${System.currentTimeMillis()}.jpg")
    return inputStream?.use { input ->
        FileOutputStream(file).use { output ->
            input.copyTo(output)
            file
        }
    }
}

fun formatTime(dateString: String): String {
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        inputFormat.timeZone = TimeZone.getTimeZone("UTC")
        val date = inputFormat.parse(dateString)
        val outputFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        outputFormat.format(date ?: Date())
    } catch (e: Exception) {
        ""
    }
}
