import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * Script de construção do projeto, sem Maven, sem Gradle e sem nenhuma dependência externa.
 * Só precisa de um JDK 21 ou superior (javac), e funciona igual em Windows, Linux e macOS.
 *
 * Uso (a partir da pasta do projeto):
 *   java Tarefas.java build     compila o código e copia os recursos para out/
 *   java Tarefas.java run       compila e inicia o servidor em http://localhost:8080
 *   java Tarefas.java test      compila e roda todos os testes automatizados
 *   java Tarefas.java limpar    apaga a pasta out/
 */
public class Tarefas {

    private static final Path RAIZ = Path.of("").toAbsolutePath();
    private static final Path SRC_MAIN = RAIZ.resolve("src/main/java");
    private static final Path RECURSOS = RAIZ.resolve("src/main/resources");
    private static final Path SRC_TEST = RAIZ.resolve("src/test/java");
    private static final Path OUT_MAIN = RAIZ.resolve("out/main");
    private static final Path OUT_TEST = RAIZ.resolve("out/test");

    public static void main(String[] args) throws Exception {
        if (Runtime.version().feature() < 21) {
            System.err.println("Este projeto precisa do JDK 21 ou superior. Versão encontrada: " + Runtime.version());
            System.exit(2);
        }
        String tarefa = args.length == 0 ? "ajuda" : args[0];
        switch (tarefa) {
            case "build" -> construir();
            case "run" -> {
                construir();
                System.exit(executar("br.ucsal.casadigital.app.Aplicacao", OUT_MAIN.toString(), restante(args)));
            }
            case "test" -> {
                construir();
                compilar(SRC_TEST, OUT_TEST, OUT_MAIN.toString());
                String cp = OUT_TEST + java.io.File.pathSeparator + OUT_MAIN;
                System.exit(executar("br.ucsal.casadigital.ExecutorDeTestes", cp, List.of(OUT_TEST.toString())));
            }
            case "limpar" -> apagar(RAIZ.resolve("out"));
            default -> System.out.println("""
                    Uso: java Tarefas.java <build | run | test | limpar>
                      build   compila o código e copia os recursos para out/
                      run     compila e inicia o servidor (http://localhost:8080)
                      test    compila e executa os testes automatizados
                      limpar  apaga a pasta out/
                    """);
        }
    }

    private static void construir() throws IOException {
        compilar(SRC_MAIN, OUT_MAIN, null);
        copiarRecursos();
        System.out.println("[build] OK -> " + RAIZ.relativize(OUT_MAIN));
    }

    private static void compilar(Path origem, Path destino, String classpath) throws IOException {
        JavaCompiler javac = ToolProvider.getSystemJavaCompiler();
        if (javac == null) {
            System.err.println("Compilador não encontrado. Instale um JDK (não apenas o JRE) na versão 21 ou superior.");
            System.exit(2);
        }
        List<String> fontes = new ArrayList<>();
        try (Stream<Path> arquivos = Files.walk(origem)) {
            arquivos.filter(p -> p.toString().endsWith(".java")).forEach(p -> fontes.add(p.toString()));
        }
        Files.createDirectories(destino);
        List<String> opcoes = new ArrayList<>(List.of("-encoding", "UTF-8", "-parameters", "-Xlint:all,-serial", "-d", destino.toString()));
        if (classpath != null) {
            opcoes.addAll(List.of("-cp", classpath));
        }
        opcoes.addAll(fontes);
        int codigo = javac.run(null, null, null, opcoes.toArray(String[]::new));
        if (codigo != 0) {
            System.err.println("[build] FALHOU: erro de compilação.");
            System.exit(1);
        }
    }

    private static void copiarRecursos() throws IOException {
        if (!Files.exists(RECURSOS)) {
            return;
        }
        try (Stream<Path> arquivos = Files.walk(RECURSOS)) {
            for (Path origem : (Iterable<Path>) arquivos::iterator) {
                Path destino = OUT_MAIN.resolve(RECURSOS.relativize(origem).toString());
                if (Files.isDirectory(origem)) {
                    Files.createDirectories(destino);
                } else {
                    Files.copy(origem, destino, StandardCopyOption.REPLACE_EXISTING);
                }
            }
        }
    }

    private static int executar(String classePrincipal, String classpath, List<String> argumentos) throws Exception {
        Path java = Path.of(System.getProperty("java.home"), "bin", "java");
        List<String> comando = new ArrayList<>(List.of(java.toString(), "-Dfile.encoding=UTF-8", "-Dstdout.encoding=UTF-8"));
        // repassa ao programa as opções -Dcasa.xxx=... dadas a este script (ex.: -Dcasa.porta=9090)
        System.getProperties().stringPropertyNames().stream().filter(n -> n.startsWith("casa."))
                .forEach(n -> comando.add("-D" + n + "=" + System.getProperty(n)));
        comando.addAll(List.of("-cp", classpath, classePrincipal));
        comando.addAll(argumentos);
        return new ProcessBuilder(comando).directory(RAIZ.toFile()).inheritIO().start().waitFor();
    }

    private static List<String> restante(String[] args) {
        return args.length <= 1 ? List.of() : List.of(args).subList(1, args.length);
    }

    private static void apagar(Path pasta) throws IOException {
        if (!Files.exists(pasta)) {
            return;
        }
        try (Stream<Path> arquivos = Files.walk(pasta)) {
            for (Path p : (Iterable<Path>) arquivos.sorted(java.util.Comparator.reverseOrder())::iterator) {
                Files.delete(p);
            }
        }
        System.out.println("[limpar] out/ removida");
    }
}
