package com.york.uihomework

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.york.data.PokemonRepository
import kotlinx.coroutines.launch

class MainViewModel(
    private val pokemonRepository: PokemonRepository
) : ViewModel() {

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