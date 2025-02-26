package com.york.data

import com.york.data.local.PokemonDao
import com.york.data.local.entity.Pokemon
import com.york.data.local.entity.PokemonType
import com.york.data.local.entity.PokemonTypeCrossRef
import com.york.data.remote.PokemonApi
import com.york.data.remote.model.ImageAndTypeResponse
import com.york.data.remote.model.PokemonResponse
import io.mockk.MockKAnnotations
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.net.SocketTimeoutException


internal class PokemonRepositoryTest {

    @MockK
    lateinit var pokemonApi: PokemonApi

    @MockK
    lateinit var pokemonDao: PokemonDao

    private lateinit var pokemonRepository: PokemonRepository

    // Mock Data
    private val mockPokemon1Name = "pikachu"
    private val mockPokemon1Id = 25
    private val mockPokemonResponse = PokemonResponse(
        listOf(PokemonResponse.Pokemon(mockPokemon1Name, "https://pokeapi.co/api/v2/pokemon/$mockPokemon1Id/"))
    )
    private val mockPokemon1Img = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/25.png"
    private val mockPokemon1Type = "electric"

    private val mockPokemon2Name = "ivysaur"
    private val mockPokemon2Id = 2
    private val mockPokemon2Img = "https://raw.githubusercontent.com/PokeAPI/sprites/master/sprites/pokemon/other/official-artwork/2.png"
    private val mockPokemon2TypeA = "grass"
    private val mockPokemon2TypeB = "poison"

    // Target Data
    private val targetPokemon1 = getMockPokemon(mockPokemon1Name, mockPokemon1Id)
    private val targetPokemon2 = getMockPokemon(mockPokemon2Name, mockPokemon2Id)

    @Before
    fun init() {
        MockKAnnotations.init(this, relaxUnitFun = true, relaxed = true)
        pokemonRepository = PokemonRepository(pokemonApi, pokemonDao)
        coEvery { pokemonDao.queryPokemonWithTypesList() } returns emptyList()
    }

    @Test
    fun getTypeWithPokemons_bypassToDB() {
        // WHEN
        pokemonRepository.typeWithPokemons
        // THEN
        coVerify(exactly = 1) { pokemonDao.queryTypeWithPokemons() }
    }

    @Test
    fun syncData_validResponse_savePokemonToDB(): Unit = runBlocking {
        // GIVEN
        coEvery { pokemonApi.getPokemon(any()) } returns mockPokemonResponse
        // WHEN
        val result = pokemonRepository.syncData()
        // THEN
        coVerify(exactly = 1) { pokemonDao.insertPokemonList(listOf(targetPokemon1)) }
        assert(result.isSuccess)
    }

    @Test
    fun syncData_invalidUrlResponse_saveUnknownPokemonIdToDB(): Unit = runBlocking {
        // GIVEN
        coEvery { pokemonApi.getPokemon(any()) } returns mockPokemonResponse.copy(
            listOf(PokemonResponse.Pokemon(mockPokemon1Name, "invalidUrl"))
        )
        // WHEN
        val result = pokemonRepository.syncData()
        // THEN
        val target = targetPokemon1.copy(
            pokemonId = Pokemon.UNKNOWN_POKEMON_ID
        )
        coVerify(exactly = 1) { pokemonDao.insertPokemonList(listOf(target)) }
        assert(result.isSuccess)
    }

    @Test
    fun syncData_networkException_resultError(): Unit = runBlocking {
        // GIVEN
        coEvery { pokemonApi.getPokemon(any()) } throws SocketTimeoutException()
        // WHEN
        val result = pokemonRepository.syncData()
        // ThEN
        coVerify(exactly = 0) { pokemonDao.insertPokemonList(any()) }
        assert(result.isFailure)
    }

    @Test
    fun syncPokemonImgAndTypes_onePokemon_callApiAndSaveToDb(): Unit = runBlocking {
        // GIVEN
        val mockImgAndTypeResponse = getMockImgAndTypeResponse(mockPokemon1Img, listOf(mockPokemon1Type))
        coEvery { pokemonApi.getImageAndType(mockPokemon1Name) } returns mockImgAndTypeResponse
        // WHEN
        pokemonRepository.syncPokemonImgAndTypes(listOf(targetPokemon1))
        // THEN
        val targetWithImg = targetPokemon1.copy(
            image = mockPokemon1Img
        )
        val targetTypeList = listOf(PokemonType(mockPokemon1Type))
        val targetCrossRefList = listOf(PokemonTypeCrossRef(mockPokemon1Name, mockPokemon1Type))
        coVerify(exactly = 1) { pokemonDao.updatePokemon(targetWithImg) }
        coVerify(exactly = 1) { pokemonDao.insertPokemonTypeList(targetTypeList) }
        coVerify(exactly = 1) { pokemonDao.insertPokemonTypeCrossRefList(targetCrossRefList)}
    }

    @Test
    fun syncPokemonImgAndTypes_twoPokemons_callApiAndSaveToDb(): Unit = runBlocking {
        // GIVEN
        val mockResponse1 = getMockImgAndTypeResponse(mockPokemon1Img, listOf(mockPokemon1Type))
        coEvery { pokemonApi.getImageAndType(mockPokemon1Name) } returns mockResponse1
        val mockResponse2 = getMockImgAndTypeResponse(mockPokemon2Img, listOf(mockPokemon2TypeA, mockPokemon2TypeB))
        coEvery { pokemonApi.getImageAndType(mockPokemon2Name) } returns mockResponse2
        // WHEN
        pokemonRepository.syncPokemonImgAndTypes(listOf(targetPokemon1, targetPokemon2))
        // THEN
        coVerify(exactly = 2) { pokemonDao.updatePokemon(any()) }
        coVerify(exactly = 2) { pokemonDao.insertPokemonTypeList(any()) }
        coVerify(exactly = 2) { pokemonDao.insertPokemonTypeCrossRefList(any())}
        val target2TypeList = listOf(PokemonType(mockPokemon2TypeA), PokemonType(mockPokemon2TypeB))
        val target2CrossRefList = listOf(
            PokemonTypeCrossRef(mockPokemon2Name, mockPokemon2TypeA),
            PokemonTypeCrossRef(mockPokemon2Name, mockPokemon2TypeB)
        )
        coVerify(exactly = 1) { pokemonDao.insertPokemonTypeList(target2TypeList) }
        coVerify(exactly = 1) { pokemonDao.insertPokemonTypeCrossRefList(target2CrossRefList)}
    }

    @Test
    fun syncPokemonImgAndTypes_zeroPokemon_noAction(): Unit = runBlocking  {
        // WHEN
        pokemonRepository.syncPokemonImgAndTypes(listOf())
        // THEN
        coVerify(exactly = 0) { pokemonDao.updatePokemon(any()) }
        coVerify(exactly = 0) { pokemonDao.insertPokemonTypeList(any()) }
        coVerify(exactly = 0) { pokemonDao.insertPokemonTypeCrossRefList(any())}
    }

    @Test
    fun syncPokemonImgAndTypes_1NetworkException2Success_get1ExceptionAndSaveToDbTwice(): Unit = runBlocking  {
        // GIVEN
        val mockPokemon3Name = "venusaur"
        coEvery { pokemonApi.getImageAndType(mockPokemon3Name) } throws SocketTimeoutException("invalid message")
        val mockResponse1 = getMockImgAndTypeResponse(mockPokemon1Img, listOf(mockPokemon1Type))
        coEvery { pokemonApi.getImageAndType(mockPokemon1Name) } returns mockResponse1
        val mockResponse2 = getMockImgAndTypeResponse(mockPokemon2Img, listOf(mockPokemon2TypeA, mockPokemon2TypeB))
        coEvery { pokemonApi.getImageAndType(mockPokemon2Name) } returns mockResponse2
        // WHEN
        val targetPokemon3 = getMockPokemon(mockPokemon3Name, 3)
        try {
            pokemonRepository.syncPokemonImgAndTypes(listOf(targetPokemon3, targetPokemon1, targetPokemon2))
        } catch (e: Exception) {
            assertEquals(e.message, "invalid message")
        }
        // THEN
        coVerify(exactly = 2) { pokemonDao.updatePokemon(any()) }
        coVerify(exactly = 2) { pokemonDao.insertPokemonTypeList(any()) }
        coVerify(exactly = 2) { pokemonDao.insertPokemonTypeCrossRefList(any())}
    }

    private fun getMockImgAndTypeResponse(
        img: String,
        typeList: List<String>
    ): ImageAndTypeResponse {
        return ImageAndTypeResponse(
            sprites = ImageAndTypeResponse.Sprite(
                other = ImageAndTypeResponse.Sprite.Other(
                    officialArtwork = ImageAndTypeResponse.Sprite.Other.OfficialArtwork (
                        frontDefault = img
                    )
                )
            ),
            types = typeList.map {
                ImageAndTypeResponse.Type(
                    type = ImageAndTypeResponse.Type.TypeItem(it)
                )
            }
        )
    }

    private fun getMockPokemon(
        name: String,
        id: Int,
        image: String? = null
    ): Pokemon {
        return Pokemon(
            pokemonName = name,
            pokemonId = id,
            image = image,
            description = null,
            evolvesFrom = null
        )
    }
}