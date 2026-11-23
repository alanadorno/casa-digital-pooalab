package br.ucsal.casadigital.infra;

import br.ucsal.casadigital.dominio.EntidadeBase;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.LongSupplier;

/**
 * Implementação de {@link Repositorio} sobre {@link ConcurrentHashMap}.
 *
 * <p>Guarda e devolve <b>cópias</b> (Prototype): quem recebe uma entidade pode alterá-la à vontade
 * e só muda o que está guardado se chamar {@link #salvar}. Isso evita que uma falha no meio de uma operação
 * deixe a matéria "meio publicada" na memória (P3: se a gravação falhar, continua rascunho).
 */
public class RepositorioEmMemoria<T extends EntidadeBase<T>> implements Repositorio<T> {

    private final Map<Long, T> dados = new ConcurrentHashMap<>();
    private final LongSupplier geradorDeIds;
    private final Runnable aposMudanca;

    public RepositorioEmMemoria(LongSupplier geradorDeIds, Runnable aposMudanca) {
        this.geradorDeIds = geradorDeIds;
        this.aposMudanca = aposMudanca;
    }

    @Override
    public T salvar(T entidade) {
        T copia = entidade.copiar();
        if (copia.id() == 0) {
            copia.atribuirId(geradorDeIds.getAsLong());
        }
        dados.put(copia.id(), copia);
        aposMudanca.run();
        return copia.copiar();
    }

    @Override
    public Optional<T> porId(long id) {
        return Optional.ofNullable(dados.get(id)).map(EntidadeBase::copiar);
    }

    @Override
    public List<T> todos() {
        return dados.values().stream()
                .sorted(Comparator.comparingLong(EntidadeBase::id))
                .map(EntidadeBase::copiar)
                .toList();
    }

    @Override
    public boolean remover(long id) {
        boolean removeu = dados.remove(id) != null;
        if (removeu) {
            aposMudanca.run();
        }
        return removeu;
    }

    /** Usado só na carga inicial a partir do arquivo: não dispara a persistência. */
    void carregar(List<T> entidades) {
        entidades.forEach(e -> dados.put(e.id(), e.copiar()));
    }
}
