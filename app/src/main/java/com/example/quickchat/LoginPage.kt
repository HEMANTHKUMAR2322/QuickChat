package com.example.quickchat

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.google.firebase.auth.FirebaseAuth

@Composable
fun LoginPage(navController: NavHostController) {

    val context = LocalContext.current

    val preferences = remember {
        context.getSharedPreferences(
            "quickchat_preferences",
            Context.MODE_PRIVATE
        )
    }

    var gdprAccepted by remember {
        mutableStateOf(
            preferences.getBoolean("gdpr_accepted", false)
        )
    }

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    val auth = remember { FirebaseAuth.getInstance() }

    val gradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF0F9D58),
            Color(0xFFF4F1A1),
            Color(0xFFFFD700)
        )
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(gradient)
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text(
            text = "Welcome to QuickChat",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            fontStyle = FontStyle.Italic,
            color = Color.Black
        )

        Spacer(modifier = Modifier.height(40.dp))

        OutlinedTextField(
            value = email,
            onValueChange = {
                email = it
                errorMessage = ""
            },
            label = {
                Text("Email")
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            enabled = gdprAccepted && !isLoading
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = password,
            onValueChange = {
                password = it
                errorMessage = ""
            },
            label = {
                Text("Password")
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            enabled = gdprAccepted && !isLoading
        )

        Spacer(modifier = Modifier.height(12.dp))

        if (errorMessage.isNotEmpty()) {
            Text(
                text = errorMessage,
                color = Color.Red,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))
        }

        Button(
            onClick = {
                if (!gdprAccepted) {
                    errorMessage = "Please accept the GDPR notice first"
                    return@Button
                }

                if (email.isBlank() || password.isBlank()) {
                    errorMessage = "Please enter email and password"
                    return@Button
                }

                isLoading = true
                errorMessage = ""

                auth.signInWithEmailAndPassword(
                    email.trim(),
                    password.trim()
                ).addOnCompleteListener { task ->

                    isLoading = false

                    if (task.isSuccessful) {
                        navController.navigate("chatlist") {
                            popUpTo("login") {
                                inclusive = true
                            }
                        }
                    } else {
                        errorMessage =
                            task.exception?.message ?: "Login failed"
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = gdprAccepted && !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    color = Color.White,
                    modifier = Modifier.padding(4.dp)
                )
            } else {
                Text("Login")
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        TextButton(
            onClick = {
                navController.navigate("signup")
            },
            enabled = gdprAccepted && !isLoading
        ) {
            Text(
                text = "Create new account",
                color = Color.Black
            )
        }
    }

    if (!gdprAccepted) {
        AlertDialog(
            onDismissRequest = {
                // Dialog cannot be dismissed by tapping outside.
            },
            title = {
                Text(
                    text = "Privacy and GDPR Notice",
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            },
            text = {
                Text(
                    text = """
                        QuickChat uses Firebase Authentication and Firestore to store account details, contacts and chat messages.
                        
                        The app collects only the information required to provide its features. Your location is accessed only when you choose to share it and grant permission.
                        
                        By selecting Accept, you confirm that you understand how your data is used and agree to the QuickChat privacy policy.
                    """.trimIndent(),
                    color = Color.Black
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        preferences.edit()
                            .putBoolean("gdpr_accepted", true)
                            .apply()

                        gdprAccepted = true
                    }
                ) {
                    Text("Accept")
                }
            },
            containerColor = Color.White
        )
    }
}