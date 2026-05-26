package com.fatec.at2_base

import kotlinx.serialization.Serializable

@Serializable
data class Receita(
    val id: Int,
    val titulo: String,
    val ingredientes: String,
    val modoPreparo: String
)