package br.ucsal.casadigital.infra;

import java.time.Instant;

/** Mensagem de e-mail (imutável, por isso um {@code record}). */
public record Email(String para, String assunto, String corpo, String responderPara) {

    /** E-mail já enviado, com a hora do envio (para a caixa de saída do painel). */
    public record Enviado(Instant quando, Email email) {
    }
}
