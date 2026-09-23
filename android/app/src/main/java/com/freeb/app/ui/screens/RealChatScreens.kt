package com.freeb.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.freeb.app.BuildConfig
import com.freeb.app.chat.ChatMessageUi
import com.freeb.app.chat.ChatSocketClient
import com.freeb.app.chat.ConversationUi
import com.freeb.app.chat.SearchUserUi
import com.freeb.app.network.FreebApi
import java.util.concurrent.Executors

private val ChatPanel = Color(0xFF151515)
private val ChatAccent = Color(0xFFFFD54A)

@Composable
fun RealChatListScreen(
    onOpen: (ConversationUi) -> Unit,
    onSearch: () -> Unit
) {
    val conversations = remember { mutableStateListOf<ConversationUi>() }
    val executor = remember { Executors.newSingleThreadExecutor() }

    DisposableEffect(Unit) {
        val job = executor.submit {
            runCatching {
                val json = FreebApi(BuildConfig.FREEB_API_URL).conversations()
                val loaded = buildList {
                    for (i in 0 until json.length()) {
                        val row = json.getJSONObject(i)
                        val peer = row.optJSONObject("peer") ?: continue
                        add(
                            ConversationUi(
                                id = row.getString("id"),
                                peerId = peer.getString("id"),
                                peerName = peer.optString("display_name").ifBlank {
                                    peer.optString("username").ifBlank { "FREEB user" }
                                },
                                peerUsername = peer.optString("username").ifBlank { null },
                                lastMessage = row.optJSONObject("last_message")?.optString("body")
                            )
                        )
                    }
                }
                android.os.Handler(android.os.Looper.getMainLooper()).post {
                    conversations.clear()
                    conversations.addAll(loaded)
                }
            }
        }
        onDispose { executor.shutdownNow() }
    }

    Box(Modifier.fillMaxSize().background(Color.Black)) {
        Column(Modifier.fillMaxSize()) {
            Row(
                Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Messages", color = Color.White)
                IconButton(onClick = onSearch) {
                    Icon(Icons.Filled.Search, null, tint = Color.White)
                }
            }
            LazyColumn(
                Modifier.fillMaxSize().padding(horizontal = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(conversations, key = { it.id }) { conversation ->
                    Row(
                        Modifier.fillMaxWidth()
                            .background(ChatPanel, RoundedCornerShape(20.dp))
                            .clickable { onOpen(conversation) }
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            Modifier.size(52.dp).background(ChatAccent, CircleShape)
                        )
                        Column(Modifier.padding(start = 12.dp).weight(1f)) {
                            Text(conversation.peerName, color = Color.White)
                            Text(
                                conversation.lastMessage ?: "Nouvelle conversation",
                                color = Color.White.copy(.55f),
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun UserSearchScreen(
    onBack: () -> Unit,
    onOpenConversation: (ConversationUi) -> Unit
) {
    var query by remember { mutableStateOf("") }
    val results = remember { mutableStateListOf<SearchUserUi>() }
    val executor = remember { Executors.newSingleThreadExecutor() }

    LaunchedEffect(query) {
        val normalized = query.trim()
        if (normalized.length < 2) {
            results.clear()
            return@LaunchedEffect
        }
        executor.submit {
            runCatching {
                val json = FreebApi(BuildConfig.FREEB_API_URL).searchUsers(normalized)
                val loaded = buildList {
                    for (i in 0 until json.length()) {
                        val row = json.getJSONObject(i)
                        add(
                            SearchUserUi(
                                id = row.getString("id"),
                                username = row.optString("username").ifBlank { null },
                                displayName = row.optString("display_name").ifBlank { null }
                            )
                        )
                    }
                }
                android.os.Handler(android.os.Looper.getMainLooper()).post {
                    results.clear()
                    results.addAll(loaded)
                }
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose { executor.shutdownNow() }
    }

    Column(Modifier.fillMaxSize().background(Color.Black).padding(12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.Filled.ArrowBack, null, tint = Color.White)
            }
            OutlinedTextField(
                value = query,
                onValueChange = { query = it.take(64) },
                modifier = Modifier.weight(1f),
                singleLine = true,
                label = { Text("Rechercher un utilisateur") }
            )
        }
        LazyColumn(
            Modifier.fillMaxSize().padding(top = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(results, key = { it.id }) { result ->
                Row(
                    Modifier.fillMaxWidth()
                        .background(ChatPanel, RoundedCornerShape(18.dp))
                        .clickable {
                            executor.submit {
                                runCatching {
                                    val api = FreebApi(BuildConfig.FREEB_API_URL)
                                    val conversation = api.createConversation(result.id)
                                    val ui = ConversationUi(
                                        id = conversation.getString("id"),
                                        peerId = result.id,
                                        peerName = result.displayName ?: result.username ?: "FREEB user",
                                        peerUsername = result.username,
                                        lastMessage = null
                                    )
                                    android.os.Handler(android.os.Looper.getMainLooper()).post {
                                        onOpenConversation(ui)
                                    }
                                }
                            }
                        }
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(Modifier.size(44.dp).background(ChatAccent, CircleShape))
                    Column(Modifier.padding(start = 12.dp)) {
                        Text(result.displayName ?: result.username ?: "FREEB user", color = Color.White)
                        result.username?.let { Text("@$it", color = Color.White.copy(.55f)) }
                    }
                }
            }
        }
    }
}

@Composable
fun RealChatDetailScreen(
    conversation: ConversationUi,
    onBack: () -> Unit,
    onCall: (String, Boolean) -> Unit
) {
    val messages = remember { mutableStateListOf<ChatMessageUi>() }
    var input by remember { mutableStateOf("") }
    val api = remember { FreebApi(BuildConfig.FREEB_API_URL) }
    val executor = remember { Executors.newSingleThreadExecutor() }
    val myFirebaseUid = remember { com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid.orEmpty() }
    var myBackendId by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        executor.submit {
            runCatching {
                val me = api.me()
                myBackendId = me.optString("id")
                val json = api.messages(conversation.id)
                val loaded = buildList {
                    for (i in 0 until json.length()) {
                        val m = json.getJSONObject(i)
                        add(
                            ChatMessageUi(
                                id = m.getString("id"),
                                senderId = m.getString("sender_id"),
                                body = m.getString("body"),
                                createdAt = m.optString("created_at")
                            )
                        )
                    }
                }
                android.os.Handler(android.os.Looper.getMainLooper()).post {
                    messages.clear()
                    messages.addAll(loaded)
                }
            }
        }
    }

    val socket = remember(conversation.id) { ChatSocketClient(BuildConfig.FREEB_API_URL, conversation.id) }

    DisposableEffect(conversation.id) {
        socket.connect { event ->
            if (event.optString("event") == "MESSAGE_NEW") {
                val p = event.optJSONObject("payload") ?: return@connect
                val message = ChatMessageUi(
                    id = p.getString("id"),
                    senderId = p.getString("sender_id"),
                    body = p.getString("body"),
                    createdAt = p.optString("created_at")
                )
                if (messages.none { it.id == message.id }) messages.add(message)
            }
        }
        onDispose {
            socket.close()
            executor.shutdownNow()
        }
    }

    Column(Modifier.fillMaxSize().background(Color.Black)) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Filled.ArrowBack, null, tint = Color.White)
            }
            Column(Modifier.weight(1f)) {
                Text(conversation.peerName, color = Color.White)
                conversation.peerUsername?.let { Text("@$it", color = Color.White.copy(.55f)) }
            }
            IconButton(onClick = { onCall(conversation.peerId, false) }) {
                Icon(Icons.Filled.Call, null, tint = Color.White)
            }
            IconButton(onClick = { onCall(conversation.peerId, true) }) {
                Icon(Icons.Filled.Videocam, null, tint = Color.White)
            }
        }

        LazyColumn(
            Modifier.weight(1f).fillMaxWidth().padding(horizontal = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            reverseLayout = false
        ) {
            items(messages, key = { it.id }) { message ->
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = if (message.senderId == myBackendId) Arrangement.End else Arrangement.Start
                ) {
                    Box(
                        Modifier.background(
                            if (message.senderId == myBackendId) Color.White else ChatPanel,
                            RoundedCornerShape(20.dp)
                        ).padding(horizontal = 14.dp, vertical = 10.dp)
                    ) {
                        Text(
                            message.body,
                            color = if (message.senderId == myBackendId) Color.Black else Color.White
                        )
                    }
                }
            }
        }

        Row(
            Modifier.fillMaxWidth().navigationBarsPadding().padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = input,
                onValueChange = { input = it.take(4000) },
                modifier = Modifier.weight(1f),
                singleLine = false,
                maxLines = 4,
                placeholder = { Text("Message") }
            )
            IconButton(
                enabled = input.isNotBlank(),
                onClick = {
                    val body = input.trim()
                    input = ""
                    executor.submit {
                        runCatching { api.sendMessage(conversation.id, body) }
                    }
                }
            ) {
                Icon(Icons.Filled.Send, null, tint = if (input.isBlank()) Color.White.copy(.25f) else ChatAccent)
            }
        }
    }
}
