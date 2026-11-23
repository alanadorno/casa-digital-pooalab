package br.ucsal.casadigital.infra;

/**
 * Porta de saída para o envio de e-mails. Os serviços dependem só desta interface (<b>Dependency
 * Inversion</b>): hoje a implementação é {@link EmailSimulado}; amanhã pode ser um adaptador (padrão
 * <b>Adapter</b>) para SMTP ou para um serviço como o Resend, sem mudar nenhuma regra de negócio.
 */
@FunctionalInterface
public interface GatewayEmail {

    void enviar(Email email) throws EmailException;
}
