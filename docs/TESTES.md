# Testes

## Comando

```bash
mvn clean test
```

## Cobertura funcional planejada

- `MoneyRoundingPolicyTest`: Singleton clássico e arredondamento;
- `MenorPrecoStrategyTest`: menor preço;
- `MenorPrazoStrategyTest`: menor prazo;
- `CustoBeneficioStrategyTest`: algoritmo ponderado;
- `ApprovalChainCoreTest`: escalonamento da aprovação;
- `SpringSingletonScopeTest`: escopo singleton do bean no ApplicationContext;
- `CompraFlowWorkflowTest`: fluxo completo via HTTP/MockMvc.

O teste do fluxo usa apenas cotações em BRL, portanto não chama a API externa de câmbio.
