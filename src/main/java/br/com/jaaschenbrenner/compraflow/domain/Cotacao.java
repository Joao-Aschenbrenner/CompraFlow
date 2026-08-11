package br.com.jaaschenbrenner.compraflow.domain;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import br.com.jaaschenbrenner.compraflow.patterns.singleton.MoneyRoundingPolicy;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "cotacoes", uniqueConstraints = {
        @UniqueConstraint(name = "uk_cotacao_solicitacao_fornecedor", columnNames = {"solicitacao_id", "fornecedor_id"})
})
public class Cotacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "solicitacao_id", nullable = false)
    private SolicitacaoCompra solicitacao;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "fornecedor_id", nullable = false)
    private Fornecedor fornecedor;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal valorProdutos;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal frete;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 3)
    private Moeda moeda;

    @Column(nullable = false)
    private Integer prazoEntregaDias;

    @Column(nullable = false)
    private Integer prazoPagamentoDias;

    @Column(nullable = false)
    private LocalDate validade;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private StatusCotacao status;

    @Column(nullable = false)
    private LocalDateTime criadaEm;

    protected Cotacao() {
    }

    public Cotacao(SolicitacaoCompra solicitacao, Fornecedor fornecedor, BigDecimal valorProdutos,
                   BigDecimal frete, Moeda moeda, Integer prazoEntregaDias,
                   Integer prazoPagamentoDias, LocalDate validade) {
        this.solicitacao = solicitacao;
        this.fornecedor = fornecedor;
        this.valorProdutos = MoneyRoundingPolicy.getInstance().money(valorProdutos);
        this.frete = MoneyRoundingPolicy.getInstance().money(frete);
        this.moeda = moeda;
        this.prazoEntregaDias = prazoEntregaDias;
        this.prazoPagamentoDias = prazoPagamentoDias;
        this.validade = validade;
        this.status = StatusCotacao.RECEBIDA;
        this.criadaEm = LocalDateTime.now();
    }

    public BigDecimal totalOriginal() {
        return MoneyRoundingPolicy.getInstance().money(valorProdutos.add(frete));
    }

    public void selecionar() {
        this.status = StatusCotacao.SELECIONADA;
    }

    public void descartar() {
        this.status = StatusCotacao.NAO_SELECIONADA;
    }

    public Long getId() {
        return id;
    }

    public SolicitacaoCompra getSolicitacao() {
        return solicitacao;
    }

    public Fornecedor getFornecedor() {
        return fornecedor;
    }

    public BigDecimal getValorProdutos() {
        return valorProdutos;
    }

    public BigDecimal getFrete() {
        return frete;
    }

    public Moeda getMoeda() {
        return moeda;
    }

    public Integer getPrazoEntregaDias() {
        return prazoEntregaDias;
    }

    public Integer getPrazoPagamentoDias() {
        return prazoPagamentoDias;
    }

    public LocalDate getValidade() {
        return validade;
    }

    public StatusCotacao getStatus() {
        return status;
    }

    public LocalDateTime getCriadaEm() {
        return criadaEm;
    }
}
