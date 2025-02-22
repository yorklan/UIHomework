package com.york.data

import com.york.data.local.PokemonRoomDataBase
import com.york.data.remote.PokemonApi
import com.york.data.remote.RetrofitManager
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val dataModule
    get() = module {
        single { RetrofitManager.retrofit.create(PokemonApi::class.java) }
        single {
            PokemonRepository(
                get(),
                PokemonRoomDataBase.getDataBase(androidContext()).pokemonDao()
            )
        }
    }