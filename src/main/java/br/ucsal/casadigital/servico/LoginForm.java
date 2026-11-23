package br.ucsal.casadigital.servico;

import br.ucsal.casadigital.validacao.Obrigatorio;

/** Credenciais do painel (RN01). */
public record LoginForm(
        @Obrigatorio(mensagem = "Informe o e-mail.") String email,
        @Obrigatorio(mensagem = "Informe a senha.") String senha) {
}
