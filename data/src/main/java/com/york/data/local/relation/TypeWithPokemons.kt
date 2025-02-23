package com.york.data.local.relation

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import com.york.data.local.entity.Pokemon
import com.york.data.local.entity.PokemonType
import com.york.data.local.entity.PokemonTypeCrossRef

data class TypeWithPokemons(
    @Embedded val type: PokemonType,
    @Relation(
        parentColumn = "typeName",
        entityColumn = "pokemonName",
        associateBy = Junction(PokemonTypeCrossRef::class)
    )
    val pokemons: List<Pokemon>
)