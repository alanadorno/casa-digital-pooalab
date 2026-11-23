package br.ucsal.casadigital.servico;

import br.ucsal.casadigital.dominio.DecisaoSugestao;
import br.ucsal.casadigital.dominio.Sugestao;
import br.ucsal.casadigital.dominio.Usuario;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/** Processo P5: enviar e tratar as sugestões de pauta (RN07 a RN10). */
public interface Sugestoes {

    /**
     * RN09: registra primeiro; só depois dispara o e-mail à equipe, em segundo plano. O visitante recebe a
     * confirmação assim que o registro termina, mesmo que o e-mail falhe.
     */
    Resultado<Sugestao> registrar(SugestaoForm form);

    Resultado<Sugestao> avaliar(long id, DecisaoSugestao decisao, Usuario por);

    /** RN10: só passa a "respondida" depois que o e-mail de resposta foi enviado com sucesso. */
    Resultado<Sugestao> responder(long id, String mensagem, Usuario por);

    List<Sugestao> listar();

    /** RN09: reenvia, em paralelo, os e-mails pendentes que já estão no horário. Devolve quantos tentou. */
    int reenviarPendentes();

    /** Reenvio manual ("Tentar agora" no painel). */
    Resultado<Sugestao> reenviarAgora(long id);

    /** Termina quando o envio em segundo plano da última sugestão registrada acabou (útil nos testes). */
    CompletableFuture<Void> aguardarEnvios();
}
