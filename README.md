# CompraFlow

Sistema de **solicitações de compras, cotações de fornecedores e aprovação de compras**, desenvolvido com Java, Spring Boot e Electron para aplicar Design Patterns em um domínio próprio.

> Projeto educacional desenvolvido para o desafio **Design Patterns com Java: Dos Clássicos (GoF) ao Spring Framework**, dentro da trilha [Santander 2026 - Java Backend da DIO](https://web.dio.me/track/santander-2026-java-backend).

## 🚀 Versão pronta para avaliação

Para testar o projeto sem precisar configurar Java ou Maven, utilize a versão desktop mais recente:

- **[Baixar CompraFlow Portable v1.2.1](https://github.com/Joao-Aschenbrenner/CompraFlow/releases/download/v1.2.1-desktop/CompraFlow-Portable-1.2.1-x64.exe)** — basta baixar e executar.
- **[Baixar CompraFlow Setup v1.2.1](https://github.com/Joao-Aschenbrenner/CompraFlow/releases/download/v1.2.1-desktop/CompraFlow-Setup-1.2.1-x64.exe)** — instalador para Windows x64.
- **[Ver a release atual](https://github.com/Joao-Aschenbrenner/CompraFlow/releases/tag/v1.2.1-desktop)**.

A versão desktop inclui **JRE 21 embutido**, backend Spring Boot local e banco H2 persistente. O usuário pode cadastrar fornecedores, criar e editar solicitações, registrar cotações, avaliar propostas, aprovar/rejeitar compras, imprimir pedidos e salvar documentos em PDF.

## 🎓 Projeto da trilha Santander 2026 + uso de IA

O CompraFlow foi criado como uma implementação própria para praticar os conceitos apresentados na trilha **Santander 2026 - Java Backend da DIO**.

Além dos conteúdos de Java, Spring Framework, APIs REST, persistência, testes e Design Patterns, foram utilizados **conceitos e práticas de Inteligência Artificial generativa trabalhados durante a formação como apoio ao processo de desenvolvimento**.

A IA foi utilizada como ferramenta de apoio em etapas como:

- ideação e refinamento do domínio do projeto;
- planejamento da arquitetura e divisão de responsabilidades;
- apoio à implementação e refatoração;
- revisão de código e identificação de possíveis erros;
- criação e expansão de testes automatizados;
- análise de falhas durante os ciclos de build e release;
- geração e revisão da documentação técnica.

O uso de IA fez parte do processo de aprendizado e desenvolvimento, enquanto as decisões de domínio, arquitetura, validações, testes e evolução do CompraFlow foram consolidadas no próprio projeto e verificadas através de testes automatizados e GitHub Actions.

O CompraFlow **não é um fork nem uma cópia do exemplo Cliente/ViaCEP do laboratório**. O domínio de solicitações de compra, fornecedores, cotações e aprovação foi escolhido especificamente para esta entrega.

## Objetivo

O CompraFlow simula um processo comum em empresas:

1. cadastrar fornecedores;
2. criar uma solicitação de compra com um ou mais itens;
3. editar ou excluir solicitações ainda em rascunho;
4. abrir a solicitação para cotação;
5. registrar propostas de diferentes fornecedores;
6. comparar as propostas usando um critério configurável;
7. identificar automaticamente o nível de aprovação necessário;
8. aprovar ou rejeitar a compra;
9. imprimir a solicitação ou salvá-la em PDF.

O projeto foi mantido pequeno o suficiente para permitir o estudo de cada padrão, mas organizado como uma aplicação real, com backend, persistência, API REST, testes, documentação e cliente desktop.

## Design Patterns aplicados

### 1. Singleton — criacional

Há duas demonstrações do conceito:

- `MoneyRoundingPolicy`: Singleton clássico com **Initialization-on-demand Holder**, centralizando a política de arredondamento monetário.
- `PoliticasCompraProperties`: bean gerenciado pelo Spring. Como não existe outro escopo configurado, o container mantém uma única instância durante o ciclo de vida da aplicação.

### 2. Strategy — comportamental

A interface `SelecionarCotacaoStrategy` define o contrato para escolha da melhor proposta. Existem três algoritmos intercambiáveis:

- `MenorPrecoStrategy` — seleciona o menor valor total em BRL;
- `MenorPrazoStrategy` — seleciona o menor prazo de entrega;
- `CustoBeneficioStrategy` — pondera preço, prazo de entrega e condição de pagamento.

A solicitação define o critério de avaliação e o `CotacaoStrategyResolver` localiza a estratégia adequada sem espalhar condicionais pelo sistema.

### 3. Facade — estrutural

`CompraFacade` funciona como a porta de entrada da camada HTTP. Os controllers não precisam conhecer detalhes de:

- persistência JPA;
- regras e validações de negócio;
- conversão de moedas;
- seleção da Strategy;
- cadeia de aprovação;
- mapeamento de DTOs.

A Facade expõe operações simples e coordena os subsistemas internos.

### 4. Chain of Responsibility — evolução adicional

O fluxo de aprovação foi implementado como evolução própria do desafio:

```text
até R$ 2.000       -> COORDENADOR
até R$ 10.000      -> GERENTE
até R$ 50.000      -> DIRETOR
acima de R$ 50.000 -> DIRETORIA
```

Cada `ApprovalHandler` verifica se possui responsabilidade suficiente para tratar a solicitação ou encaminha a decisão ao próximo elo da cadeia.

## Arquitetura

```text
Electron Desktop
      |
      | IPC seguro
      v
Electron Main
      |
      | HTTP localhost
      v
Controllers REST
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
- JUnit 5
- Spring Boot Test
- MockMvc
- Maven
- Electron
- GitHub Actions

## Aplicação desktop

O cliente Electron inicia automaticamente o backend Spring Boot usando o JRE 21 empacotado com a aplicação.

Principais funcionalidades disponíveis na interface:

- cadastro e consulta de fornecedores;
- criação de solicitações com múltiplos itens;
- edição e exclusão de solicitações em `RASCUNHO`;
- abertura do processo de cotação;
- registro de propostas;
- avaliação por menor preço, menor prazo ou custo-benefício;
- aprovação e rejeição;
- impressão da solicitação;
- geração de PDF A4;
- persistência local do banco de dados.

## Integração de câmbio

As propostas podem ser registradas em `BRL`, `USD` ou `EUR`.

- para BRL, nenhum serviço externo é necessário;
- para USD/EUR, `CambioService` utiliza OpenFeign para consultar a taxa e normalizar a proposta em BRL antes de executar a Strategy.

A integração é opcional: o fluxo principal pode ser demonstrado totalmente em BRL e com banco local.

## Como executar pelo código-fonte

Pré-requisitos:

- JDK 21+
- Maven 3.9+

```bash
mvn clean test
mvn spring-boot:run
```

Swagger:

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

1. cadastrar ou consultar fornecedores;
2. criar uma solicitação;
3. abrir a solicitação para cotação;
4. registrar pelo menos três propostas;
5. executar a avaliação;
6. verificar a proposta selecionada pela Strategy;
7. conferir o nível de aprovação definido pela Chain of Responsibility;
8. aprovar ou rejeitar;
9. imprimir ou gerar o PDF da solicitação.

Existe um roteiro completo em [`docs/ROTEIRO_DEMO.md`](docs/ROTEIRO_DEMO.md) e requisições prontas em [`http/compraflow.http`](http/compraflow.http).

## Regras de negócio demonstradas

- uma solicitação nasce como `RASCUNHO`;
- solicitações em rascunho podem ser editadas ou excluídas;
- cotações só são aceitas após abertura da fase `EM_COTACAO`;
- o mesmo fornecedor não pode cotar duas vezes a mesma solicitação;
- a avaliação exige no mínimo três cotações válidas;
- cotações vencidas não participam da seleção;
- todas as propostas são comparadas em BRL;
- somente o nível hierárquico adequado ou superior pode aprovar;
- impressão e PDF estão disponíveis independentemente do status do processo;
- erros de negócio, validação, recurso inexistente e integração externa possuem respostas HTTP distintas.

## Testes e CI

O projeto contém testes para:

- Singleton clássico;
- cada Strategy;
- Chain of Responsibility;
- escopo Singleton do Spring;
- fluxo da API via MockMvc;
- edição e exclusão de solicitações;
- contratos do desktop para Editar, Excluir, Imprimir e Salvar PDF.

Execute localmente:

```bash
mvn clean test
```

O GitHub Actions também executa os testes e o pipeline de build do aplicativo Windows antes da publicação das releases.

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

desktop
├── main.js
├── preload.js
└── renderer
```

## Originalidade e transparência

Este repositório foi criado para o desafio da DIO utilizando um **domínio próprio de compras e cotações**.

O exemplo Cliente/ViaCEP apresentado no laboratório serviu para o estudo dos conceitos, mas não foi utilizado como domínio desta implementação. Singleton, Strategy e Facade foram reaplicados a um cenário diferente, e Chain of Responsibility foi acrescentado como evolução.

O uso de Inteligência Artificial é declarado de forma transparente: IA generativa foi utilizada como **ferramenta de apoio ao aprendizado e ao ciclo de desenvolvimento**, seguindo a proposta de combinar os conhecimentos técnicos estudados na trilha com ferramentas modernas de desenvolvimento.

## Possíveis evoluções

- autenticação e perfis de usuário;
- anexos de propostas comerciais;
- histórico e auditoria de alterações;
- geração de pedido de compra após aprovação;
- envio de convite de cotação por e-mail;
- persistência em PostgreSQL/MySQL;
- dashboards e relatórios gerenciais.
