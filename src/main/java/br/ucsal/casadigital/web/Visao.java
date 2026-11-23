package br.ucsal.casadigital.web;

/**
 * Padrão <b>Template Method</b>: {@link #renderizar()} define o esqueleto fixo de qualquer página HTML
 * (documento, cabeça, corpo) e deixa para as subclasses os passos que mudam ({@link #titulo()},
 * {@link #corpo()}). Páginas públicas e do painel herdam deste esqueleto e só preenchem as lacunas.
 */
public abstract class Visao {

    public final String renderizar() {
        return "<!doctype html>\n<html lang=\"pt-BR\">\n<head>\n" + cabeca() + "</head>\n<body" + classeDoCorpo() + ">\n"
                + corpo() + "\n</body>\n</html>\n";
    }

    protected abstract String titulo();

    protected abstract String corpo();

    protected String folhasDeEstilo() {
        return "<link rel=\"stylesheet\" href=\"/css/app.css\">\n";
    }

    protected String classeDoCorpo() {
        return "";
    }

    private String cabeca() {
        return """
                <meta charset="utf-8">
                <meta name="viewport" content="width=device-width, initial-scale=1">
                <meta name="color-scheme" content="light">
                <title>%s</title>
                %s<script src="/js/app.js" defer></script>
                <noscript><style>.abas{display:flex!important}.menu-btn{display:none!important}</style></noscript>
                """.formatted(Html.esc(titulo()), folhasDeEstilo());
    }
}
