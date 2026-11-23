package br.ucsal.casadigital;

import br.ucsal.casadigital.dominio.ItemUcsal;
import br.ucsal.casadigital.dominio.SituacaoItem;
import br.ucsal.casadigital.dominio.Subsecao;
import br.ucsal.casadigital.servico.ItemUcsalForm;
import br.ucsal.casadigital.servico.Resultado;

import java.time.Duration;

/** Processo P6 e regras RN12 a RN14. */
public class UcsalTest {

    private final Cenario c = new Cenario();

    private ItemUcsalForm rolando(String inicio, String fim, boolean conferido) {
        return new ItemUcsalForm("O_QUE_ESTA_ROLANDO", "Semana de tecnologia", "Palestras e oficinas.", "Liga de Tecnologia", inicio, fim, conferido);
    }

    @Teste("RN13: sem confirmar a conferência com a fonte o item não é publicado")
    void exigeConferencia() {
        var r = (Resultado.Invalido<ItemUcsal>) c.ucsal.cadastrar(rolando("2026-11-23", "2026-11-30", false), c.equipe);
        Afirmar.verdadeiro(r.erros().tem("conferido"), "cobra a conferência");
    }

    @Teste("RN13: o item guarda quem conferiu e quando (preenchido pelo sistema)")
    void registraConferente() {
        var item = ((Resultado.Sucesso<ItemUcsal>) c.ucsal.cadastrar(rolando("2026-11-23", "2026-11-30", true), c.equipe)).valor();
        Afirmar.igual("Alana (teste)", item.conferidoPor(), "quem conferiu");
        Afirmar.igual(c.relogio.instant(), item.conferidoEm(), "quando");
    }

    @Teste("RN14: 'O que está rolando' exige datas coerentes")
    void datasCoerentes() {
        var semDatas = (Resultado.Invalido<ItemUcsal>) c.ucsal.cadastrar(rolando("", "", true), c.equipe);
        Afirmar.verdadeiro(semDatas.erros().tem("inicio") && semDatas.erros().tem("fim"), "datas obrigatórias");
        var invertidas = (Resultado.Invalido<ItemUcsal>) c.ucsal.cadastrar(rolando("2026-12-10", "2026-12-01", true), c.equipe);
        Afirmar.verdadeiro(invertidas.erros().tem("fim"), "fim antes do início");
        var passado = (Resultado.Invalido<ItemUcsal>) c.ucsal.cadastrar(rolando("2026-11-01", "2026-11-10", true), c.equipe);
        Afirmar.verdadeiro(passado.erros().tem("fim"), "fim no passado");
    }

    @Teste("RN14: agendado → no ar → encerrado conforme as datas, e só o que está no ar aparece ao público")
    void vigencia() {
        c.ucsal.cadastrar(rolando("2026-11-25", "2026-11-26", true), c.equipe);
        Afirmar.igual(0, c.ucsal.visiveisPorSubsecao().getOrDefault(Subsecao.O_QUE_ESTA_ROLANDO, java.util.List.of()).size(), "agendado ainda não aparece");
        c.relogio.avancar(Duration.ofDays(2));
        Afirmar.igual(1, c.ucsal.visiveisPorSubsecao().get(Subsecao.O_QUE_ESTA_ROLANDO).size(), "no ar");
        c.relogio.avancar(Duration.ofDays(2));
        Afirmar.igual(0, c.ucsal.visiveisPorSubsecao().getOrDefault(Subsecao.O_QUE_ESTA_ROLANDO, java.util.List.of()).size(), "encerrado sai sozinho");
        Afirmar.igual(1, c.ucsal.encerrarVencidos(), "o agendador marca como encerrado");
        Afirmar.igual(0, c.ucsal.encerrarVencidos(), "e só uma vez");
        Afirmar.igual(SituacaoItem.ENCERRADO, c.ucsal.todos().getFirst().situacao(java.time.LocalDate.now(c.relogio)), "painel mostra encerrado");
    }

    @Teste("Subseções sem vigência ficam sempre no ar")
    void semVigencia() {
        var form = new ItemUcsalForm("LIGAS", "Liga de Direito", "Descrição", "Diretoria da liga", "", "", true);
        Afirmar.verdadeiro(c.ucsal.cadastrar(form, c.equipe).ok(), "cadastrou");
        c.relogio.avancar(Duration.ofDays(400));
        Afirmar.igual(1, c.ucsal.visiveisPorSubsecao().get(Subsecao.LIGAS).size(), "continua visível");
    }
}
