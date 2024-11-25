package com.example.expensetracker2

import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface ApiService {

    @POST("auth/login")
    fun login(@Body loginRequest: LoginRequest): Call<LoginResponse>

    @POST("auth/register")
    fun register(@Body registerRequest: RegisterRequest): Call<RegisterResponse>
}

object RetrofitClient {
    private const val BASE_URL = Secrets.BASE_URL // Use your server's IP address
//    private const val BASE_URL = Secrets.BASE_URL_DEV

    val instance: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
}

object Api {
    val api: ApiService by lazy {
        RetrofitClient.instance.create(ApiService::class.java)
    }
}