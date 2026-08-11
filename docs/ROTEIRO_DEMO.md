# Roteiro de demonstração

## 1. Subir a aplicação

```powershell
mvn spring-boot:run
```

Abra `http://localhost:8080/swagger-ui.html`.

## 2. Ver fornecedores de demonstração

`GET /api/fornecedores`

A aplicação cria três fornecedores fictícios na inicialização.

## 3. Criar uma solicitação

`POST /api/solicitacoes`

```json
{
  "solicitante": "João",
  "departamento": "Tecnologia",
  "justificativa": "Renovação de monitores da equipe",
  "criterioAvaliacao": "CUSTO_BENEFICIO",
  "itens": [
    {
      "descricao": "Monitor 27 polegadas",
      "quantidade": 4,
      "unidade": "UN",
      "especificacao": "IPS, resolução QHD"
    }
  ]
}
```

Guarde o `id` retornado.

## 4. Abrir para cotação

`POST /api/solicitacoes/{id}/abrir-cotacao`

## 5. Registrar três propostas

Use os IDs dos fornecedores retornados no passo 2. Informe uma validade futura.

Exemplo:

```json
{
  "fornecedorId": 1,
  "valorProdutos": 4000.00,
  "frete": 200.00,
  "moeda": "BRL",
  "prazoEntregaDias": 8,
  "prazoPagamentoDias": 30,
  "validade": "2030-12-31"
}
```

Cadastre outras duas com valores e prazos diferentes.

## 6. Avaliar

`POST /api/solicitacoes/{id}/avaliar`

A resposta mostra:

- Strategy usada;
- fornecedor vencedor;
- valor total;
- prazo;
- justificativa do algoritmo;
- nível de aprovação exigido.

## 7. Aprovar

```json
{
  "nivelAprovador": "GERENTE",
  "observacao": "Compra aprovada dentro do orçamento."
}
```

em `POST /api/solicitacoes/{id}/aprovar`.

Se o nível for menor que o exigido, a API retorna erro de regra de negócio.
