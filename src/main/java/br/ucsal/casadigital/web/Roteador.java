package br.ucsal.casadigital.web;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Roteador por <b>Reflexão e Anotações</b>. Para cada objeto controlador registrado ele percorre
 * {@code getDeclaredMethods()}, procura os métodos anotados com {@link Rota} e lê também {@link ExigeLogin}.
 * O endereço vira uma expressão regular com grupos nomeados.
 *
 * <p>Cada {@link RotaRegistrada} é um objeto <b>Command</b>: encapsula "chamar este método deste controlador"
 * e pode ser executada sem que o roteador saiba o que ela faz.
 */
public final class Roteador {

    /** Rota descoberta: metadados lidos das anotações + a chamada reflexiva do método. */
    public record RotaRegistrada(String metodoHttp, String caminho, Pattern padrao, List<String> nomes,
                                 boolean exigeLogin, boolean apenasAdministrador, Object controlador, Method metodo) {

        public Resposta executar(Requisicao requisicao) {
            try {
                return (Resposta) metodo.invoke(controlador, requisicao);
            } catch (InvocationTargetException e) {
                if (e.getCause() instanceof RuntimeException re) {
                    throw re;
                }
                throw new IllegalStateException(e.getCause());
            } catch (IllegalAccessException e) {
                throw new IllegalStateException(e);
            }
        }
    }

    private static final Pattern PARAMETRO = Pattern.compile("\\{(\\w+)}");

    private final List<RotaRegistrada> rotas = new ArrayList<>();

    /** Descobre e registra todas as rotas de um controlador. */
    public Roteador registrar(Object controlador) {
        for (Method metodo : controlador.getClass().getDeclaredMethods()) {
            Rota rota = metodo.getAnnotation(Rota.class);
            if (rota == null) {
                continue;
            }
            if (metodo.getParameterCount() != 1 || metodo.getParameterTypes()[0] != Requisicao.class
                    || metodo.getReturnType() != Resposta.class) {
                throw new IllegalStateException("O método " + metodo + " precisa ter a forma Resposta nome(Requisicao)");
            }
            metodo.setAccessible(true);
            List<String> nomes = new ArrayList<>();
            Matcher m = PARAMETRO.matcher(rota.caminho());
            StringBuilder regex = new StringBuilder();
            while (m.find()) {
                nomes.add(m.group(1));
                m.appendReplacement(regex, "(?<" + m.group(1) + ">[^/]+)");
            }
            m.appendTail(regex);
            ExigeLogin exige = metodo.getAnnotation(ExigeLogin.class);
            rotas.add(new RotaRegistrada(rota.metodo(), rota.caminho(), Pattern.compile("^" + regex + "$"), nomes,
                    exige != null, exige != null && exige.administrador(), controlador, metodo));
        }
        return this;
    }

    /** Procura a rota que atende a requisição e, se achar, guarda os parâmetros do caminho nela. */
    public Optional<RotaRegistrada> resolver(Requisicao requisicao) {
        for (RotaRegistrada rota : rotas) {
            if (!rota.metodoHttp().equals(requisicao.metodo())) {
                continue;
            }
            Matcher m = rota.padrao().matcher(requisicao.caminho());
            if (m.matches()) {
                Map<String, String> valores = new LinkedHashMap<>();
                rota.nomes().forEach(n -> valores.put(n, m.group(n)));
                requisicao.definirParametrosDoCaminho(valores);
                return Optional.of(rota);
            }
        }
        return Optional.empty();
    }

    public List<RotaRegistrada> rotas() {
        return List.copyOf(rotas);
    }
}
