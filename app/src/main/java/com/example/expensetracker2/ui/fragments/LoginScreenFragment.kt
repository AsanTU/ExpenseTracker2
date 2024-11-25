package com.example.expensetracker2.ui.fragments

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.navigation.fragment.findNavController
import androidx.navigation.navOptions
import com.example.expensetracker2.R
import com.example.expensetracker2.databinding.FragmentLoginScreenBinding

class LoginScreenFragment : Fragment() {

    private var _binding: FragmentLoginScreenBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginScreenBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    requireActivity().finish()
                }
            })
        checkEmpty()
    }

    @SuppressLint("Recycle")
    private fun animateBtn(button: View, success: Boolean) {
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

    private fun checkEmpty() {
        binding.loginBtn.setOnClickListener {
            val email = binding.emailEt.text.toString().trim()
            val password = binding.passwordEt.text.toString().trim()
            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(requireContext(), "Please fill in all fields", Toast.LENGTH_SHORT)
                    .show()
                animateBtn(binding.loginBtn, false)
            } else {
                Toast.makeText(requireContext(), "Logged in successfully!", Toast.LENGTH_SHORT)
                    .show()
                findNavController().navigate(
                    R.id.action_loginScreenFragment_to_listOfExpensesFragment2,
                    null,
                    navOptions {
                        popUpTo(R.id.loginScreenFragment) { inclusive = true }
                    }
                )
                animateBtn(binding.loginBtn, true)
            }
        }
        binding.registerBtn.setOnClickListener {
            val email = binding.emailEt.text.toString().trim()
            val password = binding.passwordEt.text.toString().trim()
            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(requireContext(), "Please fill in all fields", Toast.LENGTH_SHORT)
                    .show()
                animateBtn(binding.registerBtn, false)
            } else {
                Toast.makeText(requireContext(), "Signed Up in successfully!", Toast.LENGTH_SHORT)
                    .show()
                findNavController().navigate(
                    R.id.action_loginScreenFragment_to_listOfExpensesFragment2,
                    null,
                    navOptions {
                        popUpTo(R.id.loginScreenFragment) { inclusive = true }
                    }
                )
                animateBtn(binding.registerBtn, true)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}