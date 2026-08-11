package br.com.jaaschenbrenner.compraflow.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "fornecedores")
public class Fornecedor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 160)
    private String razaoSocial;

    @Column(nullable = false, unique = true, length = 14)
    private String cnpj;

    @Column(nullable = false, length = 180)
    private String email;

    @Column(nullable = false)
    private boolean ativo = true;

    protected Fornecedor() {
    }

    public Fornecedor(String razaoSocial, String cnpj, String email) {
        this.razaoSocial = razaoSocial;
        this.cnpj = cnpj;
        this.email = email;
    }

    public Long getId() {
        return id;
    }

    public String getRazaoSocial() {
        return razaoSocial;
    }

    public String getCnpj() {
        return cnpj;
    }

    public String getEmail() {
        return email;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public void desativar() {
        this.ativo = false;
    }
}
