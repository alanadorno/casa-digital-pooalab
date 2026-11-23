package br.ucsal.casadigital.dominio;

import java.util.Arrays;
import java.util.Optional;

/** RN12: todo item da seção UCSAL pertence a uma destas quatro subseções. */
public enum Subsecao {
    SOBRE_A_INSTITUICAO("Sobre a instituição", "amarelo", false),
    LIGAS("Ligas", "azul", false),
    PROJETOS("Projetos", "verde", false),
    O_QUE_ESTA_ROLANDO("O que está rolando", "laranja", true);

    private final String rotulo;
    private final String cor;
    private final boolean temVigencia;

    Subsecao(String rotulo, String cor, boolean temVigencia) {
        this.rotulo = rotulo;
        this.cor = cor;
        this.temVigencia = temVigencia;
    }

    public String rotulo() {
        return rotulo;
    }

    public String cor() {
        return cor;
    }

    /** RN14: só "O que está rolando" tem data de início e de fim. As demais são exibidas assim que gravadas. */
    public boolean temVigencia() {
        return temVigencia;
    }

    public static Optional<Subsecao> porNome(String nome) {
        return Arrays.stream(values()).filter(s -> s.name().equals(nome)).findFirst();
    }
}
