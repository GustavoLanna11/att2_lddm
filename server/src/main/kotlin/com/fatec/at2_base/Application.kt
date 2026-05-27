package com.fatec.at2_base

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0", module = Application::module).start(wait = true)
}

fun Application.module() {
    // Lista em memória simulando o banco de dados (Tema: Receitas Culinárias)
    val listaReceitas = mutableListOf(
        Receita(1, "Omelete de Queijo", "2 ovos, queijo, sal", "Bata os ovos e frite com queijo"),
        Receita(2, "Bolo de Caneca", "1 ovo, 2 colheres de chocolate, leite", "Misture tudo na caneca e coloque no microondas por 1 min")
    )

    routing {
        // Rota inicial para testar no navegador
        get("/") {
            call.respondText("Servidor Ktor de Receitas Rodando!", ContentType.Text.Plain)
        }

        // 1. READ (GET) - Retorna o JSON formatado manualmente em string
        get("/receitas") {
            val jsonString = StringBuilder("[")
            listaReceitas.forEachIndexed { index, receita ->
                jsonString.append("{\"id\":${receita.id},\"titulo\":\"${receita.titulo}\",\"ingredientes\":\"${receita.ingredientes}\",\"modoPreparo\":\"${receita.modoPreparo}\"}")
                if (index < listaReceitas.size - 1) jsonString.append(",")
            }
            jsonString.append("]")

            call.respondText(jsonString.toString(), ContentType.Application.Json)
        }

        // 2. CREATE (POST) - Recebe o texto, extrai os campos e adiciona na lista
        post("/receitas") {
            val corpo = call.receiveText()

            // Parser manual simplificado para extrair strings estruturadas
            fun extrairCampo(json: String, campo: String): String {
                val chave = "\"$campo\":\""
                if (!json.contains(chave)) return ""
                val inicio = json.indexOf(chave) + chave.length
                val fim = json.indexOf("\"", inicio)
                return if (inicio != -1 && fim != -1) json.substring(inicio, fim) else ""
            }

            val titulo = extrairCampo(corpo, "titulo")
            val ingredientes = extrairCampo(corpo, "ingredientes")
            val modoPreparo = extrairCampo(corpo, "modoPreparo")

            if (titulo.isNotBlank()) {
                val novoId = if (listaReceitas.isEmpty()) 1 else listaReceitas.last().id + 1
                listaReceitas.add(Receita(novoId, titulo, ingredientes, modoPreparo))
                call.respond(HttpStatusCode.Created, "Cadastrado com sucesso")
            } else {
                call.respond(HttpStatusCode.BadRequest, "Dados inválidos")
            }
        }

        // 3. UPDATE (PUT) - Atualiza uma receita existente na lista
        put("/receitas") {
            val corpo = call.receiveText()

            fun extrairCampo(json: String, campo: String): String {
                val chave = "\"$campo\":\""
                if (!json.contains(chave)) return ""
                val inicio = json.indexOf(chave) + chave.length
                val fim = json.indexOf("\"", inicio)
                return if (inicio != -1 && fim != -1) json.substring(inicio, fim) else ""
            }

            val idChave = "\"id\":"
            val idInicio = corpo.indexOf(idChave) + idChave.length
            val idFim = corpo.indexOf(",", idInicio)
            val idStr = corpo.substring(idInicio, idFim).trim()
            val id = idStr.toIntOrNull() ?: 0

            val titulo = extrairCampo(corpo, "titulo")
            val ingredientes = extrairCampo(corpo, "ingredientes")
            val modoPreparo = extrairCampo(corpo, "modoPreparo")

            val receita = listaReceitas.find { it.id == id }
            if (receita != null) {
                listaReceitas.remove(receita)
                listaReceitas.add(receita.copy(titulo = titulo, ingredientes = ingredientes, modoPreparo = modoPreparo))
                call.respond(HttpStatusCode.OK, "Atualizado com sucesso")
            } else {
                call.respond(HttpStatusCode.NotFound, "Receita não encontrada")
            }
        }

        // 4. DELETE (DELETE) - Remove a receita pelo ID enviado na URL
        delete("/receitas/{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
            val receita = listaReceitas.find { it.id == id }
            if (receita != null) {
                listaReceitas.remove(receita)
                call.respond(HttpStatusCode.OK, "Deletado com sucesso")
            } else {
                call.respond(HttpStatusCode.NotFound, "Receita não encontrada")
            }
        }
    }
}