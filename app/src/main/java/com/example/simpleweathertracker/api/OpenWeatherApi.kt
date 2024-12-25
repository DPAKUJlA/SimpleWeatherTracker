package com.example.simpleweathertracker.api

import FiveDayWeatherResponse
import okhttp3.OkHttpClient
import okhttp3.Request
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

interface OpenWeatherApi {
    @GET("data/2.5/forecast")
    fun getFiveDayWeatherByCity(
        @Query("q") city: String,
        @Query("units") units: String = "metric",
        @Query("lang") lang: String = "ua"
    ): Call<FiveDayWeatherResponse>
}

object RetrofitClient {
    private const val BASE_URL = "https://api.openweathermap.org/"
    private const val API_KEY = "eb816001836bb1fe1c876983e2b12d32"

    private val client: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor { chain ->
                val original: Request = chain.request()
                val originalUrl = original.url

                val url = originalUrl.newBuilder()
                    .addQueryParameter("appid", API_KEY)
                    .build()

                val request = original.newBuilder()
                    .url(url)
                    .build()

                chain.proceed(request)
            }
            .build()
    }

    val api: OpenWeatherApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(OpenWeatherApi::class.java)
    }
}
