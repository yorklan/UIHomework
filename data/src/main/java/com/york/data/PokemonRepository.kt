package com.york.data

import com.york.data.local.PokemonDao
import com.york.data.local.entity.Pokemon
import com.york.data.remote.PokemonApi
import retrofit2.HttpException
import java.io.IOException

class PokemonRepository internal constructor(
    private val pokemonApi: PokemonApi,
    private val pokemonDao: PokemonDao
) {

    suspend fun syncData(): Result<Unit> {
        return safeApiCall {
            val pokemonList = pokemonApi.getPokemon(151).results.map {
                val id = it.url.split("/").let { arr ->
                    arr[arr.size - 2]
                }.toInt()
                Pokemon(
                    pokemonName = it.name,
                    pokemonId = id,
                    image = null,
                    description = null,
                    evolvesFrom = null
                )
            }
            pokemonDao.insertPokemonList(pokemonList)
        }
    }

    private suspend fun <T> safeApiCall(apiCall: suspend () -> T): Result<T> {
        return try {
            Result.success(apiCall())
        } catch (e: HttpException) {
            // Handle HTTP errors (e.g., 400, 500)
            Result.failure(e)
        } catch (e: IOException) {
            // Handle network errors
            Result.failure(IOException("Network Error: ${e.message}"))
        } catch (e: Exception) {
            // Handle other exceptions
            Result.failure(Exception("An unexpected error occurred: ${e.message}"))
        }
    }
}