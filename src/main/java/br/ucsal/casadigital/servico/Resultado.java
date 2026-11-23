package br.ucsal.casadigital.servico;

import br.ucsal.casadigital.validacao.Erros;

import java.util.function.Function;

/**
 * Resultado de um caso de uso. Interface <b>selada</b> com três {@code record}s, um para cada
 * "saída" dos gateways dos diagramas BPMN: deu certo, dados inválidos (campos com erro) ou falha
 * do sistema (por exemplo, a gravação não terminou). Quem chama trata os três com um
 * {@code switch} exaustivo (<i>pattern matching</i>), sem {@code default} e sem {@code instanceof}.
 */
public sealed interface Resultado<T> {

    record Sucesso<T>(T valor) implements Resultado<T> {
    }

    record Invalido<T>(Erros erros) implements Resultado<T> {
    }

    record Falha<T>(String mensagem) implements Resultado<T> {
    }

    static <T> Resultado<T> sucesso(T valor) {
        return new Sucesso<>(valor);
    }

    static <T> Resultado<T> invalido(Erros erros) {
        return new Invalido<>(erros);
    }

    static <T> Resultado<T> falha(String mensagem) {
        return new Falha<>(mensagem);
    }

    default boolean ok() {
        return this instanceof Sucesso<T>;
    }

    default <R> Resultado<R> mapear(Function<? super T, ? extends R> funcao) {
        return switch (this) {
            case Sucesso<T> s -> new Sucesso<>(funcao.apply(s.valor()));
            case Invalido<T> i -> new Invalido<>(i.erros());
            case Falha<T> f -> new Falha<>(f.mensagem());
        };
    }
}
