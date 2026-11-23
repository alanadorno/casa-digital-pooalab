package br.ucsal.casadigital.servico;

import br.ucsal.casadigital.dominio.Usuario;

import java.util.Optional;

/** Processo P2: só a equipe, com e-mail e senha, acessa o painel (RN01). */
public interface Autenticacao {

    /** @param origem endereço de quem tenta entrar, usado para limitar tentativas repetidas */
    Resultado<Usuario> entrar(LoginForm form, String origem);

    Optional<Usuario> porId(long id);
}
