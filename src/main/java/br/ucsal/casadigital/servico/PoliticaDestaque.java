package br.ucsal.casadigital.servico;

import br.ucsal.casadigital.dominio.Materia;

import java.time.Instant;
import java.util.List;

/**
 * Padrão <b>Strategy</b> (e <b>Polymorphism</b> do GRASP): cada política decide como compor a home.
 * Hoje há o destaque automático e o manual (RN05); uma terceira política (por exemplo, "mais assistidas")
 * seria uma nova classe, sem alterar as existentes (Open/Closed).
 */
public interface PoliticaDestaque {

    /** @param publicadasRecentesPrimeiro matérias publicadas, da mais recente para a mais antiga */
    Home compor(List<Materia> publicadasRecentesPrimeiro, Instant agora);
}
