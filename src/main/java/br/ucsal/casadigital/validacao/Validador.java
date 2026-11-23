package br.ucsal.casadigital.validacao;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;
import br.ucsal.casadigital.dominio.YouTube;

/**
 * Validador por <b>anotações + Reflexão</b>. Ele percorre os campos do formulário
 * ({@code getDeclaredFields}), lê as anotações de cada um ({@code getAnnotations}) e executa a
 * {@link Restricao} registrada para cada tipo de anotação.
 *
 * <p>Os formulários só declaram <i>o quê</i> validar ({@code @Obrigatorio}, {@code @EmailValido}...);
 * <i>como</i> validar fica aqui, uma única vez (SRP e DRY).
 */
public final class Validador {

    private static final Pattern EMAIL = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]{2,}$");

    private final Map<Class<? extends Annotation>, Restricao<? extends Annotation>> restricoes = new LinkedHashMap<>();

    /** Validador com as restrições padrão do sistema. */
    public static Validador padrao() {
        return new Validador()
                .registrar(Obrigatorio.class, (a, v) -> vazio(v) ? Optional.of(a.mensagem()) : Optional.empty())
                .registrar(EmailValido.class, (a, v) -> !vazio(v) && !EMAIL.matcher(texto(v)).matches()
                        ? Optional.of(a.mensagem()) : Optional.empty())
                .registrar(Tamanho.class, (a, v) -> !vazio(v) && texto(v).length() > a.max()
                        ? Optional.of(a.mensagem() + " Máximo de " + a.max() + " caracteres.") : Optional.empty())
                .registrar(LinkYouTube.class, (a, v) -> !vazio(v) && YouTube.extrairId(texto(v)).isEmpty()
                        ? Optional.of(a.mensagem()) : Optional.empty());
    }

    public <A extends Annotation> Validador registrar(Class<A> tipo, Restricao<A> restricao) {
        restricoes.put(tipo, restricao);
        return this;
    }

    /**
     * @param apenasRascunho se verdadeiro, só aplica {@code @Obrigatorio(paraRascunho = true)}
     *                       (para salvar um rascunho basta ter título)
     */
    public Erros validar(Object formulario, boolean apenasRascunho) {
        Erros erros = Erros.novo();
        for (Field campo : formulario.getClass().getDeclaredFields()) {
            if (campo.isSynthetic()) {
                continue;
            }
            campo.setAccessible(true);
            Object valor;
            try {
                valor = campo.get(formulario);
            } catch (IllegalAccessException e) {
                throw new IllegalStateException("Não foi possível ler o campo " + campo.getName(), e);
            }
            for (Annotation anotacao : campo.getAnnotations()) {
                if (apenasRascunho && !(anotacao instanceof Obrigatorio o && o.paraRascunho())) {
                    continue;
                }
                aplicar(anotacao, valor).ifPresent(msg -> erros.adicionar(campo.getName(), msg));
            }
        }
        return erros;
    }

    @SuppressWarnings("unchecked")
    private Optional<String> aplicar(Annotation anotacao, Object valor) {
        Restricao<Annotation> restricao = (Restricao<Annotation>) restricoes.get(anotacao.annotationType());
        return restricao == null ? Optional.empty() : restricao.verificar(anotacao, valor);
    }

    private static boolean vazio(Object valor) {
        return valor == null || texto(valor).isBlank();
    }

    private static String texto(Object valor) {
        return String.valueOf(valor).trim();
    }
}
