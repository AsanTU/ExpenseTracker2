package com.example.expensetracker2.utils

import android.content.Context
import android.widget.Toast
import java.text.SimpleDateFormat
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale

object Utils {
    fun showToastMessage(context: Context, message: String) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }

    val ISO_DATE_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    fun formatIsoDate(date: Date): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(date)
    }

    fun formatIsoTime(date: Date): String {
        return SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(date)
    }

    fun formatShowDate(date: Date): String {
        return SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(date)
    }

    fun formatShowTime(date: Date): String {
//        return SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(date)
        return formatIsoTime(date)
    }

    fun parseInputDate(inputDate: String): Date? {
        return SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(inputDate)
    }

    fun parseInputTime(inputTime: String): Date? {
        return SimpleDateFormat("HH:mm:ss", Locale.getDefault()).parse(inputTime)
    }
}