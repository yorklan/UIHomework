package com.york.data.local.relation

import androidx.room.Embedded
import androidx.room.Relation
import com.york.data.local.entity.Captured
import com.york.data.local.entity.Pokemon

data class CapturedWithPokemon (
    @Embedded val captured: Captured,
    @Relation(
        parentColumn = "pokemonName",
        entityColumn = "pokemonName"
    )
    val pokemon: Pokemon
)