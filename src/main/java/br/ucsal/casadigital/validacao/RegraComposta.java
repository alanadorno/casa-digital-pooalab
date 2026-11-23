package br.ucsal.casadigital.validacao;

import java.util.List;

/**
 * Padrão <b>Composite</b>: um conjunto de regras que se comporta como uma única regra, porque também
 * implementa {@link Regra}. Composites podem conter outros composites.
 */
public final class RegraComposta<T> implements Regra<T> {

    private final List<Regra<T>> regras;

    private RegraComposta(List<Regra<T>> regras) {
        this.regras = List.copyOf(regras);
    }

    @SafeVarargs
    public static <T> RegraComposta<T> de(Regra<T>... regras) {
        return new RegraComposta<>(List.of(regras));
    }

    @Override
    public void verificar(T alvo, Erros erros) {
        regras.forEach(r -> r.verificar(alvo, erros));
    }
}
