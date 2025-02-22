package com.york.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pokemon_type")
data class PokemonType(
    @PrimaryKey @ColumnInfo(name = "type_name") val typeName: String,
)