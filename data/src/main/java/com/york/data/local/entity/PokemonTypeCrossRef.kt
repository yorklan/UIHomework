package com.york.data.local.entity

import androidx.room.Entity

@Entity(
    tableName = "pokemon_type_cross_ref",
    primaryKeys = ["pokemonName", "typeName"],
)
data class PokemonTypeCrossRef(
    val pokemonName: String,
    val typeName: String
)