package br.ucsal.casadigital.dominio;

import java.net.URI;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * RN06: o vídeo fica hospedado no YouTube e é apenas incorporado. Esta classe utilitária
 * extrai o identificador do vídeo a partir dos formatos de link mais comuns.
 */
public final class YouTube {

    private static final Pattern ID_VALIDO = Pattern.compile("^[A-Za-z0-9_-]{11}$");

    private YouTube() {
    }

    public static Optional<String> extrairId(String link) {
        if (link == null || link.isBlank()) {
            return Optional.empty();
        }
        try {
            URI uri = URI.create(link.trim());
            String esquema = uri.getScheme();
            String host = uri.getHost();
            if (host == null || esquema == null || !(esquema.equals("http") || esquema.equals("https"))) {
                return Optional.empty();
            }
            host = host.toLowerCase().replaceFirst("^(www|m)\\.", "");
            String caminho = uri.getPath() == null ? "" : uri.getPath();
            String id = null;
            if (host.equals("youtu.be")) {
                id = primeiroSegmento(caminho, 1);
            } else if (host.equals("youtube.com") || host.equals("youtube-nocookie.com")) {
                if (caminho.equals("/watch")) {
                    id = parametro(uri.getQuery(), "v");
                } else if (caminho.matches("^/(embed|shorts|live)/.*")) {
                    id = primeiroSegmento(caminho, 2);
                }
            }
            return Optional.ofNullable(id).filter(i -> ID_VALIDO.matcher(i).matches());
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    public static String urlDeIncorporacao(String id) {
        return "https://www.youtube-nocookie.com/embed/" + id;
    }

    public static String urlDaMiniatura(String id) {
        return "https://i.ytimg.com/vi/" + id + "/hqdefault.jpg";
    }

    private static String primeiroSegmento(String caminho, int indice) {
        String[] partes = caminho.split("/");
        return partes.length > indice ? partes[indice] : null;
    }

    private static String parametro(String consulta, String nome) {
        if (consulta == null) {
            return null;
        }
        for (String par : consulta.split("&")) {
            int igual = par.indexOf('=');
            if (igual > 0 && par.substring(0, igual).equals(nome)) {
                return par.substring(igual + 1);
            }
        }
        return null;
    }
}
