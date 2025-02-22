package com.york.data.remote

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

internal object RetrofitManager {

    private const val POKEMON_URL = "https://pokeapi.co/api/v2/"

    val retrofit: Retrofit
        get() = Retrofit.Builder()
            .baseUrl(POKEMON_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
}