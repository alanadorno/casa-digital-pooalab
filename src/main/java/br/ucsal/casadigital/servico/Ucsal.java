package br.ucsal.casadigital.servico;

import br.ucsal.casadigital.dominio.ItemUcsal;
import br.ucsal.casadigital.dominio.Subsecao;
import br.ucsal.casadigital.dominio.Usuario;

import java.util.List;
import java.util.Map;

/** Processo P6: manter a seção UCSAL atualizada (RN12, RN13, RN14). */
public interface Ucsal {

    /** RN13: quem está logado vira "conferido por"; a data e a hora são registradas pelo sistema. */
    Resultado<ItemUcsal> cadastrar(ItemUcsalForm form, Usuario conferente);

    /** Área pública: só o que está no ar hoje, agrupado por subseção. */
    Map<Subsecao, List<ItemUcsal>> visiveisPorSubsecao();

    /** Painel: tudo, inclusive agendados e encerrados. */
    List<ItemUcsal> todos();

    /** RN14: marca como "encerrado" o que passou da data de fim. Devolve quantos encerrou. */
    int encerrarVencidos();
}
