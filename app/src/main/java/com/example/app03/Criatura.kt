package com.example.app03

import com.google.gson.annotations.SerializedName

// Modelo de uma criatura da Hogwarts API.
// Algumas criaturas não têm imagem (null), por isso a URL usa String?
data class Criatura(
    @SerializedName("name")
    val nome: String,
    @SerializedName("image")
    val imagem: String?
)
