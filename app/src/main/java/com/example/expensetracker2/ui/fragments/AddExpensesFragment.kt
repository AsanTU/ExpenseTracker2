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
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.expensetracker2.R
import com.example.expensetracker2.databinding.FragmentAddExpensesBinding
import com.example.expensetracker2.models.AddResponse
import com.example.expensetracker2.models.CategoriesGetResponse
import com.example.expensetracker2.models.CategoryAddRequest
import com.example.expensetracker2.models.Expense
import com.example.expensetracker2.models.ExpenseCategory
import com.example.expensetracker2.models.ExpenseUpdateRequest
import com.example.expensetracker2.utils.ApiServiceHelper
import com.example.expensetracker2.utils.RetrofitClient
import com.example.expensetracker2.utils.Utils
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AddExpensesFragment : Fragment() {

    private var _binding: FragmentAddExpensesBinding? = null
    private val binding get() = _binding!!
    private val categories = mutableListOf<ExpenseCategory>()

    private var selectedCalendar: Calendar = Calendar.getInstance()

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

        // Set default date and time
        setInitialDate()
        setInitialTime()

        // Check if we are editing an expense
        val expense = arguments?.getParcelable<Expense>("expense") // Deprecated. Might want to change this.
        if (expense != null) {
            // Populate fields with existing expense data
            binding.nameEt.setText(expense.name)
            binding.amountEt.setText(expense.amount)
//            binding.descriptionEt.setText(expense.description)
            val categoryIndex = categories.indexOfFirst { it.id == expense.categoryId }
            if (categoryIndex != -1) {
                binding.categorySpinner.setSelection(categoryIndex)
            }

            // Format and display the date
            expense.date.let {
                try {
                    val parsedDate = Utils.parseInputDate(it)
                    binding.dateTv.text = parsedDate?.let { Utils.formatShowDate(parsedDate) } ?: it
                } catch (e: Exception) {
                    binding.dateTv.text = it // Fallback to original if parsing fails
                }
            }

            // Format and display the time
            expense.time.let {
                try {
                    val parsedTime = Utils.parseInputTime(it)
                    binding.timeTv.text = parsedTime?.let { Utils.formatShowTime(parsedTime) } ?: it
                } catch (e: Exception) {
                    binding.timeTv.text = it // Fallback to original if parsing fails
                }
            }
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
            saveExpense(expense) // Pass expense if editing, null if adding
            true.animateBtn(binding.saveExpenseBtn)
        }

        binding.arrowBackIc.setOnClickListener {
            findNavController().navigate(R.id.action_addExpensesFragment_to_listOfExpensesFragment)
            animateView(binding.arrowBackIc)
        }
    }


    private fun fetchCategories() {
        RetrofitClient.categoryService.getCategories()
            .enqueue(object : Callback<CategoriesGetResponse> {
                override fun onResponse(
                    call: Call<CategoriesGetResponse>, response: Response<CategoriesGetResponse>
                ) {
                    if (response.isSuccessful) {
                        response.body()?.let { categoriesResponse ->
                            if (categoriesResponse.success) {
                                // Clear old categories and add new
                                categories.clear()
                                categories.addAll(categoriesResponse.categories)
                                (binding.categorySpinner.adapter as ArrayAdapter<*>).notifyDataSetChanged()
                            } else {
                                Utils.showToastMessage(requireContext(), categoriesResponse.message ?: "Could not load categories.")
                            }
                        }
                    } else {
                        // TODO: Make this more abstract. Make a separate function for this in ApiService.
                        val errorMessage = ApiServiceHelper.getErrorMessage(
                            response, "Failed to fetch categories."
                        )
                        Utils.showToastMessage(requireContext(), errorMessage)
                    }
                }

                override fun onFailure(call: Call<CategoriesGetResponse>, t: Throwable) {
                    Utils.showToastMessage(requireContext(), "Error: ${t.message}")
                }
            })
    }

    private fun setInitialDate() {
        val currentDate = Utils.formatShowDate(selectedCalendar.time)

        // Set the formatted date string to the TextView
        binding.dateTv.text = currentDate
    }

    private fun setInitialTime() {
        val currentTime = Utils.formatShowTime(selectedCalendar.time)

        // Set the formatted time string to the TextView
        binding.timeTv.text = currentTime
    }

    class CategorySpinnerAdapter(
        context: Context, private val categories: MutableList<ExpenseCategory>
    ) : ArrayAdapter<ExpenseCategory>(context, android.R.layout.simple_spinner_item, categories) {

        init {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val view = super.getView(position, convertView, parent)
            view.findViewById<TextView>(android.R.id.text1).text = categories[position].name
            return view
        }

        override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
            val view = super.getDropDownView(position, convertView, parent)
            view.findViewById<TextView>(android.R.id.text1).text = categories[position].name
            return view
        }

        override fun getItem(position: Int): ExpenseCategory {
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
        fetchCategories()
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
                    Utils.showToastMessage(requireContext(), "Category name cannot be empty")
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
                                        id = categoryAddResponse.id ?: 0, // Using ID from response
                                        name = categoryName
                                    )
                                )
                                (binding.categorySpinner.adapter as ArrayAdapter<*>).notifyDataSetChanged()
                                Utils.showToastMessage(requireContext(), categoryAddResponse.message)
                            } else {
                                // Handle case when success is false
                                Utils.showToastMessage(requireContext(), categoryAddResponse.message)
                            }
                        }
                    } else {
                        val errorMessage =
                            ApiServiceHelper.getErrorMessage(response, "Failed to add category.")
                        Utils.showToastMessage(requireContext(), errorMessage)
                    }
                }

                override fun onFailure(call: Call<AddResponse>, t: Throwable) {
                    Utils.showToastMessage(requireContext(), "Error: ${t.message}")
                }
            })
    }

    private fun setupCurrencySpinner() {
        val currencies = listOf("USD", "EUR", "RUB", "KGS")
        val adapter = ArrayAdapter(
            requireContext(), android.R.layout.simple_spinner_item, currencies
        ).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
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

                val date = Utils.formatShowDate(selectedCalendar.time)

                binding.dateTv.text = date
                Utils.showToastMessage(requireContext(), "Date Selected: $date")
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

                val time = Utils.formatShowTime(selectedCalendar.time)

                binding.timeTv.text = time
                Utils.showToastMessage(requireContext(), "Time Selected: $time")
            }, hour, minute, true // Use 24-hour format
        )
        timePicker.show()
    }

    private fun saveExpense(expense: Expense?) {
        val name = binding.nameEt.text.toString()
        val amountText = binding.amountEt.text.toString()
        val currency = binding.currencySpinner.selectedItem.toString()
        val categoryIndex = binding.categorySpinner.selectedItemPosition
        val selectedCategory = categories[categoryIndex]
        val categoryId = selectedCategory.id
        val date = Utils.formatIsoDate(selectedCalendar.time)
        val time = Utils.formatIsoTime(selectedCalendar.time)

        if (amountText.isEmpty() || date == "Select date") {
            Utils.showToastMessage(requireContext(), "Please fill in all fields")
            return
        }

        val amount = amountText.toDoubleOrNull()
        if (amount == null) {
            Utils.showToastMessage(requireContext(), "Invalid amount")
            return
        }

        val expenseRequest = ExpenseUpdateRequest(
            name = name,
            amount = amount,
            currency = currency,
            description = "added description", // Replace with actual input
            categoryId = categoryId,
            date = date,
            time = time,
        )

        if (expense == null) {
            // Add a new expense
            addExpense(expenseRequest)
        } else {
            // Edit the existing expense
            editExpense(expense.id, expenseRequest)
        }
    }

    private fun addExpense(
        expenseAddRequest: ExpenseUpdateRequest
    ) {
        RetrofitClient.expenseService.addExpense(expenseAddRequest)
            .enqueue(object : Callback<AddResponse> {
                override fun onResponse(call: Call<AddResponse>, response: Response<AddResponse>) {
                    if (response.isSuccessful) {
                        response.body()?.let { addResponse ->
                            if (addResponse.success) {
                                Utils.showToastMessage(requireContext(), "Expense Added!")
                                // Navigate back to the list of expenses upon success
                                findNavController().navigate(R.id.action_addExpensesFragment_to_listOfExpensesFragment)
                            } else {
                                Utils.showToastMessage(requireContext(), addResponse.message)
                            }
                        }
                    } else {
                        val errorMessage =
                            ApiServiceHelper.getErrorMessage(response, "Failed to save expense.")
                        Utils.showToastMessage(requireContext(), errorMessage)
                    }
                }

                override fun onFailure(call: Call<AddResponse>, t: Throwable) {
                    Utils.showToastMessage(requireContext(), "Error: ${t.message}")
                }
            })
    }

    private fun editExpense(expenseId: Int, expenseUpdateRequest: ExpenseUpdateRequest) {
        RetrofitClient.expenseService.editExpense(expenseId, expenseUpdateRequest)
            .enqueue(object : Callback<AddResponse> {
                override fun onResponse(call: Call<AddResponse>, response: Response<AddResponse>) {
                    if (response.isSuccessful) {
                        response.body()?.let { updateResponse ->
                            if (updateResponse.success) {
                                Utils.showToastMessage(requireContext(), "Expense Updated!")
                                // Navigate back to the list of expenses upon success
                                findNavController().navigate(R.id.action_addExpensesFragment_to_listOfExpensesFragment)
                            } else {
                                Utils.showToastMessage(requireContext(), updateResponse.message)
                            }
                        }
                    } else {
                        val errorMessage =
                            ApiServiceHelper.getErrorMessage(response, "Failed to edit expense.")
                        Utils.showToastMessage(requireContext(), errorMessage)
                    }
                }

                override fun onFailure(call: Call<AddResponse>, t: Throwable) {
                    Utils.showToastMessage(requireContext(), "Error: ${t.message}")
                }
            })
    }

    private fun animateView(view: View) {
        ObjectAnimator.ofFloat(view, "rotation", 0f, 360f).apply {
            duration = 300
            start()
        }
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