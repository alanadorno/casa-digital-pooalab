package br.ucsal.casadigital.dominio;

import java.util.Arrays;
import java.util.Optional;

/** RN03: cada matéria pertence a uma única editoria, entre as seis abaixo. */
public enum Editoria {
    ENTRETENIMENTO_E_CULTURA("entretenimento-e-cultura", "Entretenimento e Cultura", "ENTRETENIMENTO", "azul"),
    ESPORTE("esporte", "Esporte", "ESPORTE", "verde"),
    INOVACAO_E_TECNOLOGIA("inovacao-e-tecnologia", "Inovação e Tecnologia", "INOVAÇÃO", "laranja"),
    SAUDE("saude", "Saúde", "SAÚDE", "verde"),
    BAHIA("bahia", "Bahia", "BAHIA", "azul"),
    UCSAL("ucsal", "UCSAL", "UCSAL", "amarelo");

    private final String slug;
    private final String nome;
    private final String aba;
    private final String cor;

    Editoria(String slug, String nome, String aba, String cor) {
        this.slug = slug;
        this.nome = nome;
        this.aba = aba;
        this.cor = cor;
    }

    public String slug() {
        return slug;
    }

    public String nome() {
        return nome;
    }

    /** Texto da etiqueta colorida do menu (EV03). */
    public String aba() {
        return aba;
    }

    /** Nome da cor da paleta do Instagram do podcast (EV01). */
    public String cor() {
        return cor;
    }

    public static Optional<Editoria> porSlug(String slug) {
        return Arrays.stream(values()).filter(e -> e.slug.equals(slug)).findFirst();
    }
}
