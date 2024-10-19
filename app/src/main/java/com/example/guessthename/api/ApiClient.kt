package com.example.guessthename.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET

object ApiClient {
    private const val BASE_URL = "https://random-word-api.herokuapp.com/" // Replace with your actual API base URL

    // Retrofit instance
    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    // API interface
    private val apiService: ApiService by lazy {
        retrofit.create(ApiService::class.java)
    }

    // Function to get a random word from the API
    suspend fun getRandomWord(): String? {
        return try {
            val words: List<String> = apiService.getRandomWord()
            words.firstOrNull() // Return the first word from the list
        } catch (e: Exception) {
            e.printStackTrace()
            null // Return null in case of exception
        }
    }
    // Retrofit API interface
    interface ApiService {
        @GET("word") // Adjust the endpoint according to your API
        suspend fun getRandomWord(): List<String> // Expecting a list of strings as the response
    }
}