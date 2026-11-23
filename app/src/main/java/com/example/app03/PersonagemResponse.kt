package com.example.app03

import com.google.gson.annotations.SerializedName

// Resposta da busca de personagens: a API devolve a lista dentro do campo "data"
data class PersonagemResponse(
    @SerializedName("data")
    val personagens: List<Personagem>
)
