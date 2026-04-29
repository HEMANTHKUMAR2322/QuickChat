package com.example.quickchat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddContactScreen(navController: NavHostController) {

    var name by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var selectedCountryCode by remember { mutableStateOf("+44") }
    var expanded by remember { mutableStateOf(false) }

    var errorMessage by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    val countryCodes = listOf(
        "UK (+44)",
        "India (+91)",
        "USA (+1)"
    )

    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    val currentUserId = auth.currentUser?.uid ?: ""

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
            text = "Add Contact",
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold,
            fontStyle = FontStyle.Italic,
            color = Color.White
        )

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = name,
            onValueChange = {
                name = it
                errorMessage = ""
            },
            label = { Text("Contact Name") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            OutlinedTextField(
                value = selectedCountryCode,
                onValueChange = {},
                readOnly = true,
                label = { Text("Country Code") },
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor()
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                countryCodes.forEach { country ->
                    DropdownMenuItem(
                        text = { Text(country) },
                        onClick = {
                            selectedCountryCode = when (country) {
                                "UK (+44)" -> "+44"
                                "India (+91)" -> "+91"
                                "USA (+1)" -> "+1"
                                else -> "+44"
                            }
                            expanded = false
                            errorMessage = ""
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = phoneNumber,
            onValueChange = {
                phoneNumber = it.filter { char -> char.isDigit() }
                errorMessage = ""
            },
            label = { Text("Phone Number") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
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
                if (name.isBlank() || phoneNumber.isBlank()) {
                    errorMessage = "Please fill all fields"
                    return@Button
                }

                if (currentUserId.isBlank()) {
                    errorMessage = "User not logged in"
                    return@Button
                }

                isLoading = true

                val fullPhoneNumber = "$selectedCountryCode$phoneNumber"

                val contactData = hashMapOf(
                    "name" to name.trim(),
                    "countryCode" to selectedCountryCode,
                    "phoneNumber" to phoneNumber.trim(),
                    "fullPhoneNumber" to fullPhoneNumber,
                    "ownerId" to currentUserId,
                    "createdAt" to System.currentTimeMillis()
                )

                db.collection("users")
                    .document(currentUserId)
                    .collection("contacts")
                    .add(contactData)
                    .addOnSuccessListener {
                        isLoading = false
                        navController.popBackStack()
                    }
                    .addOnFailureListener { e ->
                        isLoading = false
                        errorMessage = e.message ?: "Failed to add contact"
                    }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(color = Color.White)
            } else {
                Text("Save Contact")
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        TextButton(
            onClick = { navController.popBackStack() },
            enabled = !isLoading
        ) {
            Text("Back")
        }
    }
}