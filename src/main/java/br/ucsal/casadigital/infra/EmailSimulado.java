package br.ucsal.casadigital.infra;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Caixa de saída de e-mails do MVP: "envia" guardando a mensagem em memória e, se houver pasta,
 * também em arquivo texto ({@code data/emails/}). Não envia nada pela internet (limitação declarada),
 * mas respeita o mesmo contrato {@link GatewayEmail}.
 */
public class EmailSimulado implements GatewayEmail {

    private final List<Email.Enviado> enviados = new CopyOnWriteArrayList<>();
    private final AtomicInteger contador = new AtomicInteger();
    private final Path pasta;
    private final Clock relogio;

    public EmailSimulado(Path pasta, Clock relogio) {
        this.pasta = pasta;
        this.relogio = relogio;
    }

    @Override
    public void enviar(Email email) throws EmailException {
        Email.Enviado registro = new Email.Enviado(relogio.instant(), email);
        enviados.add(registro);
        if (pasta != null) {
            try {
                Files.createDirectories(pasta);
                String nome = "email-%04d.txt".formatted(contador.incrementAndGet());
                String conteudo = "Para: %s%nResponder para: %s%nAssunto: %s%nEnviado em: %s%n%n%s%n"
                        .formatted(email.para(), email.responderPara(), email.assunto(), registro.quando(), email.corpo());
                Files.writeString(pasta.resolve(nome), conteudo, StandardCharsets.UTF_8);
            } catch (IOException e) {
                throw new EmailException("Não foi possível gravar o e-mail: " + e.getMessage());
            }
        }
    }

    /** Mais recentes primeiro ({@code reversed()}, da SequencedCollection do Java 21). */
    public List<Email.Enviado> enviados() {
        return List.copyOf(enviados.reversed());
    }
}
