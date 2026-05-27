package com.fatec.at2_base

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*

object ApiService {
    val client = HttpClient {
        // Faz o app entender as respostas em JSON vindas do servidor
        install(ContentNegotiation) {
            json()
        }
    }

    // IP especial do emulador Android para enxergar o localhost do computador
    private const val BASE_URL = "http://10.0.2.2:8080/receitas"

    suspend fun buscarReceitas(): List<Receita> {
        return client.get(BASE_URL).body()
    }

    suspend fun cadastrarReceita(receita: Receita) {
        client.post(BASE_URL) {
            contentType(ContentType.Application.Json)
            setBody(receita)
        }
    }

    suspend fun atualizarReceita(receita: Receita) {
        client.put(BASE_URL) {
            contentType(ContentType.Application.Json)
            setBody(receita)
        }
    }

    suspend fun deletarReceita(id: Int) {
        client.delete("$BASE_URL/$id")
    }
}