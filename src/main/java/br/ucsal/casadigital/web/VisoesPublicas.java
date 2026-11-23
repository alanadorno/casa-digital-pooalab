package br.ucsal.casadigital.web;

import br.ucsal.casadigital.dominio.Editoria;
import br.ucsal.casadigital.dominio.ItemUcsal;
import br.ucsal.casadigital.dominio.Materia;
import br.ucsal.casadigital.dominio.Subsecao;
import br.ucsal.casadigital.servico.Home;
import br.ucsal.casadigital.servico.Pagina;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static br.ucsal.casadigital.web.Html.esc;

/** Páginas do site público. Cada classe só preenche o que muda no esqueleto de {@link VisaoPublica}. */
public final class VisoesPublicas {

    private VisoesPublicas() {
    }

    private static String titulo(String parte) {
        return parte == null || parte.isBlank() ? "Isso Não É Um Rádio" : parte + " | Isso Não É Um Rádio";
    }

    // ------------------------------------------------------------------ Home

    public static final class PaginaInicial extends VisaoPublica {
        private final Home home;

        public PaginaInicial(Home home) {
            this.home = home;
        }

        @Override
        protected String titulo() {
            return VisoesPublicas.titulo("");
        }

        @Override
        protected String ativa() {
            return "home";
        }

        @Override
        protected String miolo() {
            if (home.principal().isEmpty()) {
                return "<div class=\"vazio\"><h2>Em breve por aqui</h2><p>Ainda não há matérias publicadas.</p></div>";
            }
            Materia p = home.principal().get();
            String link = "/materia/" + p.id();
            StringBuilder lateral = new StringBuilder();
            String[] fitas = {"clara", "rosa", "verde"};
            int i = 0;
            for (Materia m : home.secundarias()) {
                lateral.append("<a href=\"/materia/").append(m.id()).append("\" class=\"cartao\">")
                        .append(Decoracao.fita(fitas[i++ % 3], "fita"))
                        .append("<div><h3>").append(esc(m.titulo())).append("</h3>")
                        .append(m.resumo().isBlank() ? "" : "<p>" + esc(m.resumo()) + "</p>")
                        .append("</div><div class=\"cartao-foto\">").append(Decoracao.foto(m)).append("</div></a>\n");
            }
            return """
                    <div class="home-grade">
                    <article class="tv" aria-labelledby="destaque-t">
                    %s%s%s
                    <a href="%s" class="tv-foto" aria-label="Abrir: %s" tabindex="-1">%s</a>
                    <h1 id="destaque-t"><a href="%s">%s%s</a></h1>
                    %s
                    <div class="player"><a href="%s" class="play" aria-label="Assistir ao episódio">%s</a>
                    <div class="barra" aria-hidden="true"><div class="trilho"></div></div>
                    <a href="%s" class="btn">Assistir agora</a></div>
                    </article>
                    <aside class="lateral" aria-label="Mais matérias">
                    %s</aside>
                    </div>
                    """.formatted(Decoracao.alfinete("alfinete"), Decoracao.fita("amarela", "fita-a"), Decoracao.fita("xadrez", "fita-b"),
                    link, esc(p.titulo()), Decoracao.foto(p),
                    link, p.episodio() > 0 ? "Episódio #" + p.episodio() + ": " : "", esc(p.titulo()),
                    p.resumo().isBlank() ? "" : "<p class=\"resumo\">" + esc(p.resumo()) + "</p>",
                    link, Decoracao.ICONE_PLAY, link, lateral);
        }
    }

    // ------------------------------------------------------------------ Editoria

    public static final class PaginaDeEditoria extends VisaoPublica {
        private final Editoria editoria;
        private final Pagina<Materia> pagina;

        public PaginaDeEditoria(Editoria editoria, Pagina<Materia> pagina) {
            this.editoria = editoria;
            this.pagina = pagina;
        }

        @Override
        protected String titulo() {
            return VisoesPublicas.titulo(editoria.nome());
        }

        @Override
        protected String ativa() {
            return editoria.slug();
        }

        @Override
        protected String miolo() {
            StringBuilder sb = new StringBuilder("<h1 class=\"titulo-pagina\">" + esc(editoria.nome()) + "</h1>\n");
            if (pagina.itens().isEmpty()) {
                return sb.append("<div class=\"vazio\"><h2>Ainda não há matérias aqui</h2><p>Volte em breve, estamos preparando as próximas.</p></div>").toString();
            }
            sb.append("<div class=\"polaroides\">\n");
            for (Materia m : pagina.itens()) {
                sb.append("<a href=\"/materia/").append(m.id()).append("\" class=\"polaroid\"><div class=\"foto\">").append(Decoracao.foto(m))
                        .append(m.episodio() > 0 ? "<span class=\"ep\">EP. %02d</span>".formatted(m.episodio()) : "")
                        .append("</div><h3>").append(esc(m.titulo())).append("</h3>")
                        .append(m.resumo().isBlank() ? "" : "<p>" + esc(m.resumo()) + "</p>").append("</a>\n");
            }
            sb.append("</div>\n");
            if (pagina.totalDePaginas() > 1) {
                sb.append("<nav class=\"paginacao\" aria-label=\"Páginas\">");
                for (int n = 1; n <= pagina.totalDePaginas(); n++) {
                    String href = "/editoria/" + editoria.slug() + (n == 1 ? "" : "?pagina=" + n);
                    sb.append("<a href=\"").append(href).append('"').append(n == pagina.numero() ? " aria-current=\"page\"" : "")
                            .append(" aria-label=\"Página ").append(n).append("\">").append(n).append("</a>");
                }
                sb.append("</nav>\n");
            }
            return sb.toString();
        }
    }

    // ------------------------------------------------------------------ Matéria

    public static final class PaginaDeMateria extends VisaoPublica {
        private final Materia materia;

        public PaginaDeMateria(Materia materia) {
            this.materia = materia;
        }

        @Override
        protected String titulo() {
            return VisoesPublicas.titulo(materia.titulo());
        }

        @Override
        protected String ativa() {
            return materia.editoria().slug();
        }

        @Override
        protected boolean manchada() {
            return true;
        }

        @Override
        protected String miolo() {
            StringBuilder refs = new StringBuilder();
            materia.referencias().forEach(r -> refs.append("<li>").append(esc(r)).append("</li>"));
            String video = materia.videoId()
                    .map(id -> "<div class=\"video\"><div class=\"quadro\"><iframe title=\"Vídeo: " + esc(materia.titulo())
                            + "\" src=\"https://www.youtube-nocookie.com/embed/" + esc(id)
                            + "\" allow=\"accelerometer; encrypted-media; gyroscope; picture-in-picture; fullscreen\" allowfullscreen loading=\"lazy\""
                            + " referrerpolicy=\"strict-origin-when-cross-origin\"></iframe></div></div>")
                    .orElse("");
            return """
                    <article class="materia">
                    <a href="/editoria/%s" class="voltar">Voltar para %s</a>
                    <h1>%s</h1>
                    %s
                    %s
                    <div class="corpo">%s</div>
                    <section class="refs" aria-labelledby="refs-t"><h2 id="refs-t">Referências</h2><ul>%s</ul></section>
                    </article>
                    """.formatted(materia.editoria().slug(), esc(materia.editoria().nome()), esc(materia.titulo()),
                    materia.resumo().isBlank() ? "" : "<p class=\"lide\">" + esc(materia.resumo()) + "</p>",
                    video, Html.paragrafos(materia.texto()), refs);
        }
    }

    // ------------------------------------------------------------------ UCSAL

    public static final class PaginaUcsal extends VisaoPublica {
        private final Map<Subsecao, List<ItemUcsal>> itens;

        public PaginaUcsal(Map<Subsecao, List<ItemUcsal>> itens) {
            this.itens = itens;
        }

        @Override
        protected String titulo() {
            return VisoesPublicas.titulo("UCSAL");
        }

        @Override
        protected String ativa() {
            return "ucsal";
        }

        @Override
        protected String miolo() {
            StringBuilder sobre = new StringBuilder();
            itens.get(Subsecao.SOBRE_A_INSTITUICAO).forEach(i -> sobre.append("<div><h2>").append(esc(i.titulo())).append("</h2><p>")
                    .append(esc(i.descricao())).append("</p></div>"));
            if (sobre.isEmpty()) {
                sobre.append("<p>Em breve, as informações sobre a universidade, conferidas com a instituição.</p>");
            }
            Optional<java.time.Instant> ultima = itens.values().stream().flatMap(List::stream)
                    .map(ItemUcsal::conferidoEm).max(java.util.Comparator.naturalOrder());
            return """
                    <section class="u-hero"><div class="u-hero-txt">
                    <span class="u-etiqueta">Seção UCSAL</span>
                    <h1 class="titulo-pagina">UCSAL: onde a <mark>Comunicação</mark> ganha voz</h1>
                    <div class="u-papel">%s%s</div>
                    <ul class="u-nums" aria-label="Resumo da seção">
                    <li><strong>%d</strong><span>Ligas</span></li><li><strong>%d</strong><span>Projetos</span></li><li><strong>%d</strong><span>No ar agora</span></li>
                    </ul></div></section>
                    %s%s%s
                    %s
                    <p class="u-mais"><a href="/editoria/ucsal" class="btn">Ver as matérias da UCSAL</a></p>
                    """.formatted(Decoracao.fita("amarela", "fita"), sobre,
                    itens.get(Subsecao.LIGAS).size(), itens.get(Subsecao.PROJETOS).size(), itens.get(Subsecao.O_QUE_ESTA_ROLANDO).size(),
                    bloco("ligas", "Ligas", "Grupos de estudo e prática conduzidos por estudantes.", itens.get(Subsecao.LIGAS), "Nenhuma liga cadastrada ainda."),
                    bloco("projetos", "Projetos", "Iniciativas de extensão e experimentação da universidade.", itens.get(Subsecao.PROJETOS), "Nenhum projeto cadastrado ainda."),
                    bloco("rolando", "O que está rolando", "Eventos e atividades em andamento. Saem do ar sozinhos quando a data termina.",
                            itens.get(Subsecao.O_QUE_ESTA_ROLANDO), "Nada acontecendo no momento. Volte em breve."),
                    ultima.map(u -> "<p class=\"u-conferido\"><span aria-hidden=\"true\">✔</span> Informações conferidas com as fontes. Última conferência em "
                            + Html.dataHora(u) + ".</p>").orElse(""));
        }

        private static String bloco(String id, String titulo, String sub, List<ItemUcsal> lista, String vazio) {
            StringBuilder sb = new StringBuilder("<section class=\"u-bloco\" aria-labelledby=\"g-" + id + "\"><div class=\"u-cab\"><h2 id=\"g-" + id + "\">"
                    + esc(titulo) + "</h2><p>" + esc(sub) + "</p></div>");
            if (lista.isEmpty()) {
                return sb.append("<p class=\"leve u-vazio\">").append(esc(vazio)).append("</p></section>\n").toString();
            }
            sb.append("<div class=\"u-grade n").append(Math.min(lista.size(), 4)).append("\">");
            String[] fitas = {"clara", "rosa", "verde"};
            int k = 0;
            for (ItemUcsal i : lista) {
                sb.append("<article class=\"u-card\">").append(Decoracao.fita(fitas[k++ % 3], "fita"))
                        .append("<span class=\"u-tag c-").append(i.subsecao().cor()).append("\">").append(esc(i.subsecao().rotulo())).append("</span>");
                if (i.subsecao().temVigencia() && i.inicio() != null) {
                    sb.append("<p class=\"u-datas\">").append(Html.data(i.inicio())).append(" A ").append(Html.data(i.fim())).append("</p>");
                }
                sb.append("<h3>").append(esc(i.titulo())).append("</h3><p class=\"u-desc\">").append(esc(i.descricao())).append("</p></article>");
            }
            return sb.append("</div></section>\n").toString();
        }
    }

    // ------------------------------------------------------------------ Sobre nós

    public static final class PaginaSobre extends VisaoPublica {
        private final int totalDeMaterias;

        public PaginaSobre(int totalDeMaterias) {
            this.totalDeMaterias = totalDeMaterias;
        }

        @Override
        protected String titulo() {
            return VisoesPublicas.titulo("Sobre nós");
        }

        @Override
        protected String ativa() {
            return "sobre";
        }

        @Override
        protected String miolo() {
            String[][] blocos = {
                    {"laranja", "01", "Episódios em vídeo", "Cada conversa do podcast chega aqui com o vídeo do YouTube, pronto para assistir sem sair da página."},
                    {"azul", "02", "Texto e referências", "Cada episódio vira matéria: texto completo e as fontes usadas, para você aprofundar o assunto."},
                    {"verde", "03", "Seção UCSAL", "Ligas, projetos e o que está rolando na universidade, sempre conferido com quem organiza."},
                    {"amarelo", "04", "Sua pauta", "Sem cadastro e sem senha: é só escrever a sua sugestão no bloco de contato e a equipe responde."}};
            StringBuilder sb = new StringBuilder();
            for (String[] b : blocos) {
                sb.append("<article class=\"s-bloco c-").append(b[0]).append("\"><span class=\"s-num\">").append(b[1]).append("</span><h2>")
                        .append(esc(b[2])).append("</h2><p>").append(esc(b[3])).append("</p></article>\n");
            }
            return """
                    <section class="s-hero"><div class="s-papel">%s%s
                    <p class="s-etiqueta">Sobre nós</p>
                    <h1 class="s-titulo">Isso Não é Um Rádio <small class="piada">(mas podia ser)</small></h1>
                    <p class="s-sub">A voz dos alunos da UCSAL</p>
                    <p class="s-lide">Um podcast feito por estudantes de Comunicação da UCSAL, que ganhou uma casa digital: vídeo, texto, referências e um canal aberto para você sugerir o próximo tema.</p>
                    </div></section>
                    <section class="s-blocos" aria-label="O que você encontra aqui">%s</section>
                    <section class="s-nums" aria-label="Em números">
                    <div><strong>%d</strong><span>matérias no ar</span></div><div><strong>6</strong><span>editorias</span></div><div><strong>0</strong><span>cadastros para participar</span></div>
                    </section>
                    <section class="s-chamada">%s<h2>Tem uma ideia de pauta?</h2>
                    <p>Conta pra gente. Toda sugestão é lida pela equipe, avaliada e, se der, vira episódio.</p>
                    <a class="btn" href="#contato-t">Sugerir uma pauta</a></section>
                    """.formatted(Decoracao.alfinete("pin"), Decoracao.fita("amarela", "fita"), sb, totalDeMaterias, Decoracao.fita("xadrez", "fita"));
        }
    }

    // ------------------------------------------------------------------ Resultado do envio da sugestão

    public static final class PaginaDeSugestao extends VisaoPublica {
        public PaginaDeSugestao(EstadoContato estado) {
            super(estado);
        }

        @Override
        protected String titulo() {
            return VisoesPublicas.titulo("Sugestão de pauta");
        }

        @Override
        protected String ativa() {
            return "home";
        }

        @Override
        protected String miolo() {
            return "<div class=\"vazio\"><h2>Sugestão de pauta</h2><p>O resultado do envio está logo abaixo, no bloco de contato.</p>"
                    + "<a href=\"/\" class=\"btn reto\">Voltar para a home</a></div>";
        }
    }

    // ------------------------------------------------------------------ Erros (404, 403, 500)

    public static final class Erro extends VisaoPublica {
        private final int codigo;
        private final String titulo;
        private final String mensagem;

        public Erro(int codigo, String titulo, String mensagem) {
            this.codigo = codigo;
            this.titulo = titulo;
            this.mensagem = mensagem;
        }

        @Override
        protected String titulo() {
            return VisoesPublicas.titulo(titulo);
        }

        @Override
        protected String ativa() {
            return "home";
        }

        @Override
        protected String miolo() {
            return "<div class=\"vazio\" role=\"alert\"><h2>" + esc(titulo) + "</h2><p>" + esc(mensagem) + "</p>"
                    + "<a href=\"/\" class=\"btn reto\">Voltar para a home</a><p class=\"leve\">Código " + codigo + "</p></div>";
        }
    }
}
