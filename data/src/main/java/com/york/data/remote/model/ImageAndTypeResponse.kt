package com.york.data.remote.model

import com.google.gson.annotations.SerializedName

internal data class ImageAndTypeResponse(
    @SerializedName("sprites")
    val sprites: Sprite,

    @SerializedName("species")
    val species: Species,

    @SerializedName("types")
    val types: List<Type>
) {
    data class Sprite(
        @SerializedName("other")
        val other: Other
    ) {
        data class Other(
            @SerializedName("official-artwork")
            val officialArtwork: OfficialArtwork
        ) {
            data class OfficialArtwork(
                @SerializedName("front_default")
                val frontDefault: String
            )
        }
    }

    data class Species(
        @SerializedName("name")
        val name: String,

        @SerializedName("url")
        val url: String
    )

    data class Type(
        @SerializedName("type")
        val type: TypeItem
    ) {
        data class TypeItem(
            @SerializedName("name")
            val name: String
        )
    }
}
