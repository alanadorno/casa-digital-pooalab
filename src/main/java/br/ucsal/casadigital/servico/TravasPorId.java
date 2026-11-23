package br.ucsal.casadigital.servico;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;

/**
 * Uma trava por entidade ({@link ReentrantLock}). Garante que duas threads não leiam e gravem a mesma
 * sugestão ao mesmo tempo (por exemplo, o reenvio automático de e-mail e a avaliação feita pela equipe),
 * o que causaria a perda de uma das alterações (<i>lost update</i>). Threads que mexem em entidades
 * diferentes não esperam umas pelas outras.
 */
final class TravasPorId {

    private final ConcurrentHashMap<Long, ReentrantLock> travas = new ConcurrentHashMap<>();

    <R> R executar(long id, Supplier<R> acao) {
        ReentrantLock trava = travas.computeIfAbsent(id, k -> new ReentrantLock());
        trava.lock();
        try {
            return acao.get();
        } finally {
            trava.unlock();
        }
    }
}
