package com.fatec.at2_base

import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    // Configura o Ktor para trabalhar com conversão automática de objetos para JSON
    install(ContentNegotiation) {
        json()
    }

    // Nossa MutableList em memória para simular o banco de dados das receitas
    val listaReceitas = mutableListOf(
        Receita(
            id = 1,
            titulo = "Omelete de Queijo",
            ingredientes = "2 ovos, 1 fatia de queijo, sal",
            modoPreparo = "Bata os ovos, jogue na frigideira e coloque o queijo no meio antes de dobrar."
        ),
        Receita(
            id = 2,
            titulo = "Bolo de Caneca",
            ingredientes = "4 colheres de farinha, 3 de açúcar, 1 ovo, 3 de leite",
            modoPreparo = "Misture tudo na caneca e leve ao micro-ondas por 3 minutos."
        )
    )

    routing {
        // Requisito: Endpoint GET retornando a lista de dados em JSON
        get("/receitas") {
            call.respond(listaReceitas)
        }

        // Requisito: Endpoint POST para cadastro de novos itens
        post("/receitas") {
            try {
                val novaReceita = call.receive<Receita>()
                // Cria uma cópia da receita recebida inserindo o ID correto
                val receitaComId = novaReceita.copy(id = listaReceitas.size + 1)
                listaReceitas.add(receitaComId)

                // Retorna sucesso
                call.respond(io.ktor.http.HttpStatusCode.Created, mapOf("mensagem" to "Receita salva!"))
            } catch (e: Exception) {
                call.respond(io.ktor.http.HttpStatusCode.BadRequest, "Formato de dados inválido")
            }
        }
    }
}