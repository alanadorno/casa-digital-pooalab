package br.ucsal.casadigital.servico;

import java.util.List;

/**
 * Uma página de resultados (RN11). Record genérico: serve para matérias, sugestões ou qualquer lista.
 */
public record Pagina<T>(List<T> itens, int numero, int totalDePaginas, int total) {

    /** Fatia {@code todos} (já ordenado) na página pedida, usando {@code skip} e {@code limit} de Streams. */
    public static <T> Pagina<T> de(List<T> todos, int pedida, int tamanho) {
        int totalDePaginas = Math.max(1, (int) Math.ceil(todos.size() / (double) tamanho));
        int numero = Math.min(Math.max(1, pedida), totalDePaginas);
        List<T> itens = todos.stream().skip((long) (numero - 1) * tamanho).limit(tamanho).toList();
        return new Pagina<>(itens, numero, totalDePaginas, todos.size());
    }
}
