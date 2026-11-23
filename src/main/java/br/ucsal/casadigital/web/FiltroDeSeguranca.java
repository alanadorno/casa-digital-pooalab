package br.ucsal.casadigital.web;

import java.net.URI;

/**
 * Cabeçalhos de segurança em toda resposta (CSP, anti-clickjacking, sem adivinhação de tipo) e verificação de
 * origem nos POST: um formulário enviado a partir de outro site é recusado (defesa em profundidade contra CSRF,
 * além do cookie {@code SameSite=Strict} e do token CSRF das rotas do painel).
 */
public final class FiltroDeSeguranca implements Filtro {

    private static final String CSP = "default-src 'self'; img-src 'self' https://i.ytimg.com data:; "
            + "frame-src https://www.youtube-nocookie.com; style-src 'self' 'unsafe-inline'; script-src 'self'; "
            + "form-action 'self'; frame-ancestors 'none'; base-uri 'none'";

    @Override
    public Resposta aplicar(Requisicao requisicao, Cadeia proxima) {
        if (requisicao.metodo().equals("POST") && !origemConfiavel(requisicao)) {
            return Resposta.texto(403, "Origem não permitida.");
        }
        return proxima.seguir(requisicao)
                .comCabecalho("Content-Security-Policy", CSP)
                .comCabecalho("X-Content-Type-Options", "nosniff")
                .comCabecalho("X-Frame-Options", "DENY")
                .comCabecalho("Referrer-Policy", "strict-origin-when-cross-origin");
    }

    private boolean origemConfiavel(Requisicao requisicao) {
        String origem = requisicao.cabecalho("origin");
        String host = requisicao.cabecalho("host");
        if (origem == null || origem.equals("null") || host == null) {
            return true; // clientes sem Origin (ferramentas, testes); o token CSRF e o cookie SameSite continuam valendo
        }
        try {
            URI uri = URI.create(origem);
            String autoridade = uri.getPort() == -1 ? uri.getHost() : uri.getHost() + ":" + uri.getPort();
            return autoridade.equalsIgnoreCase(host);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
