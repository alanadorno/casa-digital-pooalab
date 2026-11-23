package br.ucsal.casadigital;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** Marca um método como teste. O {@link ExecutorDeTestes} os encontra por <b>Reflexão</b>, sem JUnit. */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Teste {

    /** Frase que descreve o comportamento esperado. */
    String value();
}
