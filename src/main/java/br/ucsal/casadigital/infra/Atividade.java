package br.ucsal.casadigital.infra;

import br.ucsal.casadigital.dominio.EventoDominio;

import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * Registro de atividade recente, mostrado no painel. É um <b>ouvinte</b> (Observer) de todos os
 * eventos do sistema: os serviços não sabem que ele existe.
 */
public final class Atividade {

    private static final int LIMITE = 200;

    private final ConcurrentLinkedDeque<EventoDominio> eventos = new ConcurrentLinkedDeque<>();

    public void registrar(EventoDominio evento) {
        eventos.addFirst(evento);
        while (eventos.size() > LIMITE) {
            eventos.pollLast();
        }
    }

    /** Mais recentes primeiro. */
    public List<EventoDominio> recentes(int quantidade) {
        return eventos.stream().limit(quantidade).toList();
    }
}
