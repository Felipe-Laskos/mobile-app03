package com.example.app03

import com.google.gson.annotations.SerializedName

// Modelo de um personagem da Hogwarts API.
// Espécie e casa podem vir vazias (null) da API, por isso usam String?
data class Personagem(
    @SerializedName("name")
    val nome: String,
    @SerializedName("species")
    val especie: String?,
    @SerializedName("house")
    val casa: String?
)
