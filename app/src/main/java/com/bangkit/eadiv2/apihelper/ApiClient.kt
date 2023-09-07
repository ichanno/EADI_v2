package com.bangkit.eadiv2.apihelper

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


object ApiClient {
    private const val BASE_URL = "https://eadi-ftnj426clq-et.a.run.app/"

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    fun create(): ApiService {
        val retrofitInstance = retrofit
        return retrofitInstance.create(ApiService::class.java)
    }
}