package br.ucsal.casadigital.web;

import br.ucsal.casadigital.dominio.Usuario;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Padrão <b>Adapter</b>: traduz o {@link HttpExchange} do JDK para uma interface simples e própria do
 * sistema. Os controladores e filtros não conhecem {@code com.sun.net.httpserver}, o que também permite
 * criá-la nos testes sem abrir um servidor ({@link #simulada}).
 */
public final class Requisicao {

    private static final int LIMITE_CORPO = 300_000;

    private final String metodo;
    private final String caminho;
    private final Map<String, String> consulta;
    private final Map<String, String> formulario;
    private final Map<String, String> cabecalhos;
    private final Map<String, String> cookies;
    private final String origem;
    private final Map<String, String> parametrosDoCaminho = new HashMap<>();
    private Sessao sessao;
    private Usuario usuario;

    private Requisicao(String metodo, String caminho, Map<String, String> consulta, Map<String, String> formulario,
                       Map<String, String> cabecalhos, String origem) {
        this.metodo = metodo;
        this.caminho = caminho.length() > 1 && caminho.endsWith("/") ? caminho.substring(0, caminho.length() - 1) : caminho;
        this.consulta = consulta;
        this.formulario = formulario;
        this.cabecalhos = cabecalhos;
        this.cookies = lerCookies(cabecalhos.getOrDefault("cookie", ""));
        this.origem = origem;
    }

    public static Requisicao de(HttpExchange troca) throws IOException {
        Map<String, String> cabecalhos = new HashMap<>();
        troca.getRequestHeaders().forEach((k, v) -> cabecalhos.put(k.toLowerCase(), String.join("; ", v)));
        String corpo = "";
        String tipo = cabecalhos.getOrDefault("content-type", "");
        try (InputStream in = troca.getRequestBody()) {
            byte[] bytes = in.readNBytes(LIMITE_CORPO + 1);
            if (bytes.length > LIMITE_CORPO) {
                throw new IOException("Conteúdo grande demais");
            }
            if (tipo.startsWith("application/x-www-form-urlencoded")) {
                corpo = new String(bytes, StandardCharsets.UTF_8);
            }
        }
        return new Requisicao(troca.getRequestMethod(), troca.getRequestURI().getPath(),
                decodificar(troca.getRequestURI().getRawQuery()), decodificar(corpo), cabecalhos,
                troca.getRemoteAddress().getAddress().getHostAddress());
    }

    /** Requisição montada à mão, para testes de unidade. */
    public static Requisicao simulada(String metodo, String caminho, Map<String, String> formulario) {
        return new Requisicao(metodo, caminho, Map.of(), formulario, Map.of(), "127.0.0.1");
    }

    static Map<String, String> decodificar(String texto) {
        Map<String, String> mapa = new LinkedHashMap<>();
        if (texto == null || texto.isEmpty()) {
            return mapa;
        }
        for (String par : texto.split("&")) {
            int igual = par.indexOf('=');
            String chave = igual < 0 ? par : par.substring(0, igual);
            String valor = igual < 0 ? "" : par.substring(igual + 1);
            mapa.put(URLDecoder.decode(chave, StandardCharsets.UTF_8), URLDecoder.decode(valor, StandardCharsets.UTF_8));
        }
        return mapa;
    }

    private static Map<String, String> lerCookies(String cabecalho) {
        Map<String, String> mapa = new HashMap<>();
        for (String parte : cabecalho.split(";")) {
            int igual = parte.indexOf('=');
            if (igual > 0) {
                mapa.put(parte.substring(0, igual).trim(), parte.substring(igual + 1).trim());
            }
        }
        return mapa;
    }

    public String metodo() {
        return metodo;
    }

    public String caminho() {
        return caminho;
    }

    public String origem() {
        return origem;
    }

    public String cabecalho(String nome) {
        return cabecalhos.get(nome.toLowerCase());
    }

    public String cookie(String nome) {
        return cookies.get(nome);
    }

    /** Parâmetro do caminho ({@code /materia/{id}}), do formulário ou da URL, nesta ordem de prioridade. */
    public String parametro(String nome) {
        String valor = parametrosDoCaminho.get(nome);
        if (valor == null) {
            valor = formulario.get(nome);
        }
        if (valor == null) {
            valor = consulta.get(nome);
        }
        return valor;
    }

    public String parametro(String nome, String padrao) {
        String valor = parametro(nome);
        return valor == null ? padrao : valor;
    }

    public long parametroLong(String nome, long padrao) {
        try {
            return Long.parseLong(parametro(nome, ""));
        } catch (NumberFormatException e) {
            return padrao;
        }
    }

    public Map<String, String> formulario() {
        return formulario;
    }

    void definirParametrosDoCaminho(Map<String, String> valores) {
        parametrosDoCaminho.clear();
        parametrosDoCaminho.putAll(valores);
    }

    public Optional<Sessao> sessao() {
        return Optional.ofNullable(sessao);
    }

    void definirSessao(Sessao sessao, Usuario usuario) {
        this.sessao = sessao;
        this.usuario = usuario;
    }

    public Optional<Usuario> usuario() {
        return Optional.ofNullable(usuario);
    }
}
