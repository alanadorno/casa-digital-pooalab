package br.ucsal.casadigital.infra;

import br.ucsal.casadigital.dominio.EntidadeBase;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;
import java.util.function.BooleanSupplier;

/**
 * <b>Proxy dinâmico</b> ({@link Proxy}, parte da Reflexão): cria em tempo de execução um objeto que
 * implementa {@link Repositorio} e intercepta as chamadas. Quando a falha simulada está ligada, o método
 * {@code salvar} lança {@link RepositorioIndisponivelException}; nos demais casos repassa ao repositório real.
 *
 * <p>Nenhuma linha do repositório real nem dos serviços muda para suportar o simulador (Open/Closed).
 */
public final class RepositorioComFalhas {

    private RepositorioComFalhas() {
    }

    /**
     * @param somenteInsercoes se verdadeiro, só falha ao inserir (id 0); atualizações continuam funcionando
     */
    @SuppressWarnings("unchecked")
    public static <T extends EntidadeBase<T>> Repositorio<T> envolver(Repositorio<T> alvo, BooleanSupplier falhando,
                                                                      boolean somenteInsercoes) {
        return (Repositorio<T>) Proxy.newProxyInstance(
                Repositorio.class.getClassLoader(),
                new Class<?>[]{Repositorio.class},
                (proxy, metodo, args) -> {
                    boolean ehGravacao = metodo.getName().equals("salvar");
                    if (ehGravacao && falhando.getAsBoolean()
                            && (!somenteInsercoes || ((EntidadeBase<?>) args[0]).id() == 0)) {
                        throw new RepositorioIndisponivelException("Gravação indisponível (falha simulada).");
                    }
                    try {
                        return metodo.invoke(alvo, args);
                    } catch (InvocationTargetException e) {
                        throw e.getCause();
                    }
                });
    }
}
