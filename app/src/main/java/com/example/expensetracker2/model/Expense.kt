package com.example.expensetracker2.model

import java.io.Serializable
import java.util.UUID

data class Expense(
    val id: String = UUID.randomUUID().toString(),
    val category: String,
    val date: String,
    val amount: String,
    val currency: String
) : Serializable