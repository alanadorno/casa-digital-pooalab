package br.ucsal.casadigital.servico;

import br.ucsal.casadigital.validacao.LinkYouTube;
import br.ucsal.casadigital.validacao.Obrigatorio;
import br.ucsal.casadigital.validacao.Tamanho;

import java.util.Arrays;
import java.util.List;

/**
 * Dados do editor de matérias do painel. É um {@code record}: imutável, sem código repetitivo, e as
 * anotações de validação (lidas por reflexão) ficam ao lado de cada campo.
 *
 * @param id          0 para matéria nova
 * @param referencias uma referência por linha
 */
public record MateriaForm(
        long id,
        @Obrigatorio(mensagem = "Informe o título.", paraRascunho = true)
        @Tamanho(max = 160, mensagem = "O título está longo demais.") String titulo,
        @Obrigatorio(mensagem = "Escolha uma editoria.") String editoria,
        @Tamanho(max = 400, mensagem = "O resumo está longo demais.") String resumo,
        @Obrigatorio(mensagem = "Informe o link do vídeo do YouTube.")
        @LinkYouTube String video,
        @Obrigatorio(mensagem = "Escreva o texto completo.")
        @Tamanho(max = 20000, mensagem = "O texto está longo demais.") String texto,
        @Tamanho(max = 2000, mensagem = "As referências estão longas demais.") String referencias,
        boolean revisada,
        boolean destacar) {

    public List<String> listaDeReferencias() {
        if (referencias == null) {
            return List.of();
        }
        return Arrays.stream(referencias.split("\\R")).map(String::trim).filter(s -> !s.isEmpty()).toList();
    }

    /** Formulário vazio para "Nova matéria". */
    public static MateriaForm vazio() {
        return new MateriaForm(0, "", "", "", "", "", "", false, false);
    }
}
