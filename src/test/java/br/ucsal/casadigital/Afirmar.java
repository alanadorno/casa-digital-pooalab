package br.ucsal.casadigital;

import java.util.Objects;

/** Afirmações mínimas para os testes. */
public final class Afirmar {

    private Afirmar() {
    }

    public static void verdadeiro(boolean condicao, String mensagem) {
        if (!condicao) {
            throw new AssertionError(mensagem);
        }
    }

    public static void falso(boolean condicao, String mensagem) {
        verdadeiro(!condicao, mensagem);
    }

    public static void igual(Object esperado, Object atual, String mensagem) {
        if (!Objects.equals(esperado, atual)) {
            throw new AssertionError(mensagem + " — esperado <" + esperado + "> mas foi <" + atual + ">");
        }
    }

    public static void contem(String texto, String trecho, String mensagem) {
        if (texto == null || !texto.contains(trecho)) {
            throw new AssertionError(mensagem + " — não encontrei \"" + trecho + "\"");
        }
    }

    public static void naoContem(String texto, String trecho, String mensagem) {
        if (texto != null && texto.contains(trecho)) {
            throw new AssertionError(mensagem + " — não deveria conter \"" + trecho + "\"");
        }
    }

    /** Espera uma exceção do tipo indicado. */
    public static <E extends Throwable> E lanca(Class<E> tipo, Runnable codigo, String mensagem) {
        try {
            codigo.run();
        } catch (Throwable t) {
            if (tipo.isInstance(t)) {
                return tipo.cast(t);
            }
            throw new AssertionError(mensagem + " — lançou " + t.getClass().getSimpleName() + " em vez de " + tipo.getSimpleName(), t);
        }
        throw new AssertionError(mensagem + " — nada foi lançado");
    }
}
