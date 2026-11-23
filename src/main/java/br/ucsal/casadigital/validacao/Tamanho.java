package br.ucsal.casadigital.validacao;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** Limite máximo de caracteres, para proteger o armazenamento e a página. */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.RECORD_COMPONENT})
public @interface Tamanho {

    int max();

    String mensagem() default "Texto longo demais.";
}
