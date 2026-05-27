package com.fatec.at2_base

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

object ApiService {

    // USANDO O IP NATIVO DO EMULADOR REPRODUZINDO A PORTA 8081 DO KTOR
    // Isso ignora a necessidade de rodar comandos "adb reverse" no terminal
    private const val BASE_URL = "http://10.0.2.2:8081/receitas"

    // 1. READ (GET) - Extrator baseado em varredura de texto linear (Super Blindado)
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

                val json = resposta.toString().trim()

                // Varre o texto localizando onde começa cada objeto por "{"
                var posicao = 0
                while (posicao < json.length) {
                    val inicioObjeto = json.indexOf("{", posicao)
                    if (inicioObjeto == -1) break
                    val fimObjeto = json.indexOf("}", inicioObjeto)
                    if (fimObjeto == -1) break

                    val bloco = json.substring(inicioObjeto, fimObjeto + 1)

                    // Extrator cirúrgico que pega qualquer dado contido entre aspas após a chave
                    fun pegarCampoTexto(campo: String): String {
                        val chave = "\"$campo\""
                        val idxChave = bloco.indexOf(chave)
                        if (idxChave == -1) return ""
                        val idxDoisPontos = bloco.indexOf(":", idxChave)
                        val idxAbreAspas = bloco.indexOf("\"", idxDoisPontos)
                        if (idxAbreAspas == -1) return ""
                        val idxFechaAspas = bloco.indexOf("\"", idxAbreAspas + 1)
                        return if (idxFechaAspas != -1) bloco.substring(idxAbreAspas + 1, idxFechaAspas) else ""
                    }

                    // Extrator do ID numérico
                    var id = 0
                    val idxId = bloco.indexOf("\"id\"")
                    if (idxId != -1) {
                        val idxDoisPontos = bloco.indexOf(":", idxId)
                        var idxFimNum = bloco.indexOf(",", idxDoisPontos)
                        if (idxFimNum == -1 || idxFimNum > bloco.indexOf("}", idxDoisPontos)) {
                            idxFimNum = bloco.indexOf("}", idxDoisPontos)
                        }
                        val numeroStr = bloco.substring(idxDoisPontos + 1, idxFimNum).replace("\"", "").trim()
                        id = numeroStr.toIntOrNull() ?: 0
                    }

                    val titulo = pegarCampoTexto("titulo")
                    val ingredientes = pegarCampoTexto("ingredientes")
                    val modoPreparo = pegarCampoTexto("modoPreparo")

                    if (id > 0 && titulo.isNotBlank()) {
                        lista.add(Receita(id, titulo, ingredientes, modoPreparo))
                    }

                    posicao = fimObjeto + 1
                }
            }
            conexao.disconnect()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return@withContext lista
    }

    // 2. CREATE (POST)
    suspend fun cadastrarReceita(receita: Receita): Unit = withContext(Dispatchers.IO) {
        try {
            val url = URL(BASE_URL)
            val conexao = url.openConnection() as HttpURLConnection
            conexao.requestMethod = "POST"
            conexao.setRequestProperty("Content-Type", "application/json")
            conexao.doOutput = true

            val jsonOutput = "{\"titulo\":\"${receita.titulo}\",\"ingredientes\":\"${receita.ingredientes}\",\"modoPreparo\":\"${receita.modoPreparo}\"}"

            val escritor = OutputStreamWriter(conexao.outputStream)
            escritor.write(jsonOutput)
            escritor.flush()
            escritor.close()

            conexao.responseCode
            conexao.disconnect()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // 3. UPDATE (PUT)
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

            conexao.responseCode
            conexao.disconnect()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // 4. DELETE (DELETE)
    suspend fun deletarReceita(id: Int): Unit = withContext(Dispatchers.IO) {
        try {
            val url = URL("$BASE_URL/$id")
            val conexao = url.openConnection() as HttpURLConnection
            conexao.requestMethod = "DELETE"

            conexao.responseCode
            conexao.disconnect()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}