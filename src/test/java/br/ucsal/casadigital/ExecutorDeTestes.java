package br.ucsal.casadigital;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

/**
 * Mini-framework de testes feito com <b>Reflexão</b>: varre a pasta de classes compiladas, carrega cada
 * classe, procura métodos com {@link Teste}, cria uma instância nova da classe para cada teste (isolamento) e
 * os executa. Sai com código 1 se algum falhar, para o script de build avisar.
 */
public final class ExecutorDeTestes {

    private ExecutorDeTestes() {
    }

    public static void main(String[] args) throws Exception {
        java.util.logging.Logger.getLogger("").setLevel(java.util.logging.Level.WARNING);   // menos barulho nos testes
        Path raiz = Path.of(args[0]);
        List<Class<?>> classes = descobrir(raiz);
        int ok = 0;
        List<String> falhas = new ArrayList<>();
        long inicio = System.nanoTime();
        for (Class<?> classe : classes) {
            List<Method> metodos = new ArrayList<>();
            for (Method m : classe.getDeclaredMethods()) {
                if (m.isAnnotationPresent(Teste.class)) {
                    metodos.add(m);
                }
            }
            if (metodos.isEmpty()) {
                continue;
            }
            metodos.sort(Comparator.comparing(Method::getName));
            System.out.println("\n" + classe.getSimpleName());
            for (Method m : metodos) {
                String descricao = m.getAnnotation(Teste.class).value();
                Object instancia = null;
                try {
                    var construtor = classe.getDeclaredConstructor();
                    construtor.setAccessible(true);
                    m.setAccessible(true);
                    instancia = construtor.newInstance();
                    m.invoke(instancia);
                    ok++;
                    System.out.println("  [ok]    " + descricao);
                } catch (InvocationTargetException e) {
                    falhas.add(classe.getSimpleName() + ": " + descricao + " -> " + e.getCause());
                    System.out.println("  [FALHA] " + descricao + "\n          " + e.getCause());
                    if (!(e.getCause() instanceof AssertionError)) {
                        e.getCause().printStackTrace(System.out);
                    }
                } finally {
                    liberar(classe, instancia);
                }
            }
        }
        long ms = (System.nanoTime() - inicio) / 1_000_000;
        System.out.printf("%n%d passaram, %d falharam (%d ms)%n", ok, falhas.size(), ms);
        if (!falhas.isEmpty()) {
            falhas.forEach(f -> System.out.println(" - " + f));
            System.exit(1);
        }
        System.exit(0);
    }

    /** Se a classe de teste tiver um método {@code fechar()}, ele roda depois de cada teste (por exemplo, para parar o servidor). */
    private static void liberar(Class<?> classe, Object instancia) throws Exception {
        if (instancia == null) {
            return;
        }
        try {
            Method fechar = classe.getDeclaredMethod("fechar");
            fechar.setAccessible(true);
            fechar.invoke(instancia);
        } catch (NoSuchMethodException semFechar) {
            // a classe não precisa de limpeza
        }
    }

    private static List<Class<?>> descobrir(Path raiz) throws IOException, ClassNotFoundException {
        List<Class<?>> classes = new ArrayList<>();
        try (Stream<Path> arquivos = Files.walk(raiz)) {
            for (Path p : arquivos.filter(f -> f.toString().endsWith(".class")).sorted().toList()) {
                String nome = raiz.relativize(p).toString().replace('/', '.').replace('\\', '.');
                nome = nome.substring(0, nome.length() - ".class".length());
                if (!nome.contains("$")) {
                    classes.add(Class.forName(nome));
                }
            }
        }
        return classes;
    }
}
