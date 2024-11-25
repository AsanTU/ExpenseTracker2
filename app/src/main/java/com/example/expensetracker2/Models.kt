package com.example.expensetracker2

data class LoginRequest(
    val email: String? = null,
    val password: String,
    val username: String? = null
)

data class RegisterRequest(
    val email: String,
    val password: String,
    val username: String? = null
)

data class LoginResponse(
    val success: Boolean,
    val access_token: String? = null,
    val refresh_token: String? = null,
    val message: String? = null
)

data class RegisterResponse(
    val success: Boolean,
    val message: String
)