package br.ucsal.casadigital.web;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

/** Entrega CSS, JS, imagens e fontes empacotados no classpath. Se o caminho não for estático, passa adiante. */
public final class FiltroDeArquivosEstaticos implements Filtro {

    private static final Map<String, String> TIPOS = Map.of(
            "css", "text/css; charset=utf-8", "js", "text/javascript; charset=utf-8", "png", "image/png",
            "webp", "image/webp", "jpg", "image/jpeg", "jpeg", "image/jpeg", "svg", "image/svg+xml",
            "otf", "font/otf", "ttf", "font/ttf", "woff2", "font/woff2");

    @Override
    public Resposta aplicar(Requisicao requisicao, Cadeia proxima) {
        String caminho = requisicao.caminho();
        if (!requisicao.metodo().equals("GET") || !caminho.matches("^/(css|js|img|fonts)/[\\w./-]+$") || caminho.contains("..")) {
            return proxima.seguir(requisicao);
        }
        try (InputStream in = getClass().getResourceAsStream("/static" + caminho)) {
            if (in == null) {
                return Resposta.texto(404, "Arquivo não encontrado.");
            }
            String extensao = caminho.substring(caminho.lastIndexOf('.') + 1).toLowerCase();
            return Resposta.arquivo(TIPOS.getOrDefault(extensao, "application/octet-stream"), in.readAllBytes());
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }
}
