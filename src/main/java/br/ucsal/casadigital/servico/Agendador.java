package br.ucsal.casadigital.servico;

import java.time.Duration;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Tarefa periódica em segundo plano ({@link ScheduledExecutorService}) que cumpre duas regras de negócio
 * "sem ação da equipe":
 * <ul>
 *   <li>RN09: reenviar e-mails de sugestões marcadas como "e-mail pendente";</li>
 *   <li>RN14: encerrar itens "O que está rolando" cuja data de fim passou.</li>
 * </ul>
 */
public final class Agendador implements AutoCloseable {

    private static final Logger LOG = Logger.getLogger(Agendador.class.getName());

    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor(tarefa -> {
        Thread thread = new Thread(tarefa, "agendador-casa-digital");
        thread.setDaemon(true);
        return thread;
    });

    public Agendador(Sugestoes sugestoes, Ucsal ucsal, Duration intervalo) {
        executor.scheduleWithFixedDelay(() -> {
            try {
                int reenviados = sugestoes.reenviarPendentes();
                int encerrados = ucsal.encerrarVencidos();
                if (reenviados + encerrados > 0) {
                    LOG.info("Agendador: %d reenvio(s) de e-mail, %d item(ns) encerrado(s)".formatted(reenviados, encerrados));
                }
            } catch (RuntimeException e) {
                LOG.log(Level.WARNING, "Falha no agendador (tentará de novo no próximo ciclo)", e);
            }
        }, 1, Math.max(1, intervalo.toSeconds()), TimeUnit.SECONDS);
    }

    @Override
    public void close() {
        executor.shutdownNow();
    }
}
