package br.ucsal.casadigital.servico;

import br.ucsal.casadigital.dominio.Editoria;
import br.ucsal.casadigital.dominio.Materia;

import java.util.Optional;

/** Processo P4: o visitante consulta o conteúdo, sem cadastro (RN07). */
public interface Consulta {

    Home home();

    /** RN11: 6 por página, da mais recente para a mais antiga. */
    Pagina<Materia> editoria(Editoria editoria, int pagina);

    Optional<Materia> materia(long id);

    int totalDeMaterias();
}
