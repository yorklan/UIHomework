package com.york.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import com.york.data.local.entity.Pokemon
import com.york.data.local.entity.PokemonType
import com.york.data.local.entity.PokemonTypeCrossRef

@Dao
internal interface PokemonDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPokemonList(pokemonList: List<Pokemon>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updatePokemon(pokemon: Pokemon)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPokemonTypeList(typeList: List<PokemonType>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPokemonTypeCrossRefList(crossRefList: List<PokemonTypeCrossRef>)
}