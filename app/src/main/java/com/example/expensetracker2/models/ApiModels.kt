package com.example.expensetracker2.models

import com.google.gson.annotations.SerializedName

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

data class LoginResponse(
    val success: Boolean,
    @SerializedName("access_token") val accessToken: String? = null,
    @SerializedName("refresh_token") val refreshToken: String? = null,
    @SerializedName("access_token_expires_at") val accessTokenExpiresAt: String? = null,
    @SerializedName("refresh_token_expires_at") val refreshTokenExpiresAt: String? = null,
    val message: String? = null
)

data class RegisterRequest(
    val email: String,
    val password: String,
    val username: String? = null
)

data class TokenRefreshRequest(
    val refreshToken: String
)

data class TokenRefreshResponse(
    @SerializedName("access_token") val accessToken: String? = null,
    @SerializedName("access_token_expires_at") val accessTokenExpiresAt: String? = null,
)

data class CategoriesGetResponse(
    val success: Boolean,
    val categories: List<ExpenseCategory>,
    val message: String? = null
)

data class CategoryAddRequest(
    val name: String
)

data class ExpenseAddRequest(
    val name: String,
    val amount: Double,
    val currency: String,
    val description: String? = null,
    val categoryId: Int? = null,
    val date: String,
    val time: String
)