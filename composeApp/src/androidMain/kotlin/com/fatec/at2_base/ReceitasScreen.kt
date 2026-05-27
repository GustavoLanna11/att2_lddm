package com.fatec.at2_base

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@Composable
fun ReceitasScreen() {
    val scope = rememberCoroutineScope()

    // Estados das caixas de texto do Formulário
    var titulo by remember { mutableStateOf("") }
    var ingredientes by remember { mutableStateOf("") }
    var modoPreparo by remember { mutableStateOf("") }

    // Armazena qual receita estamos editando (null = criando nova)
    var receitaEmEdicao by remember { mutableStateOf<Receita?>(null) }

    // Estado da lista que vem do servidor
    var receitas by remember { mutableStateOf(listOf<Receita>()) }

    // Função interna para recarregar as receitas do backend
    fun atualizarLista() {
        scope.launch {
            runCatching { ApiService.buscarReceitas() }
                .onSuccess { receitas = it }
        }
    }

    // Busca as receitas assim que a tela abre
    LaunchedEffect(Unit) {
        atualizarLista()
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(
            text = if (receitaEmEdicao == null) "Nova Receita" else "Editar Receita",
            style = MaterialTheme.typography.titleLarge
        )
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = titulo,
            onValueChange = { titulo = it },
            label = { Text("Nome da Receita") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = ingredientes,
            onValueChange = { ingredientes = it },
            label = { Text("Ingredientes") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = modoPreparo,
            onValueChange = { modoPreparo = it },
            label = { Text("Modo de Preparo") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = {
                    if (titulo.isNotBlank() && ingredientes.isNotBlank() && modoPreparo.isNotBlank()) {
                        scope.launch {
                            if (receitaEmEdicao == null) {
                                // CREATE
                                ApiService.cadastrarReceita(Receita(0, titulo, ingredientes, modoPreparo))
                            } else {
                                // UPDATE
                                receitaEmEdicao?.let {
                                    ApiService.atualizarReceita(it.copy(titulo = titulo, ingredientes = ingredientes, modoPreparo = modoPreparo))
                                }
                            }
                            // Limpa campos (Feedback visual de sucesso)
                            titulo = ""
                            ingredientes = ""
                            modoPreparo = ""
                            receitaEmEdicao = null
                            atualizarLista()
                        }
                    }
                },
                modifier = Modifier.weight(1f)
            ) {
                Text(if (receitaEmEdicao == null) "Salvar" else "Atualizar")
            }

            if (receitaEmEdicao != null) {
                OutlinedButton(
                    onClick = {
                        titulo = ""
                        ingredientes = ""
                        modoPreparo = ""
                        receitaEmEdicao = null
                    }
                ) {
                    Text("Cancelar")
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        HorizontalDivider()
        Spacer(modifier = Modifier.height(16.dp))

        Text("Receitas Cadastradas (Toque para editar)", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))

        // Lista dinâmica (READ) com remoção (DELETE)
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(receitas) { receita ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            // Joga as informações de volta para as caixas de texto ao clicar
                            receitaEmEdicao = receita
                            titulo = receita.titulo
                            ingredientes = receita.ingredientes
                            modoPreparo = receita.modoPreparo
                        }
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(receita.titulo, style = MaterialTheme.typography.titleSmall)
                            Text("Ingredientes: ${receita.ingredientes}", style = MaterialTheme.typography.bodyMedium)
                            Text("Preparo: ${receita.modoPreparo}", style = MaterialTheme.typography.bodySmall)
                        }

                        IconButton(
                            onClick = {
                                scope.launch {
                                    runCatching { ApiService.deletarReceita(receita.id) }
                                        .onSuccess {
                                            if (receitaEmEdicao?.id == receita.id) {
                                                titulo = ""
                                                ingredientes = ""
                                                modoPreparo = ""
                                                receitaEmEdicao = null
                                            }
                                            atualizarLista()
                                        }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Deletar",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }
        }
    }
}