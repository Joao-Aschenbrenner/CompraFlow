# Validação realizada antes do empacotamento

Data: 11/08/2026

## O que foi validado neste ambiente

- `pom.xml` analisado como XML válido;
- `application.yml` analisado como YAML válido;
- caminhos dos arquivos Java conferidos contra seus `package` declarations;
- balanceamento estrutural básico dos arquivos Java conferido;
- fontes de `src/main/java` compiladas contra stubs locais das APIs externas para detectar erros de sintaxe e inconsistências entre as próprias classes do projeto;
- fontes de `src/test/java` também passaram pela mesma validação sintática com stubs de teste;
- núcleo Java de Design Patterns compilado com JDK 21 real;
- smoke test executado para Singleton, Menor Preço, Menor Prazo, Custo-Benefício e Chain of Responsibility: **PASS**.

## Limitação do ambiente

O ambiente usado para gerar o pacote possui JDK 21, porém não possui Maven instalado e não conseguiu baixar dependências externas pelo terminal. Por isso, o build Maven real com Spring Boot/H2/OpenFeign não foi executado aqui.

Antes de entregar na DIO, rode em sua máquina:

```powershell
mvn clean test
```

Depois:

```powershell
mvn spring-boot:run
```

E valide o fluxo em:

```text
http://localhost:8080/swagger-ui.html
```

O workflow `.github/workflows/ci.yml` também executa `mvn clean test` automaticamente quando o projeto for publicado no GitHub.
