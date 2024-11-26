package com.example.expensetracker2.ui.fragments

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.navOptions
import com.example.expensetracker2.Api
import com.example.expensetracker2.LoginRequest
import com.example.expensetracker2.LoginResponse
import com.example.expensetracker2.R
import com.example.expensetracker2.RegisterRequest
import com.example.expensetracker2.RegisterResponse
import com.example.expensetracker2.databinding.FragmentLoginScreenBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

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
        binding.singUpTv.setOnClickListener {
            findNavController().navigate(R.id.action_loginScreenFragment_to_registerScreenFragment)
        }
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

    private fun isValidEmail(email: String): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun checkEmpty() {
        binding.loginBtn.setOnClickListener {
            val email = binding.emailEt.text.toString().trim()
            val password = binding.passwordEt.text.toString().trim()
            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(requireContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show()
                animateBtn(binding.loginBtn, false)
            } else if (!isValidEmail(email)) {
                Toast.makeText(requireContext(), "Please enter a valid email address", Toast.LENGTH_SHORT).show()
                animateBtn(binding.loginBtn, false)
            } else {
                loginUser(email, password)
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
    }

    private fun loginUser(email: String, password: String) {
        val loginRequest = LoginRequest(email = email, password = password)

        Api.api.login(loginRequest).enqueue(object : Callback<LoginResponse> {
            override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                val loginResponse = response.body()
                if (response.isSuccessful) {
                    if (loginResponse?.success == true) {
                        Toast.makeText(requireContext(), "Logged in successfully!", Toast.LENGTH_SHORT).show()
                        navigateToExpenseList()
                    } else {
                        Toast.makeText(requireContext(), loginResponse?.message ?: "Login failed", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(requireContext(), "Response failed", Toast.LENGTH_SHORT).show()
                }
                animateBtn(binding.loginBtn, response.isSuccessful)
            }

            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                Toast.makeText(requireContext(), "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                animateBtn(binding.loginBtn, false)
            }
        })
    }

    private fun navigateToExpenseList() {
        findNavController().navigate(
            R.id.action_loginScreenFragment_to_listOfExpensesFragment2,
            null,
            navOptions { popUpTo(R.id.loginScreenFragment) { inclusive = true }}
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}