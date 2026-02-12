package com.uxonauts.resq.controllers

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.navigation.NavController

class AuthController : ViewModel() {
    // State Login
    var loginEmail by mutableStateOf("")
    var loginPassword by mutableStateOf("")

    // State Sign Up - Navigasi
    var currentSignUpStep by mutableStateOf(1)

    // Step 1: Biodata
    var firstName by mutableStateOf("")
    var lastName by mutableStateOf("")
    var gender by mutableStateOf("Laki-laki")
    var address by mutableStateOf("")
    var phone by mutableStateOf("")
    var email by mutableStateOf("")
    var password by mutableStateOf("")

    // Step 2: KTP
    var ktpImageUri by mutableStateOf<String?>(null)

    // Step 3: Medis
    var height by mutableStateOf("")
    var weight by mutableStateOf("")
    var bloodType by mutableStateOf("AB-")
    var diseaseHistory by mutableStateOf("")
    var routineMeds by mutableStateOf("")
    var allergies by mutableStateOf("")

    // Step 4: Kontak Darurat
    var ecFirstName by mutableStateOf("")
    var ecLastName by mutableStateOf("")
    var ecRelation by mutableStateOf("Orang Tua")
    var ecPhone by mutableStateOf("")
    var termsAccepted by mutableStateOf(false)

    fun doLogin(navController: NavController) {
        println("Memproses login untuk: $loginEmail")
        // TODO: Integrasi FirebaseAuth
    }

    fun submitRegistration(navController: NavController) {
        println("Menyimpan pendaftaran ke Firebase...")
        // TODO: Integrasi FirebaseAuth, Storage (KTP), & Firestore
    }
}