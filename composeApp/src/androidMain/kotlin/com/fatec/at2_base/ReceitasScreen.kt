package com.fatec.at2_base

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReceitasScreen() {
    val scope = rememberCoroutineScope()

    // Estados das caixas de texto do Formulário
    var titulo by remember { mutableStateOf("") }
    var ingredientes by remember { mutableStateOf("") }
    var modoPreparo by remember { mutableStateOf("") }

    // Armazena qual receita estamos editando (null = criando nova)
    var receitaEmEdicao by remember { mutableStateOf<Receita?>(null) }

    // Estado reativo da lista que alimenta a tela
    var receitas by remember { mutableStateOf(listOf<Receita>()) }

    // Tarja de aviso caso aconteça algo na rede
    var mensagemErro by remember { mutableStateOf<String?>(null) }

    // Cores do Tema Verde Profissional
    val verdePrincipal = Color(0xFF2E7D32)
    val verdeClaroCard = Color(0xFFE8F5E9)
    val verdeTextoBotao = Color.White
    val fundoAlerta = Color(0xFFFFF3CD)
    val textoAlerta = Color(0xFF856404)

    // Função interna para recarregar as receitas chamando o ApiService geral
    fun atualizarLista() {
        scope.launch {
            try {
                val resultado = ApiService.buscarReceitas()
                if (resultado.isEmpty()) {
                    mensagemErro = "Lista vazia ou aguardando conexão..."
                } else {
                    receitas = resultado
                    mensagemErro = null // Limpa a barra se tudo der certo!
                }
            } catch (e: Exception) {
                mensagemErro = "Erro de rede: ${e.message}"
            }
        }
    }

    // Busca as receitas assim que a tela abre pela primeira vez
    LaunchedEffect(Unit) {
        atualizarLista()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Chef Digital",
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = verdePrincipal)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            Text(
                text = if (receitaEmEdicao == null) "Cadastrar Nova Receita" else "Editar Receita",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = verdePrincipal
            )
            Spacer(modifier = Modifier.height(12.dp))

            // Mostra mensagens de aviso apenas se necessário
            if (mensagemErro != null && receitas.isEmpty()) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = fundoAlerta),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                ) {
                    Text(
                        text = mensagemErro!!,
                        color = textoAlerta,
                        modifier = Modifier.padding(10.dp),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            OutlinedTextField(
                value = titulo,
                onValueChange = { titulo = it },
                label = { Text("Nome da Receita") },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = verdePrincipal,
                    focusedLabelColor = verdePrincipal
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = ingredientes,
                onValueChange = { ingredientes = it },
                label = { Text("Ingredientes") },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = verdePrincipal,
                    focusedLabelColor = verdePrincipal
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = modoPreparo,
                onValueChange = { modoPreparo = it },
                label = { Text("Modo de Preparo") },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = verdePrincipal,
                    focusedLabelColor = verdePrincipal
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        if (titulo.isNotBlank() && ingredientes.isNotBlank() && modoPreparo.isNotBlank()) {
                            scope.launch {
                                if (receitaEmEdicao == null) {
                                    ApiService.cadastrarReceita(Receita(0, titulo, ingredientes, modoPreparo))
                                } else {
                                    receitaEmEdicao?.let {
                                        ApiService.atualizarReceita(it.copy(titulo = titulo, ingredientes = ingredientes, modoPreparo = modoPreparo))
                                    }
                                }
                                titulo = ""
                                ingredientes = ""
                                modoPreparo = ""
                                receitaEmEdicao = null
                                atualizarLista()
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = verdePrincipal),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = if (receitaEmEdicao == null) "Salvar Receita" else "Atualizar",
                        color = verdeTextoBotao
                    )
                }

                if (receitaEmEdicao != null) {
                    OutlinedButton(
                        onClick = {
                            titulo = ""
                            ingredientes = ""
                            modoPreparo = ""
                            receitaEmEdicao = null
                        },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Gray),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Cancelar")
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
            HorizontalDivider(color = verdeClaroCard, thickness = 2.dp)
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Receitas Cadastradas (Toque para editar)",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.DarkGray
            )
            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(
                modifier = Modifier.fillMaxWidth().weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(receitas) { receita ->
                    val fundoCard = if (receitaEmEdicao?.id == receita.id) Color(0xFFC8E6C9) else verdeClaroCard

                    Card(
                        colors = CardDefaults.cardColors(containerColor = fundoCard),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                receitaEmEdicao = receita
                                titulo = receita.titulo
                                ingredientes = receita.ingredientes
                                modoPreparo = receita.modoPreparo
                            }
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = receita.titulo,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF1B5E20)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Ingredientes: ${receita.ingredientes}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.DarkGray
                                )
                                Text(
                                    text = "Preparo: ${receita.modoPreparo}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.Gray
                                )
                            }

                            TextButton(
                                onClick = {
                                    scope.launch {
                                        ApiService.deletarReceita(receita.id)
                                        if (receitaEmEdicao?.id == receita.id) {
                                            titulo = ""
                                            ingredientes = ""
                                            modoPreparo = ""
                                            receitaEmEdicao = null
                                        }
                                        atualizarLista()
                                    }
                                }
                            ) {
                                Text("Excluir", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}