package br.ucsal.casadigital.validacao;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** O texto, quando preenchido, precisa ter formato de e-mail (RN08). */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.RECORD_COMPONENT})
public @interface EmailValido {

    String mensagem() default "Esse e-mail não parece válido. Ex.: nome@email.com";
}
