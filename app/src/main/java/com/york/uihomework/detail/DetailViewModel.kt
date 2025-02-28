package com.york.uihomework.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.york.data.PokemonRepository
import com.york.data.local.entity.Pokemon
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class DetailViewModel(
    private val pokemonRepository: PokemonRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _inputPokemon: Pokemon =
        savedStateHandle.get<Pokemon>(DetailActivity.INPUT_EXTRA_POKEMON)!!
    val pokemonDetail: StateFlow<Pokemon> = pokemonRepository.getPokemon(_inputPokemon.pokemonName)
        .distinctUntilChanged()
        .map { it ?: _inputPokemon }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), _inputPokemon)

    private val _evolvesFrom = MutableStateFlow<Pokemon?>(null)
    val evolvesFrom: StateFlow<Pokemon?> = _evolvesFrom.asStateFlow()

    init {
        viewModelScope.launch {
            pokemonDetail.collectLatest { detail ->
                if (detail.description == null) {
                    pokemonRepository.syncDetail(detail)
                        .onSuccess {
                            println("success")
                        }.onFailure {
                            println("failure:$it")
                        }
                }
                val evolvesFromPokemon = detail.evolvesFrom
                if (!evolvesFromPokemon.isNullOrBlank() && evolvesFrom.value == null) {
                    pokemonRepository.getPokemon(evolvesFromPokemon)
                        .catch {}
                        .collectLatest {
                            if (it == null) {
                                syncPokemonImgAndTypes(pokemonName = evolvesFromPokemon)
                            } else {
                                _evolvesFrom.value = it
                            }
                        }
                }
            }
        }
    }

    private fun syncPokemonImgAndTypes(pokemonName: String) {
        viewModelScope.launch {
            pokemonRepository.syncPokemonImgAndTypes(
                listOf(
                    Pokemon(
                        pokemonName = pokemonName,
                        pokemonId = Pokemon.UNKNOWN_POKEMON_ID,
                        image = null,
                        description = null,
                        evolvesFrom = null
                    )
                )
            )
        }
    }

    suspend fun getPokemonTypes(): List<String> {
        return pokemonRepository.getPokemonTypes(_inputPokemon.pokemonName).map { it.typeName }
    }
}