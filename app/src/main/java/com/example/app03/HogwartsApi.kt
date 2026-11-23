package com.example.app03

import retrofit2.http.GET
import retrofit2.http.Query

// Endpoints da Hogwarts API (URL base: https://hogwarts-api.com/api/)
interface HogwartsApi {

    // Busca personagens pelo nome.
    // O @Query coloca o texto no final da URL: characters?search=weasley
    @GET("characters")
    suspend fun buscarPersonagens(@Query("search") nome: String): PersonagemResponse

    // Busca criaturas pelo nome: creatures?search=hippogriff
    @GET("creatures")
    suspend fun buscarCriaturas(@Query("search") nome: String): CriaturaResponse
}
