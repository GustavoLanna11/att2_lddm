## 👨‍🍳 Gerenciador de Receitas Culinárias
O ecossistema foi projetado para resolver o problema de armazenamento e gerenciamento local de receitas. Ele funciona como um organizador digital onde o usuário pode interagir com um catálogo em tempo real. A aplicação implementa um ciclo CRUD completo:

- Create: Cadastro de novas receitas com título, ingredientes e modo de preparo.
- Read: Listagem dinâmica dos pratos salvos no servidor.
- Update: Alteração de receitas existentes ao tocar em um item da lista.
- Delete: Exclusão definitiva de registros através do botão "Excluir".

---

## 🚀 Como Rodar o Projeto
Para o correto funcionamento do ecossistema, o backend deve ser iniciado antes do aplicativo móvel, garantindo que os endpoints estejam prontos para receber as conexões.

Passo 1: Iniciar o Servidor (Backend Ktor)
- No IntelliJ IDEA, abra a árvore de arquivos e navegue até a pasta: ```server/src/main/kotlin/com/fatec/at2_base/```.
- Abra o arquivo ```Application.kt```.
- Localize a função ```fun main()``` (linha 11) e clique no triângulo verde (Play) ao lado esquerdo dela.
- Escolha a opção Run ```ApplicationKt```.
- Confirme no terminal inferior do IntelliJ se a linha Responding at ```http://127.0.0.1:8080``` apareceu. Deixe essa execução ativa.

Passo 2: Iniciar o Aplicativo (Módulo Android)
- Na barra de ferramentas superior do IntelliJ (ao centro/direita), localize o menu de configurações de execução.
- Altere a seleção de ```ApplicationKt``` para ```composeApp```.
- Certifique-se de que o emulador (ex: Pixel 6) está selecionado na caixa ao lado.
- Clique no botão de Play Verde da barra superior.
- O IntelliJ compilará o módulo Android, abrirá o emulador e instalará o aplicativo automaticamente.

---

## 🛠️ Estrutura e Recursos
O projeto está dividido em três módulos principais, seguindo o padrão de arquitetura multiplataforma:

- Módulo server (Backend)
- Motor Netty: Servidor web embutido de alta performance configurado na porta 8080.
- Persistência em Memória: Uso de uma MutableList thread-safe que simula o banco de dados e já inicia com receitas pré-cadastradas para testes.

### Rotas de API estruturadas:

- ```GET /receitas``` -> Transmite o catálogo de dados estruturados.
- ```POST /receitas``` -> Processa e adiciona novos registros.
- ```PUT /receitas``` -> Modifica dados existentes por ID.
- ```DELETE /receitas/{id}``` -> Remove o item correspondente da memória.

### Módulo composeApp (Frontend Android)
- UI Reativa (Jetpack Compose): Interface moderna baseada em componentes declarativos do Material Design 3.
- LazyColumn: Lista otimizada que renderiza os cartões de receitas de forma dinâmica, economizando memória do dispositivo.
- Formulário Inteligente: Campos de texto (OutlinedTextField) que alteram seu estado automaticamente entre os modos de "Nova Receita" ou "Editar Receita" dependendo da ação do usuário.
- Comunicação Segura de Rede: Classe ApiService adaptada com conexões assíncronas em segundo plano (Dispatchers.IO), configurada para conversar de forma transparente com o endereço de loopback do emulador (10.0.2.2).

### Módulo shared (Código Compartilhado)
- Data Class Receita: O modelo de dados unificado contendo os atributos id, titulo, ingredientes e modoPreparo, compartilhado e compreendido tanto pelo servidor quanto pelo aplicativo Android.

---

Gustavo Lanna - 2026
