package br.ucsal.casadigital.infra;

/** A gravação não pôde ser concluída (P3 e P5: o fluxo trata essa falha explicitamente). */
public class RepositorioIndisponivelException extends RuntimeException {

    public RepositorioIndisponivelException(String mensagem) {
        super(mensagem);
    }
}
