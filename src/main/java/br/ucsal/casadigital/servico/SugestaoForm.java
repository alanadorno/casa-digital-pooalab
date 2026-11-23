package br.ucsal.casadigital.servico;

import br.ucsal.casadigital.validacao.EmailValido;
import br.ucsal.casadigital.validacao.Obrigatorio;
import br.ucsal.casadigital.validacao.Tamanho;

/** RN08: a sugestão exige nome, sobrenome, e-mail válido e o texto da sugestão. */
public record SugestaoForm(
        @Obrigatorio(mensagem = "Informe o seu nome.") @Tamanho(max = 80) String nome,
        @Obrigatorio(mensagem = "Informe o seu sobrenome.") @Tamanho(max = 80) String sobrenome,
        @Obrigatorio(mensagem = "Informe o seu e-mail.") @EmailValido @Tamanho(max = 120) String email,
        @Obrigatorio(mensagem = "Escreva a sua sugestão.")
        @Tamanho(max = 2000, mensagem = "A sugestão está longa demais.") String sugestao) {

    public static SugestaoForm vazio() {
        return new SugestaoForm("", "", "", "");
    }
}
