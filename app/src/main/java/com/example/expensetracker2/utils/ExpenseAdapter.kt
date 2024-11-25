package com.example.expensetracker2.utils

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.expensetracker2.R
import com.example.expensetracker2.databinding.ItemExpenseBinding
import com.example.expensetracker2.model.Expense

class ExpenseAdapter(
    private var expenses: MutableList<Expense>,
    private val onEdit: (Expense, Int) -> Unit
) : RecyclerView.Adapter<ExpenseAdapter.ExpenseViewHolder>() {

    @SuppressLint("NotifyDataSetChanged")
    fun updateExpenses(updatedExpenses: List<Expense>) {
        expenses.clear()
        expenses.addAll(updatedExpenses)
        notifyDataSetChanged()
    }

    fun updateItem(updatedExpenses: Expense) {
        val index = expenses.indexOfFirst { it.id == updatedExpenses.id }
        if (index != -1) {
            expenses[index] = updatedExpenses
            notifyItemChanged(index)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExpenseViewHolder {
        val binding = ItemExpenseBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ExpenseViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return expenses.size
    }

    override fun onBindViewHolder(holder: ExpenseViewHolder, position: Int) {
        val expense = expenses[position]
        holder.bind(expense)
        holder.itemView.setOnClickListener {
            onEdit(expense, position)
        }
    }

    class ExpenseViewHolder(private val binding: ItemExpenseBinding) :
        RecyclerView.ViewHolder(binding.root) {
        @SuppressLint("SetTextI18n")
        fun bind(expense: Expense) {
            binding.apply {
                expenseTitleTv.text = expense.category
                expenseDateTv.text = expense.date
                expenseAmountTv.text = "${expense.amount} ${expense.currency}"
                setCategoryIcon(expense.category)
                expenseAmountTv.setTextColor(
                    if (expense.amount.toDouble() >= 0) root.context.getColor(R.color.green)
                    else root.context.getColor(R.color.error_red)
                )
            }
        }

        private fun setCategoryIcon(category: String) {
            val iconRes = when (category) {
                "Food" -> R.drawable.ic_food
                "Transport" -> R.drawable.ic_transport
                "Entertainment" -> R.drawable.ic_entertainment
                else -> R.drawable.ic_category
            }
            binding.categoryIconIv.setImageResource(iconRes)
        }
    }

    fun deleteItem(position: Int) {
        expenses.removeAt(position)
        notifyItemRemoved(position)
    }

}