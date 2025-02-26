package com.york.data.local.entity

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Entity(tableName = "pokemon")
@Parcelize
data class Pokemon(
    @PrimaryKey val pokemonName: String,
    val pokemonId: Int,
    val image: String?,
    val description: String?,
    val evolvesFrom: String?
): Parcelable {
    companion object {
        const val UNKNOWN_POKEMON_ID = -1
    }
}