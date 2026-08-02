package com.example.know_it_all.presentation.ui.screen.main

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.know_it_all.presentation.viewmodel.ChatViewModel
import com.example.know_it_all.ui.theme.AcidGreen
import com.example.know_it_all.ui.theme.CharcoalGray
import com.example.know_it_all.ui.theme.Cream
import com.example.know_it_all.ui.theme.CreamDark
import com.example.know_it_all.ui.theme.NearBlack
import com.example.know_it_all.ui.theme.WarmGray
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    navController: NavHostController,
    chatViewModel: ChatViewModel,
    swapId: String,
    swapSkillName: String,
    counterpartName: String,
    currentUserId: String
) {
    val messages by chatViewModel.messages.collectAsState()
    var messageText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    LaunchedEffect(swapId) {
        chatViewModel.observeMessages(swapId)
    }

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            scope.launch { listState.animateScrollToItem(messages.size - 1) }
        }
    }

    Scaffold(
        containerColor = Cream,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            counterpartName,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Black,
                            color = NearBlack
                        )
                        Text(swapSkillName, fontSize = 12.sp, color = CharcoalGray)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = NearBlack
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Cream)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .imePadding()
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (messages.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 40.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("💬", fontSize = 32.sp)
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    "Start the conversation",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = NearBlack
                                )
                                Text(
                                    "Coordinate your skill session here",
                                    fontSize = 12.sp,
                                    color = CharcoalGray
                                )
                            }
                        }
                    }
                }
                items(messages, key = { it.messageId }) { message ->
                    MessageBubble(
                        text = message.text,
                        isMe = message.senderId == currentUserId,
                        counterpartName = counterpartName,
                        timestamp = message.timestamp
                    )
                }
            }

            // Input bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CreamDark)
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = messageText,
                    onValueChange = { messageText = it },
                    placeholder = {
                        Text("Type a message...", color = WarmGray, fontSize = 14.sp)
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(24.dp),
                    maxLines = 4,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NearBlack,
                        unfocusedBorderColor = WarmGray,
                        focusedContainerColor = Cream,
                        unfocusedContainerColor = Cream
                    ),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                    keyboardActions = KeyboardActions(onSend = {
                        if (messageText.isNotBlank()) {
                            chatViewModel.sendMessage(swapId, currentUserId, messageText.trim())
                            messageText = ""
                        }
                    })
                )
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(
                            if (messageText.isNotBlank()) NearBlack else WarmGray,
                            CircleShape
                        )
                        .clip(CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    IconButton(onClick = {
                        if (messageText.isNotBlank()) {
                            chatViewModel.sendMessage(swapId, currentUserId, messageText.trim())
                            messageText = ""
                        }
                    }) {
                        Icon(
                            Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Send",
                            tint = if (messageText.isNotBlank()) AcidGreen else Cream,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MessageBubble(
    text: String,
    isMe: Boolean,
    counterpartName: String,
    timestamp: Long
) {
    val timeStr = remember(timestamp) {
        SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(timestamp))
    }
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isMe) Alignment.End else Alignment.Start
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = 280.dp)
                .background(
                    if (isMe) NearBlack else CreamDark,
                    RoundedCornerShape(
                        topStart = 16.dp, topEnd = 16.dp,
                        bottomStart = if (isMe) 16.dp else 4.dp,
                        bottomEnd = if (isMe) 4.dp else 16.dp
                    )
                )
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Text(
                text = text,
                fontSize = 14.sp,
                color = if (isMe) Cream else NearBlack,
                lineHeight = 20.sp
            )
        }
        Spacer(Modifier.height(2.dp))
        Text(timeStr, fontSize = 10.sp, color = WarmGray)
    }
}