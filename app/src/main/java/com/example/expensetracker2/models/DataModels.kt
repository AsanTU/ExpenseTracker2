package com.example.expensetracker2.models

import java.io.Serializable

data class Expense(
    val id: Int,
    val name: String,
    val amount: String,
    val currency: String,
    val description: String?,
    val category_id: Int?,
    val category_name: String?,
    val date: String,
    val time: String
) : Serializable

data class ExpenseCategory(
    val id: Int,
    val name: String
) : Serializable