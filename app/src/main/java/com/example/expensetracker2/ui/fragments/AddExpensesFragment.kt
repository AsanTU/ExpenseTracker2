package com.example.expensetracker2.ui.fragments

import android.animation.ObjectAnimator
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.icu.util.Calendar
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.example.expensetracker2.model.Expense
import com.example.expensetracker2.repository.ExpenseRepository
import com.example.expensetracker2.R
import com.example.expensetracker2.databinding.FragmentAddExpensesBinding

class AddExpensesFragment : Fragment() {

    private var _binding: FragmentAddExpensesBinding? = null
    private val binding get() = _binding!!
    private val categories = mutableListOf("Food", "Transport", "Entertainment", "Other")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddExpensesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupCategorySpinner()
        setupCurrencySpinner()

        val position = arguments?.getInt("position", -1) ?: -1
        if (position != -1) {
            val expense = ExpenseRepository.expenseList[position]
            binding.categorySpinner.setSelection(categories.indexOf(expense.category))
            binding.amountEt.setText(expense.amount)
            binding.dateTv.text = expense.date
            binding.currencySpinner.setSelection(getCurrencyIndex(expense.currency))
        }

        binding.addCategoryBtn.setOnClickListener {
            showAddCategoryDialog()
            animateView(binding.addCategoryBtn)
        }

        binding.selectDateBtn.setOnClickListener {
            showDatePickerDialog()
            animateView(binding.selectDateBtn)
        }

        binding.saveExpenseBtn.setOnClickListener {
            saveExpense(position)
            true.animateBtn(binding.saveExpenseBtn)
        }
    }

    private fun setupCategorySpinner() {
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            categories
        )
        binding.categorySpinner.adapter = adapter
    }

    private fun showAddCategoryDialog() {
        val input = EditText(requireContext())
        input.hint = "Enter new category"

        val dialog = AlertDialog.Builder(requireContext())
            .setTitle("Add Category")
            .setView(input)
            .setPositiveButton("Add") { dialog, _ ->
                val newCategory = input.text.toString().trim()
                if (newCategory.isNotEmpty()) {
                    categories.add(newCategory)
                    val adapter = binding.categorySpinner.adapter as ArrayAdapter<*>
                    adapter.notifyDataSetChanged()
                    Toast.makeText(
                        requireContext(),
                        "Category added: $newCategory",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    Toast.makeText(
                        requireContext(),
                        "Category name cannot be empty",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .create()

        dialog.show()
    }

    private fun setupCurrencySpinner() {
        val currencies = listOf("USD", "EUR", "RUB", "KGS")
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            currencies
        )
        binding.currencySpinner.adapter = adapter
    }

    private fun showDatePickerDialog() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        val datePicker = DatePickerDialog(
            requireContext(),
            { _, selectedYear, selectedMonth, selectedDay ->
                val date = "$selectedDay/${selectedMonth + 1}/$selectedYear"
                binding.dateTv.text = date
                Toast.makeText(requireContext(), "Date Selected: $date", Toast.LENGTH_SHORT).show()
            },
            year,
            month,
            day
        )
        datePicker.show()
    }

    private fun saveExpense(position: Int) {
        val category = binding.categorySpinner.selectedItem.toString()
        val date = binding.dateTv.text.toString()
        val amount = binding.amountEt.text.toString()
        val currency = binding.currencySpinner.selectedItem.toString()

        if (amount.isEmpty() || date == "Select date") {
            Toast.makeText(requireContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show()
            return
        }

        val expense =
            Expense(System.currentTimeMillis().toString(), category, date, amount, currency)
        if (position != -1) {
            ExpenseRepository.expenseList[position] = expense
            Toast.makeText(requireContext(), "Expense Updated!", Toast.LENGTH_SHORT).show()
        } else {
            ExpenseRepository.expenseList.add(expense)
            Toast.makeText(requireContext(), "Expense Added!", Toast.LENGTH_SHORT).show()
        }

        findNavController().navigate(R.id.action_addExpensesFragment_to_listOfExpensesFragment)
    }


    private fun animateView(view: View) {
        ObjectAnimator.ofFloat(view, "rotation", 0f, 360f).apply {
            duration = 300
            start()
        }
    }

    private fun getCurrencyIndex(currency: String): Int {
        val currencies = listOf("USD", "EUR", "RUB", "KGS")
        return currencies.indexOf(currency)
    }

    private fun Boolean.animateBtn(button: View) {
        val scaleX = if (this) 1.1f else 0.9f
        val scaleY = if (this) 1.1f else 0.9f
        ObjectAnimator.ofFloat(button, "scaleX", scaleX).apply {
            duration = 150
            repeatMode = ObjectAnimator.REVERSE
            repeatCount = 1
        }.start()

        ObjectAnimator.ofFloat(button, "scaleY", scaleY).apply {
            duration = 150
            repeatMode = ObjectAnimator.REVERSE
            repeatCount = 1
        }.start()
    }
}