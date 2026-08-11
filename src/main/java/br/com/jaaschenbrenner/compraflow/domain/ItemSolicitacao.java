package br.com.jaaschenbrenner.compraflow.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "itens_solicitacao")
public class ItemSolicitacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "solicitacao_id", nullable = false)
    private SolicitacaoCompra solicitacao;

    @Column(nullable = false, length = 180)
    private String descricao;

    @Column(nullable = false)
    private Integer quantidade;

    @Column(nullable = false, length = 20)
    private String unidade;

    @Column(length = 1000)
    private String especificacao;

    protected ItemSolicitacao() {
    }

    public ItemSolicitacao(String descricao, Integer quantidade, String unidade, String especificacao) {
        this.descricao = descricao;
        this.quantidade = quantidade;
        this.unidade = unidade;
        this.especificacao = especificacao;
    }

    void vincular(SolicitacaoCompra solicitacao) {
        this.solicitacao = solicitacao;
    }

    public Long getId() {
        return id;
    }

    public String getDescricao() {
        return descricao;
    }

    public Integer getQuantidade() {
        return quantidade;
    }

    public String getUnidade() {
        return unidade;
    }

    public String getEspecificacao() {
        return especificacao;
    }
}
