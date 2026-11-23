package br.ucsal.casadigital.web;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/** Sessão de um membro da equipe logado (RN01): quem é, quando expira, token CSRF e avisos para a próxima tela. */
public final class Sessao {

    /** Aviso exibido uma única vez na próxima tela do painel. */
    public record Aviso(String tipo, String texto) {
    }

    private final String token;
    private final long usuarioId;
    private final String csrf;
    private final Instant expiraEm;
    private final List<Aviso> avisos = new CopyOnWriteArrayList<>();

    Sessao(String token, long usuarioId, String csrf, Instant expiraEm) {
        this.token = token;
        this.usuarioId = usuarioId;
        this.csrf = csrf;
        this.expiraEm = expiraEm;
    }

    public String token() {
        return token;
    }

    public long usuarioId() {
        return usuarioId;
    }

    public String csrf() {
        return csrf;
    }

    boolean expirada(Instant agora) {
        return !agora.isBefore(expiraEm);
    }

    public void avisar(String tipo, String texto) {
        avisos.add(new Aviso(tipo, texto));
    }

    /** Devolve os avisos pendentes e os apaga (padrão "flash message"). */
    public List<Aviso> consumirAvisos() {
        List<Aviso> copia = new ArrayList<>(avisos);
        avisos.removeAll(copia);
        return copia;
    }
}
