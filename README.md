# 💰 Budgeting — Assistente Financeiro com IA

## 📌 O que o projeto faz

O **Budgeting** é uma API REST construída com **Spring Boot** e **Spring AI** que funciona como um assistente financeiro inteligente. O sistema permite registrar, consultar e analisar transações financeiras de duas formas:

- **Via endpoints REST tradicionais**: criando transações por JSON, listando por categoria e consultando um resumo financeiro.
- **Via áudio com Inteligência Artificial**: o usuário envia um áudio descrevendo um gasto (ex: *"Gastei 50 reais na farmácia"*), e a IA transcreve o áudio, interpreta a intenção, categoriza automaticamente a transação e persiste no banco de dados — devolvendo a resposta também em áudio.

### Fluxo principal (IA por voz)

```
Áudio do usuário → Whisper (transcrição) → GPT-4o-mini (interpretação + Tool Calling) → Banco de Dados (persistência) → TTS (resposta em áudio)
```

### Endpoints disponíveis

| Método | Endpoint                   | Descrição                                      |
|--------|----------------------------|-------------------------------------------------|
| POST   | `/transactions`            | Cria uma transação via JSON                     |
| GET    | `/transactions/{category}` | Lista transações por categoria                  |
| GET    | `/transactions/summary`    | Retorna resumo financeiro geral e por categoria |
| POST   | `/transactions/ai`         | Processa áudio com IA (entrada e saída em áudio)|

### Categorias disponíveis

`GROCERIES` · `PHARMA` · `AUTO`

---

## 🚀 Como executar a aplicação

### Pré-requisitos

- **Java 21+**
- **Docker** (para o banco de dados MySQL)
- **Chave da API da OpenAI** ([obter aqui](https://platform.openai.com/api-keys))

### Passo a passo

**1. Clone o repositório**
```bash
git clone <url-do-repositorio>
cd budgeting
```

**2. Configure a variável de ambiente com sua chave da OpenAI**

Windows (PowerShell):
```powershell
$env:OPENAI_API_KEY="sk-sua-chave-aqui"
```

Linux/macOS:
```bash
export OPENAI_API_KEY=sk-sua-chave-aqui
```

**3. Execute a aplicação**
```bash
./gradlew bootRun
```

> O Spring Boot Docker Compose sobe o MySQL automaticamente via `compose.yml`. Não é necessário rodar `docker compose up` manualmente.

**4. A API estará disponível em** `http://localhost:8080`

---

## 🛠️ Quais tecnologias foram usadas

| Tecnologia                  | Finalidade                                              |
|-----------------------------|---------------------------------------------------------|
| **Java 21**                 | Linguagem principal                                     |
| **Spring Boot 4.1**         | Framework da aplicação                                  |
| **Spring AI 2.0**           | Integração com modelos de IA (chat, transcrição, fala)  |
| **OpenAI GPT-4o-mini**      | Modelo de linguagem para interpretar transações         |
| **OpenAI Whisper**          | Transcrição de áudio para texto                         |
| **OpenAI TTS**              | Geração de áudio a partir de texto (Text-to-Speech)     |
| **Spring AI Tool Calling**  | Permite que a IA execute funções Java automaticamente   |
| **Spring Data JPA**         | Persistência de dados                                   |
| **MySQL 8.0**               | Banco de dados relacional                               |
| **Docker Compose**          | Orquestração do container do banco de dados             |
| **Bean Validation**         | Validação de dados de entrada nos endpoints             |
| **Lombok**                  | Redução de boilerplate no código Java                   |
| **JUnit 5 + Mockito**       | Testes unitários e mocks                                |
| **MockMvc**                 | Testes de integração da camada HTTP                     |

---

## 🧪 Como testar o fluxo principal

### Opção 1 — Pelo IntelliJ HTTP Client

Abra o arquivo `requests.http` na raiz do projeto. Ele contém requisições prontas para todos os endpoints. Clique no botão ▶ ao lado de cada requisição.

### Opção 2 — Via cURL

**Criar uma transação (POST)**
```bash
curl -X POST http://localhost:8080/transactions \
  -H "Content-Type: application/json" \
  -d '{
    "description": "Compras no supermercado",
    "amount": 15000,
    "category": "GROCERIES"
  }'
```

**Listar transações por categoria (GET)**
```bash
curl http://localhost:8080/transactions/GROCERIES
```

**Consultar resumo financeiro (GET)**
```bash
curl http://localhost:8080/transactions/summary
```

**Enviar áudio para o assistente de IA (POST)**
```bash
curl -X POST http://localhost:8080/transactions/ai \
  -F "file=@./src/test/resources/audio/audio11.m4a" \
  --output resposta.mp3
```

### Opção 3 — Rodar os testes automatizados

```bash
./gradlew test
```

Os testes cobrem:

- ✅ **Domínio** — Validação de regras de negócio (descrição, valor, categoria)
- ✅ **Casos de Uso** — Persistência, listagem por categoria e resumo financeiro
- ✅ **Controller** — Endpoints REST, respostas HTTP (201, 200, 400) e validação de entrada

### Exemplo de resposta — `POST /transactions` (sucesso)
```json
{
  "id": "a1b2c3d4-...",
  "description": "Compras no supermercado",
  "category": "GROCERIES",
  "amount": 15000.0
}
```

### Exemplo de resposta — `POST /transactions` (erro de validação)
```json
{
  "timestamp": "2026-11-04T...",
  "status": 400,
  "error": "Bad Request",
  "message": "Falha de validação nos campos fornecidos",
  "fields": {
    "description": "A descrição é obrigatória",
    "amount": "O valor deve ser maior que zero"
  }
}
```

### Exemplo de resposta — `GET /transactions/summary`
```json
{
  "totalAmount": 37000,
  "totalTransactions": 4,
  "totalByCategory": {
    "GROCERIES": 12000,
    "AUTO": 20000,
    "PHARMA": 5000
  }
}
```
