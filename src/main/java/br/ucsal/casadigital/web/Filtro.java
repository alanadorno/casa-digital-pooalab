package br.ucsal.casadigital.web;

/**
 * Padrão <b>Chain of Responsibility</b>: cada filtro trata a requisição e decide se passa adiante
 * ({@code proxima.seguir(...)}) ou responde sozinho (por exemplo, recusando um acesso). A ordem dos
 * filtros é definida em um único lugar, no {@code ServidorWeb}.
 */
@FunctionalInterface
public interface Filtro {

    Resposta aplicar(Requisicao requisicao, Cadeia proxima);

    /** O "resto da cadeia" visto por um filtro. */
    @FunctionalInterface
    interface Cadeia {
        Resposta seguir(Requisicao requisicao);
    }
}
