package br.ucsal.casadigital.dominio;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Barramento de eventos (padrão <b>Observer</b> / publish-subscribe).
 *
 * <p>Quem publica não conhece quem escuta (GRASP <b>Low Coupling</b>, e o princípio <b>Open/Closed</b>:
 * dá para acrescentar um novo ouvinte sem alterar o serviço que publica). Estrutura segura para várias
 * threads: {@link ConcurrentHashMap} + {@link CopyOnWriteArrayList}. Uma falha em um ouvinte não impede
 * os outros de receberem o evento.
 */
public class BarramentoEventos {

    private static final Logger LOG = Logger.getLogger(BarramentoEventos.class.getName());

    private final Map<Class<? extends EventoDominio>, List<Consumer<EventoDominio>>> ouvintes = new ConcurrentHashMap<>();

    public <E extends EventoDominio> void inscrever(Class<E> tipo, Consumer<? super E> ouvinte) {
        ouvintes.computeIfAbsent(tipo, t -> new CopyOnWriteArrayList<>())
                .add(evento -> ouvinte.accept(tipo.cast(evento)));
    }

    public void publicar(EventoDominio evento) {
        List<Consumer<EventoDominio>> interessados = ouvintes.getOrDefault(evento.getClass(), List.of());
        for (Consumer<EventoDominio> ouvinte : interessados) {
            try {
                ouvinte.accept(evento);
            } catch (RuntimeException e) {
                LOG.log(Level.WARNING, "Ouvinte falhou ao tratar " + evento.getClass().getSimpleName(), e);
            }
        }
    }

    /** Atalho para um ouvinte que quer receber todos os tipos de evento (por exemplo, a auditoria). */
    public void inscreverEmTodos(Consumer<EventoDominio> ouvinte) {
        for (Class<?> permitido : EventoDominio.class.getPermittedSubclasses()) {
            @SuppressWarnings("unchecked")
            Class<? extends EventoDominio> tipo = (Class<? extends EventoDominio>) permitido;
            inscrever(tipo, ouvinte);
        }
    }
}
