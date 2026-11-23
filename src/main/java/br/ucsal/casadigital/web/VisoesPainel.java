package br.ucsal.casadigital.web;

import br.ucsal.casadigital.dominio.DestaqueManual;
import br.ucsal.casadigital.dominio.Editoria;
import br.ucsal.casadigital.dominio.EventoDominio;
import br.ucsal.casadigital.dominio.ItemUcsal;
import br.ucsal.casadigital.dominio.Materia;
import br.ucsal.casadigital.dominio.StatusMateria;
import br.ucsal.casadigital.dominio.Subsecao;
import br.ucsal.casadigital.dominio.Sugestao;
import br.ucsal.casadigital.dominio.Usuario;
import br.ucsal.casadigital.infra.Email;
import br.ucsal.casadigital.infra.MedidorDeChamadas;
import br.ucsal.casadigital.infra.SimuladorDeFalhas;
import br.ucsal.casadigital.servico.Home;
import br.ucsal.casadigital.servico.ItemUcsalForm;
import br.ucsal.casadigital.servico.LoginForm;
import br.ucsal.casadigital.servico.MateriaForm;
import br.ucsal.casadigital.validacao.Erros;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static br.ucsal.casadigital.web.Html.esc;

/** Telas do painel da equipe. Cada classe preenche o conteúdo dentro da moldura de {@link VisaoPainel}. */
public final class VisoesPainel {

    private VisoesPainel() {
    }

    // ------------------------------------------------------------ campos reutilizáveis

    private static String campo(String nome, String rotulo, String valor, Erros erros, String tipo, String dica) {
        boolean erro = erros.tem(nome);
        return "<div class=\"p-campo" + (erro ? " erro" : "") + "\"><label for=\"f-" + nome + "\">" + esc(rotulo) + "</label>"
                + "<input id=\"f-" + nome + "\" name=\"" + nome + "\" type=\"" + tipo + "\" value=\"" + esc(valor) + "\""
                + (erro ? " aria-invalid=\"true\" aria-describedby=\"e-" + nome + "\"" : "") + ">"
                + (dica.isEmpty() ? "" : "<p class=\"p-dica\">" + esc(dica) + "</p>")
                + (erro ? "<p class=\"p-erro\" id=\"e-" + nome + "\">" + esc(erros.get(nome)) + "</p>" : "") + "</div>";
    }

    private static String area(String nome, String rotulo, String valor, Erros erros, String dica) {
        boolean erro = erros.tem(nome);
        return "<div class=\"p-campo" + (erro ? " erro" : "") + "\"><label for=\"f-" + nome + "\">" + esc(rotulo) + "</label>"
                + "<textarea id=\"f-" + nome + "\" name=\"" + nome + "\"" + (erro ? " aria-invalid=\"true\" aria-describedby=\"e-" + nome + "\"" : "")
                + ">" + esc(valor) + "</textarea>" + (dica.isEmpty() ? "" : "<p class=\"p-dica\">" + esc(dica) + "</p>")
                + (erro ? "<p class=\"p-erro\" id=\"e-" + nome + "\">" + esc(erros.get(nome)) + "</p>" : "") + "</div>";
    }

    private static String caixa(String nome, String rotulo, boolean marcada, Erros erros) {
        boolean erro = erros.tem(nome);
        return "<div class=\"p-campo" + (erro ? " erro" : "") + "\"><label><input type=\"checkbox\" name=\"" + nome + "\"" + (marcada ? " checked" : "")
                + (erro ? " aria-invalid=\"true\" aria-describedby=\"e-" + nome + "\"" : "") + "> " + esc(rotulo) + "</label>"
                + (erro ? "<p class=\"p-erro\" id=\"e-" + nome + "\">" + esc(erros.get(nome)) + "</p>" : "") + "</div>";
    }

    private static String resumoDeErros(Erros erros) {
        if (erros.vazio()) {
            return "";
        }
        StringBuilder sb = new StringBuilder("<div class=\"p-aviso erro\" role=\"alert\">Revise os campos marcados (" + erros.quantidade() + "):<ul>");
        erros.comoMapa().values().stream().sorted().forEach(m -> sb.append("<li>").append(esc(m)).append("</li>"));
        return sb.append("</ul></div>").toString();
    }

    private static String etiquetaStatus(StatusMateria s) {
        String cor = switch (s) {
            case RASCUNHO -> "amarelo";
            case EM_REVISAO -> "azul";
            case PUBLICADA -> "verde";
        };
        return "<span class=\"etiqueta " + cor + "\">" + esc(s.rotulo()) + "</span>";
    }

    // ------------------------------------------------------------ P2: entrar

    public static final class Entrar extends Visao {
        private final LoginForm valores;
        private final Erros erros;
        private final String mensagem;

        public Entrar(LoginForm valores, Erros erros, String mensagem) {
            this.valores = valores;
            this.erros = erros;
            this.mensagem = mensagem;
        }

        @Override
        protected String titulo() {
            return "Entrar no painel | Isso Não É Um Rádio";
        }

        @Override
        protected String folhasDeEstilo() {
            return "<link rel=\"stylesheet\" href=\"/css/painel.css\">\n";
        }

        @Override
        protected String corpo() {
            return """
                    <main class="p-main p-login" id="conteudo">
                    <h1>Painel da equipe</h1>
                    %s
                    <form class="p-form" method="post" action="/painel/entrar" novalidate>
                    %s%s
                    <button class="btn" type="submit">Entrar</button>
                    <p class="p-dica">Acesso só para a equipe do podcast. <a href="/">Voltar ao site</a></p>
                    </form></main>
                    """.formatted(mensagem.isEmpty() ? "" : "<p class=\"p-aviso erro\" role=\"alert\">" + esc(mensagem) + "</p>",
                    campo("email", "E-mail", valores.email(), erros, "email", ""),
                    campo("senha", "Senha", "", erros, "password", ""));
        }
    }

    // ------------------------------------------------------------ Matérias

    public static final class Materias extends VisaoPainel {
        private final List<Materia> lista;
        private final Optional<Editoria> editoria;
        private final Optional<StatusMateria> status;
        private final Home home;
        private final Optional<DestaqueManual> destaque;
        private final String tituloDoDestaque;

        public Materias(Usuario u, String csrf, List<Sessao.Aviso> avisos, List<Materia> lista, Optional<Editoria> editoria,
                        Optional<StatusMateria> status, Home home, String tituloDoDestaque) {
            super(u, csrf, avisos, "materias");
            this.lista = lista;
            this.editoria = editoria;
            this.status = status;
            this.home = home;
            this.destaque = home.destaqueManual();
            this.tituloDoDestaque = tituloDoDestaque;
        }

        @Override
        protected String titulo() {
            return "Matérias | Painel";
        }

        @Override
        protected String conteudo() {
            StringBuilder filtroEd = new StringBuilder("<option value=\"\">Todas as editorias</option>");
            for (Editoria e : Editoria.values()) {
                filtroEd.append("<option value=\"").append(e.slug()).append('"').append(editoria.filter(x -> x == e).isPresent() ? " selected" : "")
                        .append('>').append(esc(e.nome())).append("</option>");
            }
            StringBuilder filtroSt = new StringBuilder("<option value=\"\">Todos os status</option>");
            for (StatusMateria s : StatusMateria.values()) {
                filtroSt.append("<option value=\"").append(s.name()).append('"').append(status.filter(x -> x == s).isPresent() ? " selected" : "")
                        .append('>').append(esc(s.rotulo())).append("</option>");
            }
            String faixa = destaque.map(d -> "<p class=\"p-aviso info\">Destaque manual ativo: <strong>" + esc(tituloDoDestaque) + "</strong> até "
                            + Html.dataHora(d.ate()) + ". <form method=\"post\" action=\"/painel/destaque/automatico\" style=\"display:inline\">" + campoCsrf()
                            + "<button class=\"btn sec\" type=\"submit\">Voltar ao automático</button></form></p>")
                    .orElse("<p class=\"p-aviso info\">A home está no <strong>destaque automático</strong>: a matéria mais recente é a principal.</p>");
            StringBuilder linhas = new StringBuilder();
            for (Materia m : lista) {
                String naHome = home.principal().filter(p -> p.id() == m.id()).isPresent() ? "Principal"
                        : home.secundarias().stream().anyMatch(s -> s.id() == m.id()) ? "Secundária" : "—";
                linhas.append("<tr><td><a href=\"/painel/materias/").append(m.id()).append("\">").append(esc(m.titulo())).append("</a>")
                        .append(m.demonstracao() ? " <span class=\"p-meta\">(demonstração)</span>" : "").append("</td><td>")
                        .append(m.editoria() == null ? "—" : esc(m.editoria().nome())).append("</td><td>").append(etiquetaStatus(m.status()))
                        .append("</td><td>").append(Html.dataHora(m.atualizadaEm())).append("</td><td>").append(naHome).append("</td><td class=\"p-linha\">")
                        .append("<a class=\"btn sec\" href=\"/painel/materias/").append(m.id()).append("\">Editar</a>");
                if (m.publicada()) {
                    linhas.append("<form method=\"post\" action=\"/painel/destaque/").append(m.id()).append("\">").append(campoCsrf())
                            .append("<button class=\"btn sec\" type=\"submit\">Fixar na home</button></form>")
                            .append("<form method=\"post\" action=\"/painel/materias/").append(m.id()).append("/despublicar\">").append(campoCsrf())
                            .append("<button class=\"btn perigo\" type=\"submit\">Despublicar</button></form>");
                }
                linhas.append("</td></tr>\n");
            }
            return """
                    <h1>Matérias</h1>
                    %s
                    <div class="p-linha" style="justify-content:space-between;margin:10px 0">
                    <form class="p-linha" method="get" action="/painel"><label class="p-meta" for="fe">Editoria</label>
                    <select id="fe" name="editoria">%s</select><label class="p-meta" for="fs">Status</label><select id="fs" name="status">%s</select>
                    <button class="btn sec" type="submit">Filtrar</button></form>
                    <a class="btn" href="/painel/materias/nova">Nova matéria</a></div>
                    <div class="p-tabela"><table><caption class="p-meta" style="text-align:left;padding:6px">%d matéria(s)</caption>
                    <thead><tr><th>Título</th><th>Editoria</th><th>Status</th><th>Atualizada</th><th>Na home</th><th>Ações</th></tr></thead>
                    <tbody>%s</tbody></table></div>
                    """.formatted(faixa, filtroEd, filtroSt, lista.size(), linhas);
        }
    }

    // ------------------------------------------------------------ Editor de matéria (P1 e P3)

    public static final class Editor extends VisaoPainel {
        private final MateriaForm form;
        private final Erros erros;
        private final StatusMateria status;
        private final String falha;

        public Editor(Usuario u, String csrf, List<Sessao.Aviso> avisos, MateriaForm form, Erros erros, StatusMateria status, String falha) {
            super(u, csrf, avisos, "materias");
            this.form = form;
            this.erros = erros;
            this.status = status;
            this.falha = falha;
        }

        @Override
        protected String titulo() {
            return (form.id() == 0 ? "Nova matéria" : "Editar matéria") + " | Painel";
        }

        @Override
        protected String conteudo() {
            StringBuilder opcoes = new StringBuilder("<option value=\"\">Escolha…</option>");
            for (Editoria e : Editoria.values()) {
                opcoes.append("<option value=\"").append(e.slug()).append('"').append(e.slug().equals(form.editoria()) ? " selected" : "")
                        .append('>').append(esc(e.nome())).append("</option>");
            }
            boolean publicada = status == StatusMateria.PUBLICADA;
            String seletor = "<div class=\"p-campo" + (erros.tem("editoria") ? " erro" : "") + "\"><label for=\"f-editoria\">Editoria</label>"
                    + "<select id=\"f-editoria\" name=\"editoria\"" + (erros.tem("editoria") ? " aria-invalid=\"true\" aria-describedby=\"e-editoria\"" : "") + ">"
                    + opcoes + "</select>" + (erros.tem("editoria") ? "<p class=\"p-erro\" id=\"e-editoria\">" + esc(erros.get("editoria")) + "</p>" : "") + "</div>";
            String botoes = publicada
                    ? "<button class=\"btn verde\" type=\"submit\" name=\"acao\" value=\"publicar\">Atualizar matéria publicada</button>"
                    + "<button class=\"btn sec\" type=\"submit\" name=\"acao\" value=\"rascunho\">Voltar a rascunho (despublica)</button>"
                    : "<button class=\"btn sec\" type=\"submit\" name=\"acao\" value=\"rascunho\">Salvar rascunho</button>"
                    + "<button class=\"btn sec\" type=\"submit\" name=\"acao\" value=\"revisao\">Enviar para revisão</button>"
                    + "<button class=\"btn verde\" type=\"submit\" name=\"acao\" value=\"publicar\">Publicar</button>";
            return """
                    <h1>%s %s</h1>
                    %s%s
                    <form class="p-form" method="post" action="/painel/materias" novalidate>
                    %s<input type="hidden" name="id" value="%d">
                    %s%s%s%s%s%s%s
                    <div class="p-linha">%s<a class="btn sec" href="/painel">Cancelar</a></div>
                    </form>
                    """.formatted(form.id() == 0 ? "Nova matéria" : "Editar matéria", status == null ? "" : etiquetaStatus(status),
                    falha.isEmpty() ? "" : "<p class=\"p-aviso erro\" role=\"alert\">" + esc(falha) + "</p>", resumoDeErros(erros),
                    campoCsrf(), form.id(),
                    campo("titulo", "Título", form.titulo(), erros, "text", ""),
                    seletor,
                    area("resumo", "Resumo (aparece nos cartões)", form.resumo(), erros, "Opcional. Até 400 caracteres."),
                    campo("video", "Link do vídeo no YouTube", form.video(), erros, "text", "O vídeo fica no YouTube; aqui ele só é incorporado."),
                    area("texto", "Texto completo", form.texto(), erros, "Separe os parágrafos com uma linha em branco."),
                    area("referencias", "Referências (uma por linha)", form.referencias(), erros, "Inclua ao menos uma."),
                    caixa("revisada", "Esta matéria foi revisada e aprovada por um membro da equipe", form.revisada(), erros)
                            + caixa("destacar", "Fixar como principal da home por 7 dias ao publicar", form.destacar(), erros),
                    botoes);
        }
    }

    // ------------------------------------------------------------ Sugestões (P5)

    public static final class Sugestoes extends VisaoPainel {
        private final List<Sugestao> lista;
        private final Instant agora;

        public Sugestoes(Usuario u, String csrf, List<Sessao.Aviso> avisos, List<Sugestao> lista, Instant agora) {
            super(u, csrf, avisos, "sugestoes");
            this.lista = lista;
            this.agora = agora;
        }

        @Override
        protected String titulo() {
            return "Sugestões | Painel";
        }

        @Override
        protected String conteudo() {
            if (lista.isEmpty()) {
                return "<h1>Sugestões de pauta</h1><p>Nenhuma sugestão recebida ainda.</p>";
            }
            StringBuilder sb = new StringBuilder("<h1>Sugestões de pauta</h1>\n");
            for (Sugestao s : lista) {
                String cor = switch (s.estado()) {
                    case NOVA -> "laranja";
                    case AVALIADA -> "azul";
                    case RESPONDIDA -> "verde";
                };
                sb.append("<article class=\"p-cartao\"><h3>#").append(s.id()).append(" · ").append(esc(s.nomeCompleto())).append(" <span class=\"etiqueta ")
                        .append(cor).append("\">").append(esc(s.estado().rotulo())).append("</span>");
                if (s.decisao() != null) {
                    sb.append(" <span class=\"etiqueta\">").append(esc(s.decisao().rotulo())).append("</span>");
                }
                if (s.emailPendente()) {
                    sb.append(" <span class=\"etiqueta amarelo\">e-mail pendente</span>");
                }
                sb.append("</h3><p class=\"p-meta\">").append(esc(s.email())).append(" · recebida em ").append(Html.dataHora(s.criadaEm())).append("</p><p>")
                        .append(esc(s.texto()).replace("\n", "<br>")).append("</p>");
                if (s.emailPendente()) {
                    sb.append("<p class=\"p-meta\">O e-mail à equipe falhou ").append(s.falhasDeEnvio()).append(" vez(es). ")
                            .append(s.proximaTentativa() == null ? "O reenvio automático terminou; use o botão ao lado."
                                    : "Nova tentativa automática em " + Html.dataHora(s.proximaTentativa()) + ".")
                            .append("</p><form method=\"post\" action=\"/painel/sugestoes/").append(s.id()).append("/reenviar\">").append(campoCsrf())
                            .append("<button class=\"btn sec\" type=\"submit\">Tentar enviar agora</button></form>");
                }
                if (s.estado().podeAvaliar()) {
                    sb.append("<form class=\"p-linha\" method=\"post\" action=\"/painel/sugestoes/").append(s.id()).append("/avaliar\">").append(campoCsrf())
                            .append("<button class=\"btn verde\" type=\"submit\" name=\"decisao\" value=\"APROVEITADA\">Aproveitar na pauta</button>")
                            .append("<button class=\"btn sec\" type=\"submit\" name=\"decisao\" value=\"ARQUIVADA\">Arquivar</button></form>");
                } else if (s.estado().podeResponder()) {
                    sb.append(s.falhaNaResposta() ? "<p class=\"p-aviso erro\" role=\"alert\">A última resposta não foi enviada. A sugestão continua avaliada.</p>" : "")
                            .append("<form class=\"p-form\" method=\"post\" action=\"/painel/sugestoes/").append(s.id()).append("/responder\">").append(campoCsrf())
                            .append("<div class=\"p-campo\"><label for=\"r").append(s.id()).append("\">Resposta ao visitante</label><textarea id=\"r").append(s.id())
                            .append("\" name=\"mensagem\" placeholder=\"Deixe em branco para usar a resposta padrão\"></textarea></div>")
                            .append("<div><button class=\"btn\" type=\"submit\">Enviar resposta por e-mail</button></div></form>");
                } else {
                    sb.append("<p class=\"p-meta\">Respondida em ").append(Html.dataHora(s.respondidaEm())).append(".</p>");
                }
                sb.append("</article>\n");
            }
            return sb.toString();
        }
    }

    // ------------------------------------------------------------ UCSAL (P6)

    public static final class PainelUcsal extends VisaoPainel {
        private final List<ItemUcsal> itens;
        private final ItemUcsalForm form;
        private final Erros erros;
        private final LocalDate hoje;
        private final String falha;

        public PainelUcsal(Usuario u, String csrf, List<Sessao.Aviso> avisos, List<ItemUcsal> itens, ItemUcsalForm form, Erros erros,
                           LocalDate hoje, String falha) {
            super(u, csrf, avisos, "ucsal");
            this.itens = itens;
            this.form = form;
            this.erros = erros;
            this.hoje = hoje;
            this.falha = falha;
        }

        @Override
        protected String titulo() {
            return "UCSAL | Painel";
        }

        @Override
        protected String conteudo() {
            StringBuilder linhas = new StringBuilder();
            for (ItemUcsal i : itens) {
                linhas.append("<tr><td>").append(esc(i.titulo())).append(i.demonstracao() ? " <span class=\"p-meta\">(demonstração)</span>" : "")
                        .append("</td><td>").append(esc(i.subsecao().rotulo())).append("</td><td>")
                        .append(i.inicio() == null ? "—" : Html.data(i.inicio()) + " a " + Html.data(i.fim())).append("</td><td><span class=\"etiqueta ")
                        .append(switch (i.situacao(hoje)) {
                            case NO_AR -> "verde";
                            case AGENDADO -> "azul";
                            case ENCERRADO -> "amarelo";
                        }).append("\">").append(i.situacao(hoje).rotulo()).append("</span></td><td>").append(esc(i.fonte())).append("</td><td>")
                        .append(esc(i.conferidoPor())).append("<br><span class=\"p-meta\">").append(Html.dataHora(i.conferidoEm())).append("</span></td></tr>\n");
            }
            StringBuilder opcoes = new StringBuilder("<option value=\"\">Escolha…</option>");
            for (Subsecao s : Subsecao.values()) {
                opcoes.append("<option value=\"").append(s.name()).append('"').append(s.name().equals(form.subsecao()) ? " selected" : "").append('>')
                        .append(esc(s.rotulo())).append(s.temVigencia() ? " (com datas)" : "").append("</option>");
            }
            String seletor = "<div class=\"p-campo" + (erros.tem("subsecao") ? " erro" : "") + "\"><label for=\"f-subsecao\">Subseção</label><select id=\"f-subsecao\" name=\"subsecao\">"
                    + opcoes + "</select>" + (erros.tem("subsecao") ? "<p class=\"p-erro\">" + esc(erros.get("subsecao")) + "</p>" : "") + "</div>";
            return """
                    <h1>Seção UCSAL</h1>
                    %s%s
                    <div class="p-tabela"><table><thead><tr><th>Item</th><th>Subseção</th><th>Vigência</th><th>Situação hoje</th><th>Fonte</th><th>Conferido por</th></tr></thead>
                    <tbody>%s</tbody></table></div>
                    <h2>Novo item</h2>
                    <form class="p-form" method="post" action="/painel/ucsal" novalidate>%s%s%s%s%s
                    <div class="p-grade">%s%s</div>%s
                    <div><button class="btn" type="submit">Publicar item</button></div>
                    <p class="p-dica">Itens de "O que está rolando" saem do ar sozinhos quando a data de fim passa.</p></form>
                    """.formatted(falha.isEmpty() ? "" : "<p class=\"p-aviso erro\" role=\"alert\">" + esc(falha) + "</p>", resumoDeErros(erros), linhas,
                    campoCsrf(), seletor,
                    campo("titulo", "Título", form.titulo(), erros, "text", ""),
                    area("descricao", "Descrição", form.descricao(), erros, ""),
                    campo("fonte", "Conferido com (liga, projeto ou setor)", form.fonte(), erros, "text", ""),
                    campo("inicio", "Início (só para \"O que está rolando\")", form.inicio(), erros, "date", ""),
                    campo("fim", "Fim (só para \"O que está rolando\")", form.fim(), erros, "date", ""),
                    caixa("conferido", "Conferi estas informações com a fonte antes de publicar", form.conferido(), erros));
        }
    }

    // ------------------------------------------------------------ Demonstração (administrador)

    public static final class Demonstracao extends VisaoPainel {
        private final SimuladorDeFalhas simulador;
        private final List<Email.Enviado> emails;
        private final List<EventoDominio> atividade;
        private final List<MedidorDeChamadas.Linha> medidas;
        private final List<String> rotas;

        public Demonstracao(Usuario u, String csrf, List<Sessao.Aviso> avisos, SimuladorDeFalhas simulador, List<Email.Enviado> emails,
                            List<EventoDominio> atividade, List<MedidorDeChamadas.Linha> medidas, List<String> rotas) {
            super(u, csrf, avisos, "demonstracao");
            this.simulador = simulador;
            this.emails = emails;
            this.atividade = atividade;
            this.medidas = medidas;
            this.rotas = rotas;
        }

        @Override
        protected String titulo() {
            return "Demonstração | Painel";
        }

        @Override
        protected String conteudo() {
            StringBuilder falhas = new StringBuilder();
            for (SimuladorDeFalhas.Falha f : SimuladorDeFalhas.Falha.values()) {
                boolean ligada = simulador.ativa(f);
                falhas.append("<form class=\"p-cartao p-linha\" method=\"post\" action=\"/painel/demonstracao/falhas\">").append(campoCsrf())
                        .append("<input type=\"hidden\" name=\"falha\" value=\"").append(f.name()).append("\"><input type=\"hidden\" name=\"ligada\" value=\"")
                        .append(ligada ? "nao" : "sim").append("\"><span style=\"flex:1\">").append(esc(f.descricao())).append("</span><span class=\"etiqueta ")
                        .append(ligada ? "laranja" : "verde").append("\">").append(ligada ? "falhando" : "normal").append("</span><button class=\"btn sec\" type=\"submit\">")
                        .append(ligada ? "Desligar falha" : "Provocar falha").append("</button></form>\n");
            }
            StringBuilder cx = new StringBuilder();
            emails.stream().limit(10).forEach(e -> cx.append("<article class=\"p-cartao\"><h3>").append(esc(e.email().assunto())).append("</h3><p class=\"p-meta\">Para ")
                    .append(esc(e.email().para())).append(" · ").append(Html.dataHora(e.quando())).append("</p><pre>").append(esc(e.email().corpo())).append("</pre></article>"));
            StringBuilder at = new StringBuilder("<ul>");
            atividade.stream().limit(25).forEach(e -> at.append("<li><span class=\"p-meta\">").append(Html.dataHora(e.quando())).append("</span> ").append(esc(e.descricao())).append("</li>"));
            at.append("</ul>");
            StringBuilder med = new StringBuilder("<table><thead><tr><th>Método do serviço</th><th>Chamadas</th><th>Tempo médio (ms)</th></tr></thead><tbody>");
            medidas.forEach(m -> med.append("<tr><td>").append(esc(m.metodo())).append("</td><td>").append(m.chamadas()).append("</td><td>")
                    .append("%.2f".formatted(m.milissegundosMedios())).append("</td></tr>"));
            med.append("</tbody></table>");
            StringBuilder rt = new StringBuilder("<pre>");
            rotas.forEach(r -> rt.append(esc(r)).append('\n'));
            rt.append("</pre>");
            return """
                    <h1>Demonstração</h1>
                    <p class="p-aviso-demo">Área só para a apresentação: provoca as falhas descritas nos processos P3 e P5 para mostrar o tratamento de erros ao vivo.</p>
                    <h2>Simulador de falhas</h2>%s
                    <h2>E-mails enviados (caixa de saída simulada)</h2>%s
                    <h2>Atividade recente (Observer)</h2>%s
                    <h2>Chamadas aos serviços (Proxy dinâmico)</h2><div class="p-tabela">%s</div>
                    <h2>Rotas descobertas por reflexão (@Rota)</h2>%s
                    """.formatted(falhas, cx.isEmpty() ? "<p>Nenhum e-mail enviado ainda.</p>" : cx, at, med, rt);
        }
    }
}
