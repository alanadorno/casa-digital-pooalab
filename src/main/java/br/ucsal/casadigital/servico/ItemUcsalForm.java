package br.ucsal.casadigital.servico;

import br.ucsal.casadigital.validacao.Obrigatorio;
import br.ucsal.casadigital.validacao.Tamanho;

/** Cadastro de item da seção UCSAL (RN12, RN13, RN14). As datas chegam como texto AAAA-MM-DD. */
public record ItemUcsalForm(
        @Obrigatorio(mensagem = "Escolha a subseção.") String subsecao,
        @Obrigatorio(mensagem = "Informe o título.") @Tamanho(max = 120) String titulo,
        @Obrigatorio(mensagem = "Escreva a descrição.") @Tamanho(max = 1000) String descricao,
        @Obrigatorio(mensagem = "Informe com qual liga, projeto ou setor a informação foi conferida.")
        @Tamanho(max = 120) String fonte,
        String inicio,
        String fim,
        boolean conferido) {

    public static ItemUcsalForm vazio() {
        return new ItemUcsalForm("", "", "", "", "", "", false);
    }
}
