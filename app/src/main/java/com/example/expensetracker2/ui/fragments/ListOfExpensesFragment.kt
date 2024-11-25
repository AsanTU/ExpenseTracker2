package com.example.expensetracker2.ui.fragments

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.expensetracker2.model.Expense
import com.example.expensetracker2.utils.ExpenseAdapter
import com.example.expensetracker2.repository.ExpenseRepository
import com.example.expensetracker2.R
import com.example.expensetracker2.databinding.FragmentListOfExpensesBinding
import java.util.Calendar

class ListOfExpensesFragment : Fragment() {

    private var _binding: FragmentListOfExpensesBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: ExpenseAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentListOfExpensesBinding.inflate(inflater, container, false)
        return binding.root
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupBottomNavigationListener()

        adapter = ExpenseAdapter(ExpenseRepository.expenseList) { expense, position ->
            findNavController().navigate(
                R.id.action_listOfExpensesFragment_to_addExpensesFragment2,
                Bundle().apply {
                    putString("category", expense.category)
                    putString("amount", expense.amount)
                    putString("date", expense.date)
                    putString("currency", expense.currency)
                    putInt("position", position)
                }
            )
        }

        binding.expenseRv.layoutManager = LinearLayoutManager(requireContext())
        binding.expenseRv.adapter = adapter

        setupSwipeToDeleteAndEdit(binding.expenseRv, adapter)

        binding.filterByDateText.setOnClickListener { showDatePickerDialog() }
        binding.addExpenseBtn.setOnClickListener {
            findNavController().navigate(R.id.action_listOfExpensesFragment_to_addExpensesFragment2)
            true.animateBtn(binding.addExpenseBtn)
        }
    }

    private fun showDatePickerDialog() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _, selectedYear, selectedMonth, selectedDay ->
                val selectedDate = "$selectedDay/${selectedMonth + 1}/$selectedYear"
                binding.filterByDateText.text = selectedDate
                filterExpensesByDate(selectedDate)
            },
            year,
            month,
            day
        )
        datePickerDialog.show()
    }

    private fun filterExpensesByDate(selectedData: String) {
        val filteredExpenses = ExpenseRepository.expenseList.filter { expense ->
            expense.date == selectedData
        }
        adapter.updateExpenses(filteredExpenses)
    }

    private fun setupBottomNavigationListener() {
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_expenses -> {
                    val navOptions = NavOptions.Builder()
                        .setPopEnterAnim(R.anim.slide_in_right)
                        .setPopExitAnim(R.anim.slide_out_left)
                        .build()
                    findNavController().navigate(R.id.listOfExpensesFragment, null, navOptions)
                    true
                }

                R.id.nav_conversion -> {
                    val navOptions = NavOptions.Builder()
                        .setPopEnterAnim(R.anim.slide_in_right)
                        .setPopExitAnim(R.anim.slide_out_left)
                        .build()
                    findNavController().navigate(R.id.conversionFragment, null, navOptions)
                    true
                }

                R.id.nav_settings -> {
                    val navOptions = NavOptions.Builder()
                        .setPopEnterAnim(R.anim.slide_in_left)
                        .setPopExitAnim(R.anim.slide_out_right)
                        .build()
                    findNavController().navigate(R.id.settingsFragment, null, navOptions)
                    true
                }

                else -> false
            }
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

    private fun setupSwipeToDeleteAndEdit(recyclerView: RecyclerView, adapter: ExpenseAdapter) {
        val itemTouchHelperCallback = object :
            ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT) {

            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                when (direction) {
                    ItemTouchHelper.LEFT -> {
                        adapter.deleteItem(position)
                        Toast.makeText(requireContext(), "Expense deleted", Toast.LENGTH_SHORT)
                            .show()
                    }

                    ItemTouchHelper.RIGHT -> {
                        val expense = ExpenseRepository.expenseList[position]
                        showEditExpenseDialog(expense, position)
                        adapter.notifyItemChanged(position)
                    }
                }
            }

            override fun onChildDraw(
                c: Canvas,
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                dX: Float,
                dY: Float,
                actionState: Int,
                isCurrentlyActive: Boolean
            ) {
                if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {
                    val itemView = viewHolder.itemView
                    val direction = if (dX > 0) ItemTouchHelper.RIGHT else ItemTouchHelper.LEFT

                    drawSwipeBackground(c, itemView, direction)

                    drawSwipeIcons(c, itemView, direction)
                }
                super.onChildDraw(
                    c,
                    recyclerView,
                    viewHolder,
                    dX,
                    dY,
                    actionState,
                    isCurrentlyActive
                )
            }
        }

        val itemTouchHelper = ItemTouchHelper(itemTouchHelperCallback)
        itemTouchHelper.attachToRecyclerView(recyclerView)
    }

    @SuppressLint("SetTextI18n")
    private fun showEditExpenseDialog(expense: Expense, position: Int) {
        val dialogView =
            LayoutInflater.from(requireContext()).inflate(R.layout.dialog_edit_expense, null)

        val categorySpinner = dialogView.findViewById<Spinner>(R.id.category_spinner)
        val dateTextView = dialogView.findViewById<TextView>(R.id.date_text_view)
        val amountEditText = dialogView.findViewById<EditText>(R.id.amount_edit_text)
        val currencySpinner = dialogView.findViewById<Spinner>(R.id.currency_spinner)

        amountEditText.setText(expense.amount)
        dateTextView.text = expense.date

        val categoryAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            listOf("Food", "Transport", "Entertainment", "Other")
        )
        categorySpinner.adapter = categoryAdapter
        categorySpinner.setSelection(categoryAdapter.getPosition(expense.category))

        val currencyAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            listOf("USD", "EUR", "RUB", "KGS")
        )
        currencySpinner.adapter = currencyAdapter
        currencySpinner.setSelection(currencyAdapter.getPosition(expense.currency))

        dateTextView.setOnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            DatePickerDialog(
                requireContext(),
                { _, selectedYear, selectedMonth, selectedDay ->
                    dateTextView.text = "$selectedDay/${selectedMonth + 1}/$selectedYear"
                },
                year,
                month,
                day
            ).show()
        }

        val dialog = AlertDialog.Builder(requireContext())
            .setTitle("Edit Expense")
            .setView(dialogView)
            .setPositiveButton("Save") { _, _ ->
                val updatedExpense = Expense(
                    id = expense.id,
                    category = categorySpinner.selectedItem.toString(),
                    date = dateTextView.text.toString(),
                    amount = amountEditText.text.toString(),
                    currency = currencySpinner.selectedItem.toString()
                )

                updateExpense(position, updatedExpense)
            }
            .setNegativeButton("Cancel", null)
            .create()

        dialog.show()
    }

    private fun updateExpense(position: Int, updatedExpense: Expense) {
        ExpenseRepository.expenseList[position] = updatedExpense
        adapter.updateItem(updatedExpense)
        Toast.makeText(requireContext(), "Expense Updated!", Toast.LENGTH_SHORT).show()
    }

    private fun drawSwipeIcons(
        c: Canvas,
        itemView: View,
        direction: Int
    ) {
        val editIcon =
            ContextCompat.getDrawable(itemView.context, R.drawable.ic_edit)
        val deleteIcon = ContextCompat.getDrawable(
            itemView.context,
            R.drawable.ic_delete
        )

        if (editIcon != null && direction == ItemTouchHelper.RIGHT) {
            val iconMargin = (itemView.height - editIcon.intrinsicHeight) / 2
            val iconLeft = itemView.left + iconMargin
            val iconRight = iconLeft + editIcon.intrinsicWidth
            val iconTop = itemView.top + iconMargin
            val iconBottom = iconTop + editIcon.intrinsicHeight
            editIcon.setBounds(iconLeft, iconTop, iconRight, iconBottom)
            editIcon.draw(c)
        }

        if (deleteIcon != null && direction == ItemTouchHelper.LEFT) {
            val iconMargin = (itemView.height - deleteIcon.intrinsicHeight) / 2
            val iconLeft = itemView.right - iconMargin - deleteIcon.intrinsicWidth
            val iconRight = itemView.right - iconMargin
            val iconTop = itemView.top + iconMargin
            val iconBottom = iconTop + deleteIcon.intrinsicHeight
            deleteIcon.setBounds(iconLeft, iconTop, iconRight, iconBottom)
            deleteIcon.draw(c)
        }
    }

    private fun drawSwipeBackground(
        c: Canvas,
        itemView: View,
        direction: Int
    ) {
        val backgroundColor = if (direction == ItemTouchHelper.RIGHT) Color.GREEN else Color.RED
        val paint = Paint().apply {
            color = backgroundColor
        }

        val backgroundRect = if (direction == ItemTouchHelper.RIGHT) {
            Rect(itemView.left, itemView.top, itemView.right, itemView.bottom)
        } else {
            Rect(itemView.right, itemView.top, itemView.left, itemView.bottom)
        }

        c.drawRect(backgroundRect, paint)
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}