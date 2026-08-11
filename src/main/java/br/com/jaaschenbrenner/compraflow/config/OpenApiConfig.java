package br.com.jaaschenbrenner.compraflow.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    OpenAPI compraFlowOpenApi() {
        return new OpenAPI().info(new Info()
                .title("CompraFlow API")
                .version("1.0.0")
                .description("API autoral para solicitações de compras, cotações, seleção de fornecedores e aprovações, aplicando Singleton, Strategy, Facade e Chain of Responsibility.")
                .contact(new Contact().name("Projeto de estudo DIO")));
    }
}
