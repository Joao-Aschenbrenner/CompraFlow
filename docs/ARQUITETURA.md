# Arquitetura do CompraFlow

## Componentes

```mermaid
flowchart TD
    C[Controllers] --> F[CompraFacade]
    F --> FS[FornecedorService]
    F --> SS[SolicitacaoService]
    F --> CS[CotacaoService]
    F --> AS[AvaliacaoCotacaoService]
    AS --> SR[CotacaoStrategyResolver]
    SR --> MP[MenorPrecoStrategy]
    SR --> MPR[MenorPrazoStrategy]
    SR --> CB[CustoBeneficioStrategy]
    AS --> AC[ApprovalChain]
    AS --> CAM[CambioService]
    CAM --> FEIGN[FrankfurterClient / OpenFeign]
    FS --> JPA[(Spring Data JPA / H2)]
    SS --> JPA
    CS --> JPA
```

## Princípios usados

- controllers finos;
- regras de negócio em services/domínio;
- dependências por construtor;
- DTOs separados das entidades JPA;
- enums para estados e critérios;
- validação na borda HTTP;
- exceções específicas para diferentes classes de erro;
- extensão de algoritmos por Strategy.
