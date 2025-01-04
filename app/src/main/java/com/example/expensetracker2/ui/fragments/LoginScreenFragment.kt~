package com.example.expensetracker2.ui.fragments

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.navOptions
import com.example.expensetracker2.utils.ApiServiceHelper
import com.example.expensetracker2.R
import com.example.expensetracker2.utils.RetrofitClient
import com.example.expensetracker2.databinding.FragmentLoginScreenBinding
import com.example.expensetracker2.models.LoginRequest
import com.example.expensetracker2.models.LoginResponse
import com.example.expensetracker2.utils.SharedPreferencesManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginScreenFragment : Fragment() {

    private var _binding: FragmentLoginScreenBinding? = null
    private val binding get() = _binding!!
    private var isLoginInProgress = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginScreenBinding.inflate(inflater, container, false)

        // Clear SharedPreferences on logout
        SharedPreferencesManager.clearSessionData()

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
    private fun animateBtn(button: View) {
        val scaleX = 0.9f
        val scaleY = 0.9f

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
            if (isLoginInProgress) return@setOnClickListener // Ignore subsequent clicks if a login is in progress
            animateBtn(binding.loginBtn)

            val email = binding.emailEt.text.toString().trim()
            val password = binding.passwordEt.text.toString().trim()
            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(requireContext().applicationContext, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            } else if (!isValidEmail(email)) {
                Toast.makeText(requireContext().applicationContext, "Please enter a valid email address", Toast.LENGTH_SHORT).show()
            } else {
                isLoginInProgress = true
                loginUser(email, password)
            }
        }
    }

    private fun loginUser(email: String, password: String) {
        val loginRequest = LoginRequest(email = email, password = password)

        RetrofitClient.authService.login(loginRequest).enqueue(object : Callback<LoginResponse> {
            override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                val loginResponse = response.body()
                Log.d("LoginResponse: ", "$loginResponse")
                if (response.isSuccessful) {
                    if (loginResponse?.success == true) {
                        // Store tokens in SharedPreferences
                        loginResponse.accessToken?.let { SharedPreferencesManager.storeAccessToken(it) }
                        loginResponse.refreshToken?.let { SharedPreferencesManager.storeRefreshToken(it) }
                        loginResponse.accessTokenExpiresAt?.let { SharedPreferencesManager.storeAccessTokenExpiresAt(it) }
                        loginResponse.refreshTokenExpiresAt?.let { SharedPreferencesManager.storeRefreshTokenExpiresAt(it) }

                        Toast.makeText(requireContext().applicationContext, "Logged in successfully!", Toast.LENGTH_SHORT).show() // TODO: Make Toasts independent of fragments
                        navigateToExpenseList()
                    } else {
                        Toast.makeText(requireContext().applicationContext, loginResponse?.message ?: "Login failed", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    val errorMessage = ApiServiceHelper.getErrorMessage(response, "Failed to log in.")
                    Toast.makeText(requireContext().applicationContext, errorMessage, Toast.LENGTH_LONG).show()
                }
                isLoginInProgress = false // Reset flag here
            }

            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                Toast.makeText(requireContext().applicationContext, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                isLoginInProgress = false // Reset flag here too
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