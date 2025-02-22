package com.york.data.remote.model

import com.google.gson.annotations.SerializedName

internal data class PokemonResponse(
    @SerializedName("results")
    val results: List<Pokemon>
) {
    data class Pokemon(
        @SerializedName("name")
        val name: String,

        @SerializedName("url")
        val url: String
    )
}
