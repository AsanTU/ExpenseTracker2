package com.example.expensetracker2

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.expensetracker2.databinding.FragmentListOfExpensesBinding

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

        adapter = ExpenseAdapter(ExpenseRepository.expenseList)
        binding.expenseRv.layoutManager = LinearLayoutManager(requireContext())
        binding.expenseRv.adapter = adapter

        val category = arguments?.getString("category") ?: ""
        val date = arguments?.getString("date") ?: ""
        val amount = arguments?.getString("amount") ?: ""
        val currency = arguments?.getString("currency") ?: ""

        if (category.isNotEmpty() && date.isNotEmpty() && amount.isNotEmpty() && currency.isNotEmpty()) {
            addExpense(category, date, amount, currency)
        }
        binding.addExpenseBtn.setOnClickListener {
            findNavController().navigate(R.id.action_listOfExpensesFragment_to_addExpensesFragment2)
        }
        adapter.notifyDataSetChanged()
    }

    private fun addExpense(category: String, date: String, amount: String, currency: String) {
        val expense = Expense(category, date, amount, currency)
        ExpenseRepository.expenseList.add(0, expense)
        adapter.notifyItemInserted(0)
        binding.expenseRv.scrollToPosition(0)
        Toast.makeText(requireContext(), "Expense Added!", Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}