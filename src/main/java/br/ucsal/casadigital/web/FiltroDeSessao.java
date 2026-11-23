package br.ucsal.casadigital.web;

import br.ucsal.casadigital.dominio.Usuario;
import br.ucsal.casadigital.servico.Autenticacao;

import java.util.Optional;

/** Lê o cookie de sessão e, se for válido, deixa a sessão e o usuário disponíveis na requisição. */
public final class FiltroDeSessao implements Filtro {

    private final GerenciadorDeSessoes sessoes;
    private final Autenticacao autenticacao;

    public FiltroDeSessao(GerenciadorDeSessoes sessoes, Autenticacao autenticacao) {
        this.sessoes = sessoes;
        this.autenticacao = autenticacao;
    }

    @Override
    public Resposta aplicar(Requisicao requisicao, Cadeia proxima) {
        Optional<Sessao> sessao = sessoes.buscar(requisicao.cookie(GerenciadorDeSessoes.COOKIE));
        Optional<Usuario> usuario = sessao.flatMap(s -> autenticacao.porId(s.usuarioId()));
        if (sessao.isPresent() && usuario.isEmpty()) {
            sessoes.encerrar(sessao.get().token());
            sessao = Optional.empty();
        }
        requisicao.definirSessao(sessao.orElse(null), usuario.orElse(null));
        return proxima.seguir(requisicao);
    }
}
