package br.ucsal.casadigital.infra;

import br.ucsal.casadigital.dominio.EntidadeBase;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Padrão <b>Repository</b>: o domínio e os serviços enxergam uma "coleção de entidades" e não sabem se
 * os dados estão em memória, em arquivo ou em banco (<b>Dependency Inversion</b> / <b>Protected Variations</b>).
 * Interface genérica: o mesmo contrato serve para matérias, sugestões, itens da UCSAL e usuários.
 */
public interface Repositorio<T extends EntidadeBase<T>> {

    /** Insere (id 0) ou substitui. Devolve a entidade como foi gravada, já com id. */
    T salvar(T entidade);

    Optional<T> porId(long id);

    /** Todas as entidades, em ordem crescente de id. */
    List<T> todos();

    boolean remover(long id);

    default Stream<T> stream() {
        return todos().stream();
    }
}
