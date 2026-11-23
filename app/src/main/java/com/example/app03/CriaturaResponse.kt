package com.example.app03

import com.google.gson.annotations.SerializedName

// Resposta da busca de criaturas: a API devolve a lista dentro do campo "data"
data class CriaturaResponse(
    @SerializedName("data")
    val criaturas: List<Criatura>
)
