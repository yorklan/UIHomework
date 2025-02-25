package com.york.uihomework

import com.york.data.PokemonRepository
import com.york.data.local.entity.Captured
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test


class MainViewModelTest {

    @MockK
    lateinit var pokemonRepository: PokemonRepository

    private lateinit var mainViewModel: MainViewModel
    @Before
    fun init() {
        MockKAnnotations.init(this, relaxUnitFun = true, relaxed = true)
        coEvery { pokemonRepository.syncData() } returns Result.success(Unit)
        mainViewModel = MainViewModel(pokemonRepository)
    }

    @Test
    fun init_syncData_bypassToRepo() = runTest {
        // THEN
        coVerify(exactly = 1) { pokemonRepository.syncData() }
    }

    @Test
    fun getCapturedWithPokemonFlow_bypassToRepo() = runTest  {
        // WHEN
        mainViewModel.capturedWithPokemonFlow
        // THEN
        verify(exactly = 1) { pokemonRepository.capturedWithPokemon }
    }

    @Test
    fun getTypeWithPokemonsFlow_bypassToRepo() = runTest  {
        // WHEN
        mainViewModel.typeWithPokemonsFlow
        // THEN
        verify(exactly = 1) { pokemonRepository.typeWithPokemons }
    }

    @Test
    fun capturePokemon_bypassToRepo() = runTest {
        coEvery { pokemonRepository.capture(any()) } returns Unit
        // WHEN
        val pikachu = "pikachu"
        mainViewModel.capturePokemon(pikachu)
        // THEN
        coVerify(exactly = 1) { pokemonRepository.capture(any()) }
    }

    @Test
    fun releasePokemon_bypassToRepo() = runTest {
        // WHEN
        val captured = Captured( "pikachu", 1732809600000)
        mainViewModel.releasePokemon(captured)
        // THEN
        coVerify(exactly = 1) { pokemonRepository.release(captured) }
    }
}