package com.fatec.at2_base

import io.ktor.http.*
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
    // Conversor automático: Objeto Kotlin <-> Texto JSON
    install(ContentNegotiation) {
        json()
    }

    // MutableList em memória simulando o Banco de Dados
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

        // 1. READ (Listar todas as receitas) - GET
        get("/receitas") {
            call.respond(listaReceitas)
        }

        // 2. CREATE (Cadastrar nova receita) - POST
        post("/receitas") {
            try {
                val novaReceita = call.receive<Receita>()
                // Calcula o próximo ID de forma dinâmica e segura
                val proximoId = if (listaReceitas.isEmpty()) 1 else listaReceitas.maxOf { it.id } + 1
                val receitaComId = novaReceita.copy(id = proximoId)

                listaReceitas.add(receitaComId)
                call.respond(HttpStatusCode.Created, mapOf("mensagem" to "Receita salva com sucesso!"))
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, "Formato de dados inválido")
            }
        }

        // 3. UPDATE (Editar receita existente) - PUT
        put("/receitas") {
            try {
                val receitaEditada = call.receive<Receita>()
                val index = listaReceitas.indexOfFirst { it.id == receitaEditada.id }

                if (index != -1) {
                    listaReceitas[index] = receitaEditada
                    call.respond(HttpStatusCode.OK, mapOf("mensagem" to "Receita atualizada com sucesso!"))
                } else {
                    call.respond(HttpStatusCode.NotFound, "Receita não encontrada para edição")
                }
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, "Dados inválidos para atualização")
            }
        }

        // 4. DELETE (Remover receita por ID) - DELETE
        delete("/receitas/{id}") {
            val idParam = call.parameters["id"]?.toIntOrNull()
            if (idParam != null) {
                val foiRemovido = listaReceitas.removeIf { it.id == idParam }
                if (foiRemovido) {
                    call.respond(HttpStatusCode.OK, mapOf("mensagem" to "Receita removida com sucesso!"))
                } else {
                    call.respond(HttpStatusCode.NotFound, "Receita não encontrada")
                }
            } else {
                call.respond(HttpStatusCode.BadRequest, "ID da receita inválido")
            }
        }
    }
}