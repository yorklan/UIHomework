package com.york.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.york.data.local.entity.Captured
import com.york.data.local.entity.Pokemon
import com.york.data.local.entity.PokemonType
import com.york.data.local.entity.PokemonTypeCrossRef

@Database(
    entities = [Pokemon::class, PokemonType::class, PokemonTypeCrossRef::class, Captured::class],
    version = 1,
    exportSchema = true
)
internal abstract class PokemonRoomDataBase : RoomDatabase() {

    abstract fun pokemonDao(): PokemonDao

    companion object {
        @Volatile
        private var INSTANCE: PokemonRoomDataBase? = null
        fun getDataBase(content: Context): PokemonRoomDataBase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    content.applicationContext,
                    PokemonRoomDataBase::class.java,
                    "ui-homework-pokemon"
                )
                    .build()
                INSTANCE = instance
                return instance
            }
        }
    }
}