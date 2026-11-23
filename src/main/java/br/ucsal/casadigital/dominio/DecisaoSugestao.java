package br.ucsal.casadigital.dominio;

/** O que a equipe decide ao avaliar uma sugestão: o tema entra na pauta (volta ao P1) ou é arquivado. */
public enum DecisaoSugestao {
    APROVEITADA("aproveitada"),
    ARQUIVADA("arquivada");

    private final String rotulo;

    DecisaoSugestao(String rotulo) {
        this.rotulo = rotulo;
    }

    public String rotulo() {
        return rotulo;
    }
}
