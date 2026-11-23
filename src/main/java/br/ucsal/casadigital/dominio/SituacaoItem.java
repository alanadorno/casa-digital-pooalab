package br.ucsal.casadigital.dominio;

/** RN14: situação de um item da seção UCSAL, calculada a partir das datas e de "hoje". */
public enum SituacaoItem {
    AGENDADO("agendado"),
    NO_AR("no ar"),
    ENCERRADO("encerrado");

    private final String rotulo;

    SituacaoItem(String rotulo) {
        this.rotulo = rotulo;
    }

    public String rotulo() {
        return rotulo;
    }
}
