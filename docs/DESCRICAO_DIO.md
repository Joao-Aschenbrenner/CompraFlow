# Texto para colar na entrega da DIO

Desenvolvi o **CompraFlow**, uma API REST para gerenciamento de solicitações de compras e cotações de fornecedores, criada do zero para aplicar na prática os padrões de projeto estudados no desafio.

O sistema permite cadastrar fornecedores, criar solicitações com múltiplos itens, abrir processos de cotação, registrar diferentes propostas, selecionar a melhor cotação conforme um critério de negócio e encaminhar a compra para aprovação.

Apliquei **Singleton** em uma política central de arredondamento monetário e também explorei o escopo singleton padrão dos beans do Spring. O padrão **Strategy** foi utilizado para permitir diferentes algoritmos de escolha de fornecedor: menor preço, menor prazo e melhor custo-benefício. O padrão **Facade** foi aplicado por meio da `CompraFacade`, que concentra a orquestração dos serviços e oferece uma interface simples para os controllers.

Como evolução adicional, implementei **Chain of Responsibility** para definir o nível de aprovação da compra conforme seu valor (coordenador, gerente, diretor ou diretoria).

O projeto utiliza Java, Spring Boot, Spring Web, Spring Data JPA, H2, Bean Validation, OpenFeign e Swagger/OpenAPI. Também foram criados testes automatizados para os padrões e para o fluxo principal da API.

O domínio do projeto é próprio e diferente do exemplo Cliente/ViaCEP apresentado no laboratório. Ferramentas de IA foram utilizadas como apoio durante arquitetura, revisão, testes e documentação, e o código foi estruturado para estudo e evolução.
