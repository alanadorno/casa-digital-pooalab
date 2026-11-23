package br.ucsal.casadigital.web;

import br.ucsal.casadigital.dominio.DecisaoSugestao;
import br.ucsal.casadigital.dominio.Editoria;
import br.ucsal.casadigital.dominio.Materia;
import br.ucsal.casadigital.dominio.StatusMateria;
import br.ucsal.casadigital.dominio.Usuario;
import br.ucsal.casadigital.infra.Atividade;
import br.ucsal.casadigital.infra.EmailSimulado;
import br.ucsal.casadigital.infra.MedidorDeChamadas;
import br.ucsal.casadigital.infra.SimuladorDeFalhas;
import br.ucsal.casadigital.servico.Autenticacao;
import br.ucsal.casadigital.servico.Consulta;
import br.ucsal.casadigital.servico.ItemUcsalForm;
import br.ucsal.casadigital.servico.LoginForm;
import br.ucsal.casadigital.servico.MateriaForm;
import br.ucsal.casadigital.servico.Publicacao;
import br.ucsal.casadigital.servico.Resultado;
import br.ucsal.casadigital.servico.Sugestoes;
import br.ucsal.casadigital.servico.Ucsal;
import br.ucsal.casadigital.validacao.Erros;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Controlador do painel da equipe (processos P1, P2, P3, P5 e P6). Todas as rotas, menos a de entrada, levam
 * {@link ExigeLogin}: o {@link FiltroDeAcesso} lê essa anotação por reflexão e barra quem não está logado.
 *
 * <p>GRASP <b>Controller</b>: recebe o pedido, chama o serviço certo e escolhe a tela. Nenhuma regra de negócio
 * mora aqui; cada resposta a um {@link Resultado} usa um {@code switch} exaustivo.
 */
public final class PainelControlador {

    private final Autenticacao autenticacao;
    private final GerenciadorDeSessoes sessoes;
    private final Publicacao publicacao;
    private final Consulta consulta;
    private final Sugestoes sugestoes;
    private final Ucsal ucsal;
    private final SimuladorDeFalhas simulador;
    private final EmailSimulado caixaDeSaida;
    private final Atividade atividade;
    private final MedidorDeChamadas medidor;
    private final Roteador roteador;
    private final Clock relogio;

    public PainelControlador(Autenticacao autenticacao, GerenciadorDeSessoes sessoes, Publicacao publicacao, Consulta consulta,
                             Sugestoes sugestoes, Ucsal ucsal, SimuladorDeFalhas simulador, EmailSimulado caixaDeSaida,
                             Atividade atividade, MedidorDeChamadas medidor, Roteador roteador, Clock relogio) {
        this.autenticacao = autenticacao;
        this.sessoes = sessoes;
        this.publicacao = publicacao;
        this.consulta = consulta;
        this.sugestoes = sugestoes;
        this.ucsal = ucsal;
        this.simulador = simulador;
        this.caixaDeSaida = caixaDeSaida;
        this.atividade = atividade;
        this.medidor = medidor;
        this.roteador = roteador;
        this.relogio = relogio;
    }

    // ------------------------------------------------------------ P2: acesso

    @Rota(caminho = "/painel/entrar")
    public Resposta telaDeEntrada(Requisicao req) {
        if (req.usuario().isPresent()) {
            return Resposta.redirecionar("/painel");
        }
        return Resposta.html(new VisoesPainel.Entrar(new LoginForm("", ""), Erros.novo(), "").renderizar());
    }

    @Rota(metodo = "POST", caminho = "/painel/entrar")
    public Resposta entrar(Requisicao req) {
        LoginForm form = Formularios.ligar(req, LoginForm.class);
        return switch (autenticacao.entrar(form, req.origem())) {
            case Resultado.Sucesso<Usuario> s -> {
                Sessao sessao = sessoes.criar(s.valor().id());
                yield Resposta.redirecionar("/painel").comCookie(GerenciadorDeSessoes.COOKIE, sessao.token(), sessoes.segundosDeValidade());
            }
            case Resultado.Invalido<Usuario> i ->
                    Resposta.html(422, new VisoesPainel.Entrar(new LoginForm(form.email(), ""), i.erros(), "").renderizar());
            case Resultado.Falha<Usuario> f ->
                    Resposta.html(401, new VisoesPainel.Entrar(new LoginForm(form.email(), ""), Erros.novo(), f.mensagem()).renderizar());
        };
    }

    @ExigeLogin
    @Rota(metodo = "POST", caminho = "/painel/sair")
    public Resposta sair(Requisicao req) {
        req.sessao().ifPresent(s -> sessoes.encerrar(s.token()));
        return Resposta.redirecionar("/painel/entrar").comCookie(GerenciadorDeSessoes.COOKIE, "", 0);
    }

    // ------------------------------------------------------------ Matérias (P1, P3)

    @ExigeLogin
    @Rota(caminho = "/painel")
    public Resposta materias(Requisicao req) {
        Optional<Editoria> editoria = Editoria.porSlug(req.parametro("editoria", ""));
        Optional<StatusMateria> status = statusDe(req.parametro("status", ""));
        List<Materia> lista = publicacao.listar(editoria, status);
        var home = consulta.home();
        String tituloDoDestaque = home.destaqueManual().flatMap(d -> publicacao.buscar(d.materiaId())).map(Materia::titulo).orElse("");
        return Resposta.html(new VisoesPainel.Materias(req.usuario().orElseThrow(), csrf(req), avisos(req), lista, editoria, status, home,
                tituloDoDestaque).renderizar());
    }

    @ExigeLogin
    @Rota(caminho = "/painel/materias/nova")
    public Resposta novaMateria(Requisicao req) {
        return Resposta.html(editor(req, MateriaForm.vazio(), Erros.novo(), null, "").renderizar());
    }

    @ExigeLogin
    @Rota(caminho = "/painel/materias/{id}")
    public Resposta editarMateria(Requisicao req) {
        return publicacao.buscar(req.parametroLong("id", -1))
                .map(m -> Resposta.html(editor(req, paraFormulario(m), Erros.novo(), m.status(), "").renderizar()))
                .orElseGet(() -> Resposta.html(404, new VisoesPublicas.Erro(404, "Matéria não encontrada", "Ela pode ter sido removida.").renderizar()));
    }

    /** P3: três botões no mesmo formulário (rascunho, revisão, publicar) escolhidos pelo campo {@code acao}. */
    @ExigeLogin
    @Rota(metodo = "POST", caminho = "/painel/materias")
    public Resposta salvarMateria(Requisicao req) {
        MateriaForm form = Formularios.ligar(req, MateriaForm.class);
        Usuario usuario = req.usuario().orElseThrow();
        String acao = req.parametro("acao", "rascunho");
        Resultado<Materia> resultado = switch (acao) {
            case "publicar" -> publicacao.publicar(form, usuario);
            case "revisao" -> publicacao.enviarParaRevisao(form, usuario);
            default -> publicacao.salvarRascunho(form, usuario);
        };
        StatusMateria statusAtual = form.id() == 0 ? null : publicacao.buscar(form.id()).map(Materia::status).orElse(null);
        return switch (resultado) {
            case Resultado.Sucesso<Materia> s -> {
                String texto = switch (s.valor().status()) {
                    case PUBLICADA -> "Matéria publicada e já visível na área pública.";
                    case EM_REVISAO -> "Matéria enviada para revisão.";
                    case RASCUNHO -> "Rascunho salvo.";
                };
                req.sessao().ifPresent(x -> x.avisar("ok", texto));
                yield Resposta.redirecionar("/painel");
            }
            case Resultado.Invalido<Materia> i -> Resposta.html(422, editor(req, form, i.erros(), statusAtual, "").renderizar());
            case Resultado.Falha<Materia> f -> Resposta.html(503, editor(req, form, Erros.novo(), statusAtual, f.mensagem()).renderizar());
        };
    }

    @ExigeLogin
    @Rota(metodo = "POST", caminho = "/painel/materias/{id}/despublicar")
    public Resposta despublicar(Requisicao req) {
        return aviso(req, publicacao.despublicar(req.parametroLong("id", -1), req.usuario().orElseThrow()),
                "Matéria despublicada: saiu da área pública e voltou a rascunho.");
    }

    @ExigeLogin
    @Rota(metodo = "POST", caminho = "/painel/destaque/{id}")
    public Resposta fixarDestaque(Requisicao req) {
        return aviso(req, publicacao.fixarDestaque(req.parametroLong("id", -1), req.usuario().orElseThrow()),
                "Matéria fixada como principal da home por 7 dias.");
    }

    @ExigeLogin
    @Rota(metodo = "POST", caminho = "/painel/destaque/automatico")
    public Resposta destaqueAutomatico(Requisicao req) {
        publicacao.voltarAoAutomatico(req.usuario().orElseThrow());
        req.sessao().ifPresent(s -> s.avisar("ok", "A home voltou ao destaque automático."));
        return Resposta.redirecionar("/painel");
    }

    // ------------------------------------------------------------ Sugestões (P5)

    @ExigeLogin
    @Rota(caminho = "/painel/sugestoes")
    public Resposta listarSugestoes(Requisicao req) {
        return Resposta.html(new VisoesPainel.Sugestoes(req.usuario().orElseThrow(), csrf(req), avisos(req), sugestoes.listar(),
                relogio.instant()).renderizar());
    }

    @ExigeLogin
    @Rota(metodo = "POST", caminho = "/painel/sugestoes/{id}/avaliar")
    public Resposta avaliar(Requisicao req) {
        DecisaoSugestao decisao;
        try {
            decisao = DecisaoSugestao.valueOf(req.parametro("decisao", ""));
        } catch (IllegalArgumentException e) {
            req.sessao().ifPresent(s -> s.avisar("erro", "Escolha aproveitar ou arquivar."));
            return Resposta.redirecionar("/painel/sugestoes");
        }
        return avisoEm(req, "/painel/sugestoes", sugestoes.avaliar(req.parametroLong("id", -1), decisao, req.usuario().orElseThrow()),
                "Sugestão avaliada.");
    }

    @ExigeLogin
    @Rota(metodo = "POST", caminho = "/painel/sugestoes/{id}/responder")
    public Resposta responder(Requisicao req) {
        return avisoEm(req, "/painel/sugestoes", sugestoes.responder(req.parametroLong("id", -1), req.parametro("mensagem", ""),
                req.usuario().orElseThrow()), "Resposta enviada por e-mail. A sugestão está respondida.");
    }

    @ExigeLogin
    @Rota(metodo = "POST", caminho = "/painel/sugestoes/{id}/reenviar")
    public Resposta reenviar(Requisicao req) {
        return avisoEm(req, "/painel/sugestoes", sugestoes.reenviarAgora(req.parametroLong("id", -1)),
                "E-mail enviado à equipe.");
    }

    // ------------------------------------------------------------ UCSAL (P6)

    @ExigeLogin
    @Rota(caminho = "/painel/ucsal")
    public Resposta telaUcsal(Requisicao req) {
        return Resposta.html(telaUcsal(req, ItemUcsalForm.vazio(), Erros.novo(), "").renderizar());
    }

    @ExigeLogin
    @Rota(metodo = "POST", caminho = "/painel/ucsal")
    public Resposta cadastrarItem(Requisicao req) {
        ItemUcsalForm form = Formularios.ligar(req, ItemUcsalForm.class);
        return switch (ucsal.cadastrar(form, req.usuario().orElseThrow())) {
            case Resultado.Sucesso<?> s -> {
                req.sessao().ifPresent(x -> x.avisar("ok", "Item publicado na seção UCSAL."));
                yield Resposta.redirecionar("/painel/ucsal");
            }
            case Resultado.Invalido<?> i -> Resposta.html(422, telaUcsal(req, form, i.erros(), "").renderizar());
            case Resultado.Falha<?> f -> Resposta.html(503, telaUcsal(req, form, Erros.novo(), f.mensagem()).renderizar());
        };
    }

    // ------------------------------------------------------------ Demonstração (só administrador)

    @ExigeLogin(administrador = true)
    @Rota(caminho = "/painel/demonstracao")
    public Resposta demonstracao(Requisicao req) {
        List<String> rotas = roteador.rotas().stream()
                .map(r -> "%-5s %-40s %s".formatted(r.metodoHttp(), r.caminho(), r.exigeLogin() ? (r.apenasAdministrador() ? "[administrador]" : "[login]") : "[público]"))
                .toList();
        return Resposta.html(new VisoesPainel.Demonstracao(req.usuario().orElseThrow(), csrf(req), avisos(req), simulador,
                caixaDeSaida.enviados(), atividade.recentes(25), medidor.resumo(), rotas).renderizar());
    }

    @ExigeLogin(administrador = true)
    @Rota(metodo = "POST", caminho = "/painel/demonstracao/falhas")
    public Resposta alternarFalha(Requisicao req) {
        try {
            SimuladorDeFalhas.Falha falha = SimuladorDeFalhas.Falha.valueOf(req.parametro("falha", ""));
            boolean ligar = req.parametro("ligada", "nao").equals("sim");
            simulador.definir(falha, ligar);
            req.sessao().ifPresent(s -> s.avisar("ok", (ligar ? "Falha ligada: " : "Falha desligada: ") + falha.descricao()));
        } catch (IllegalArgumentException e) {
            req.sessao().ifPresent(s -> s.avisar("erro", "Falha desconhecida."));
        }
        return Resposta.redirecionar("/painel/demonstracao");
    }

    // ------------------------------------------------------------ apoio

    private VisoesPainel.Editor editor(Requisicao req, MateriaForm form, Erros erros, StatusMateria status, String falha) {
        return new VisoesPainel.Editor(req.usuario().orElseThrow(), csrf(req), avisos(req), form, erros, status, falha);
    }

    private VisoesPainel.PainelUcsal telaUcsal(Requisicao req, ItemUcsalForm form, Erros erros, String falha) {
        return new VisoesPainel.PainelUcsal(req.usuario().orElseThrow(), csrf(req), avisos(req), ucsal.todos(), form, erros,
                LocalDate.now(relogio), falha);
    }

    private static MateriaForm paraFormulario(Materia m) {
        return new MateriaForm(m.id(), m.titulo(), m.editoria() == null ? "" : m.editoria().slug(), m.resumo(), m.videoUrl(), m.texto(),
                String.join("\n", m.referencias()), m.revisadaPor() != null, false);
    }

    private static Optional<StatusMateria> statusDe(String nome) {
        try {
            return Optional.of(StatusMateria.valueOf(nome));
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    private Resposta aviso(Requisicao req, Resultado<?> resultado, String sucesso) {
        return avisoEm(req, "/painel", resultado, sucesso);
    }

    /** Converte qualquer {@link Resultado} em aviso para a próxima tela, sem repetir o mesmo switch em cada rota. */
    private Resposta avisoEm(Requisicao req, String destino, Resultado<?> resultado, String sucesso) {
        Sessao sessao = req.sessao().orElseThrow();
        switch (resultado) {
            case Resultado.Sucesso<?> s -> sessao.avisar("ok", sucesso);
            case Resultado.Invalido<?> i -> sessao.avisar("erro", i.erros().comoMapa().values().stream().findFirst().orElse("Dados inválidos."));
            case Resultado.Falha<?> f -> sessao.avisar("erro", f.mensagem());
        }
        return Resposta.redirecionar(destino);
    }

    private static String csrf(Requisicao req) {
        return req.sessao().map(Sessao::csrf).orElse("");
    }

    private static List<Sessao.Aviso> avisos(Requisicao req) {
        return req.sessao().map(Sessao::consumirAvisos).orElse(List.of());
    }
}
