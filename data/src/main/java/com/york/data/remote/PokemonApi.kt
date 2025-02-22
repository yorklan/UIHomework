package com.york.data.remote

import com.york.data.remote.model.ImageAndTypeResponse
import com.york.data.remote.model.PokemonResponse
import com.york.data.remote.model.SpeciesAndDescriptionResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

internal interface PokemonApi {

    @GET("pokemon")
    suspend fun getPokemon(
        @Query("limit") limit:Int
    ): PokemonResponse

    @GET("pokemon/{name}")
    suspend fun getImageAndType(
        @Path("name") name:String
    ): ImageAndTypeResponse

    @GET("pokemon-species/{name} ")
    suspend fun getSpeciesAndDescription(
        @Path("name") name:String
    ): SpeciesAndDescriptionResponse

}