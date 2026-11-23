package br.ucsal.casadigital.infra;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.RecordComponent;
import java.lang.reflect.Type;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Conversor JSON escrito do zero, sem biblioteca, usando <b>Reflexão</b> ({@code java.lang.reflect}).
 *
 * <p>Ele não conhece nenhuma classe do domínio: descobre os campos em tempo de execução
 * ({@code getDeclaredFields}, {@code getRecordComponents}), lê e grava valores com
 * {@code Field.get/set} e cria objetos com {@code getDeclaredConstructor().newInstance()}. Assim o
 * mesmo código serializa matérias, sugestões, usuários e qualquer entidade futura.
 *
 * <p>Tipos suportados: texto, números, booleanos, enums, {@link Instant}, {@link LocalDate},
 * listas, conjuntos, mapas de texto e objetos/records formados por esses tipos.
 */
public final class Json {

    private Json() {
    }

    // =====================================================================
    // Escrita
    // =====================================================================

    public static String escrever(Object valor) {
        StringBuilder sb = new StringBuilder();
        escrever(valor, sb, 0);
        return sb.toString();
    }

    private static void escrever(Object valor, StringBuilder sb, int nivel) {
        switch (valor) {
            case null -> sb.append("null");
            case String s -> texto(s, sb);
            case Boolean b -> sb.append(b);
            case Number n -> sb.append(n);
            case Enum<?> e -> texto(e.name(), sb);
            case Instant i -> texto(i.toString(), sb);
            case LocalDate d -> texto(d.toString(), sb);
            case Collection<?> c -> colecao(c, sb, nivel);
            case Map<?, ?> m -> mapa(m, sb, nivel);
            default -> objeto(valor, sb, nivel);
        }
    }

    private static void colecao(Collection<?> c, StringBuilder sb, int nivel) {
        if (c.isEmpty()) {
            sb.append("[]");
            return;
        }
        sb.append("[\n");
        int i = 0;
        for (Object item : c) {
            recuo(sb, nivel + 1);
            escrever(item, sb, nivel + 1);
            sb.append(++i < c.size() ? ",\n" : "\n");
        }
        recuo(sb, nivel);
        sb.append(']');
    }

    private static void mapa(Map<?, ?> m, StringBuilder sb, int nivel) {
        if (m.isEmpty()) {
            sb.append("{}");
            return;
        }
        sb.append("{\n");
        int i = 0;
        for (Map.Entry<?, ?> e : m.entrySet()) {
            recuo(sb, nivel + 1);
            texto(String.valueOf(e.getKey()), sb);
            sb.append(": ");
            escrever(e.getValue(), sb, nivel + 1);
            sb.append(++i < m.size() ? ",\n" : "\n");
        }
        recuo(sb, nivel);
        sb.append('}');
    }

    private static void objeto(Object o, StringBuilder sb, int nivel) {
        Map<String, Object> campos = new LinkedHashMap<>();
        try {
            if (o.getClass().isRecord()) {
                for (RecordComponent rc : o.getClass().getRecordComponents()) {
                    var acessor = rc.getAccessor();
                    acessor.setAccessible(true);
                    campos.put(rc.getName(), acessor.invoke(o));
                }
            } else {
                for (Field f : camposDe(o.getClass())) {
                    campos.put(f.getName(), f.get(o));
                }
            }
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Não foi possível ler " + o.getClass().getName(), e);
        }
        mapa(campos, sb, nivel);
    }

    private static void texto(String s, StringBuilder sb) {
        sb.append('"');
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '"' -> sb.append("\\\"");
                case '\\' -> sb.append("\\\\");
                case '\n' -> sb.append("\\n");
                case '\r' -> sb.append("\\r");
                case '\t' -> sb.append("\\t");
                default -> {
                    if (c < 0x20) {
                        sb.append(String.format("\\u%04x", (int) c));
                    } else {
                        sb.append(c);
                    }
                }
            }
        }
        sb.append('"');
    }

    private static void recuo(StringBuilder sb, int nivel) {
        sb.append("  ".repeat(nivel));
    }

    /** Campos de instância (não estáticos, não transient) da classe e das superclasses, superclasse primeiro. */
    static List<Field> camposDe(Class<?> tipo) {
        List<Field> campos = new ArrayList<>();
        for (Class<?> c = tipo; c != null && c != Object.class; c = c.getSuperclass()) {
            List<Field> daClasse = new ArrayList<>();
            for (Field f : c.getDeclaredFields()) {
                if (!Modifier.isStatic(f.getModifiers()) && !Modifier.isTransient(f.getModifiers()) && !f.isSynthetic()) {
                    f.setAccessible(true);
                    daClasse.add(f);
                }
            }
            campos.addAll(0, daClasse);
        }
        return campos;
    }

    // =====================================================================
    // Leitura
    // =====================================================================

    public static <T> T ler(String json, Class<T> tipo) {
        Object arvore = new Analisador(json).analisar();
        return tipo.cast(converter(arvore, tipo));
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static Object converter(Object no, Type tipo) {
        Class<?> bruto = tipo instanceof Class<?> c ? c : (Class<?>) ((ParameterizedType) tipo).getRawType();
        if (no == null) {
            return valorPadrao(bruto);
        }
        try {
            if (bruto == String.class) {
                return (String) no;
            } else if (bruto == long.class || bruto == Long.class) {
                return ((Number) no).longValue();
            } else if (bruto == int.class || bruto == Integer.class) {
                return ((Number) no).intValue();
            } else if (bruto == double.class || bruto == Double.class) {
                return ((Number) no).doubleValue();
            } else if (bruto == boolean.class || bruto == Boolean.class) {
                return (Boolean) no;
            } else if (bruto.isEnum()) {
                return Enum.valueOf((Class<? extends Enum>) bruto, (String) no);
            } else if (bruto == Instant.class) {
                return Instant.parse((String) no);
            } else if (bruto == LocalDate.class) {
                return LocalDate.parse((String) no);
            } else if (Collection.class.isAssignableFrom(bruto)) {
                Type elemento = ((ParameterizedType) tipo).getActualTypeArguments()[0];
                Collection<Object> destino = Set.class.isAssignableFrom(bruto) ? new LinkedHashSet<>() : new ArrayList<>();
                for (Object item : (List<Object>) no) {
                    destino.add(converter(item, elemento));
                }
                return destino;
            } else if (Map.class.isAssignableFrom(bruto)) {
                Type valor = ((ParameterizedType) tipo).getActualTypeArguments()[1];
                Map<String, Object> destino = new LinkedHashMap<>();
                ((Map<String, Object>) no).forEach((k, v) -> destino.put(k, converter(v, valor)));
                return destino;
            } else if (bruto.isRecord()) {
                return criarRecord(bruto, (Map<String, Object>) no);
            }
            return criarObjeto(bruto, (Map<String, Object>) no);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Não foi possível criar " + bruto.getName(), e);
        }
    }

    private static Object criarRecord(Class<?> tipo, Map<String, Object> dados) throws ReflectiveOperationException {
        RecordComponent[] componentes = tipo.getRecordComponents();
        Class<?>[] tipos = new Class<?>[componentes.length];
        Object[] argumentos = new Object[componentes.length];
        for (int i = 0; i < componentes.length; i++) {
            tipos[i] = componentes[i].getType();
            argumentos[i] = converter(dados.get(componentes[i].getName()), componentes[i].getGenericType());
        }
        Constructor<?> canonico = tipo.getDeclaredConstructor(tipos);
        canonico.setAccessible(true);
        return canonico.newInstance(argumentos);
    }

    private static Object criarObjeto(Class<?> tipo, Map<String, Object> dados) throws ReflectiveOperationException {
        Constructor<?> construtor = tipo.getDeclaredConstructor();
        construtor.setAccessible(true);
        Object objeto = construtor.newInstance();
        for (Field campo : camposDe(tipo)) {
            if (dados.containsKey(campo.getName())) {
                campo.set(objeto, converter(dados.get(campo.getName()), campo.getGenericType()));
            }
        }
        return objeto;
    }

    private static Object valorPadrao(Class<?> tipo) {
        if (tipo == long.class) {
            return 0L;
        } else if (tipo == int.class) {
            return 0;
        } else if (tipo == double.class) {
            return 0.0;
        } else if (tipo == boolean.class) {
            return false;
        }
        return null;
    }

    // =====================================================================
    // Analisador (parser) de JSON
    // =====================================================================

    private static final class Analisador {
        private final String s;
        private int pos;

        Analisador(String s) {
            this.s = s;
        }

        Object analisar() {
            Object v = valor();
            espaco();
            if (pos != s.length()) {
                throw erro("conteúdo inesperado depois do fim");
            }
            return v;
        }

        private Object valor() {
            espaco();
            if (pos >= s.length()) {
                throw erro("fim inesperado");
            }
            char c = s.charAt(pos);
            return switch (c) {
                case '{' -> objeto();
                case '[' -> lista();
                case '"' -> texto();
                case 't' -> literal("true", Boolean.TRUE);
                case 'f' -> literal("false", Boolean.FALSE);
                case 'n' -> literal("null", null);
                default -> numero();
            };
        }

        private Map<String, Object> objeto() {
            Map<String, Object> m = new LinkedHashMap<>();
            pos++;
            espaco();
            if (consome('}')) {
                return m;
            }
            do {
                espaco();
                String chave = texto();
                espaco();
                esperar(':');
                m.put(chave, valor());
                espaco();
            } while (consome(','));
            esperar('}');
            return m;
        }

        private List<Object> lista() {
            List<Object> l = new ArrayList<>();
            pos++;
            espaco();
            if (consome(']')) {
                return l;
            }
            do {
                l.add(valor());
                espaco();
            } while (consome(','));
            esperar(']');
            return l;
        }

        private String texto() {
            esperar('"');
            StringBuilder sb = new StringBuilder();
            while (pos < s.length()) {
                char c = s.charAt(pos++);
                if (c == '"') {
                    return sb.toString();
                }
                if (c == '\\') {
                    char e = s.charAt(pos++);
                    switch (e) {
                        case 'n' -> sb.append('\n');
                        case 'r' -> sb.append('\r');
                        case 't' -> sb.append('\t');
                        case 'b' -> sb.append('\b');
                        case 'f' -> sb.append('\f');
                        case 'u' -> {
                            sb.append((char) Integer.parseInt(s.substring(pos, pos + 4), 16));
                            pos += 4;
                        }
                        default -> sb.append(e);
                    }
                } else {
                    sb.append(c);
                }
            }
            throw erro("texto sem fechar");
        }

        private Object numero() {
            int inicio = pos;
            while (pos < s.length() && "+-0123456789.eE".indexOf(s.charAt(pos)) >= 0) {
                pos++;
            }
            String t = s.substring(inicio, pos);
            if (t.isEmpty()) {
                throw erro("valor inválido");
            }
            return t.contains(".") || t.contains("e") || t.contains("E") ? (Object) Double.parseDouble(t) : (Object) Long.parseLong(t);
        }

        private Object literal(String texto, Object valor) {
            if (!s.startsWith(texto, pos)) {
                throw erro("valor inválido");
            }
            pos += texto.length();
            return valor;
        }

        private void espaco() {
            while (pos < s.length() && Character.isWhitespace(s.charAt(pos))) {
                pos++;
            }
        }

        private boolean consome(char c) {
            if (pos < s.length() && s.charAt(pos) == c) {
                pos++;
                return true;
            }
            return false;
        }

        private void esperar(char c) {
            if (!consome(c)) {
                throw erro("esperava '" + c + "'");
            }
        }

        private IllegalArgumentException erro(String motivo) {
            return new IllegalArgumentException("JSON inválido na posição " + pos + ": " + motivo);
        }
    }
}
