package br.ucsal.casadigital.validacao;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** Campo de preenchimento obrigatório (RN02, RN08). Lido por reflexão pelo {@link Validador}. */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.RECORD_COMPONENT})
public @interface Obrigatorio {

    String mensagem();

    /** Se verdadeiro, o campo também é exigido ao salvar apenas como rascunho. */
    boolean paraRascunho() default false;
}
