package com.york.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pokemon")
data class Pokemon(
    @PrimaryKey val pokemonName: String,
    val pokemonId: Int,
    val image: String?,
    val description: String?,
    val evolvesFrom: String?
)