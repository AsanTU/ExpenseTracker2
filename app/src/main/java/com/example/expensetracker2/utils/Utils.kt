package com.example.expensetracker2.utils

import android.content.Context
import android.widget.Toast
import java.text.SimpleDateFormat
import java.time.format.DateTimeFormatter
import java.util.Locale

object Utils {
    fun showToastMessage(context: Context, message: String) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }

    val ISO_DATE_FORMAT = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val ISO_TIME_FORMAT = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
    val ISO_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd")
}