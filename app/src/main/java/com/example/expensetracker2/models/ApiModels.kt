package com.example.expensetracker2.models

data class SuccessMessageResponse(
    val success: Boolean,
    val message: String
)

data class AddResponse(
    val success: Boolean,
    val message: String,
    val id: Int
)

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

data class ExpenseAddRequest(
    val name: String,
    val amount: Double,
    val currency: String,
    val description: String? = null,
    val category_id: Int? = null,
    val date: String,
    val time: String
)

data class CategoryAddRequest(
    val name: String
)