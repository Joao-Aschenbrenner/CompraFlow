# CompraFlow

API REST para gerenciamento de **solicitações de compra, cotações de fornecedores e aprovação de compras**, criada do zero para praticar Design Patterns com Java e Spring Boot.

> Projeto educacional desenvolvido para o desafio **Design Patterns com Java: Dos Clássicos (GoF) ao Spring Framework**, da DIO. O domínio e a implementação do CompraFlow são diferentes do exemplo Cliente + ViaCEP apresentado no laboratório.

## Objetivo

O CompraFlow simula um processo comum em empresas:

1. cadastrar fornecedores;
2. criar uma solicitação de compra com um ou mais itens;
3. abrir a solicitação para cotação;
4. registrar propostas de fornecedores;
5. comparar as propostas usando um critério configurável;
6. identificar o nível de aprovação necessário;
7. aprovar ou rejeitar a compra.

O projeto foi intencionalmente mantido pequeno o suficiente para ser estudado, mas estruturado como uma API real.

## Design Patterns aplicados

### 1. Singleton — criacional

Há duas demonstrações do conceito:

- `MoneyRoundingPolicy`: Singleton clássico com **Initialization-on-demand Holder**. Centraliza a política de arredondamento monetário.
- `PoliticasCompraProperties`: bean gerenciado pelo Spring. Como não há escopo diferente configurado, o container mantém uma única instância do componente durante a aplicação.

### 2. Strategy — comportamental

A interface `SelecionarCotacaoStrategy` define o contrato para escolher a melhor proposta. Existem três algoritmos intercambiáveis:

- `MenorPrecoStrategy` — menor valor total em BRL;
- `MenorPrazoStrategy` — menor prazo de entrega;
- `CustoBeneficioStrategy` — pondera 60% preço, 25% prazo de entrega e 15% condição de pagamento.

A solicitação escolhe o critério no momento do cadastro. O `CotacaoStrategyResolver` localiza a implementação adequada sem `if/else` espalhado pelo sistema.

### 3. Facade — estrutural

`CompraFacade` é a porta de entrada da camada HTTP. Os controllers não precisam conhecer detalhes de:

- persistência JPA;
- validações de negócio;
- conversão de moeda;
- seleção da Strategy;
- cadeia de aprovação;
- mapeamento de DTOs.

A Facade expõe operações simples e coordena os subsistemas internos.

### 4. Chain of Responsibility — bônus

O fluxo de aprovação é uma evolução própria do desafio:

```text
até R$ 2.000       -> COORDENADOR
até R$ 10.000      -> GERENTE
até R$ 50.000      -> DIRETOR
acima de R$ 50.000 -> DIRETORIA
```

Cada `ApprovalHandler` decide se pode tratar o valor ou encaminha a responsabilidade para o próximo elo.

## Arquitetura

```text
HTTP / Swagger
      |
      v
Controllers
      |
      v
CompraFacade                         <- FACADE
      |
      +--> SolicitacaoService
      +--> CotacaoService
      +--> FornecedorService
      +--> AvaliacaoCotacaoService
                |
                +--> CotacaoStrategyResolver
                |       +--> MenorPrecoStrategy
                |       +--> MenorPrazoStrategy          <- STRATEGY
                |       +--> CustoBeneficioStrategy
                |
                +--> ApprovalChain                       <- CHAIN OF RESPONSIBILITY
                |
                +--> CambioService -> OpenFeign
      |
      v
Spring Data JPA -> H2

MoneyRoundingPolicy                                      <- SINGLETON clássico
PoliticasCompraProperties                                <- SINGLETON via Spring
```

## Tecnologias

- Java 21
- Spring Boot 4.1
- Spring Web
- Spring Data JPA
- Bean Validation
- Spring Cloud OpenFeign
- H2 Database
- Springdoc OpenAPI / Swagger UI
- JUnit 5 + Spring Boot Test + MockMvc
- Maven

## Integração de câmbio

As propostas podem ser registradas em `BRL`, `USD` ou `EUR`.

- para BRL, nenhum serviço externo é necessário;
- para USD/EUR, `CambioService` usa OpenFeign para consultar a taxa e normalizar a proposta em BRL antes de executar a Strategy.

A integração é propositalmente opcional: todo o roteiro principal de demonstração funciona somente com H2 local.

## Como executar

Pré-requisitos:

- JDK 21+
- Maven 3.9+

No terminal:

```bash
mvn clean test
mvn spring-boot:run
```

Depois acesse:

```text
http://localhost:8080/swagger-ui.html
```

H2 Console:

```text
http://localhost:8080/h2-console
JDBC URL: jdbc:h2:mem:compraflow
User: sa
Password: (vazio)
```

## Fluxo rápido para demonstrar

Ao iniciar, três fornecedores fictícios são cadastrados automaticamente.

1. `GET /api/fornecedores`
2. `POST /api/solicitacoes`
3. `POST /api/solicitacoes/{id}/abrir-cotacao`
4. registrar três propostas em `POST /api/solicitacoes/{id}/cotacoes`
5. `POST /api/solicitacoes/{id}/avaliar`
6. `POST /api/solicitacoes/{id}/aprovar`

Existe um roteiro completo em [`docs/ROTEIRO_DEMO.md`](docs/ROTEIRO_DEMO.md) e requisições prontas em [`http/compraflow.http`](http/compraflow.http).

## Regras de negócio demonstradas

- uma solicitação nasce como `RASCUNHO`;
- cotações só são aceitas após abertura da fase `EM_COTACAO`;
- o mesmo fornecedor não pode cotar duas vezes a mesma solicitação;
- a avaliação exige no mínimo três cotações válidas;
- cotações vencidas não participam da seleção;
- todas as propostas são comparadas em BRL;
- somente o nível hierárquico adequado (ou superior) pode aprovar;
- erros de negócio, validação, recurso inexistente e integração externa retornam respostas HTTP distintas.

## Testes

O projeto contém testes para:

- Singleton clássico;
- cada Strategy;
- Chain of Responsibility;
- escopo Singleton do Spring;
- fluxo E2E da API via MockMvc, sem depender de internet.

Execute:

```bash
mvn clean test
```

## Estrutura principal

```text
src/main/java/br/com/jaaschenbrenner/compraflow
├── api
│   ├── dto
│   ├── CompraMapper.java
│   ├── FornecedorController.java
│   ├── GlobalExceptionHandler.java
│   └── SolicitacaoController.java
├── bootstrap
├── config
├── domain
├── exception
├── integration/frankfurter
├── patterns
│   ├── chain
│   ├── facade
│   ├── singleton
│   └── strategy
├── repository
└── service
```

## Sobre originalidade e uso de IA

O CompraFlow **não é um fork do projeto Cliente/ViaCEP do laboratório**. O problema de negócio, entidades, endpoints e aplicação dos padrões foram construídos especificamente para este desafio.

Ferramentas de IA foram usadas como apoio para arquitetura, revisão, geração de testes e documentação. A recomendação antes da entrega é executar o projeto, revisar os arquivos e conseguir explicar onde e por que cada padrão foi usado.

## Possíveis evoluções

- autenticação e perfis de usuário;
- anexos de propostas comerciais;
- histórico/auditoria de alterações;
- pedido de compra após aprovação;
- envio de convite de cotação por e-mail;
- persistência em PostgreSQL/MySQL;
- front-end React/Angular.
