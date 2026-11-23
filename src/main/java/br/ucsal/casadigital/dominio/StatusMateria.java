package br.ucsal.casadigital.dominio;

/**
 * Estados de uma matéria e as transições permitidas entre eles (máquina de estados em enum).
 * RN02: a matéria só vira PUBLICADA depois que a gravação termina com sucesso.
 */
public enum StatusMateria {
    RASCUNHO("rascunho"),
    EM_REVISAO("em revisão"),
    PUBLICADA("publicada");

    private final String rotulo;

    StatusMateria(String rotulo) {
        this.rotulo = rotulo;
    }

    public String rotulo() {
        return rotulo;
    }

    public boolean podeIrPara(StatusMateria destino) {
        return switch (this) {
            case RASCUNHO -> destino == EM_REVISAO || destino == PUBLICADA;
            case EM_REVISAO -> destino == RASCUNHO || destino == PUBLICADA;
            case PUBLICADA -> destino == RASCUNHO;
        };
    }
}
