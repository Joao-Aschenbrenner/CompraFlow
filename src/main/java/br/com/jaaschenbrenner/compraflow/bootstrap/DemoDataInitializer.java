package br.com.jaaschenbrenner.compraflow.bootstrap;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import br.com.jaaschenbrenner.compraflow.repository.FornecedorRepository;
import br.com.jaaschenbrenner.compraflow.service.FornecedorService;

@Component
public class DemoDataInitializer implements CommandLineRunner {

    private final FornecedorRepository repository;
    private final FornecedorService service;

    public DemoDataInitializer(FornecedorRepository repository, FornecedorService service) {
        this.repository = repository;
        this.service = service;
    }

    @Override
    public void run(String... args) {
        if (repository.count() > 0) {
            return;
        }
        service.criar("Office Prime Suprimentos", "11111111000111", "vendas@officeprime.example");
        service.criar("Tech Supply Brasil", "22222222000122", "comercial@techsupply.example");
        service.criar("Entrega Rápida Distribuidora", "33333333000133", "cotacao@entregarapida.example");
    }
}
