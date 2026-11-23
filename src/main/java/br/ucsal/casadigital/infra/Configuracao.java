package br.ucsal.casadigital.infra;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Configuração da aplicação.
 *
 * <p>Padrão <b>Singleton</b> (com <i>holder idiom</i>, seguro para várias threads) para a configuração
 * padrão do processo, usada só no {@code main}. Os serviços não chamam {@link #instancia()}:
 * recebem a configuração pelo construtor (<b>Inversão de Dependência</b>), e os testes criam a sua própria
 * com {@link #de(Map)}.
 *
 * <p>Ordem de prioridade de cada chave: propriedade de sistema {@code -Dcasa.chave}, variável de ambiente
 * {@code CASA_CHAVE}, arquivo {@code config/casa-digital.properties}, valor padrão.
 */
public final class Configuracao {

    private static final class Holder {
        private static final Configuracao INSTANCIA = new Configuracao(null);
    }

    private final Map<String, String> sobrescritas;
    private final Properties arquivo = new Properties();

    private Configuracao(Map<String, String> sobrescritas) {
        this.sobrescritas = sobrescritas;
        if (sobrescritas == null) {
            Path caminho = Path.of("config", "casa-digital.properties");
            if (Files.isRegularFile(caminho)) {
                try (InputStream in = Files.newInputStream(caminho)) {
                    arquivo.load(new java.io.InputStreamReader(in, java.nio.charset.StandardCharsets.UTF_8));
                } catch (IOException e) {
                    throw new IllegalStateException("Não foi possível ler " + caminho, e);
                }
            }
        }
    }

    public static Configuracao instancia() {
        return Holder.INSTANCIA;
    }

    /** Configuração isolada (sem arquivo, sem ambiente): usada nos testes. */
    public static Configuracao de(Map<String, String> valores) {
        return new Configuracao(new HashMap<>(valores));
    }

    public String texto(String chave, String padrao) {
        if (sobrescritas != null) {
            return sobrescritas.getOrDefault(chave, padrao);
        }
        String valor = System.getProperty("casa." + chave);
        if (valor == null) {
            valor = System.getenv("CASA_" + chave.toUpperCase().replace('.', '_'));
        }
        if (valor == null) {
            valor = arquivo.getProperty(chave);
        }
        return valor == null || valor.isBlank() ? padrao : valor.trim();
    }

    public int inteiro(String chave, int padrao) {
        return Integer.parseInt(texto(chave, String.valueOf(padrao)));
    }

    public boolean booleano(String chave, boolean padrao) {
        return Boolean.parseBoolean(texto(chave, String.valueOf(padrao)));
    }

    // ---------- valores usados pela aplicação ----------

    public int porta() {
        return inteiro("porta", 8080);
    }

    /** Endereço em que o servidor escuta. Por padrão só esta máquina; use 0.0.0.0 para testar no celular. */
    public String host() {
        return texto("host", "127.0.0.1");
    }

    /** Pasta onde ficam o banco em arquivo JSON e a caixa de saída de e-mails. Vazio = tudo em memória. */
    public String pastaDeDados() {
        return texto("dados.pasta", "data");
    }

    public ZoneId fusoHorario() {
        return ZoneId.of(texto("fuso", "America/Bahia"));
    }

    /** RN09: intervalo entre os reenvios automáticos do e-mail (padrão: 15 minutos). */
    public Duration intervaloDeReenvio() {
        return Duration.ofSeconds(inteiro("reenvio.segundos", 15 * 60));
    }

    /** RN09: quantos reenvios automáticos (padrão: 3). */
    public int maximoDeReenvios() {
        return inteiro("reenvio.maximo", 3);
    }

    /** De quanto em quanto tempo o agendador procura sugestões com e-mail pendente e itens vencidos. */
    public Duration intervaloDoAgendador() {
        return Duration.ofSeconds(inteiro("agendador.segundos", 30));
    }

    /** RN05: validade do destaque manual da home (padrão: 7 dias). */
    public int diasDoDestaqueManual() {
        return inteiro("destaque.dias", 7);
    }

    /** RN11: matérias por página nas editorias (padrão: 6). */
    public int materiasPorPagina() {
        return inteiro("pagina.tamanho", 6);
    }

    public int horasDeSessao() {
        return inteiro("sessao.horas", 8);
    }

    public boolean carregarDadosDeDemonstracao() {
        return booleano("demo.dados", true);
    }

    public String emailDaEquipe() {
        return texto("email.equipe", "equipe@exemplo.com");
    }

    public String administradorEmail() {
        return texto("admin.email", "equipe@exemplo.com");
    }

    public String administradorNome() {
        return texto("admin.nome", "Equipe (demonstração)");
    }

    /** Credencial fictícia, apenas para a demonstração. Em uso real, defina CASA_ADMIN_SENHA. */
    public String administradorSenha() {
        return texto("admin.senha", "trocar-senha-123");
    }

    public String instagram() {
        return texto("social.instagram", "https://www.instagram.com/issonaoeumradio/");
    }

    public String youtubeDoPodcast() {
        return texto("social.youtube", "https://www.youtube.com/@issonaoeumradio");
    }
}
