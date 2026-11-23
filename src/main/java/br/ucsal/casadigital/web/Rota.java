package br.ucsal.casadigital.web;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Anotação que liga um método de controlador a um endereço, no estilo de frameworks web. O
 * {@link Roteador} descobre estes métodos por <b>Reflexão</b> ao iniciar; nenhuma tabela de rotas é
 * escrita à mão. {@code {nome}} no caminho vira um parâmetro (ex.: {@code /materia/{id}}).
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Rota {

    String metodo() default "GET";

    String caminho();
}
