package com.york.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.york.data.local.entity.Pokemon
import com.york.data.local.entity.PokemonType
import com.york.data.local.entity.PokemonTypeCrossRef
import com.york.data.local.relation.PokemonWithTypes
import com.york.data.local.relation.TypeWithPokemons
import kotlinx.coroutines.flow.Flow

@Dao
internal interface PokemonDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertPokemonList(pokemonList: List<Pokemon>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updatePokemon(pokemon: Pokemon)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPokemonTypeList(typeList: List<PokemonType>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPokemonTypeCrossRefList(crossRefList: List<PokemonTypeCrossRef>)

    @Transaction
    @Query("SELECT * FROM pokemon_type")
    fun queryTypeWithPokemons(): Flow<List<TypeWithPokemons>>

    @Transaction
    @Query("SELECT * FROM pokemon WHERE pokemonName = :pokemonName")
    suspend fun queryPokemonWithTypes(pokemonName: String): PokemonWithTypes
}