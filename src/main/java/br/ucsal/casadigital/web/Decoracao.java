package br.ucsal.casadigital.web;

import br.ucsal.casadigital.dominio.Materia;
import br.ucsal.casadigital.dominio.YouTube;

/** Peças visuais da identidade do podcast (alfinete, fitas, tesoura, ícones) e a foto das matérias. */
final class Decoracao {

    private Decoracao() {
    }

    static String alfinete(String classe) {
        return """
                <svg class="deco alfinete %s" viewBox="0 0 104 114" aria-hidden="true"><defs>
                <radialGradient id="pinTampa" cx="38%%" cy="32%%" r="75%%"><stop offset="0" stop-color="#FF9C9A"/><stop offset=".55" stop-color="#E9494C"/><stop offset="1" stop-color="#B8272D"/></radialGradient>
                <linearGradient id="pinCorpo" x1="0" x2="1"><stop offset="0" stop-color="#F0585B"/><stop offset=".6" stop-color="#CF3A3F"/><stop offset="1" stop-color="#9E2228"/></linearGradient>
                <linearGradient id="pinAgulha" x1="0" x2="1"><stop offset="0" stop-color="#F2F2F2"/><stop offset="1" stop-color="#8A8A8A"/></linearGradient></defs>
                <g transform="translate(32 28) rotate(-40)"><rect x="-1.8" y="48" width="3.6" height="63" rx="1.8" fill="url(#pinAgulha)"/>
                <path d="M-24 4 C-24 24 -17 40 -13 50 L13 50 C17 40 24 24 24 4 Z" fill="url(#pinCorpo)"/>
                <ellipse cx="0" cy="50" rx="15" ry="5.5" fill="#8C1F25"/><ellipse cx="0" cy="48" rx="15" ry="5" fill="#B8272D"/>
                <ellipse cx="0" cy="3" rx="34" ry="11" fill="#9E2228"/><ellipse cx="0" cy="0" rx="34" ry="11" fill="url(#pinTampa)"/>
                <ellipse cx="-11" cy="-3" rx="12" ry="3.2" fill="#fff" opacity=".55"/></g></svg>
                """.formatted(classe);
    }

    static String alfineteMini(String classe) {
        return "<svg class=\"deco alfinete " + classe + "\" viewBox=\"0 0 60 60\" aria-hidden=\"true\"><circle cx=\"40\" cy=\"20\" r=\"14\" fill=\"#161616\"/>"
                + "<path d=\"M32 30 L6 56\" stroke=\"#161616\" stroke-width=\"4\" stroke-linecap=\"round\"/></svg>";
    }

    static String tesoura(String classe) {
        return "<svg class=\"deco tesoura " + classe + "\" viewBox=\"0 0 120 80\" aria-hidden=\"true\">"
                + "<circle cx=\"20\" cy=\"22\" r=\"14\" fill=\"none\" stroke=\"#161616\" stroke-width=\"7\"/>"
                + "<circle cx=\"20\" cy=\"58\" r=\"14\" fill=\"none\" stroke=\"#161616\" stroke-width=\"7\"/>"
                + "<path d=\"M30 28 L112 52 L112 60 L30 40 Z\" fill=\"#161616\"/><path d=\"M30 52 L112 28 L112 20 L30 40 Z\" fill=\"#161616\"/></svg>";
    }

    static final String ICONE_INSTAGRAM = "<svg viewBox=\"0 0 40 40\" aria-hidden=\"true\"><rect x=\"4\" y=\"4\" width=\"32\" height=\"32\" rx=\"9\" fill=\"none\" stroke=\"#fff\" stroke-width=\"3.5\"/>"
            + "<circle cx=\"20\" cy=\"20\" r=\"7.5\" fill=\"none\" stroke=\"#fff\" stroke-width=\"3.5\"/><circle cx=\"29.5\" cy=\"10.5\" r=\"2.3\" fill=\"#fff\"/></svg>";
    static final String ICONE_YOUTUBE = "<svg viewBox=\"0 0 40 40\" aria-hidden=\"true\"><rect x=\"2\" y=\"8\" width=\"36\" height=\"24\" rx=\"8\" fill=\"#fff\"/><path d=\"M16 14 L27 20 L16 26 Z\" fill=\"#161616\"/></svg>";
    static final String ICONE_PLAY = "<svg viewBox=\"0 0 24 24\" aria-hidden=\"true\"><path d=\"M7 4 L20 12 L7 20 Z\"/></svg>";

    /** Fita adesiva decorativa: amarela, rosa, verde, xadrez, clara ou clara2. */
    static String fita(String tipo, String classe) {
        String arquivo = switch (tipo) {
            case "rosa" -> "tape-pink.webp";
            case "verde" -> "tape-green.webp";
            case "xadrez" -> "tape-grid-brown.webp";
            case "clara" -> "tape-clear-00.webp";
            case "clara2" -> "tape-clear-03.webp";
            default -> "tape-yellow.webp";
        };
        return "<img class=\"fita " + classe + "\" src=\"/img/" + arquivo + "\" alt=\"\" aria-hidden=\"true\" loading=\"lazy\">";
    }

    /** Capa da matéria: miniatura do vídeo no YouTube ou o quadro "FOTO" quando não há vídeo reconhecível. */
    static String foto(Materia m) {
        return m.videoId()
                .map(id -> "<img src=\"" + Html.esc(YouTube.urlDaMiniatura(id)) + "\" alt=\"\" loading=\"lazy\">")
                .orElse("<div class=\"foto-vazia\" aria-hidden=\"true\">FOTO</div>");
    }
}
