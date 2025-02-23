package com.york.uihomework

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.york.data.PokemonRepository
import com.york.data.local.relation.TypeWithPokemons
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class MainViewModel(
    private val pokemonRepository: PokemonRepository
) : ViewModel() {

    val typeWithPokemonsFlow: Flow<List<TypeWithPokemons>>
        = pokemonRepository.typeWithPokemons

    init {
        syncData()
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