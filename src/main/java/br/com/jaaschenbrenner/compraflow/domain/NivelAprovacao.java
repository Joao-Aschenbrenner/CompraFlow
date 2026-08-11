package br.com.jaaschenbrenner.compraflow.domain;

public enum NivelAprovacao {
    COORDENADOR(1),
    GERENTE(2),
    DIRETOR(3),
    DIRETORIA(4);

    private final int peso;

    NivelAprovacao(int peso) {
        this.peso = peso;
    }

    public boolean podeAprovar(NivelAprovacao exigido) {
        return this.peso >= exigido.peso;
    }
}
