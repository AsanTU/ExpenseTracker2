package com.example.expensetracker2.models

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize
import java.io.Serializable

@Parcelize
data class Expense(
    val id: Int,
    val name: String,
    val amount: String,
    val currency: String,
    val description: String?,
    @SerializedName("category_id") val categoryId: Int?,
    val date: String,
    val time: String
) : Serializable, Parcelable

data class ExpenseCategory(
    val id: Int,
    val name: String
) : Serializable