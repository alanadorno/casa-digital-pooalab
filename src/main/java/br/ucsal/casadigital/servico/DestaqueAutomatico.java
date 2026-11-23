package br.ucsal.casadigital.servico;

import br.ucsal.casadigital.dominio.Materia;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/** RN05 (padrão): a principal é a matéria publicada mais recentemente e as secundárias são as três seguintes. */
public final class DestaqueAutomatico implements PoliticaDestaque {

    @Override
    public Home compor(List<Materia> publicadasRecentesPrimeiro, Instant agora) {
        if (publicadasRecentesPrimeiro.isEmpty()) {
            return new Home(Optional.empty(), List.of(), Optional.empty());
        }
        List<Materia> secundarias = publicadasRecentesPrimeiro.stream().skip(1).limit(Home.SECUNDARIAS).toList();
        return new Home(Optional.of(publicadasRecentesPrimeiro.getFirst()), secundarias, Optional.empty());
    }
}
