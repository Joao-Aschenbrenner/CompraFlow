package br.com.jaaschenbrenner.compraflow.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import br.com.jaaschenbrenner.compraflow.exception.RegraNegocioException;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;

@Entity
@Table(name = "solicitacoes_compra")
public class SolicitacaoCompra {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 32)
    private String codigo;

    @Column(nullable = false, length = 120)
    private String solicitante;

    @Column(nullable = false, length = 100)
    private String departamento;

    @Column(nullable = false, length = 1000)
    private String justificativa;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private StatusSolicitacao status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private CriterioCotacao criterioAvaliacao;

    @Column(nullable = false)
    private LocalDateTime criadaEm;

    @Column(name = "cotacao_selecionada_id")
    private Long cotacaoSelecionadaId;

    @Column(precision = 19, scale = 2)
    private BigDecimal valorSelecionadoBrl;

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private NivelAprovacao nivelAprovacaoExigido;

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private NivelAprovacao nivelAprovador;

    @Column(length = 500)
    private String observacaoDecisao;

    private LocalDateTime decididaEm;

    @OneToMany(mappedBy = "solicitacao", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @OrderBy("id ASC")
    private List<ItemSolicitacao> itens = new ArrayList<>();

    protected SolicitacaoCompra() {
    }

    public SolicitacaoCompra(String codigo, String solicitante, String departamento,
                             String justificativa, CriterioCotacao criterioAvaliacao) {
        this.codigo = codigo;
        this.solicitante = solicitante;
        this.departamento = departamento;
        this.justificativa = justificativa;
        this.criterioAvaliacao = criterioAvaliacao;
        this.status = StatusSolicitacao.RASCUNHO;
        this.criadaEm = LocalDateTime.now();
    }

    public void adicionarItem(ItemSolicitacao item) {
        item.vincular(this);
        this.itens.add(item);
    }

    public void atualizarRascunho(String solicitante, String departamento, String justificativa,
                                  CriterioCotacao criterioAvaliacao, List<ItemSolicitacao> novosItens) {
        garantirRascunho("editar");
        this.solicitante = solicitante;
        this.departamento = departamento;
        this.justificativa = justificativa;
        this.criterioAvaliacao = criterioAvaliacao;
        this.itens.clear();
        novosItens.forEach(this::adicionarItem);
    }

    public void validarExclusao() {
        garantirRascunho("excluir");
    }

    private void garantirRascunho(String operacao) {
        if (status != StatusSolicitacao.RASCUNHO) {
            throw new RegraNegocioException("Somente solicitações em RASCUNHO podem ser " + operacao + "adas.");
        }
    }

    public void abrirCotacao() {
        if (status != StatusSolicitacao.RASCUNHO) {
            throw new RegraNegocioException("Somente solicitações em RASCUNHO podem ser abertas para cotação.");
        }
        status = StatusSolicitacao.EM_COTACAO;
    }

    public void aguardarAprovacao(Long cotacaoId, BigDecimal totalBrl, NivelAprovacao nivel) {
        if (status != StatusSolicitacao.EM_COTACAO) {
            throw new RegraNegocioException("A solicitação precisa estar EM_COTACAO para ser avaliada.");
        }
        this.cotacaoSelecionadaId = cotacaoId;
        this.valorSelecionadoBrl = totalBrl;
        this.nivelAprovacaoExigido = nivel;
        this.status = StatusSolicitacao.AGUARDANDO_APROVACAO;
    }

    public void aprovar(NivelAprovacao nivelAprovador, String observacao) {
        if (status != StatusSolicitacao.AGUARDANDO_APROVACAO) {
            throw new RegraNegocioException("A solicitação não está aguardando aprovação.");
        }
        if (nivelAprovacaoExigido == null || !nivelAprovador.podeAprovar(nivelAprovacaoExigido)) {
            throw new RegraNegocioException("Nível de aprovação insuficiente. Exigido: " + nivelAprovacaoExigido);
        }
        this.nivelAprovador = nivelAprovador;
        this.observacaoDecisao = observacao;
        this.decididaEm = LocalDateTime.now();
        this.status = StatusSolicitacao.APROVADA;
    }

    public void rejeitar(String motivo) {
        if (status != StatusSolicitacao.AGUARDANDO_APROVACAO) {
            throw new RegraNegocioException("A solicitação não está aguardando aprovação.");
        }
        this.observacaoDecisao = motivo;
        this.decididaEm = LocalDateTime.now();
        this.status = StatusSolicitacao.REJEITADA;
    }

    public Long getId() { return id; }
    public String getCodigo() { return codigo; }
    public String getSolicitante() { return solicitante; }
    public String getDepartamento() { return departamento; }
    public String getJustificativa() { return justificativa; }
    public StatusSolicitacao getStatus() { return status; }
    public CriterioCotacao getCriterioAvaliacao() { return criterioAvaliacao; }
    public LocalDateTime getCriadaEm() { return criadaEm; }
    public Long getCotacaoSelecionadaId() { return cotacaoSelecionadaId; }
    public BigDecimal getValorSelecionadoBrl() { return valorSelecionadoBrl; }
    public NivelAprovacao getNivelAprovacaoExigido() { return nivelAprovacaoExigido; }
    public NivelAprovacao getNivelAprovador() { return nivelAprovador; }
    public String getObservacaoDecisao() { return observacaoDecisao; }
    public LocalDateTime getDecididaEm() { return decididaEm; }
    public List<ItemSolicitacao> getItens() { return Collections.unmodifiableList(itens); }
}
