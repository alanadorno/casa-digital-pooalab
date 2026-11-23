package br.ucsal.casadigital.servico;

import br.ucsal.casadigital.dominio.DestaqueManual;
import br.ucsal.casadigital.dominio.Materia;

import java.util.List;
import java.util.Optional;

/** Composição da home (RN05): uma matéria principal e até três secundárias. */
public record Home(Optional<Materia> principal, List<Materia> secundarias, Optional<DestaqueManual> destaqueManual) {

    public static final int SECUNDARIAS = 3;
}
