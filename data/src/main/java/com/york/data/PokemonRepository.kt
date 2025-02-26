package com.york.data

import androidx.room.Transaction
import com.york.data.local.PokemonDao
import com.york.data.local.entity.Captured
import com.york.data.local.entity.Pokemon
import com.york.data.local.entity.Pokemon.Companion.UNKNOWN_POKEMON_ID
import com.york.data.local.entity.PokemonType
import com.york.data.local.entity.PokemonTypeCrossRef
import com.york.data.local.relation.CapturedWithPokemon
import com.york.data.local.relation.TypeWithPokemons
import com.york.data.remote.PokemonApi
import com.york.data.remote.model.ImageAndTypeResponse
import com.york.data.remote.model.SpeciesAndDescriptionResponse
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.supervisorScope
import retrofit2.HttpException
import java.io.IOException

class PokemonRepository internal constructor(
    private val pokemonApi: PokemonApi,
    private val pokemonDao: PokemonDao
) {

    val typeWithPokemons: Flow<List<TypeWithPokemons>> = pokemonDao.queryTypeWithPokemons()

    val capturedWithPokemon: Flow<List<CapturedWithPokemon>> = pokemonDao.queryCaptured()

    suspend fun syncData(): Result<Unit> {
        return safeApiCall {
            val pokemonList = pokemonApi.getPokemon(151).results.map {
                val id = it.url.split("/").let { arr ->
                    arr.getOrNull(arr.size - 2)
                }?.toInt() ?: UNKNOWN_POKEMON_ID
                Pokemon(
                    pokemonName = it.name,
                    pokemonId = id,
                    image = null,
                    description = null,
                    evolvesFrom = null
                )
            }
            pokemonDao.insertPokemonList(pokemonList)
            syncPokemonImgAndTypes(pokemonList)
        }
    }

    suspend fun syncPokemonImgAndTypes(newPokemonList: List<Pokemon>) = supervisorScope {
        val pokemonWithTypes = pokemonDao.queryPokemonWithTypesList()
        newPokemonList.map { newPokemon ->
            async {
                val savedPokemonWithTypes = pokemonWithTypes
                    .firstOrNull { it.pokemon.pokemonName == newPokemon.pokemonName }
                if (savedPokemonWithTypes != null &&
                    !savedPokemonWithTypes.pokemon.image.isNullOrBlank() &&
                    savedPokemonWithTypes.types.isNotEmpty()
                ) {
                    return@async
                }
                val imageAndType = pokemonApi.getImageAndType(newPokemon.pokemonName)
                updatePokemonImg(
                    newPokemon = newPokemon,
                    savedPokemon = savedPokemonWithTypes?.pokemon,
                    img = imageAndType.img
                )
                storeTypeAndCrossRef(
                    pokemonName = newPokemon.pokemonName,
                    imageAndType = imageAndType
                )
            }
        }.awaitAll()
    }

    suspend fun capture(pokemonName: String) {
        pokemonDao.insertCaptured(
            Captured(
                pokemonName = pokemonName,
                capturedTime = System.currentTimeMillis()
            )
        )
    }

    suspend fun release(captured: Captured) {
        pokemonDao.deleteCaptured(captured)
    }

    suspend fun syncDetail(pokemon: Pokemon): Result<Unit> {
        return safeApiCall {
            val specAndDesc = pokemonApi.getSpeciesAndDescription(pokemon.pokemonName)
            pokemonDao.updatePokemon(
                pokemon.copy(
                    description = specAndDesc.description,
                    evolvesFrom = specAndDesc.evolvesFrom
                )
            )
        }
    }

    suspend fun getPokemonTypes(pokemonName: String): List<PokemonType> {
        return pokemonDao.queryPokemonWithTypes(pokemonName).types
    }

    fun getPokemon(pokemonName: String): Flow<Pokemon?> {
        return pokemonDao.queryPokemon(pokemonName)
    }

    private suspend fun updatePokemonImg(
        savedPokemon: Pokemon?,
        newPokemon: Pokemon,
        img: String
    ) {
        val pokemonWithImg = newPokemon.copy(
            image = img,
            description = savedPokemon?.description,
            evolvesFrom = savedPokemon?.evolvesFrom
        )
        if (savedPokemon != newPokemon) {
            pokemonDao.updatePokemon(pokemonWithImg)
        }
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

private val SpeciesAndDescriptionResponse.evolvesFrom: String
    get() = evolvesFromSpecies.name

private val SpeciesAndDescriptionResponse.description: String
    get() = flavorTextEntries.firstOrNull { it.language.name == "en" }?.flavorText ?: ""