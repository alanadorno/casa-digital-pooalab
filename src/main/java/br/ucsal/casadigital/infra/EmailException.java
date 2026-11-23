package br.ucsal.casadigital.infra;

/** O serviço de e-mail não conseguiu enviar a mensagem. */
public class EmailException extends Exception {

    public EmailException(String mensagem) {
        super(mensagem);
    }
}
