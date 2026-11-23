package br.ucsal.casadigital.web;

import java.util.logging.Level;
import java.util.logging.Logger;

/** Primeiro da cadeia: qualquer erro inesperado vira uma página 500 amigável, sem vazar detalhes técnicos. */
public final class FiltroDeErros implements Filtro {

    private static final Logger LOG = Logger.getLogger(FiltroDeErros.class.getName());

    @Override
    public Resposta aplicar(Requisicao requisicao, Cadeia proxima) {
        try {
            return proxima.seguir(requisicao);
        } catch (RuntimeException e) {
            LOG.log(Level.SEVERE, "Erro ao tratar " + requisicao.metodo() + " " + requisicao.caminho(), e);
            return Resposta.html(500, new VisoesPublicas.Erro(500, "Algo deu errado do nosso lado",
                    "Tente de novo em instantes. Se continuar, avise a equipe.").renderizar());
        }
    }
}
