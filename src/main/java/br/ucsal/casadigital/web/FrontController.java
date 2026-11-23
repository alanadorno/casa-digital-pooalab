package br.ucsal.casadigital.web;

import java.util.List;

/**
 * Padrão <b>Front Controller</b>: todo pedido HTTP entra por aqui. Monta a cadeia de {@link Filtro}s e, no fim
 * dela, usa o {@link Roteador} para executar o controlador certo (ou responder 404).
 */
public final class FrontController {

    private final Filtro.Cadeia cadeia;

    public FrontController(Roteador roteador, List<Filtro> filtros) {
        Filtro.Cadeia terminal = requisicao -> roteador.resolver(requisicao)
                .map(rota -> rota.executar(requisicao))
                .orElseGet(() -> Resposta.html(404, new VisoesPublicas.Erro(404, "Página não encontrada",
                        "O endereço pode ter mudado ou a matéria foi despublicada.").renderizar()));
        Filtro.Cadeia montada = terminal;
        for (int i = filtros.size() - 1; i >= 0; i--) {
            Filtro filtro = filtros.get(i);
            Filtro.Cadeia seguinte = montada;
            montada = requisicao -> filtro.aplicar(requisicao, seguinte);
        }
        this.cadeia = montada;
    }

    public Resposta tratar(Requisicao requisicao) {
        return cadeia.seguir(requisicao);
    }
}
