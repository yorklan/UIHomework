package com.york.data.local.relation

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import com.york.data.local.entity.Pokemon
import com.york.data.local.entity.PokemonType
import com.york.data.local.entity.PokemonTypeCrossRef

data class PokemonWithTypes(
    @Embedded val pokemon: Pokemon,
    @Relation(
        parentColumn = "pokemonName",
        entityColumn = "typeName",
        associateBy = Junction(PokemonTypeCrossRef::class)
    )
    val types: List<PokemonType>
)