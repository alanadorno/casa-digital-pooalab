package br.ucsal.casadigital.servico;

import br.ucsal.casadigital.dominio.BarramentoEventos;
import br.ucsal.casadigital.dominio.DecisaoSugestao;
import br.ucsal.casadigital.dominio.EventoDominio.EmailDaSugestaoEnviado;
import br.ucsal.casadigital.dominio.EventoDominio.EmailDaSugestaoFalhou;
import br.ucsal.casadigital.dominio.EventoDominio.SugestaoAvaliada;
import br.ucsal.casadigital.dominio.EventoDominio.SugestaoRegistrada;
import br.ucsal.casadigital.dominio.EventoDominio.SugestaoRespondida;
import br.ucsal.casadigital.dominio.Sugestao;
import br.ucsal.casadigital.dominio.TransicaoInvalidaException;
import br.ucsal.casadigital.dominio.Usuario;
import br.ucsal.casadigital.infra.Email;
import br.ucsal.casadigital.infra.EmailException;
import br.ucsal.casadigital.infra.GatewayEmail;
import br.ucsal.casadigital.infra.Repositorio;
import br.ucsal.casadigital.infra.RepositorioIndisponivelException;
import br.ucsal.casadigital.validacao.Erros;
import br.ucsal.casadigital.validacao.Validador;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executor;

/**
 * Caso de uso P5. Pontos de <b>concorrência</b>:
 * <ul>
 *   <li>o e-mail à equipe é enviado em segundo plano por uma <i>virtual thread</i> (o {@link Executor}
 *       recebido), então o visitante não espera o serviço de e-mail;</li>
 *   <li>o reenvio automático roda várias tentativas em paralelo com {@link CompletableFuture};</li>
 *   <li>cada sugestão tem sua própria trava ({@link TravasPorId}), evitando que o reenvio e a avaliação
 *       da equipe se sobreponham.</li>
 * </ul>
 */
public final class SugestoesServico implements Sugestoes {

    private final Repositorio<Sugestao> repositorio;
    private final GatewayEmail emailDaEquipe;
    private final GatewayEmail emailDeResposta;
    private final BarramentoEventos eventos;
    private final Clock relogio;
    private final Validador validador;
    private final Executor executor;
    private final String enderecoDaEquipe;
    private final Duration intervaloDeReenvio;
    private final int maximoDeReenvios;
    private final TravasPorId travas = new TravasPorId();
    private final List<CompletableFuture<Void>> envios = new CopyOnWriteArrayList<>();

    public SugestoesServico(Repositorio<Sugestao> repositorio, GatewayEmail emailDaEquipe, GatewayEmail emailDeResposta,
                            BarramentoEventos eventos, Clock relogio, Validador validador, Executor executor,
                            String enderecoDaEquipe, Duration intervaloDeReenvio, int maximoDeReenvios) {
        this.repositorio = repositorio;
        this.emailDaEquipe = emailDaEquipe;
        this.emailDeResposta = emailDeResposta;
        this.eventos = eventos;
        this.relogio = relogio;
        this.validador = validador;
        this.executor = executor;
        this.enderecoDaEquipe = enderecoDaEquipe;
        this.intervaloDeReenvio = intervaloDeReenvio;
        this.maximoDeReenvios = maximoDeReenvios;
    }

    // ---------- P5, passos 1 a 5: validar, registrar, enviar ----------

    @Override
    public Resultado<Sugestao> registrar(SugestaoForm form) {
        Erros erros = validador.validar(form, false);
        if (!erros.vazio()) {
            return Resultado.invalido(erros);
        }
        Sugestao nova = new Sugestao(form.nome().trim(), form.sobrenome().trim(),
                form.email().trim().toLowerCase(), form.sugestao().trim(), relogio.instant());
        Sugestao registrada;
        try {
            registrada = repositorio.salvar(nova);
        } catch (RepositorioIndisponivelException e) {
            // RN09: se o registro falhar, nada é enviado por e-mail.
            return Resultado.falha("Não conseguimos registrar a sua sugestão agora. Tente mais tarde.");
        }
        eventos.publicar(new SugestaoRegistrada(relogio.instant(), registrada.id()));
        // RN09: o e-mail só é disparado depois do registro, em segundo plano.
        long id = registrada.id();
        envios.add(CompletableFuture.runAsync(() -> enviarParaEquipe(id), executor));
        return Resultado.sucesso(registrada);
    }

    private void enviarParaEquipe(long id) {
        travas.executar(id, () -> {
            Optional<Sugestao> atual = repositorio.porId(id);
            if (atual.isEmpty() || !(atual.get().emailPendente() || atual.get().emailEnviadoEm() == null)) {
                return null;
            }
            Sugestao s = atual.get();
            Instant agora = relogio.instant();
            try {
                emailDaEquipe.enviar(new Email(enderecoDaEquipe,
                        "Sugestão de pauta de " + s.nomeCompleto(),
                        "Nova sugestão de pauta pelo site%n%nNome: %s%nE-mail: %s%nRecebida em: %s%n%n%s%n"
                                .formatted(s.nomeCompleto(), s.email(), s.criadaEm(), s.texto()),
                        s.email()));
                int tentativa = s.falhasDeEnvio() + 1;
                s.registrarEmailEnviado(agora);
                repositorio.salvar(s);
                eventos.publicar(new EmailDaSugestaoEnviado(agora, id, tentativa));
            } catch (EmailException e) {
                // RN09: a sugestão continua registrada, ganha a marca "e-mail pendente" e será reenviada sozinha.
                s.registrarEmailFalhou(agora, intervaloDeReenvio, maximoDeReenvios);
                repositorio.salvar(s);
                eventos.publicar(new EmailDaSugestaoFalhou(agora, id, s.falhasDeEnvio(), e.getMessage()));
            }
            return null;
        });
    }

    @Override
    public int reenviarPendentes() {
        Instant agora = relogio.instant();
        List<Long> devidos = repositorio.stream().filter(s -> s.aguardandoReenvio(agora)).map(Sugestao::id).toList();
        List<CompletableFuture<Void>> tarefas = devidos.stream()
                .map(id -> CompletableFuture.runAsync(() -> enviarParaEquipe(id), executor))
                .toList();
        tarefas.forEach(CompletableFuture::join);
        return devidos.size();
    }

    @Override
    public Resultado<Sugestao> reenviarAgora(long id) {
        if (repositorio.porId(id).isEmpty()) {
            return Resultado.falha("Sugestão não encontrada.");
        }
        enviarParaEquipe(id);
        return repositorio.porId(id).<Resultado<Sugestao>>map(Resultado::sucesso)
                .orElseGet(() -> Resultado.falha("Sugestão não encontrada."));
    }

    // ---------- P5, passos 6 a 8: avaliar e responder (RN10) ----------

    @Override
    public Resultado<Sugestao> avaliar(long id, DecisaoSugestao decisao, Usuario por) {
        return travas.executar(id, () -> {
            Optional<Sugestao> atual = repositorio.porId(id);
            if (atual.isEmpty()) {
                return Resultado.falha("Sugestão não encontrada.");
            }
            Sugestao s = atual.get();
            Instant agora = relogio.instant();
            try {
                s.avaliar(decisao, agora);
                Sugestao gravada = repositorio.salvar(s);
                eventos.publicar(new SugestaoAvaliada(agora, id, decisao, por.nome()));
                return Resultado.sucesso(gravada);
            } catch (TransicaoInvalidaException e) {
                return Resultado.falha(e.getMessage());
            }
        });
    }

    @Override
    public Resultado<Sugestao> responder(long id, String mensagem, Usuario por) {
        return travas.executar(id, () -> {
            Optional<Sugestao> atual = repositorio.porId(id);
            if (atual.isEmpty()) {
                return Resultado.falha("Sugestão não encontrada.");
            }
            Sugestao s = atual.get();
            if (!s.estado().podeResponder()) {
                return Resultado.falha("Só é possível responder uma sugestão já avaliada.");
            }
            Instant agora = relogio.instant();
            String corpo = mensagem == null || mensagem.isBlank() ? respostaPadrao(s) : mensagem.trim();
            try {
                emailDeResposta.enviar(new Email(s.email(), "Sobre a sua sugestão de pauta", corpo, enderecoDaEquipe));
            } catch (EmailException e) {
                // RN10: se o envio da resposta falhar, a sugestão continua "avaliada" e o painel avisa.
                s.registrarFalhaNaResposta();
                repositorio.salvar(s);
                return Resultado.falha("Não foi possível enviar a resposta. A sugestão continua avaliada; tente de novo.");
            }
            s.registrarRespostaEnviada(agora);
            Sugestao gravada = repositorio.salvar(s);
            eventos.publicar(new SugestaoRespondida(agora, id));
            return Resultado.sucesso(gravada);
        });
    }

    private static String respostaPadrao(Sugestao s) {
        String decisao = s.decisao() == DecisaoSugestao.APROVEITADA
                ? "Gostamos da ideia e o tema entrou na nossa pauta. Obrigado por ajudar a escolher os próximos episódios!"
                : "Obrigado pela ideia. Neste momento ela não entra na pauta, mas ficou arquivada e pode voltar em outra edição.";
        return "Olá, %s!%n%n%s%n%nEquipe do podcast Isso Não É Um Rádio".formatted(s.nome(), decisao);
    }

    @Override
    public List<Sugestao> listar() {
        return repositorio.stream().sorted(java.util.Comparator.comparing(Sugestao::criadaEm).reversed()).toList();
    }

    @Override
    public CompletableFuture<Void> aguardarEnvios() {
        return CompletableFuture.allOf(envios.toArray(CompletableFuture[]::new));
    }
}
