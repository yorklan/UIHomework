package com.york.data.remote.model

import com.google.gson.annotations.SerializedName

internal data class SpeciesAndDescriptionResponse(
    @SerializedName("evolves_from_species")
    val evolvesFromSpecies: EvolvesFromSpecies,

    @SerializedName("flavor_text_entries")
    val flavorTextEntries: List<FlavorTextEntries>
) {
    data class EvolvesFromSpecies(
        @SerializedName("name")
        val name: String
    )

    data class FlavorTextEntries(
        @SerializedName("flavor_text")
        val flavorText: String,

        @SerializedName("language")
        val language: Language
    ) {
        data class Language(
            @SerializedName("name")
            val name: String
        )
    }
}
