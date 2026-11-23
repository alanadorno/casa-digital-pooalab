package br.ucsal.casadigital.web;

import java.lang.reflect.Constructor;
import java.lang.reflect.RecordComponent;
import java.util.Set;

/**
 * Liga os campos de um formulário HTML a um {@code record} por <b>Reflexão</b>: lê os componentes do record
 * ({@code getRecordComponents}), converte cada texto recebido para o tipo do componente e chama o construtor
 * canônico. Um único método serve para todos os formulários do sistema.
 */
public final class Formularios {

    private static final Set<String> VERDADEIRO = Set.of("on", "true", "1", "sim");

    private Formularios() {
    }

    public static <R extends Record> R ligar(Requisicao requisicao, Class<R> tipo) {
        RecordComponent[] componentes = tipo.getRecordComponents();
        Class<?>[] tipos = new Class<?>[componentes.length];
        Object[] valores = new Object[componentes.length];
        for (int i = 0; i < componentes.length; i++) {
            tipos[i] = componentes[i].getType();
            String bruto = requisicao.formulario().get(componentes[i].getName());
            valores[i] = converter(bruto, tipos[i]);
        }
        try {
            Constructor<R> construtor = tipo.getDeclaredConstructor(tipos);
            return construtor.newInstance(valores);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Não foi possível montar " + tipo.getSimpleName(), e);
        }
    }

    private static Object converter(String bruto, Class<?> tipo) {
        if (tipo == boolean.class) {
            return bruto != null && VERDADEIRO.contains(bruto.toLowerCase());
        }
        if (tipo == long.class) {
            try {
                return Long.parseLong(bruto == null ? "" : bruto.trim());
            } catch (NumberFormatException e) {
                return 0L;
            }
        }
        return bruto == null ? "" : bruto;
    }
}
