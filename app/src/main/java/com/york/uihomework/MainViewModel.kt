package com.york.uihomework

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.york.data.PokemonRepository
import com.york.data.local.entity.Captured
import com.york.data.local.relation.CapturedWithPokemon
import com.york.data.local.relation.TypeWithPokemons
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(
    private val pokemonRepository: PokemonRepository
) : ViewModel() {

    val capturedWithPokemonFlow: StateFlow<List<CapturedWithPokemon>>
        = pokemonRepository.capturedWithPokemon
            .distinctUntilChanged()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())


    val typeWithPokemonsFlow: StateFlow<List<TypeWithPokemons>>
        = pokemonRepository.typeWithPokemons
            .distinctUntilChanged()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        syncData()
    }

    fun capturePokemon(pokemonName: String) {
        viewModelScope.launch {
            pokemonRepository.capture(pokemonName)
        }
    }

    fun releasePokemon(captured: Captured) {
        viewModelScope.launch {
            pokemonRepository.release(captured)
        }
    }

    private fun syncData() {
        viewModelScope.launch {
            pokemonRepository.syncData().onSuccess {
                println("success")
            }.onFailure { exception ->
                println("error: ${exception.message}")
            }
        }
    }
}