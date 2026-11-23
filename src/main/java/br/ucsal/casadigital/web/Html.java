package br.ucsal.casadigital.web;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/** Funções de apoio às páginas: escape de HTML (contra XSS) e formatação de datas em português. */
public final class Html {

    private static final DateTimeFormatter DATA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm", Locale.of("pt", "BR"));
    private static final DateTimeFormatter DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.of("pt", "BR"));
    private static final DateTimeFormatter DATA_CURTA = DateTimeFormatter.ofPattern("d 'de' MMM", Locale.of("pt", "BR"));
    private static ZoneId fuso = ZoneId.of("America/Bahia");

    private Html() {
    }

    public static void definirFuso(ZoneId novo) {
        fuso = novo;
    }

    /** Todo texto vindo do usuário passa por aqui antes de entrar na página. */
    public static String esc(Object valor) {
        if (valor == null) {
            return "";
        }
        String s = String.valueOf(valor);
        StringBuilder sb = new StringBuilder(s.length() + 16);
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '&' -> sb.append("&amp;");
                case '<' -> sb.append("&lt;");
                case '>' -> sb.append("&gt;");
                case '"' -> sb.append("&quot;");
                case '\'' -> sb.append("&#39;");
                default -> sb.append(c);
            }
        }
        return sb.toString();
    }

    public static String dataHora(Instant instante) {
        return instante == null ? "—" : DATA_HORA.format(instante.atZone(fuso));
    }

    public static String data(LocalDate data) {
        return data == null ? "—" : DATA.format(data);
    }

    public static String dataCurta(Instant instante) {
        return instante == null ? "" : DATA_CURTA.format(instante.atZone(fuso)).toUpperCase(Locale.ROOT);
    }

    /** Parágrafos a partir de texto com linhas em branco. */
    public static String paragrafos(String texto) {
        if (texto == null || texto.isBlank()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (String p : texto.trim().split("\\R\\s*\\R")) {
            sb.append("<p>").append(esc(p.trim()).replace("\n", "<br>")).append("</p>\n");
        }
        return sb.toString();
    }
}
