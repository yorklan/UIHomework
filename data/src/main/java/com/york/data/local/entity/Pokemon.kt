package com.york.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pokemon")
data class Pokemon(
    @PrimaryKey @ColumnInfo(name = "pokemon_name") val pokemonName: String,
    @ColumnInfo(name = "pokemon_id") val pokemonId: Int,
    @ColumnInfo("image") val image: String?,
    @ColumnInfo(name = "description") val description: String?,
    @ColumnInfo(name = "evolves_from") val evolvesFrom: String?
)