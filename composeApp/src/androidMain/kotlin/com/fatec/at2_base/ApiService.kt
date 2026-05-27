package com.fatec.at2_base

import android.os.Handler
import android.os.Looper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

object ApiService {

    private const val BASE_URL = "http://10.0.2.2:8080/receitas"

    // 1. READ (GET) - Busca as receitas e converte o texto JSON de forma simples
    suspend fun buscarReceitas(): List<Receita> = withContext(Dispatchers.IO) {
        val lista = mutableListOf<Receita>()
        try {
            val url = URL(BASE_URL)
            val conexao = url.openConnection() as HttpURLConnection
            conexao.requestMethod = "GET"
            conexao.connectTimeout = 5000
            conexao.readTimeout = 5000

            if (conexao.responseCode == 200) {
                val leitor = BufferedReader(InputStreamReader(conexao.inputStream))
                val resposta = StringBuilder()
                var linha: String?
                while (leitor.readLine().also { linha = it } != null) {
                    resposta.append(linha)
                }
                leitor.close()

                // Parser manual simples para extrair os dados do JSON sem bibliotecas complexas
                val textoJson = resposta.toString().trim()
                if (textoJson.startsWith("[") && textoJson.endsWith("]")) {
                    val itens = textoJson.substring(1, textoJson.length - 1).split("},{")
                    for (item in itens) {
                        var limpo = item.replace("{", "").replace("}", "")
                        val propriedades = limpo.split(",")

                        var id = 0
                        var titulo = ""
                        var ingredientes = ""
                        var modoPreparo = ""

                        for (prop in propriedades) {
                            val chaveValor = prop.split(":", limit = 2)
                            if (chaveValor.size == 2) {
                                val chave = chaveValor[0].replace("\"", "").trim()
                                val valor = chaveValor[1].replace("\"", "").trim()
                                when (chave) {
                                    "id" -> id = valor.toIntOrNull() ?: 0
                                    "titulo" -> titulo = valor
                                    "ingredientes" -> ingredientes = valor
                                    "modoPreparo" -> modoPreparo = valor
                                }
                            }
                        }
                        if (id != 0) {
                            lista.add(Receita(id, titulo, ingredientes, modoPreparo))
                        }
                    }
                }
            }
            conexao.disconnect()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return@withContext lista
    }

    // 2. CREATE (POST) - Envia uma nova receita formatada em texto JSON
    suspend fun cadastrarReceita(receita: Receita): Unit = withContext(Dispatchers.IO) {
        try {
            val url = URL(BASE_URL)
            val conexao = url.openConnection() as HttpURLConnection
            conexao.requestMethod = "POST"
            conexao.setRequestProperty("Content-Type", "application/json")
            conexao.doOutput = true

            val jsonOutput = "{\"id\":${receita.id},\"titulo\":\"${receita.titulo}\",\"ingredientes\":\"${receita.ingredientes}\",\"modoPreparo\":\"${receita.modoPreparo}\"}"

            val escritor = OutputStreamWriter(conexao.outputStream)
            escritor.write(jsonOutput)
            escritor.flush()
            escritor.close()

            conexao.responseCode // Força a execução
            conexao.disconnect()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // 3. UPDATE (PUT) - Atualiza uma receita existente
    suspend fun atualizarReceita(receita: Receita): Unit = withContext(Dispatchers.IO) {
        try {
            val url = URL(BASE_URL)
            val conexao = url.openConnection() as HttpURLConnection
            conexao.requestMethod = "PUT"
            conexao.setRequestProperty("Content-Type", "application/json")
            conexao.doOutput = true

            val jsonOutput = "{\"id\":${receita.id},\"titulo\":\"${receita.titulo}\",\"ingredientes\":\"${receita.ingredientes}\",\"modoPreparo\":\"${receita.modoPreparo}\"}"

            val escritor = OutputStreamWriter(conexao.outputStream)
            escritor.write(jsonOutput)
            escritor.flush()
            escritor.close()

            conexao.responseCode // Força a execução
            conexao.disconnect()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // 4. DELETE (DELETE) - Remove a receita passando o ID na URL
    suspend fun deletarReceita(id: Int): Unit = withContext(Dispatchers.IO) {
        try {
            val url = URL("$BASE_URL/$id")
            val conexao = url.openConnection() as HttpURLConnection
            conexao.requestMethod = "DELETE"

            conexao.responseCode // Força a execução
            conexao.disconnect()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}