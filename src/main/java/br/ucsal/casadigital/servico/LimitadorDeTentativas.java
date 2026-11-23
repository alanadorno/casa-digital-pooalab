package br.ucsal.casadigital.servico;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.ConcurrentHashMap;

/** Janela deslizante: no máximo N falhas por chave em um intervalo. Protege o login contra tentativa e erro. */
final class LimitadorDeTentativas {

    private final ConcurrentHashMap<String, Deque<Instant>> falhas = new ConcurrentHashMap<>();
    private final int maximo;
    private final Duration janela;

    LimitadorDeTentativas(int maximo, Duration janela) {
        this.maximo = maximo;
        this.janela = janela;
    }

    boolean bloqueado(String chave, Instant agora) {
        Deque<Instant> fila = falhas.computeIfAbsent(chave, k -> new ArrayDeque<>());
        synchronized (fila) {
            while (!fila.isEmpty() && fila.peekFirst().isBefore(agora.minus(janela))) {
                fila.pollFirst();
            }
            return fila.size() >= maximo;
        }
    }

    void registrarFalha(String chave, Instant agora) {
        Deque<Instant> fila = falhas.computeIfAbsent(chave, k -> new ArrayDeque<>());
        synchronized (fila) {
            fila.addLast(agora);
        }
    }

    void limpar(String chave) {
        falhas.remove(chave);
    }
}
