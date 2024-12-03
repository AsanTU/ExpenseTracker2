package com.example.expensetracker2.ui.fragments

import android.animation.ObjectAnimator
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.icu.util.Calendar
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.expensetracker2.R
import com.example.expensetracker2.databinding.FragmentAddExpensesBinding
import com.example.expensetracker2.models.AddResponse
import com.example.expensetracker2.models.CategoryAddRequest
import com.example.expensetracker2.models.Expense
import com.example.expensetracker2.models.ExpenseAddRequest
import com.example.expensetracker2.models.ExpenseCategory
import com.example.expensetracker2.repository.ExpenseRepository
import com.example.expensetracker2.utils.ApiServiceHelper
import com.example.expensetracker2.utils.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat

class AddExpensesFragment : Fragment() {

    private var _binding: FragmentAddExpensesBinding? = null
    private val binding get() = _binding!!
    private val categories = mutableListOf(
        ExpenseCategory(1, "Food"),
        ExpenseCategory(2, "Transport"),
        ExpenseCategory(3, "Entertainment"),
        ExpenseCategory(4, "Other")
    )

    private var selectedCalendar: Calendar = Calendar.getInstance()
    val showDateFormat = SimpleDateFormat("dd/MM/yyyy")
    val showTimeFormat = SimpleDateFormat("HH:mm:ss")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddExpensesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupCategorySpinner()
        setupCurrencySpinner()

        // Here's where you set the current date and time as the default
        setInitialDate()
        setInitialTime()

        val position = arguments?.getInt("position", -1) ?: -1
        if (position != -1) {
            val expense = ExpenseRepository.expenseList[position]
            binding.categorySpinner.setSelection(categories.indexOfFirst { it.id == expense.categoryId })
            binding.amountEt.setText(expense.amount)
            binding.dateTv.text =
                expense.date // This line will override today's date if editing an expense
            binding.timeTv.text =
                expense.time // This line will override the current time if editing an expense
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

        binding.selectTimeBtn.setOnClickListener {
            showTimePickerDialog()
            animateView(binding.selectTimeBtn)
        }

        binding.saveExpenseBtn.setOnClickListener {
            saveExpense(position)
            true.animateBtn(binding.saveExpenseBtn)
        }

        binding.arrowBackIc.setOnClickListener {
            findNavController().navigate(R.id.action_addExpensesFragment_to_listOfExpensesFragment)
            animateView(binding.arrowBackIc)
        }
    }

    private fun setInitialDate() {
        val currentDate = showDateFormat.format(selectedCalendar.time)

        // Set the formatted date string to the TextView
        binding.dateTv.text = currentDate
    }

    private fun setInitialTime() {
        val currentTime = showTimeFormat.format(selectedCalendar.time)

        // Set the formatted time string to the TextView
        binding.timeTv.text = currentTime
    }

    class CategorySpinnerAdapter(
        context: Context, private val categories: MutableList<ExpenseCategory>
    ) : ArrayAdapter<ExpenseCategory>(context, android.R.layout.simple_spinner_item, categories) {

        init {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }

        override fun getItem(position: Int): ExpenseCategory? {
            return categories[position]
        }

        override fun getItemId(position: Int): Long {
            return categories[position].id.toLong()
        }

        override fun getPosition(item: ExpenseCategory?): Int {
            return categories.indexOf(item)
        }
    }

    private fun setupCategorySpinner() {
        val adapter = CategorySpinnerAdapter(requireContext(), categories)
        binding.categorySpinner.adapter = adapter
    }

    private fun showAddCategoryDialog() {
        val input = EditText(requireContext())
        input.hint = "Enter new category"

        val dialog = AlertDialog.Builder(requireContext()).setTitle("Add Category").setView(input)
            .setPositiveButton("Add") { dialog, _ ->
                val newCategoryName = input.text.toString().trim()
                if (newCategoryName.isNotEmpty()) {
                    addCategory(newCategoryName)
                } else {
                    Toast.makeText(
                        requireContext(), "Category name cannot be empty", Toast.LENGTH_SHORT
                    ).show()
                }
                dialog.dismiss()
            }.setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }.create()

        dialog.show()
    }

    private fun addCategory(categoryName: String) {
        // Create request body for category addition
        val categoryAddRequest = CategoryAddRequest(name = categoryName)

        // Call the category service to add a new category
        RetrofitClient.categoryService.addCategory(categoryAddRequest)
            .enqueue(object : Callback<AddResponse> {
                override fun onResponse(
                    call: Call<AddResponse>, response: Response<AddResponse>
                ) {
                    if (response.isSuccessful) {
                        response.body()?.let { categoryAddResponse ->
                            if (categoryAddResponse.success) {
                                // Update local categories list and UI
                                categories.add(
                                    ExpenseCategory(
                                        id = categoryAddResponse.id, // Using ID from response
                                        name = categoryName
                                    )
                                )
                                val adapter =
                                    binding.categorySpinner.adapter as ArrayAdapter<ExpenseCategory>
                                adapter.notifyDataSetChanged()
                                Toast.makeText(
                                    requireContext(),
                                    categoryAddResponse.message,
                                    Toast.LENGTH_SHORT
                                ).show()
                            } else {
                                // Handle case when success is false
                                Toast.makeText(
                                    requireContext(),
                                    categoryAddResponse.message,
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    } else {
                        val errorMessage =
                            ApiServiceHelper.getErrorMessage(response, "Failed to add category.")
                        Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_LONG).show()
                    }
                }

                override fun onFailure(call: Call<AddResponse>, t: Throwable) {
                    Toast.makeText(requireContext(), "Error: ${t.message}", Toast.LENGTH_SHORT)
                        .show()
                }
            })
    }

    private fun setupCurrencySpinner() {
        val currencies = listOf("USD", "EUR", "RUB", "KGS")
        val adapter = ArrayAdapter(
            requireContext(), android.R.layout.simple_spinner_dropdown_item, currencies
        )
        binding.currencySpinner.adapter = adapter
    }

    private fun showDatePickerDialog() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        val datePicker = DatePickerDialog(
            requireContext(), { _, selectedYear, selectedMonth, selectedDay ->
                // Update the selected calendar with date values
                selectedCalendar.set(Calendar.YEAR, selectedYear)
                selectedCalendar.set(Calendar.MONTH, selectedMonth)
                selectedCalendar.set(Calendar.DAY_OF_MONTH, selectedDay)

                val date = showDateFormat.format(selectedCalendar.time)

                binding.dateTv.text = date
                Toast.makeText(requireContext(), "Date Selected: $date", Toast.LENGTH_SHORT)
                    .show()
            }, year, month, day
        )
        datePicker.show()
    }

    private fun showTimePickerDialog() {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        val timePicker = TimePickerDialog(
            requireContext(), { _, selectedHour, selectedMinute ->
                // Update the selected calendar with time values
                selectedCalendar.set(Calendar.HOUR_OF_DAY, selectedHour)
                selectedCalendar.set(Calendar.MINUTE, selectedMinute)
                selectedCalendar.set(Calendar.SECOND, 0)

                val time = showTimeFormat.format(selectedCalendar.time)

                binding.timeTv.text = time
                Toast.makeText(requireContext(), "Time Selected: $time", Toast.LENGTH_SHORT)
                    .show()
            }, hour, minute, true // Use 24-hour format
        )
        timePicker.show()
    }

    private fun saveExpense(position: Int) {
        val isoDateFormat = SimpleDateFormat("yyyy-MM-dd")
        val isoTimeFormat = SimpleDateFormat("HH:mm:ss")

        val name = binding.nameEt.text.toString()
        val amountText = binding.amountEt.text.toString()
        val currency = binding.currencySpinner.selectedItem.toString()
        val selectedCategory = binding.categorySpinner.selectedItem as ExpenseCategory
        val categoryId = selectedCategory.id
        val categoryName = selectedCategory.name
        val date = isoDateFormat.format(selectedCalendar.time)
        val time = isoTimeFormat.format(selectedCalendar.time)

        if (amountText.isEmpty() || date == "Select date") {
            Toast.makeText(requireContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show()
            return
        }

        val amount = amountText.toDoubleOrNull()
        if (amount == null) {
            Toast.makeText(requireContext(), "Invalid amount", Toast.LENGTH_SHORT).show()
            return
        }

        val expenseAddRequest = ExpenseAddRequest(
            name = name,
            amount = amount,
            currency = currency,
            description = "description", // Replace with actual input
            categoryId = categoryId,
            date = date,
            time = time,
        )

        addExpense(expenseAddRequest, position, categoryName)
    }

    private fun addExpense(
        expenseAddRequest: ExpenseAddRequest, position: Int, selectedCategoryName: String
    ) {
        RetrofitClient.expenseService.addExpense(expenseAddRequest)
            .enqueue(object : Callback<AddResponse> {
                override fun onResponse(call: Call<AddResponse>, response: Response<AddResponse>) {
                    if (response.isSuccessful) {
                        response.body()?.let { addResponse ->
                            if (addResponse.success) {
                                val expense = Expense(
                                    id = addResponse.id,
                                    name = expenseAddRequest.name,
                                    amount = expenseAddRequest.amount.toString(), // Convert back to String for display
                                    currency = expenseAddRequest.currency,
                                    description = expenseAddRequest.description,
                                    categoryId = expenseAddRequest.categoryId,
                                    categoryName = selectedCategoryName,
                                    date = expenseAddRequest.date,
                                    time = expenseAddRequest.time
                                )

                                if (position != -1) {
                                    ExpenseRepository.expenseList[position] = expense
                                    Toast.makeText(
                                        requireContext(), "Expense Updated!", Toast.LENGTH_SHORT
                                    ).show()
                                } else {
                                    ExpenseRepository.expenseList.add(expense)
                                    Toast.makeText(
                                        requireContext(), "Expense Added!", Toast.LENGTH_SHORT
                                    ).show()
                                }

                                findNavController().navigate(R.id.action_addExpensesFragment_to_listOfExpensesFragment)
                            } else {
                                Toast.makeText(
                                    requireContext(), addResponse.message, Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    } else {
                        val errorMessage =
                            ApiServiceHelper.getErrorMessage(response, "Failed to save expense.")
                        Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_LONG).show()
                    }
                }

                override fun onFailure(call: Call<AddResponse>, t: Throwable) {
                    Toast.makeText(requireContext(), "Error: ${t.message}", Toast.LENGTH_SHORT)
                        .show()
                }
            })
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