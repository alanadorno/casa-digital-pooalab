package br.ucsal.casadigital.validacao;

import java.util.LinkedHashMap;
import java.util.Map;

/** Mensagens de erro por campo, na ordem em que foram encontradas (o primeiro erro de cada campo vale). */
public final class Erros {

    private final Map<String, String> porCampo = new LinkedHashMap<>();

    public static Erros novo() {
        return new Erros();
    }

    public Erros adicionar(String campo, String mensagem) {
        porCampo.putIfAbsent(campo, mensagem);
        return this;
    }

    public boolean vazio() {
        return porCampo.isEmpty();
    }

    public String get(String campo) {
        return porCampo.get(campo);
    }

    public boolean tem(String campo) {
        return porCampo.containsKey(campo);
    }

    public Map<String, String> comoMapa() {
        return Map.copyOf(porCampo);
    }

    public int quantidade() {
        return porCampo.size();
    }

    @Override
    public String toString() {
        return porCampo.toString();
    }
}
