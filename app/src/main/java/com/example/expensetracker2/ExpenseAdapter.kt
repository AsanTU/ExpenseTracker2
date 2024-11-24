package com.example.expensetracker2

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.expensetracker2.databinding.ItemExpenseBinding
import kotlin.math.exp

class ExpenseAdapter(private var expenses: MutableList<Expense>):RecyclerView.Adapter<ExpenseAdapter.ExpenseViewHolder>() {

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
    }

    class ExpenseViewHolder(private val binding: ItemExpenseBinding): RecyclerView.ViewHolder(binding.root){
        @SuppressLint("SetTextI18n")
        fun bind(expense: Expense){
            binding.apply {
                expenseTitleTv.text = expense.category
                expenseDateTv.text = expense.date
                expenseAmountTv.text = "${expense.amount} ${expense.currency}"
            }

            when (expense.category) {
                "Food" -> binding.categoryIconIv.setImageResource(R.drawable.ic_food)
                "Transport" -> binding.categoryIconIv.setImageResource(R.drawable.ic_transport)
                "Entertainment" -> binding.categoryIconIv.setImageResource(R.drawable.ic_entertainment)
                else -> binding.categoryIconIv.setImageResource(R.drawable.ic_category)
            }

            binding.expenseAmountTv.setTextColor(
                if (expense.amount >= 0.toString()) binding.root.context.getColor(R.color.green)
                else binding.root.context.getColor(R.color.error_red)
            )
        }
    }
}