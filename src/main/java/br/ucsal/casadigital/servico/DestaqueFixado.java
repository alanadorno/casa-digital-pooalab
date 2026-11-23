package br.ucsal.casadigital.servico;

import br.ucsal.casadigital.dominio.DestaqueManual;
import br.ucsal.casadigital.dominio.Materia;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * RN05 (manual): a matéria fixada vira a principal e as secundárias são as três mais recentes
 * <i>entre as demais</i>. Por isso, uma matéria nova publicada com destaque manual ativo não o substitui:
 * entra como a primeira das secundárias.
 */
public final class DestaqueFixado implements PoliticaDestaque {

    private final DestaqueManual destaque;

    public DestaqueFixado(DestaqueManual destaque) {
        this.destaque = destaque;
    }

    @Override
    public Home compor(List<Materia> publicadasRecentesPrimeiro, Instant agora) {
        Optional<Materia> fixada = publicadasRecentesPrimeiro.stream()
                .filter(m -> m.id() == destaque.materiaId())
                .findFirst();
        if (fixada.isEmpty()) {
            return new DestaqueAutomatico().compor(publicadasRecentesPrimeiro, agora);
        }
        List<Materia> secundarias = publicadasRecentesPrimeiro.stream()
                .filter(m -> m.id() != destaque.materiaId())
                .limit(Home.SECUNDARIAS)
                .toList();
        return new Home(fixada, secundarias, Optional.of(destaque));
    }
}
