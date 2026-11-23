package br.ucsal.casadigital.web;

import br.ucsal.casadigital.dominio.Editoria;
import br.ucsal.casadigital.servico.SugestaoForm;
import br.ucsal.casadigital.validacao.Erros;

import static br.ucsal.casadigital.web.Html.esc;

/** Moldura das páginas do site público: topo com o menu, miolo, bloco de contato e logos. */
public abstract class VisaoPublica extends Visao {

    /** Estado do bloco "Contato": valores digitados, erros por campo e a mensagem de resultado. */
    public record EstadoContato(SugestaoForm valores, Erros erros, String tipo, String mensagem) {
        public static EstadoContato vazio() {
            return new EstadoContato(SugestaoForm.vazio(), Erros.novo(), "parado", "");
        }
    }

    /** Endereços das redes do podcast; definidos uma vez na inicialização. */
    public record Redes(String instagram, String youtube) {
    }

    private static volatile Redes redes = new Redes("#", "#");

    public static void definirRedes(Redes novas) {
        redes = novas;
    }

    private final EstadoContato contato;

    protected VisaoPublica(EstadoContato contato) {
        this.contato = contato;
    }

    protected VisaoPublica() {
        this(EstadoContato.vazio());
    }

    /** Identificador da aba ativa: "home", o slug de uma editoria, "ucsal" ou "sobre". */
    protected abstract String ativa();

    protected abstract String miolo();

    protected boolean manchada() {
        return false;
    }

    @Override
    protected String corpo() {
        return "<div class=\"pagina " + (manchada() ? "manchada" : "") + "\">\n"
                + "<a class=\"pular\" href=\"#conteudo\">Pular para o conteúdo</a>\n"
                + topo()
                + "<main id=\"conteudo\" class=\"miolo\" tabindex=\"-1\">\n" + miolo() + "\n</main>\n"
                + blocoDeContato()
                + "</div>";
    }

    private String topo() {
        StringBuilder abas = new StringBuilder();
        aba(abas, "/", "HOME", "laranja", "home");
        for (Editoria e : Editoria.values()) {
            if (e != Editoria.UCSAL) {
                aba(abas, "/editoria/" + e.slug(), e.aba(), e.cor(), e.slug());
            }
        }
        aba(abas, "/ucsal", "UCSAL", "amarelo", "ucsal");
        aba(abas, "/sobre-nos", "SOBRE NÓS", "amarelo", "sobre");
        return """
                <header class="topo">
                <a href="/" class="logo" aria-label="Isso Não É Um Rádio – página inicial"><img src="/img/logo-radio.webp" alt="Isso não é um rádio" width="900" height="456"></a>
                <button class="menu-btn" type="button" aria-expanded="false" aria-controls="abas">MENU</button>
                <nav aria-label="Principal" style="display:contents"><ul id="abas" class="abas">
                %s</ul></nav>
                </header>
                """.formatted(abas);
    }

    private void aba(StringBuilder sb, String destino, String rotulo, String cor, String id) {
        boolean atual = ativa().equals(id);
        boolean cheia = ativa().equals("home") || atual;
        sb.append("<li><a href=\"").append(destino).append("\" class=\"aba c-").append(cor).append(' ')
                .append(cheia ? "cheia" : "contorno").append('"').append(atual ? " aria-current=\"page\"" : "")
                .append('>').append(esc(rotulo)).append("</a></li>\n");
    }

    private String blocoDeContato() {
        SugestaoForm v = contato.valores();
        Erros e = contato.erros();
        String status = switch (contato.tipo()) {
            case "ok" -> "<p class=\"msg-ok\" role=\"status\">" + esc(contato.mensagem()) + "</p>";
            case "erro" -> "<p class=\"msg-falha\" role=\"alert\">" + esc(contato.mensagem()) + "</p>";
            case "falha" -> "<p class=\"msg-falha\" role=\"alert\">" + esc(contato.mensagem()) + " Os dados que você digitou foram mantidos.</p>";
            default -> "";
        };
        return """
                <div class="logos"><img src="/img/logo-ucsal.webp" alt="Universidade Católica do Salvador e WebTV UCSAL" width="700" height="164"></div>
                <section class="recorte" aria-labelledby="contato-t">
                <div class="linha">%s<span class="legenda">RECORTE AQUI</span></div>
                <div class="contato">
                %s%s%s%s
                <form class="contato-grade" method="post" action="/sugestoes#contato-t" novalidate>
                <div><h2 id="contato-t" tabindex="-1">Contato</h2>
                <p class="chamada">Gostaria de dar uma sugestão ao nosso podcast?</p>
                <p class="social">
                <a class="ic" href="%s" target="_blank" rel="noopener noreferrer" aria-label="Instagram (abre em nova aba)">%s</a>
                <a class="ic" href="%s" target="_blank" rel="noopener noreferrer" aria-label="YouTube (abre em nova aba)">%s</a>
                <a href="%s" target="_blank" rel="noopener noreferrer">@issonaoeumradio<span class="sr"> (abre o Instagram em nova aba)</span></a></p></div>
                <div class="contato-campos">%s%s%s</div>
                <div class="col-sug">%s<div class="acoes"><button class="btn" type="submit">Enviar</button></div></div>
                <div class="status" aria-live="polite">%s</div>
                </form></div></section>
                """.formatted(
                Decoracao.tesoura("tesoura"),
                Decoracao.alfineteMini("alfinete"), Decoracao.fita("clara", "tira1"), Decoracao.fita("xadrez", "tira2"),
                Decoracao.fita("clara2", "tira3"),
                esc(redes.instagram()), Decoracao.ICONE_INSTAGRAM, esc(redes.youtube()), Decoracao.ICONE_YOUTUBE, esc(redes.instagram()),
                campo("nome", "Nome", v.nome(), e, "", false), campo("sobrenome", "Sobrenome", v.sobrenome(), e, "", false),
                campo("email", "E-mail", v.email(), e, "largo", false),
                campo("sugestao", "Sugestão", v.sugestao(), e, "", true), status);
    }

    private static String campo(String nome, String rotulo, String valor, Erros erros, String classe, boolean area) {
        boolean temErro = erros.tem(nome);
        String ligacao = temErro ? " aria-invalid=\"true\" aria-describedby=\"e-" + nome + "\"" : "";
        String entrada = area
                ? "<textarea id=\"c-" + nome + "\" name=\"" + nome + "\" maxlength=\"2000\"" + ligacao + ">" + esc(valor) + "</textarea>"
                : "<input id=\"c-" + nome + "\" name=\"" + nome + "\" value=\"" + esc(valor) + "\" type=\"" + (nome.equals("email") ? "email" : "text")
                + "\"" + (nome.equals("email") ? " inputmode=\"email\" autocomplete=\"email\"" : "") + ligacao + ">";
        return "<div class=\"campo " + classe + (temErro ? " erro" : "") + "\"><label for=\"c-" + nome + "\">" + rotulo + "</label>" + entrada
                + (temErro ? "<p class=\"msg-erro\" id=\"e-" + nome + "\">" + esc(erros.get(nome)) + "</p>" : "") + "</div>";
    }
}
