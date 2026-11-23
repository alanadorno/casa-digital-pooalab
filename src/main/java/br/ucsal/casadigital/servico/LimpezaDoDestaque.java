package br.ucsal.casadigital.servico;

import br.ucsal.casadigital.dominio.BarramentoEventos;
import br.ucsal.casadigital.dominio.EventoDominio.DestaqueRemovido;
import br.ucsal.casadigital.dominio.EventoDominio.MateriaDespublicada;
import br.ucsal.casadigital.infra.ArmazemDeDestaque;

/**
 * Ouvinte (Observer) que cumpre a parte final da RN05: se a matéria fixada for despublicada, a home volta
 * ao destaque automático. O serviço de publicação não precisa saber que esta regra existe.
 */
public final class LimpezaDoDestaque {

    public LimpezaDoDestaque(BarramentoEventos eventos, ArmazemDeDestaque armazem) {
        eventos.inscrever(MateriaDespublicada.class, e -> armazem.destaque()
                .filter(d -> d.materiaId() == e.materiaId())
                .ifPresent(d -> {
                    armazem.limparDestaque();
                    eventos.publicar(new DestaqueRemovido(e.quando(), "a matéria fixada foi despublicada"));
                }));
    }
}
