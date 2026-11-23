package br.ucsal.casadigital;

import br.ucsal.casadigital.dominio.DecisaoSugestao;
import br.ucsal.casadigital.dominio.EstadoSugestao;
import br.ucsal.casadigital.dominio.EventoDominio;
import br.ucsal.casadigital.dominio.Sugestao;
import br.ucsal.casadigital.infra.SimuladorDeFalhas.Falha;
import br.ucsal.casadigital.servico.Resultado;
import br.ucsal.casadigital.servico.SugestaoForm;

import java.time.Duration;

/** Processo P5 e regras RN07 a RN10. */
public class SugestoesTest {

    private final Cenario c = new Cenario();

    private static SugestaoForm form() {
        return new SugestaoForm("Ana", "Silva", "ana@exemplo.com", "Falem sobre games universitários.");
    }

    private Sugestao registrar() {
        return ((Resultado.Sucesso<Sugestao>) c.sugestoes.registrar(form())).valor();
    }

    @Teste("RN08: nome, sobrenome, e-mail válido e texto são obrigatórios")
    void validacao() {
        var r = c.sugestoes.registrar(new SugestaoForm("", "", "isso-nao-e-email", ""));
        var erros = ((Resultado.Invalido<Sugestao>) r).erros();
        Afirmar.verdadeiro(erros.tem("nome") && erros.tem("sobrenome") && erros.tem("email") && erros.tem("sugestao"), "quatro campos com erro");
        Afirmar.igual(0, c.banco.sugestoes().todos().size(), "nada foi registrado");
    }

    @Teste("RN10: a sugestão nasce como NOVA e o e-mail à equipe sai depois do registro (RN09)")
    void nasceNovaEEnviaEmail() {
        Sugestao s = registrar();
        Afirmar.igual(EstadoSugestao.NOVA, s.estado(), "estado inicial");
        Afirmar.igual(1, c.caixa.enviados().size(), "um e-mail enviado");
        Afirmar.igual("equipe@exemplo.com", c.caixa.enviados().getFirst().email().para(), "para a equipe");
        Afirmar.falso(c.banco.sugestoes().porId(s.id()).orElseThrow().emailPendente(), "não está pendente");
    }

    @Teste("RN09: se o registro falhar, nenhum e-mail é enviado")
    void registroFalha() {
        c.falhas.definir(Falha.REGISTRO_DA_SUGESTAO, true);
        var r = c.sugestoes.registrar(form());
        Afirmar.verdadeiro(r instanceof Resultado.Falha<Sugestao>, "falha do sistema");
        Afirmar.igual(0, c.caixa.enviados().size(), "sem e-mail");
        Afirmar.igual(0, c.banco.sugestoes().todos().size(), "sem registro");
    }

    @Teste("RN09: se o e-mail falhar, a sugestão fica registrada com a marca 'e-mail pendente'")
    void emailFalha() {
        c.falhas.definir(Falha.ENVIO_DO_EMAIL_DA_EQUIPE, true);
        Sugestao s = registrar();
        var salva = c.banco.sugestoes().porId(s.id()).orElseThrow();
        Afirmar.verdadeiro(salva.emailPendente(), "pendente");
        Afirmar.igual(1, salva.falhasDeEnvio(), "uma falha");
        Afirmar.igual(0, c.caixa.enviados().size(), "nada saiu");
    }

    @Teste("RN09: o reenvio automático espera o intervalo de 15 minutos e depois entrega")
    void reenvio() {
        c.falhas.definir(Falha.ENVIO_DO_EMAIL_DA_EQUIPE, true);
        Sugestao s = registrar();
        Afirmar.igual(0, c.sugestoes.reenviarPendentes(), "antes do intervalo não tenta");
        c.relogio.avancar(Duration.ofMinutes(16));
        c.falhas.definir(Falha.ENVIO_DO_EMAIL_DA_EQUIPE, false);
        Afirmar.igual(1, c.sugestoes.reenviarPendentes(), "tenta uma");
        var salva = c.banco.sugestoes().porId(s.id()).orElseThrow();
        Afirmar.falso(salva.emailPendente(), "não está mais pendente");
        Afirmar.igual(1, c.caixa.enviados().size(), "e-mail entregue");
    }

    @Teste("RN09: depois de 3 reenvios sem sucesso para de reenviar sozinho, mas continua pendente")
    void limiteDeReenvios() {
        c.falhas.definir(Falha.ENVIO_DO_EMAIL_DA_EQUIPE, true);
        Sugestao s = registrar();
        for (int i = 0; i < 3; i++) {
            c.relogio.avancar(Duration.ofMinutes(16));
            Afirmar.igual(1, c.sugestoes.reenviarPendentes(), "reenvio " + (i + 1));
        }
        c.relogio.avancar(Duration.ofMinutes(16));
        Afirmar.igual(0, c.sugestoes.reenviarPendentes(), "não tenta mais sozinho");
        var salva = c.banco.sugestoes().porId(s.id()).orElseThrow();
        Afirmar.verdadeiro(salva.emailPendente(), "continua pendente para a equipe ver");
        c.falhas.definir(Falha.ENVIO_DO_EMAIL_DA_EQUIPE, false);
        c.sugestoes.reenviarAgora(s.id());
        Afirmar.falso(c.banco.sugestoes().porId(s.id()).orElseThrow().emailPendente(), "o botão 'tentar agora' resolve");
    }

    @Teste("RN10: NOVA → AVALIADA → RESPONDIDA, nessa ordem")
    void estados() {
        Sugestao s = registrar();
        Afirmar.falso(c.sugestoes.responder(s.id(), "", c.equipe).ok(), "não responde antes de avaliar");
        Afirmar.verdadeiro(c.sugestoes.avaliar(s.id(), DecisaoSugestao.APROVEITADA, c.equipe).ok(), "avalia");
        Afirmar.falso(c.sugestoes.avaliar(s.id(), DecisaoSugestao.ARQUIVADA, c.equipe).ok(), "não avalia duas vezes");
        Afirmar.verdadeiro(c.sugestoes.responder(s.id(), "", c.equipe).ok(), "responde");
        var salva = c.banco.sugestoes().porId(s.id()).orElseThrow();
        Afirmar.igual(EstadoSugestao.RESPONDIDA, salva.estado(), "estado final");
        Afirmar.verdadeiro(c.ocorridos.stream().anyMatch(e -> e instanceof EventoDominio.SugestaoRespondida), "evento emitido");
    }

    @Teste("RN10: se o e-mail de resposta falhar, a sugestão continua AVALIADA")
    void respostaFalha() {
        Sugestao s = registrar();
        c.sugestoes.avaliar(s.id(), DecisaoSugestao.ARQUIVADA, c.equipe);
        c.falhas.definir(Falha.ENVIO_DA_RESPOSTA, true);
        Afirmar.falso(c.sugestoes.responder(s.id(), "Obrigado!", c.equipe).ok(), "falha informada");
        var salva = c.banco.sugestoes().porId(s.id()).orElseThrow();
        Afirmar.igual(EstadoSugestao.AVALIADA, salva.estado(), "continua avaliada");
        Afirmar.verdadeiro(salva.falhaNaResposta(), "painel avisa");
        c.falhas.definir(Falha.ENVIO_DA_RESPOSTA, false);
        Afirmar.verdadeiro(c.sugestoes.responder(s.id(), "Obrigado!", c.equipe).ok(), "tenta de novo e conclui");
    }
}
