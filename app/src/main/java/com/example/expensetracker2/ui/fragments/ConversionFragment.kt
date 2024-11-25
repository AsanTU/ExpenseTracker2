package com.example.expensetracker2.ui.fragments

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.expensetracker2.R
import com.example.expensetracker2.databinding.FragmentConversionBinding
import org.json.JSONObject

class ConversionFragment : Fragment() {

    private var _binding: FragmentConversionBinding? = null
    private val binding get() = _binding!!

    private val currencyList = listOf("USD", "EUR", "RUB", "KGS")


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentConversionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupBottomNavigationListener()

        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            currencyList
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.fromCurrencySpinner.adapter = adapter
        binding.toCurrencySpinner.adapter = adapter
        binding.convertBtn.setOnClickListener {
            true.animateBtn(binding.convertBtn)
            convertCurrency()

        }
    }

    @SuppressLint("SetTextI18n")
    private fun convertCurrency() {
        val amountString = binding.amountEt.text.toString()
        if (amountString.isEmpty()) {
            Toast.makeText(requireContext(), "Please enter an amount", Toast.LENGTH_SHORT).show()
            return
        }
        val amount = amountString.toDouble()
        val fromCurrency = binding.fromCurrencySpinner.selectedItem.toString()
        val toCurrency = binding.toCurrencySpinner.selectedItem.toString()
        fetchConversionRate(fromCurrency, toCurrency) { rate ->
            val result = amount * rate
            binding.conversionResultTv.text = "Result: $result $toCurrency"
        }
    }

    private fun fetchConversionRate(
        fromCurrency: String,
        toCurrency: String,
        callback: (Double) -> Unit
    ) {
        val url = "https://api.exchangerate-api.com/v4/latest/$fromCurrency"
        val request = StringRequest(
            Request.Method.GET, url,
            { response ->
                val jsonResponse = JSONObject(response)
                val rates = jsonResponse.getJSONObject("rates")
                val rate = rates.getDouble(toCurrency)
                callback(rate)
            },
            { _ ->
                Toast.makeText(
                    requireContext(),
                    "Error fetching conversion rate",
                    Toast.LENGTH_SHORT
                ).show()
            }
        )
        Volley.newRequestQueue(requireContext()).add(request)
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

}