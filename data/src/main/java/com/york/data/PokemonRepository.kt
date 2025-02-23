package com.york.data

import androidx.room.Transaction
import com.york.data.local.PokemonDao
import com.york.data.local.entity.Pokemon
import com.york.data.local.entity.PokemonType
import com.york.data.local.entity.PokemonTypeCrossRef
import com.york.data.remote.PokemonApi
import com.york.data.remote.model.ImageAndTypeResponse
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
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
            handlePokemonList(pokemonList)
        }
    }

    private suspend fun handlePokemonList(pokemonList: List<Pokemon>) = coroutineScope {
        pokemonList.map { pokemon ->
            async {
                val imageAndType = pokemonApi.getImageAndType(pokemon.pokemonName)
                updatePokemonImg(
                    originPokemon = pokemon,
                    img = imageAndType.img
                )
                storeTypeAndCrossRef(
                    pokemonName = pokemon.pokemonName,
                    imageAndType = imageAndType
                )
            }
        }.awaitAll()
    }

    private suspend fun updatePokemonImg(
        originPokemon: Pokemon,
        img: String
    ) {
        val pokemonWithImg = originPokemon.copy(image = img)
        pokemonDao.updatePokemon(pokemonWithImg)
    }

    @Transaction
    private suspend fun storeTypeAndCrossRef(
        pokemonName: String,
        imageAndType: ImageAndTypeResponse
    ) {
        val (typeList, crossRefList) = imageAndType.typeList.map { typeName ->
            PokemonType(
                typeName = typeName
            ) to PokemonTypeCrossRef(
                typeName = typeName,
                pokemonName = pokemonName
            )
        }.unzip()
        pokemonDao.insertPokemonTypeList(typeList)
        pokemonDao.insertPokemonTypeCrossRefList(crossRefList)
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

private val ImageAndTypeResponse.img: String
    get() = sprites.other.officialArtwork.frontDefault

private val ImageAndTypeResponse.typeList: List<String>
    get() = types.map { it.type.name }