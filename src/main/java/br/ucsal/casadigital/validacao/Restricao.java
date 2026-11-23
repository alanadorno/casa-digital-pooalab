package br.ucsal.casadigital.validacao;

import java.lang.annotation.Annotation;
import java.util.Optional;

/**
 * Padrão <b>Strategy</b>: uma estratégia de verificação para cada anotação. Para criar uma nova
 * validação basta criar a anotação e registrar a sua {@code Restricao} no {@link Validador}, sem
 * alterar o código que já existe (Open/Closed).
 */
@FunctionalInterface
public interface Restricao<A extends Annotation> {

    /** Devolve a mensagem de erro, ou vazio se o valor é aceitável. */
    Optional<String> verificar(A anotacao, Object valor);
}
