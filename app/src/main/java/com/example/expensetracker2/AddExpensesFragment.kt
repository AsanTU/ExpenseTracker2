package com.example.expensetracker2

import android.animation.ObjectAnimator
import android.app.DatePickerDialog
import android.icu.util.Calendar
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.example.expensetracker2.databinding.FragmentAddExpensesBinding
import com.example.expensetracker2.databinding.FragmentLoginScreenBinding

class AddExpensesFragment : Fragment() {

    private var _binding: FragmentAddExpensesBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentAddExpensesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupCategorySpinner()
        setupCurrencySpinner()
        binding.addCategoryBtn.setOnClickListener{
            Toast.makeText(requireContext(), "Add Category Clicked", Toast.LENGTH_SHORT).show()
            animateView(binding.addCategoryBtn)
        }

        binding.selectDateBtn.setOnClickListener {
            showDatePickerDialog()
            animateView(binding.selectDateBtn)
        }

        binding.saveExpenseBtn.setOnClickListener {
            saveExpense()
            animateBtn(binding.saveExpenseBtn, true)
        }
    }

    private fun setupCategorySpinner() {
        val categories = listOf("Food", "Transport", "Entertainment", "Other")
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            categories
        )
        binding.categorySpinner.adapter = adapter
    }

    private fun setupCurrencySpinner(){
        val currencies = listOf("USD", "EUR", "RUB", "KGS")
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            currencies
        )
        binding.currencySpinner.adapter = adapter
    }

    private fun showDatePickerDialog(){
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        val datePicker = DatePickerDialog(
            requireContext(),
            {_, selectedYear, selectedMonth, selectedDay ->
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

    private fun saveExpense() {
        val category = binding.categorySpinner.selectedItem.toString()
        val date = binding.dateTv.text.toString()
        val amount = binding.amountEt.text.toString()
        val currency = binding.currencySpinner.selectedItem.toString()

        if (date == "Select date" || amount.isEmpty()) {
            Toast.makeText(requireContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show()
        } else {
            val bundle = Bundle().apply {
                putString("category", category)
                putString("date", date)
                putString("amount", amount)
                putString("currency", currency)
            }

            findNavController().navigate(
                R.id.action_addExpensesFragment_to_listOfExpensesFragment,
                bundle
            )
        }
    }


    private fun animateView(view: View){
        ObjectAnimator.ofFloat(view, "rotation", 0f, 360f).apply {
            duration = 300
            start()
        }
    }

    private fun animateBtn(button: View, success: Boolean){
        val scaleX = if (success) 1.1f else 0.9f
        val scaleY = if (success) 1.1f else 0.9f
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

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}