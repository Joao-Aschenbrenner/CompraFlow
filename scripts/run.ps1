$ErrorActionPreference = "Stop"
if (-not (Get-Command mvn -ErrorAction SilentlyContinue)) {
    throw "Maven não encontrado. Instale Maven 3.9+ e garanta que 'mvn' esteja no PATH."
}
mvn spring-boot:run
