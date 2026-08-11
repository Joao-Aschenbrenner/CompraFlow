# Guia de estudo — CompraFlow

## 1. O problema que o projeto resolve

Em muitas empresas, uma área precisa solicitar um produto ou serviço e o setor de compras busca propostas de diferentes fornecedores. A decisão não é sempre “o menor preço”: uma compra urgente pode privilegiar prazo de entrega, enquanto outra pode ponderar preço, prazo e condição de pagamento.

O CompraFlow transforma esse cenário em uma API pequena para praticar orientação a objetos e Design Patterns.

## 2. Fluxo do domínio

```text
RASCUNHO
   |
   | abrir cotação
   v
EM_COTACAO
   |
   | registrar >= 3 propostas
   | avaliar com Strategy
   v
AGUARDANDO_APROVACAO
   |                         |
   | aprovar                 | rejeitar
   v                         v
APROVADA                  REJEITADA
```

## 3. Singleton

### Implementação clássica

`MoneyRoundingPolicy` possui:

- construtor privado;
- classe interna estática `Holder`;
- método público `getInstance()`;
- única instância compartilhada.

A classe evita espalhar regras diferentes de arredondamento pelo código.

### No Spring

`PoliticasCompraProperties` é um `@Component`. O Spring administra sua criação e, no escopo padrão, reutiliza a mesma instância. O teste `SpringSingletonScopeTest` pede o bean duas vezes ao `ApplicationContext` e verifica que é o mesmo objeto.

## 4. Strategy

Problema: precisamos selecionar a melhor cotação, mas “melhor” pode ter significados diferentes.

Contrato:

```java
public interface SelecionarCotacaoStrategy {
    CriterioCotacao criterio();
    DecisaoCotacao selecionar(List<OfertaCotacao> ofertas);
}
```

Implementações:

- `MenorPrecoStrategy`;
- `MenorPrazoStrategy`;
- `CustoBeneficioStrategy`.

O `CotacaoStrategyResolver` recebe todas as implementações injetadas pelo Spring e cria um mapa por `CriterioCotacao`. Quando aparece um novo algoritmo, basta criar outra classe que implemente a interface.

Isso reduz acoplamento e respeita o princípio aberto/fechado: o comportamento pode ser estendido sem reescrever o serviço de avaliação.

## 5. Facade

Sem Facade, o controller poderia precisar conhecer `FornecedorService`, `SolicitacaoService`, `CotacaoService`, `AvaliacaoCotacaoService`, mapper e integrações.

Com `CompraFacade`, ele conhece uma única interface de alto nível:

```text
Controller -> CompraFacade -> subsistemas
```

O objetivo não é esconder toda classe do projeto, mas oferecer uma porta de entrada coesa para o caso de uso.

## 6. Chain of Responsibility

É um padrão adicional.

A aprovação de uma compra depende do valor. Em vez de uma longa sequência de `if/else`, os handlers ficam encadeados:

```text
Coordenador -> Gerente -> Diretor -> Diretoria
```

Cada handler verifica o limite. Se não puder aprovar, encaminha para o próximo.

## 7. JPA e H2

As entidades são:

- `Fornecedor`;
- `SolicitacaoCompra`;
- `ItemSolicitacao`;
- `Cotacao`.

Os repositories herdam de `JpaRepository`, então operações comuns de persistência são fornecidas pelo Spring Data.

O H2 é usado em memória para facilitar a execução do desafio sem instalar um servidor de banco.

## 8. OpenFeign

`FrankfurterClient` demonstra um cliente HTTP declarativo. Ele só é acionado quando uma cotação usa moeda diferente de BRL. Antes de comparar propostas, o `CambioService` normaliza tudo em reais.

O fluxo principal de teste usa somente BRL e, portanto, não depende de internet.

## 9. Tratamento de erros

`GlobalExceptionHandler` transforma exceções em respostas HTTP:

- recurso inexistente -> 404;
- validação -> 400;
- regra de negócio -> 422;
- falha externa -> 503.

Isso evita deixar stack traces ou respostas genéricas para quem consome a API.

## 10. Como explicar em uma entrevista

Uma resposta curta:

> “Criei uma API de compras em Spring Boot. Usei Strategy para trocar o algoritmo de escolha de fornecedor, Facade para expor um fluxo simples de compras e Singleton tanto de forma clássica quanto pelo container do Spring. Como evolução, usei Chain of Responsibility para escalonar aprovação conforme o valor. JPA/H2 cuidam da persistência e OpenFeign demonstra integração externa.”
