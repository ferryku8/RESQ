package com.uxonauts.resq.controllers

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.uxonauts.resq.models.User
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class HomeController : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    var userName by mutableStateOf("Pengguna")
    var isLoading by mutableStateOf(true)
    var sosState by mutableStateOf(false) // false = idle, true = active/sending

    init {
        fetchUserProfile()
    }

    fun fetchUserProfile() {
        viewModelScope.launch {
            val uid = auth.currentUser?.uid
            if (uid != null) {
                try {
                    val document = db.collection("users").document(uid).get().await()
                    val user = document.toObject(User::class.java)
                    if (user != null) {
                        userName = user.namaLengkap.split(" ").firstOrNull() ?: "Pengguna"
                    }
                } catch (e: Exception) {
                    userName = "Pengguna"
                }
            }
            isLoading = false
        }
    }

    fun triggerSos() {
        viewModelScope.launch {
            sosState = true
            kotlinx.coroutines.delay(2000) // Delay simulasi
            sosState = false
        }
    }

    fun logout(navController: androidx.navigation.NavController) {
        auth.signOut()
        navController.navigate("login") {
            popUpTo(0)
        }
    }
}