package com.example.expensetracker2.utils

import android.util.Log
import com.example.expensetracker2.Secrets
import com.example.expensetracker2.models.AddResponse
import com.example.expensetracker2.models.CategoriesGetResponse
import com.example.expensetracker2.models.CategoryAddRequest
import com.example.expensetracker2.models.Expense
import com.example.expensetracker2.models.ExpenseAddRequest
import com.example.expensetracker2.models.ExpenseCategory
import com.example.expensetracker2.models.LoginRequest
import com.example.expensetracker2.models.LoginResponse
import com.example.expensetracker2.models.RegisterRequest
import com.example.expensetracker2.models.SuccessMessageResponse
import com.example.expensetracker2.models.TokenRefreshRequest
import com.example.expensetracker2.models.TokenRefreshResponse
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import retrofit2.Call
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import java.io.IOException
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

interface AuthService {
    @POST("login")
    fun login(@Body loginRequest: LoginRequest): Call<LoginResponse>

    @POST("register")
    fun register(@Body registerRequest: RegisterRequest): Call<SuccessMessageResponse>

    // Endpoint to refresh the access token
    @POST("refresh")
    fun refreshToken(@Body tokenRefreshRequest: TokenRefreshRequest): Call<TokenRefreshResponse>
}

interface ExpenseService {
    @GET("")
    fun getExpenses(): Call<List<Expense>>

    @POST("add")
    fun addExpense(@Body expense: ExpenseAddRequest): Call<AddResponse>

    @POST("edit/{id}")
    fun editExpense(@Path("id") id: String, @Body expense: Expense): Call<Expense>

    @POST("delete/{id}")
    fun deleteExpense(@Path("id") id: String): Call<Void>
}

interface CategoryService {
    @GET(".")
    fun getCategories(): Call<CategoriesGetResponse>

    @POST("add")
    fun addCategory(@Body category: CategoryAddRequest): Call<AddResponse>

    @POST("edit/{id}")
    fun editCategory(@Path("id") id: String, @Body expense: ExpenseCategory): Call<ExpenseCategory>

    @POST("delete/{id}")
    fun deleteCategory(@Path("id") id: String): Call<Void>
}

object RetrofitClient {
    private const val BASE_URL = Secrets.BASE_URL // Use your server's IP address
//    private const val BASE_URL = Secrets.BASE_URL_DEV

    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder().addInterceptor(AuthInterceptor()).build()
    }

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder().baseUrl("$BASE_URL/").client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create()).build()
    }

    val authService: AuthService by lazy {
        retrofit.newBuilder().baseUrl("$BASE_URL/auth/").build().create(AuthService::class.java)
    }

    val expenseService: ExpenseService by lazy {
        retrofit.newBuilder().baseUrl("$BASE_URL/expenses/").build()
            .create(ExpenseService::class.java)
    }

    val categoryService: CategoryService by lazy {
        retrofit.newBuilder().baseUrl("$BASE_URL/categories/").build()
            .create(CategoryService::class.java)
    }
}

object ApiServiceHelper {

    private const val TAG = "ApiServiceHelper" // Tag for logging

    /**
     * Constructs an error message based on the response and a custom message.
     *
     * @param T The type of the response.
     * @param response The Retrofit response object to analyze.
     * @param customMessage A custom message to prefix the error details.
     * @return A formatted string containing the error details.
     */
    fun <T> getErrorMessage(response: Response<T>, customMessage: String): String {
        val baseMessage = "$customMessage Status code: ${response.code()}"
        var errorBodyMessage = "No additional details"

        try {
            response.errorBody()?.string()?.let {
                errorBodyMessage = it
            }
        } catch (e: IOException) {
            errorBodyMessage = "Failed to parse error body"
        }

        val fullMessage = "$baseMessage. Details: $errorBodyMessage"

        // Log the error details
        Log.e(TAG, fullMessage)

        return fullMessage
    }
}

class AuthInterceptor : Interceptor {

    override fun intercept(chain: Interceptor.Chain): okhttp3.Response {
        var request = chain.request()

        // Check Access Token Expiration
        val accessTokenExpiresAt = SharedPreferencesManager.getAccessTokenExpiresAt()
        val refreshTokenExpiresAt = SharedPreferencesManager.getRefreshTokenExpiresAt()

        // Log when checking the expiration
        Log.d("AuthInterceptor", "Checking token expiration.")

        if (shouldRefreshAuthToken(accessTokenExpiresAt, refreshTokenExpiresAt)) {
            Log.d("AuthInterceptor", "Refreshing token...")
            // Attempt to refresh the access token
            val newAccessToken = refreshToken()
            if (newAccessToken != null) {
                SharedPreferencesManager.storeAccessToken(newAccessToken)
                request = addAuthorizationHeader(request, newAccessToken)
            } else {
                Log.e("AuthInterceptor", "Token refresh failed. Handling session expiry.")
                handleFailedRefresh()  // Implement a method to handle session expiry or user logout
            }
        } else {
            val accessToken = SharedPreferencesManager.getAccessToken()
            request = addAuthorizationHeader(request, accessToken)
            Log.d("AuthInterceptor", "Using existing access token.")
        }

        return chain.proceed(request)
    }

    private fun addAuthorizationHeader(request: Request, token: String?): Request {
        return request.newBuilder()
            .header("Authorization", "Bearer $token")
            .build()
    }

    private fun refreshToken(): String? {
        val refreshToken = SharedPreferencesManager.getRefreshToken() ?: return null

        try {
            val response = RetrofitClient.authService.refreshToken(TokenRefreshRequest(refreshToken)).execute()
            if (response.isSuccessful) {
                // Store new expiration times if provided by your backend
                val responseBody = response.body()
                responseBody?.accessTokenExpiresAt?.let { SharedPreferencesManager.storeAccessTokenExpiresAt(it) }
                Log.d("AuthInterceptor", "Token refresh successful.")
                return responseBody?.accessToken
            } else {
                Log.e("AuthInterceptor", "Failed to refresh token: ${response.code()}")
                // Handle potential user re-authentication if the refresh token itself has expired
            }
        } catch (e: IOException) {
            Log.e("AuthInterceptor", "Error refreshing token", e)
        }
        return null
    }

    private fun shouldRefreshAuthToken(accessTokenExpiration: String?, refreshTokenExpiration: String?): Boolean {
        // Log the input expiration times
        Log.d("AuthInterceptor", "Checking token expiration. Access Token Expiration: $accessTokenExpiration, Refresh Token Expiration: $refreshTokenExpiration")

        if (accessTokenExpiration.isNullOrEmpty() || refreshTokenExpiration.isNullOrEmpty()) {
            Log.d("AuthInterceptor", "Expiration times are incomplete, refreshing tokens.")
            return true
        }

        val accessTokenDateTime = LocalDateTime.parse(accessTokenExpiration, DateTimeFormatter.ISO_DATE_TIME)
        val refreshTokenDateTime = LocalDateTime.parse(refreshTokenExpiration, DateTimeFormatter.ISO_DATE_TIME)
        val currentDateTime = LocalDateTime.now(ZoneOffset.UTC)

        // Log the parsed date times
        Log.d("AuthInterceptor", "Parsed DateTimes: Current: $currentDateTime, Access Token: $accessTokenDateTime, Refresh Token: $refreshTokenDateTime")

        // Determine whether the access token needs refreshing or if overall refresh should be handled
        val shouldRefresh = currentDateTime.isAfter(accessTokenDateTime.minusMinutes(5)) || currentDateTime.isAfter(refreshTokenDateTime.minusDays(1))  // Adjust buffer before refresh token expiration

        // Log the decision
        Log.d("AuthInterceptor", "Should refresh token: $shouldRefresh")

        return shouldRefresh
    }

    private fun handleFailedRefresh() {
        // Implement logic to redirect the user to a login screen or notify them about session expiry
        // Could trigger app-wide logged-out state and clear sensitive stored data

        // Clear the user's session info if necessary
        SharedPreferencesManager.clearSessionData()

        // TODO: Implement this. Seriously though, why is navigating from anywhere so hard?
        // Navigate to the login screen
//        findNavController(R.id.nav_host_fragment).navigate(R.id.loginScreenFragment)
    }
}