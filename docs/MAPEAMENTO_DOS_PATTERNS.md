# Mapeamento dos Design Patterns

| Padrão | Categoria | Onde está | Problema resolvido |
|---|---|---|---|
| Singleton | Criacional | `MoneyRoundingPolicy` | manter uma única política de arredondamento |
| Singleton via Spring | Criacional / IoC | `PoliticasCompraProperties` | centralizar regras configuráveis em um bean único |
| Strategy | Comportamental | `SelecionarCotacaoStrategy` e implementações | trocar o critério de escolha de proposta |
| Facade | Estrutural | `CompraFacade` | esconder a coordenação dos subsistemas dos controllers |
| Chain of Responsibility | Comportamental | `ApprovalHandler` e cadeia | escalonar a aprovação de acordo com o valor |

## Strategy em detalhe

```text
SelecionarCotacaoStrategy
       |
       +-- MenorPrecoStrategy
       +-- MenorPrazoStrategy
       +-- CustoBeneficioStrategy
```

Adicionar um quarto critério exige criar uma nova implementação e acrescentar o valor ao enum. O serviço de avaliação continua dependendo do contrato.

## Facade em detalhe

```text
SolicitacaoController ----+
                          |
FornecedorController -----+--> CompraFacade
                                  |
                                  +--> FornecedorService
                                  +--> SolicitacaoService
                                  +--> CotacaoService
                                  +--> AvaliacaoCotacaoService
```

## Chain em detalhe

```text
valor da compra
      |
      v
Coordenador (<= 2 mil)
      |
      v
Gerente (<= 10 mil)
      |
      v
Diretor (<= 50 mil)
      |
      v
Diretoria (> 50 mil)
```
