package com.york.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "captured",
    primaryKeys = ["pokemonName", "capturedTime"],
    foreignKeys = [ForeignKey(
        entity = Pokemon::class,
        parentColumns = ["pokemonName"],
        childColumns = ["pokemonName"]
    )],
    indices = [Index(value = ["pokemonName"])]
)
data class Captured(
    val pokemonName: String,
    val capturedTime: Long
)