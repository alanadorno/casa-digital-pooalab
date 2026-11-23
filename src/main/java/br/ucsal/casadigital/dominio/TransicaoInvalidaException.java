package br.ucsal.casadigital.dominio;

/** Lançada quando alguém tenta uma mudança de estado que as regras de negócio não permitem. */
public class TransicaoInvalidaException extends RuntimeException {

    public TransicaoInvalidaException(String mensagem) {
        super(mensagem);
    }
}
