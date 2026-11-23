package br.ucsal.casadigital.web;

import br.ucsal.casadigital.dominio.Usuario;

import java.util.List;

import static br.ucsal.casadigital.web.Html.esc;

/** Moldura do painel da equipe (RN01): menu, aviso de usuário logado, botão de sair e mensagens da sessão. */
public abstract class VisaoPainel extends Visao {

    private final Usuario usuario;
    private final String csrf;
    private final List<Sessao.Aviso> avisos;
    private final String ativa;

    protected VisaoPainel(Usuario usuario, String csrf, List<Sessao.Aviso> avisos, String ativa) {
        this.usuario = usuario;
        this.csrf = csrf;
        this.avisos = avisos;
        this.ativa = ativa;
    }

    protected abstract String conteudo();

    @Override
    protected String folhasDeEstilo() {
        return "<link rel=\"stylesheet\" href=\"/css/painel.css\">\n";
    }

    protected String campoCsrf() {
        return "<input type=\"hidden\" name=\"_csrf\" value=\"" + esc(csrf) + "\">";
    }

    @Override
    protected String corpo() {
        StringBuilder menu = new StringBuilder();
        item(menu, "/painel", "Matérias", "materias");
        item(menu, "/painel/sugestoes", "Sugestões", "sugestoes");
        item(menu, "/painel/ucsal", "UCSAL", "ucsal");
        if (usuario.administrador()) {
            item(menu, "/painel/demonstracao", "Demonstração", "demonstracao");
        }
        StringBuilder msgs = new StringBuilder();
        for (Sessao.Aviso a : avisos) {
            msgs.append("<p class=\"p-aviso ").append(esc(a.tipo())).append("\" role=\"").append(a.tipo().equals("erro") ? "alert" : "status")
                    .append("\">").append(esc(a.texto())).append("</p>\n");
        }
        return """
                <a class="pular" href="#conteudo">Pular para o conteúdo</a>
                <header class="p-topo"><strong>Painel da equipe</strong>
                <nav aria-label="Painel" class="p-linha">%s<a href="/" target="_blank" rel="noopener">Ver o site</a></nav>
                <form method="post" action="/painel/sair"><span>%s</span>%s<button class="btn sec" type="submit">Sair</button></form></header>
                <main id="conteudo" class="p-main" tabindex="-1">
                %s%s
                </main>
                """.formatted(menu, esc(usuario.nome()), campoCsrf(), msgs, conteudo());
    }

    private void item(StringBuilder sb, String destino, String rotulo, String id) {
        sb.append("<a href=\"").append(destino).append('"').append(id.equals(ativa) ? " aria-current=\"page\"" : "").append('>')
                .append(rotulo).append("</a>");
    }
}
