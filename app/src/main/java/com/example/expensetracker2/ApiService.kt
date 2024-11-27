package com.example.expensetracker2

import com.example.expensetracker2.models.CategoryAddRequest
import com.example.expensetracker2.models.AddResponse
import com.example.expensetracker2.models.Expense
import com.example.expensetracker2.models.ExpenseAddRequest
import com.example.expensetracker2.models.ExpenseCategory
import com.example.expensetracker2.models.LoginRequest
import com.example.expensetracker2.models.LoginResponse
import com.example.expensetracker2.models.RegisterRequest
import com.example.expensetracker2.models.SuccessMessageResponse
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface AuthService {
    @POST("login")
    fun login(@Body loginRequest: LoginRequest): Call<LoginResponse>

    @POST("register")
    fun register(@Body registerRequest: RegisterRequest): Call<SuccessMessageResponse>
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
    @GET("")
    fun getCategories(): Call<List<ExpenseCategory>>

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

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val authService: AuthService by lazy {
        retrofit.newBuilder()
            .baseUrl("$BASE_URL/auth/")
            .build()
            .create(AuthService::class.java)
    }

    val expenseService: ExpenseService by lazy {
        retrofit.newBuilder()
            .baseUrl("$BASE_URL/expenses/")
            .build()
            .create(ExpenseService::class.java)
    }

    val categoryService: CategoryService by lazy {
        retrofit.newBuilder()
            .baseUrl("$BASE_URL/categories/")
            .build()
            .create(CategoryService::class.java)
    }
}