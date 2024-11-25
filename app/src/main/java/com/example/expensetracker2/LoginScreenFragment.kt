package com.example.expensetracker2

import android.adservices.measurement.WebSourceRegistrationRequest
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.navigation.fragment.findNavController
import androidx.navigation.navOptions
import retrofit2.converter.gson.GsonConverterFactory
import com.example.expensetracker2.databinding.FragmentListOfExpensesBinding
import com.example.expensetracker2.databinding.FragmentLoginScreenBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import android.util.Patterns

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
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true){
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
            }
        }

        binding.registerBtn.setOnClickListener {
            val email = binding.emailEt.text.toString().trim()
            val password = binding.passwordEt.text.toString().trim()
            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(requireContext(), "Please fill in all fields", Toast.LENGTH_SHORT).show()
                animateBtn(binding.registerBtn, false)
            } else {
                registerUser(email, password)
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
                    Log.e("LoginError", "HTTP ${response.code()}: ${response.errorBody()?.string()}")
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

    private fun registerUser(email: String, password: String) {
        val registerRequest = RegisterRequest(email = email, password = password)

        Api.api.register(registerRequest).enqueue(object : Callback<RegisterResponse> {
            override fun onResponse(call: Call<RegisterResponse>, response: Response<RegisterResponse>) {
                if (response.isSuccessful) {
                    val registerResponse = response.body()
                    if (registerResponse?.success == true) {
                        Toast.makeText(requireContext(), "Registered successfully!", Toast.LENGTH_SHORT).show()
//                        navigateToExpenseList()
                    } else {
                        Toast.makeText(requireContext(), registerResponse?.message ?: "Registration failed", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(requireContext(), "Response failed: ${response.errorBody()?.string()}", Toast.LENGTH_LONG).show()
                }
                animateBtn(binding.registerBtn, response.isSuccessful)
            }

            override fun onFailure(call: Call<RegisterResponse>, t: Throwable) {
                Toast.makeText(requireContext(), "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                animateBtn(binding.registerBtn, false)
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