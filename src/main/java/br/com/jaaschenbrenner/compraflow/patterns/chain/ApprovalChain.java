package br.com.jaaschenbrenner.compraflow.patterns.chain;

import java.math.BigDecimal;

import org.springframework.stereotype.Component;

import br.com.jaaschenbrenner.compraflow.config.PoliticasCompraProperties;
import br.com.jaaschenbrenner.compraflow.domain.NivelAprovacao;

@Component
public class ApprovalChain {

    private final ApprovalHandler first;

    public ApprovalChain(PoliticasCompraProperties properties) {
        ApprovalHandler diretoria = new DiretoriaApprovalHandler();
        ApprovalHandler diretor = new DiretorApprovalHandler(properties.getApproval().getDirectorLimit(), diretoria);
        ApprovalHandler gerente = new GerenteApprovalHandler(properties.getApproval().getManagerLimit(), diretor);
        this.first = new CoordenadorApprovalHandler(properties.getApproval().getCoordinatorLimit(), gerente);
    }

    public NivelAprovacao resolver(BigDecimal valorTotalBrl) {
        return first.resolver(valorTotalBrl);
    }
}
