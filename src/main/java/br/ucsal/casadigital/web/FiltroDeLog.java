package br.ucsal.casadigital.web;

import java.util.logging.Logger;

/** Registra cada requisição com o status e o tempo gasto. */
public final class FiltroDeLog implements Filtro {

    private static final Logger LOG = Logger.getLogger(FiltroDeLog.class.getName());

    @Override
    public Resposta aplicar(Requisicao requisicao, Cadeia proxima) {
        long inicio = System.nanoTime();
        Resposta resposta = proxima.seguir(requisicao);
        if (!requisicao.caminho().matches("^/(css|js|img|fonts)/.*")) {
            LOG.info("%s %s -> %d (%d ms) [%s]".formatted(requisicao.metodo(), requisicao.caminho(), resposta.status(),
                    (System.nanoTime() - inicio) / 1_000_000, Thread.currentThread().isVirtual() ? "virtual" : "plataforma"));
        }
        return resposta;
    }
}
