package br.ucsal.casadigital.web;

import br.ucsal.casadigital.infra.Senhas;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/** Guarda as sessões ativas em memória ({@link ConcurrentHashMap}, seguro para várias threads). */
public final class GerenciadorDeSessoes {

    public static final String COOKIE = "CASA_SESSAO";

    private final ConcurrentHashMap<String, Sessao> sessoes = new ConcurrentHashMap<>();
    private final Clock relogio;
    private final Duration duracao;

    public GerenciadorDeSessoes(Clock relogio, Duration duracao) {
        this.relogio = relogio;
        this.duracao = duracao;
    }

    public Sessao criar(long usuarioId) {
        Sessao sessao = new Sessao(Senhas.token(), usuarioId, Senhas.token(), relogio.instant().plus(duracao));
        sessoes.put(sessao.token(), sessao);
        return sessao;
    }

    public Optional<Sessao> buscar(String token) {
        if (token == null) {
            return Optional.empty();
        }
        Instant agora = relogio.instant();
        Sessao sessao = sessoes.get(token);
        if (sessao != null && sessao.expirada(agora)) {
            sessoes.remove(token);
            return Optional.empty();
        }
        return Optional.ofNullable(sessao);
    }

    public void encerrar(String token) {
        if (token != null) {
            sessoes.remove(token);
        }
    }

    public int segundosDeValidade() {
        return (int) duracao.toSeconds();
    }
}
