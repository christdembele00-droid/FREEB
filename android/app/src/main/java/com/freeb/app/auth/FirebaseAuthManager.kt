package com.freeb.app.auth

import com.google.firebase.auth.FirebaseAuth

class FirebaseAuthManager(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {
    val currentUser get() = auth.currentUser

    fun signOut() = auth.signOut()

    fun signIn(
        email: String,
        password: String,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener(onError)
    }

    fun register(
        email: String,
        password: String,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener(onError)
    }
}
