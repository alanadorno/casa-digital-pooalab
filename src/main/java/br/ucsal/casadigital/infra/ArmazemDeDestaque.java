package br.ucsal.casadigital.infra;

import br.ucsal.casadigital.dominio.DestaqueManual;

import java.util.Optional;

/**
 * Onde fica guardado o destaque manual da home (RN05). Interface pequena e específica
 * (<b>Interface Segregation</b>): quem só precisa do destaque não enxerga todo o {@link Banco}.
 */
public interface ArmazemDeDestaque {

    Optional<DestaqueManual> destaque();

    void definirDestaque(DestaqueManual destaque);

    void limparDestaque();
}
