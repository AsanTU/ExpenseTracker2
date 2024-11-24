package com.example.expensetracker2

import java.io.Serializable

data class Expense(
    val category: String,
    val date: String,
    val amount: String,
    val currency: String
): Serializable