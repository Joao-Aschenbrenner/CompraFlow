package br.com.jaaschenbrenner.compraflow;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class CompraFlowApplication {
    public static void main(String[] args) {
        SpringApplication.run(CompraFlowApplication.class, args);
    }
}
