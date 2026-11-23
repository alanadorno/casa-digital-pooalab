package br.ucsal.casadigital.validacao;

/**
 * Regra de negócio que olha o formulário inteiro (vários campos ao mesmo tempo), como "a data de
 * fim não pode ser anterior à de início" (RN14).
 */
@FunctionalInterface
public interface Regra<T> {

    void verificar(T alvo, Erros erros);
}
