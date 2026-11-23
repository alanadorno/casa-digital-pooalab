package br.ucsal.casadigital.validacao;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** O texto, quando preenchido, precisa ser um link de vídeo do YouTube (RN02, RN06). */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.RECORD_COMPONENT})
public @interface LinkYouTube {

    String mensagem() default "O link precisa ser de um vídeo do YouTube.";
}
