package br.ucsal.casadigital.infra;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.LongAdder;

/**
 * <b>Proxy dinâmico</b> de medição: envolve qualquer serviço (por interface) e conta quantas vezes cada
 * método foi chamado e quanto tempo levou. É um exemplo de aspecto transversal (logging/telemetria)
 * feito com Reflexão ({@link Proxy#newProxyInstance}), sem repetir código em cada serviço.
 */
public final class MedidorDeChamadas {

    private record Estatistica(LongAdder chamadas, LongAdder nanos) {
        Estatistica() {
            this(new LongAdder(), new LongAdder());
        }
    }

    public record Linha(String metodo, long chamadas, double milissegundosMedios) {
    }

    private final Map<String, Estatistica> estatisticas = new ConcurrentHashMap<>();

    @SuppressWarnings("unchecked")
    public <I> I envolver(Class<I> interfaceDoServico, I alvo) {
        return (I) Proxy.newProxyInstance(interfaceDoServico.getClassLoader(), new Class<?>[]{interfaceDoServico},
                (proxy, metodo, args) -> {
                    long inicio = System.nanoTime();
                    try {
                        return metodo.invoke(alvo, args);
                    } catch (InvocationTargetException e) {
                        throw e.getCause();
                    } finally {
                        Estatistica e = estatisticas.computeIfAbsent(
                                interfaceDoServico.getSimpleName() + "." + metodo.getName(), k -> new Estatistica());
                        e.chamadas().increment();
                        e.nanos().add(System.nanoTime() - inicio);
                    }
                });
    }

    public List<Linha> resumo() {
        return estatisticas.entrySet().stream()
                .map(e -> new Linha(e.getKey(), e.getValue().chamadas().sum(),
                        e.getValue().nanos().sum() / 1_000_000.0 / Math.max(1, e.getValue().chamadas().sum())))
                .sorted(java.util.Comparator.comparing(Linha::metodo))
                .toList();
    }
}
