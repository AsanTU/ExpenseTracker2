package com.example.expensetracker2.ui.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.example.expensetracker2.R
import com.example.expensetracker2.databinding.FragmentSettingsBinding

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupBottomNavigationListener()
        binding.logoutBtn.setOnClickListener {
            findNavController().navigate(R.id.action_settingsFragment_to_loginScreenFragment)
        }
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
}