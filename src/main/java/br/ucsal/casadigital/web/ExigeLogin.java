package br.ucsal.casadigital.web;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** RN01: a rota só pode ser usada por um membro da equipe autenticado. Lida por reflexão no filtro de acesso. */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface ExigeLogin {

    /** Se verdadeiro, além de logado, o usuário precisa ser administrador. */
    boolean administrador() default false;
}
