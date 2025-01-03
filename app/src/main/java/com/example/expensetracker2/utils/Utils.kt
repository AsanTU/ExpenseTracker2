package com.example.expensetracker2.utils

import android.content.Context
import android.widget.Toast

object Utils {
    fun showToastMessage(context: Context, message: String) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }
}