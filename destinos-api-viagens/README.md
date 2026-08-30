# API de Gerenciamento de Destinos de Viagem

Trabalho da disciplina **Desenvolvimento de Sistemas Web** — Planejamento da
arquitetura e desenvolvimento inicial de uma API REST.

API RESTful desenvolvida em **Java + Spring Boot** para uma agência de
viagens que está modernizando seus serviços digitais, permitindo a futura
integração com aplicativos de turismo, parceiros comerciais e outras
plataformas.

---

## 1. Visão geral do problema

A agência trabalha com informações sobre destinos turísticos: nome,
localização, descrição, atividades disponíveis, disponibilidade de hotéis e
avaliações. O objetivo deste projeto é entregar a **primeira versão
funcional** de uma API que permita:

- cadastrar destinos de viagem;
- listar todos os destinos disponíveis;
- pesquisar destinos por nome ou localização;
- visualizar detalhes de um destino específico;
- atualizar informações de um destino;
- registrar avaliações de um destino, recalculando sua média;
- excluir um destino.

Nesta etapa, conforme definido no escopo do desafio, **não há integração com
banco de dados nem mecanismos avançados de segurança** — o foco está na
arquitetura, na organização do código e na construção dos endpoints
principais.

---

## 2. Arquitetura proposta

O projeto segue uma **arquitetura em camadas (layered architecture)**,
padrão amplamente utilizado em aplicações Spring Boot por favorecer a
separação de responsabilidades, a manutenção e a evolução futura do sistema:

```
Requisição HTTP
      │
      ▼
┌─────────────────────┐
│     Controller       │  Recebe a requisição, valida o formato de entrada
│  (DestinoController) │  e devolve a resposta HTTP. Não contém regra de
└─────────┬────────────┘  negócio.
          │
          ▼
┌─────────────────────┐
│      Service         │  Concentra as regras de negócio (ex: cálculo da
│   (DestinoService)    │  média de avaliações, filtros de pesquisa,
└─────────┬────────────┘  validação de existência do recurso).
          │
          ▼
┌─────────────────────┐
│    Model / Entity     │  Representa o destino de viagem e seus dados.
│      (Destino)         │  Nesta versão, os objetos são mantidos em um
└─────────────────────┘  Map em memória, dentro do próprio Service.
```

Camadas adicionais de apoio:

- **dto**: classes `DestinoRequestDTO` e `AvaliacaoRequestDTO`, usadas para
  validar e restringir os dados que o cliente pode enviar, sem expor a
  entidade `Destino` diretamente (evita que o cliente informe, por exemplo,
  o `id` ou a média de avaliações manualmente).
- **exception**: `ResourceNotFoundException` e `GlobalExceptionHandler`,
  responsáveis por padronizar as respostas de erro (404, 400, etc.) em um
  formato JSON único e previsível.
- **config**: `DataInitializer`, que popula alguns destinos de exemplo ao
  iniciar a aplicação, apenas para facilitar os testes manuais.

### Por que essa arquitetura?

Para o porte e o objetivo desta etapa do projeto — uma primeira versão
funcional, sem banco de dados — a arquitetura em camadas dentro de um
monólito bem organizado é a mais adequada, pois:

- é o padrão nativamente incentivado pelo Spring Boot (Controller → Service
  → Repository/Model), facilitando a leitura por qualquer desenvolvedor da
  equipe;
- separa claramente "o que a API expõe" (Controller) de "como o negócio
  funciona" (Service), permitindo alterar regras de negócio sem tocar nas
  rotas e vice-versa;
- prepara o terreno para evoluções futuras: quando a persistência real for
  necessária, basta introduzir uma camada de `Repository` (Spring Data JPA)
  entre o `Service` e o banco, sem alterar o `Controller`;
- evita over-engineering: uma arquitetura de microsserviços, por exemplo,
  seria desproporcional para o escopo atual (uma única entidade de negócio,
  sem integração externa ainda definida).

---

## 3. Justificativa da linguagem e dos frameworks escolhidos

**Java + Spring Boot** foram definidos no próprio escopo do desafio, e são
uma escolha coerente com o problema pelos seguintes motivos:

- **Java** é uma linguagem madura, fortemente tipada e amplamente utilizada
  em sistemas corporativos, o que facilita a manutenção do código por
  diferentes desenvolvedores ao longo do tempo — algo relevante para uma API
  que pretende ser integrada por múltiplos parceiros comerciais.
- **Spring Boot** simplifica drasticamente a criação de APIs REST:
  - fornece um servidor web embutido (Tomcat), eliminando configuração manual
    de servidor de aplicação;
  - o módulo **Spring Web (MVC)** oferece anotações (`@RestController`,
    `@GetMapping`, `@PostMapping` etc.) que tornam a definição de rotas REST
    simples, legível e alinhada aos verbos HTTP;
  - a serialização/desserialização JSON é automática (via Jackson), sem
    necessidade de código manual de conversão;
  - o **Spring Validation** permite validar os dados de entrada de forma
    declarativa (`@NotBlank`, `@NotNull`, `@DecimalMin`/`@DecimalMax`);
  - a injeção de dependências nativa do Spring (`@Service`,
    `@RestController`) organiza o projeto em componentes desacoplados e
    testáveis;
  - possui um ecossistema maduro (Spring Data JPA, Spring Security) que
    permitirá evoluir facilmente o projeto para incluir banco de dados e
    autenticação nas próximas etapas, sem reescrever a base atual.

---

## 4. Estrutura do projeto

```
destinos-api-viagens/
├── pom.xml
├── README.md
└── src/
    ├── main/
    │   ├── java/com/agencia/destinosapi/
    │   │   ├── DestinosApiApplication.java   # classe principal
    │   │   ├── controller/
    │   │   │   └── DestinoController.java    # endpoints REST
    │   │   ├── service/
    │   │   │   └── DestinoService.java       # regras de negocio + "banco" em memoria
    │   │   ├── model/
    │   │   │   └── Destino.java              # entidade de dominio
    │   │   ├── dto/
    │   │   │   ├── DestinoRequestDTO.java    # dados de entrada (criar/atualizar)
    │   │   │   └── AvaliacaoRequestDTO.java  # dados de entrada (avaliar)
    │   │   ├── exception/
    │   │   │   ├── ResourceNotFoundException.java
    │   │   │   ├── GlobalExceptionHandler.java
    │   │   │   └── ErrorResponse.java
    │   │   └── config/
    │   │       └── DataInitializer.java      # dados de exemplo ao iniciar
    │   └── resources/
    │       └── application.properties
    └── test/
        └── java/com/agencia/destinosapi/
            └── DestinosApiApplicationTests.java
```

---

## 5. Endpoints da API

Base URL: `http://localhost:8080/api/destinos`

| Método | Rota                        | Descrição                                              |
|--------|-----------------------------|----------------------------------------------------------|
| POST   | `/api/destinos`             | Cadastra um novo destino                                 |
| GET    | `/api/destinos`              | Lista todos os destinos                                   |
| GET    | `/api/destinos?nome=`        | Pesquisa destinos pelo nome (contém, case-insensitive)    |
| GET    | `/api/destinos?localizacao=` | Pesquisa destinos pela localização (contém, case-insensitive) |
| GET    | `/api/destinos/{id}`         | Detalha um destino específico                              |
| PUT    | `/api/destinos/{id}`         | Atualiza os dados de um destino                            |
| PATCH  | `/api/destinos/{id}/avaliacoes` | Registra uma avaliação e recalcula a média              |
| DELETE | `/api/destinos/{id}`         | Exclui um destino                                          |

> `nome` e `localizacao` podem ser combinados na mesma requisição GET
> (`/api/destinos?nome=praia&localizacao=santa+catarina`), evitando a
> duplicação de um endpoint de "listar" e outro de "pesquisar" — a mesma
> rota atende aos dois casos de uso descritos no desafio.

### 5.1 Cadastrar destino — `POST /api/destinos`

Corpo da requisição:
```json
{
  "nome": "Fernando de Noronha",
  "localizacao": "Pernambuco, Brasil",
  "descricao": "Arquipelago com praias preservadas e mergulho.",
  "atividadesTuristicas": ["Mergulho", "Trilhas", "Passeio de barco"],
  "disponibilidadeHoteis": true
}
```
Resposta: `201 Created` com o destino criado (incluindo `id` gerado).

### 5.2 Listar / pesquisar — `GET /api/destinos`

`GET /api/destinos` → lista todos.
`GET /api/destinos?nome=noronha` → filtra por nome.
Resposta: `200 OK` com um array de destinos.

### 5.3 Detalhar — `GET /api/destinos/{id}`

Resposta: `200 OK` com o destino, ou `404 Not Found` se o id não existir.

### 5.4 Atualizar — `PUT /api/destinos/{id}`

Mesmo corpo do cadastro (`DestinoRequestDTO`). Substitui os dados do
destino. Resposta: `200 OK` com o destino atualizado.

### 5.5 Avaliar — `PATCH /api/destinos/{id}/avaliacoes`

Corpo da requisição:
```json
{
  "nota": 4.5
}
```
A nota deve estar entre `0.0` e `5.0`. A API recalcula automaticamente a
`mediaAvaliacao` do destino. Resposta: `200 OK` com o destino atualizado
(incluindo nova média e total de avaliações).

### 5.6 Excluir — `DELETE /api/destinos/{id}`

Resposta: `204 No Content` em caso de sucesso, ou `404 Not Found` se o id
não existir.

---

## 6. Como executar o projeto

### Pré-requisitos
- Java 17 ou superior
- Maven 3.8+ (ou usar o Maven Wrapper, se disponível)

### Passos

```bash
# 1. Clonar o repositório
git clone <URL_DO_REPOSITORIO>
cd destinos-api-viagens

# 2. Rodar a aplicação
mvn spring-boot:run
```

A API sobe em `http://localhost:8080`. Ao iniciar, alguns destinos de
exemplo são cadastrados automaticamente (`DataInitializer`), facilitando os
testes.

### Testando os endpoints

Pode-se usar Postman, Insomnia, ou `curl`. Exemplos:

```bash
# Listar todos os destinos
curl http://localhost:8080/api/destinos

# Cadastrar um destino
curl -X POST http://localhost:8080/api/destinos \
  -H "Content-Type: application/json" \
  -d '{"nome":"Gramado","localizacao":"Rio Grande do Sul, Brasil","descricao":"Cidade europeia no sul do Brasil","atividadesTuristicas":["Chocolate","Natal Luz"],"disponibilidadeHoteis":true}'

# Avaliar um destino (id 1)
curl -X PATCH http://localhost:8080/api/destinos/1/avaliacoes \
  -H "Content-Type: application/json" \
  -d '{"nota": 5}'
```

### Rodando os testes

```bash
mvn test
```

---

## 7. Próximos passos (fora do escopo desta entrega)

- Persistência em banco de dados relacional (Spring Data JPA + PostgreSQL/MySQL).
- Autenticação e autorização (Spring Security + JWT) para parceiros comerciais.
- Documentação interativa via Swagger/OpenAPI.
- Paginação nos endpoints de listagem.
