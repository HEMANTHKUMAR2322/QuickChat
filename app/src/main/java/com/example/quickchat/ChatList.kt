package com.example.quickchat

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

data class ContactItem(
    val id: String = "",
    val name: String = "",
    val phoneNumber: String = "",
    val fullPhoneNumber: String = ""
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatList(navController: NavHostController) {

    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    val currentUserId = auth.currentUser?.uid ?: ""

    val contacts = remember { mutableStateListOf<ContactItem>() }
    var selectedTab by remember { mutableStateOf("chats") }

    val backgroundBrush = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF0F9D58),
            Color(0xFFF4F1A1),
            Color(0xFFFFD700)
        )
    )

    var listenerRegistration: ListenerRegistration? = remember { null }

    DisposableEffect(currentUserId) {
        if (currentUserId.isNotBlank()) {
            listenerRegistration = db.collection("users")
                .document(currentUserId)
                .collection("contacts")
                .addSnapshotListener { result, error ->
                    if (error == null && result != null) {
                        contacts.clear()
                        for (doc in result.documents) {
                            contacts.add(
                                ContactItem(
                                    id = doc.id,
                                    name = doc.getString("name") ?: "",
                                    phoneNumber = doc.getString("phoneNumber") ?: "",
                                    fullPhoneNumber = doc.getString("fullPhoneNumber") ?: ""
                                )
                            )
                        }
                    }
                }
        }

        onDispose {
            listenerRegistration?.remove()
        }
    }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "QuickChat",
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                },
                actions = {
                    IconButton(
                        onClick = {
                            auth.signOut()
                            navController.navigate("login") {
                                popUpTo("chatlist") { inclusive = true }
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                            contentDescription = "Logout",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF0B8043)
                )
            )
        },
        floatingActionButton = {
            if (selectedTab == "chats") {
                FloatingActionButton(
                    onClick = { navController.navigate("add_contact") },
                    containerColor = Color(0xFF0B8043),
                    contentColor = Color.White
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Contact"
                    )
                }
            }
        },
        bottomBar = {
            NavigationBar(
                containerColor = Color.White.copy(alpha = 0.95f)
            ) {
                NavigationBarItem(
                    selected = selectedTab == "chats",
                    onClick = { selectedTab = "chats" },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.ChatBubble,
                            contentDescription = "Chats"
                        )
                    },
                    label = { Text("Chats") }
                )

                NavigationBarItem(
                    selected = selectedTab == "profile",
                    onClick = {
                        navController.navigate("profile")
                    },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Profile"
                        )
                    },
                    label = { Text("Profile") }
                )
            }
        }
    ) { innerPadding ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundBrush)
                .padding(innerPadding)
        ) {
            if (selectedTab == "chats") {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    Text(
                        text = "Chats",
                        fontSize = 30.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(bottom = 14.dp)
                    )

                    if (contacts.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = Color.White.copy(alpha = 0.9f)
                                ),
                                shape = RoundedCornerShape(20.dp)
                            ) {
                                Text(
                                    text = "No contacts yet.\nTap + to add and start chatting.",
                                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 18.dp),
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = Color(0xFF0B8043),
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(contacts) { contact ->
                                ContactCard(
                                    contact = contact,
                                    onClick = {
                                        navController.navigate("chat/${contact.id}/${contact.name}")
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ContactCard(
    contact: ContactItem,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.95f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Surface(
                modifier = Modifier.size(52.dp),
                shape = CircleShape,
                color = Color(0xFF0F9D58)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = contact.name.take(1).uppercase(),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                }
            }

            Spacer(modifier = Modifier.size(12.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = contact.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Color.Black
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = contact.fullPhoneNumber,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.DarkGray
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = "Tap to open chat",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }

            Text(
                text = "Now",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
        }
    }
}