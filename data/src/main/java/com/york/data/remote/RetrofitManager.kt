package com.york.data.remote

import com.york.data.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

internal object RetrofitManager {

    private const val POKEMON_URL = "https://pokeapi.co/api/v2/"

    val retrofit: Retrofit
        get() = Retrofit.Builder()
            .baseUrl(POKEMON_URL)
            .client(
                OkHttpClient.Builder().let {
                    if(BuildConfig.DEBUG) {
                        it.addInterceptor(
                            HttpLoggingInterceptor().setLevel(
                                HttpLoggingInterceptor.Level.BODY
                            )
                        )
                    }
                    it
                }.build()
            )
            .addConverterFactory(GsonConverterFactory.create())
            .build()
}