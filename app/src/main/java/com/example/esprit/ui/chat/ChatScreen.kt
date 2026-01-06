package com.example.esprit.ui.chat

import android.Manifest
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.rememberDraggableState
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
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Reply
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush

import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.*
import androidx.hilt.navigation.compose.hiltViewModel
import kotlin.math.roundToInt
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.esprit.model.chat.MessageDto
import com.example.esprit.ui.components.EmojiReactionPicker
import com.example.esprit.util.AudioRecorder
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

@RequiresApi(Build.VERSION_CODES.O)
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
    val translations by viewModel.translations.collectAsState()
    var messageText by remember { mutableStateOf("") }
    var replyingTo by remember { mutableStateOf<MessageDto?>(null) }
    
    val summary by viewModel.summary.collectAsState()
    val isLoadingSummary by viewModel.isLoadingSummary.collectAsState()
    
    // Edit dialog state
    var messageToEdit by remember { mutableStateOf<MessageDto?>(null) }
    var editText by remember { mutableStateOf("") }
    
    // Delete confirmation state
    var messageToDelete by remember { mutableStateOf<MessageDto?>(null) }
    
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
                    if (partnerId != null) {
                        IconButton(onClick = { viewModel.sendGoogleMeetLink() }) {
                            Icon(
                                imageVector = Icons.Default.Videocam,
                                contentDescription = "Video Call",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        IconButton(onClick = { viewModel.summarizeChat() }) {
                            if (isLoadingSummary) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = MaterialTheme.colorScheme.primary,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = "Summarize",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
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
                        onEdit = {
                            messageToEdit = message
                            editText = message.content
                        },
                        onDelete = {
                            messageToDelete = message
                        },
                        onReact = { emoji -> viewModel.addReaction(message.id, emoji) },

                        onReply = { replyingTo = message },
                        onTranslate = { lang -> viewModel.translateMessage(message.id, lang) },
                        translatedText = translations[message.id],
                        onMarkAsRead = { viewModel.markAsRead(message.id) },
                        currentUserId = currentUser?.id
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
        
        // Edit Message Dialog
        if (messageToEdit != null) {
            AlertDialog(
                onDismissRequest = { messageToEdit = null },
                title = { Text("Edit Message") },
                text = {
                    OutlinedTextField(
                        value = editText,
                        onValueChange = { editText = it },
                        label = { Text("Message") },
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            if (editText.isNotBlank()) {
                                viewModel.editMessage(messageToEdit!!.id, editText)
                                messageToEdit = null
                                editText = ""
                            }
                        }
                    ) {
                        Text("Save")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { messageToEdit = null }) {
                        Text("Cancel")
                    }
                }
            )
        }
        
        // Delete Confirmation Dialog
        if (messageToDelete != null) {
            AlertDialog(
                onDismissRequest = { messageToDelete = null },
                title = { Text("Delete Message") },
                text = { Text("Are you sure you want to delete this message?") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            viewModel.deleteMessage(messageToDelete!!.id)
                            messageToDelete = null
                        }
                    ) {
                        Text("Delete", color = MaterialTheme.colorScheme.error)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { messageToDelete = null }) {
                        Text("Cancel")
                    }
                }
            )
        }
        
        // AI Summary Alert
        if (summary != null) {
            AlertDialog(
                onDismissRequest = { viewModel.clearSummary() },
                title = { 
                    Text(
                        "AI Chat Summary",
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    ) 
                },
                text = { Text(summary!!) },
                confirmButton = {
                    Button(
                        onClick = { viewModel.clearSummary() },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text("Awesome")
                    }
                }
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

    onReply: () -> Unit,
    onTranslate: (String) -> Unit,
    translatedText: String? = null,
    onMarkAsRead: () -> Unit,
    currentUserId: String?
) {
    val alignment = if (isMe) Alignment.End else Alignment.Start
    val boxAlignment = if (isMe) Alignment.CenterEnd else Alignment.CenterStart
    
    // Premium Palettes
    val meGradient = Brush.linearGradient(
        colors = listOf(Color(0xFFD9352A), Color(0xFFC22B20))
    )
    val partnerColor = MaterialTheme.colorScheme.surface
    val partnerBorder = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
    
    val textColor = if (isMe) Color.White else MaterialTheme.colorScheme.onSurface
    
    val bubbleShape = if (isMe) {
        RoundedCornerShape(16.dp, 16.dp, 2.dp, 16.dp)
    } else {
        RoundedCornerShape(16.dp, 16.dp, 16.dp, 2.dp)
    }
    
    var showMenu by remember { mutableStateOf(false) }
    var showTranslationDialog by remember { mutableStateOf(false) }
    var showReactionPicker by remember { mutableStateOf(false) }
    
    // Swipe to Reply State
    val swipeOffset = remember { Animatable(0f) }
    val density = LocalDensity.current
    val coroutineScope = rememberCoroutineScope()

    // Mark as read side-effect
    LaunchedEffect(message.id) {
        if (!isMe && currentUserId != null) {
            val alreadyRead = message.readBy.any { it.userId == currentUserId }
            if (!alreadyRead) {
                onMarkAsRead()
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        contentAlignment = boxAlignment
    ) {
        // Reply Icon Background (Visible when swiping)
        if (swipeOffset.value > 0) {
            Icon(
                imageVector = Icons.Default.Reply,
                contentDescription = "Reply",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = 16.dp)
                    .scale(swipeOffset.value / 100f) // Scale effect
            )
        }

        Column(
            horizontalAlignment = alignment,
            modifier = Modifier
                .offset { IntOffset(swipeOffset.value.roundToInt(), 0) }
                .draggable(
                    orientation = Orientation.Horizontal,
                    state = rememberDraggableState { delta ->
                        coroutineScope.launch {
                            // Only allow swiping right (positive delta)
                            val newOffset = (swipeOffset.value + delta).coerceIn(0f, 200f)
                            swipeOffset.snapTo(newOffset)
                        }
                    },
                    onDragStopped = {
                        if (swipeOffset.value > 150f) { // Threshold to trigger reply
                            onReply()
                        }
                        coroutineScope.launch {
                            swipeOffset.animateTo(0f, animationSpec = tween(300))
                        }
                    }
                )
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
                         
                         if (avatarUrl != null && avatarUrl.isNotBlank()) {
                             val fullAvatarUrl = if (avatarUrl.startsWith("http")) {
                                 avatarUrl
                             } else {
                                 val relativePath = avatarUrl.replace(Regex("^/+"), "")
                                 if (baseUrl.endsWith("/api") && relativePath.startsWith("api/")) {
                                     "${baseUrl.removeSuffix("/api")}/$relativePath"
                                 } else {
                                     "$baseUrl/$relativePath"
                                 }
                             }
                             
                             AsyncImage(
                                 model = fullAvatarUrl,
                                 contentDescription = null,
                                 modifier = Modifier
                                     .size(32.dp)
                                     .clip(CircleShape)
                                     .background(Color.Gray)
                             )
                         } else {
                             // Fallback: Initials
                             val initial = (message.senderId.firstName?.take(1) ?: message.senderId.lastName?.take(1) ?: "?").uppercase()
                             Box(
                                 contentAlignment = Alignment.Center,
                                 modifier = Modifier
                                     .size(32.dp)
                                     .clip(CircleShape)
                                     .background(MaterialTheme.colorScheme.primaryContainer)
                             ) {
                                 Text(
                                     text = initial,
                                     fontWeight = FontWeight.Bold,
                                     fontSize = 14.sp,
                                     color = MaterialTheme.colorScheme.onPrimaryContainer
                                 )
                             }
                         }
                         Spacer(modifier = Modifier.width(8.dp))
                     }

              Box(
                  modifier = Modifier
                      .widthIn(max = 300.dp)
                      .shadow(
                          elevation = if (isMe) 6.dp else 2.dp,
                          shape = bubbleShape,
                          clip = false
                      )
                      .clip(bubbleShape)
                      .then(
                          if (isMe) Modifier.background(meGradient)
                          else Modifier
                              .background(partnerColor)
                              .border(1.dp, partnerBorder, bubbleShape)
                      )
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
                                         text = if (message.replyTo.senderId.isJsonObject) {
                                             message.replyTo.senderId.asJsonObject.get("firstName").asString
                                         } else {
                                             "User"
                                         },
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
                                 val baseUrl = com.example.esprit.util.Constants.BASE_URL
                                 val fullUrl = if (message.attachmentUrl.startsWith("http")) {
                                     message.attachmentUrl
                                 } else {
                                     // Ensure no double slash
                                     val cleanBase = baseUrl.replace(Regex("/$"), "")
                                     val cleanPath = message.attachmentUrl.replace(Regex("^/"), "")
                                     "$cleanBase/$cleanPath"
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
                                 val baseUrl = com.example.esprit.util.Constants.BASE_URL
                                 val fullUrl = if (message.attachmentUrl.startsWith("http")) {
                                     message.attachmentUrl
                                 } else {
                                     val cleanBase = baseUrl.replace(Regex("/$"), "")
                                     val cleanPath = message.attachmentUrl.replace(Regex("^/"), "")
                                     "$cleanBase/$cleanPath"
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
                         
                         if (translatedText != null) {
                             HorizontalDivider(
                                 modifier = Modifier.padding(vertical = 4.dp),
                                 color = textColor.copy(alpha = 0.3f)
                             )
                             Row(verticalAlignment = Alignment.CenterVertically) {
                                 Icon(
                                     Icons.Default.Language, 
                                     contentDescription = "Translated",
                                     tint = textColor.copy(alpha = 0.7f),
                                     modifier = Modifier.size(12.dp)
                                 )
                                 Spacer(modifier = Modifier.width(4.dp))
                                 Text(
                                     text = translatedText,
                                     color = textColor,
                                     style = MaterialTheme.typography.bodyMedium,
                                     fontStyle = FontStyle.Italic
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
                            if (isMe) {
                                Spacer(modifier = Modifier.width(4.dp))
                                val isSeen = message.readBy.isNotEmpty()
                                Icon(
                                    imageVector = if (isSeen) Icons.Default.DoneAll else Icons.Default.Done,
                                    contentDescription = if (isSeen) "Seen" else "Sent",
                                    tint = if (isSeen) Color(0xFF2196F3) else textColor.copy(alpha = 0.7f), // Blue if seen
                                    modifier = Modifier.size(16.dp)
                                )
                            }
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
                    
                    if (message.type == "TEXT") {
                        DropdownMenuItem(
                            text = { Text("Translate") },
                            onClick = { 
                                showTranslationDialog = true
                                showMenu = false
                            },
                             leadingIcon = { Icon(Icons.Default.Language, null) }
                        )
                    }

                    DropdownMenuItem(
                        text = { Text("React") },
                        onClick = {
                            showReactionPicker = true
                            showMenu = false
                        },
                        leadingIcon = { Icon(Icons.Default.Add, null) }
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
        
        // Translation Selection Dialog
        if (showTranslationDialog) {
            AlertDialog(
                onDismissRequest = { showTranslationDialog = false },
                title = { Text("Select Language") },
                text = {
                    Column {
                        TextButton(
                            onClick = { 
                                onTranslate("en")
                                showTranslationDialog = false 
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                             Text("English", modifier = Modifier.fillMaxWidth())
                        }
                         TextButton(
                            onClick = { 
                                onTranslate("fr")
                                showTranslationDialog = false 
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                             Text("French", modifier = Modifier.fillMaxWidth())
                        }
                         TextButton(
                            onClick = { 
                                onTranslate("ar")
                                showTranslationDialog = false 
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                             Text("Arabic", modifier = Modifier.fillMaxWidth())
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showTranslationDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }

        AnimatedVisibility(visible = showReactionPicker) {
            EmojiReactionPicker(
                onEmojiSelected = { emoji ->
                    onReact(emoji)
                    showReactionPicker = false
                },
                modifier = Modifier.padding(vertical = 4.dp)
            )
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
    val waveform = remember { List(25) { (6..32).random().dp } } // More bars, controlled range
    
    val activeBarColor = if (isMe) Color.White else MaterialTheme.colorScheme.primary
    val inactiveBarColor = activeBarColor.copy(alpha = 0.3f)
    
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
        modifier = Modifier
            .padding(vertical = 4.dp)
            .fillMaxWidth()
    ) {
        Surface(
            shape = CircleShape,
            color = activeBarColor.copy(alpha = 0.1f),
            modifier = Modifier.size(36.dp)
        ) {
            IconButton(
                onClick = { isPlaying = !isPlaying },
                modifier = Modifier.fillMaxSize()
            ) {
                Icon(
                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (isPlaying) "Pause" else "Play",
                    tint = activeBarColor,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(3.dp),
            modifier = Modifier.weight(1f)
        ) {
            waveform.forEachIndexed { index, height ->
                val isPlayed = (index.toFloat() / waveform.size.toFloat()) < progress
                val color = if (isPlayed) activeBarColor else inactiveBarColor
                
                Box(
                    modifier = Modifier
                        .width(2.5.dp)
                        .height(height)
                        .background(color, RoundedCornerShape(2.dp))
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
    Box(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth()
    ) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            shadowElevation = 8.dp,
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
                            val replyName = if (replyingTo.senderId is com.example.esprit.model.chat.ChatUserDto) {
                                replyingTo.senderId.firstName
                            } else {
                                "User"
                            }
                            Text(
                                "Replying to $replyName",
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
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text(
                        text = "Recording... ${String.format("%02d:%02d", recordingTime / 60, recordingTime % 60)}",
                        color = MaterialTheme.colorScheme.primary,
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
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            disabledContainerColor = Color.Transparent,
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
                            .background(if (isRecording) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) else Color.Transparent)
                            .scale(if (isRecording) 1.2f else 1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = "Record",
                            tint = if (isRecording) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
                        )
                }
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

