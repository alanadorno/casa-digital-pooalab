package br.ucsal.casadigital.web;

import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

/** Resposta HTTP imutável do ponto de vista dos controladores; os filtros podem acrescentar cabeçalhos. */
public final class Resposta {

    private final int status;
    private final Map<String, String> cabecalhos = new LinkedHashMap<>();
    private final byte[] corpo;

    private Resposta(int status, String tipo, byte[] corpo) {
        this.status = status;
        this.corpo = corpo;
        if (tipo != null) {
            cabecalhos.put("Content-Type", tipo);
        }
    }

    public static Resposta html(int status, String html) {
        return new Resposta(status, "text/html; charset=utf-8", html.getBytes(StandardCharsets.UTF_8));
    }

    public static Resposta html(String html) {
        return html(200, html);
    }

    public static Resposta texto(int status, String texto) {
        return new Resposta(status, "text/plain; charset=utf-8", texto.getBytes(StandardCharsets.UTF_8));
    }

    public static Resposta arquivo(String tipo, byte[] bytes) {
        Resposta r = new Resposta(200, tipo, bytes);
        r.cabecalhos.put("Cache-Control", "public, max-age=3600");
        return r;
    }

    public static Resposta redirecionar(String destino) {
        Resposta r = new Resposta(303, null, new byte[0]);
        r.cabecalhos.put("Location", destino);
        return r;
    }

    public Resposta comCabecalho(String nome, String valor) {
        cabecalhos.put(nome, valor);
        return this;
    }

    public Resposta comCookie(String nome, String valor, int segundos) {
        cabecalhos.put("Set-Cookie", "%s=%s; Path=/; Max-Age=%d; HttpOnly; SameSite=Strict".formatted(nome, valor, segundos));
        return this;
    }

    public int status() {
        return status;
    }

    public String cabecalho(String nome) {
        return cabecalhos.get(nome);
    }

    public String corpoComoTexto() {
        return new String(corpo, StandardCharsets.UTF_8);
    }

    void enviar(HttpExchange troca, boolean semCorpo) throws IOException {
        cabecalhos.forEach((k, v) -> troca.getResponseHeaders().add(k, v));
        if (semCorpo || corpo.length == 0) {
            troca.sendResponseHeaders(status, -1);
        } else {
            troca.sendResponseHeaders(status, corpo.length);
            try (OutputStream out = troca.getResponseBody()) {
                out.write(corpo);
            }
        }
        troca.close();
    }
}
