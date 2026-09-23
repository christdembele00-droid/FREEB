package com.freeb.app.chat

data class ConversationUi(
    val id: String,
    val peerId: String,
    val peerName: String,
    val peerUsername: String?,
    val lastMessage: String?
)

data class ChatMessageUi(
    val id: String,
    val senderId: String,
    val body: String,
    val createdAt: String
)

data class SearchUserUi(
    val id: String,
    val username: String?,
    val displayName: String?
)
