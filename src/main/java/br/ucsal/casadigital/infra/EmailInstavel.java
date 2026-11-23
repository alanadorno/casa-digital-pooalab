package br.ucsal.casadigital.infra;

import java.util.function.BooleanSupplier;

/**
 * Padrão <b>Decorator</b>: "embrulha" qualquer {@link GatewayEmail} e, quando a falha simulada está ligada,
 * recusa o envio. Implementa a mesma interface do objeto embrulhado, então quem usa não percebe a diferença.
 */
public class EmailInstavel implements GatewayEmail {

    private final GatewayEmail real;
    private final BooleanSupplier falhando;

    public EmailInstavel(GatewayEmail real, BooleanSupplier falhando) {
        this.real = real;
        this.falhando = falhando;
    }

    @Override
    public void enviar(Email email) throws EmailException {
        if (falhando.getAsBoolean()) {
            throw new EmailException("Serviço de e-mail indisponível (falha simulada).");
        }
        real.enviar(email);
    }
}
