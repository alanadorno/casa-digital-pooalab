package br.ucsal.casadigital;

import br.ucsal.casadigital.app.Sistema;
import br.ucsal.casadigital.infra.Configuracao;
import br.ucsal.casadigital.infra.SimuladorDeFalhas.Falha;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Ponta a ponta: sobe o servidor de verdade (porta 0, memória) e conversa com ele por HTTP. */
public class HttpTest {

    private static final Pattern CSRF = Pattern.compile("name=\"_csrf\" value=\"([^\"]+)\"");

    private final Sistema sistema;
    private final HttpClient cliente;
    /** Cookie de sessão guardado à mão: deixa claro, no teste, o que o navegador faria. */
    private volatile String cookieDeSessao = "";
    private final String base;

    public HttpTest() throws IOException {
        Configuracao config = Configuracao.de(Map.of("porta", "0", "dados.pasta", "", "demo.dados", "false",
                "agendador.segundos", "3600", "admin.email", "admin@exemplo.com", "admin.senha", "senha-de-teste-1"));
        sistema = Sistema.montar(config, Clock.systemUTC());
        sistema.iniciar();
        base = "http://127.0.0.1:" + sistema.porta();
        cliente = HttpClient.newBuilder().followRedirects(HttpClient.Redirect.NEVER).build();
    }

    private HttpResponse<String> get(String caminho) {
        return enviar(HttpRequest.newBuilder(URI.create(base + caminho)).header("Cookie", cookieDeSessao).GET().build());
    }

    private HttpResponse<String> post(String caminho, Map<String, String> campos) {
        StringBuilder corpo = new StringBuilder();
        campos.forEach((k, v) -> corpo.append(corpo.isEmpty() ? "" : "&").append(URLEncoder.encode(k, StandardCharsets.UTF_8))
                .append('=').append(URLEncoder.encode(v, StandardCharsets.UTF_8)));
        return enviar(HttpRequest.newBuilder(URI.create(base + caminho))
                .header("Cookie", cookieDeSessao)
                .header("Content-Type", "application/x-www-form-urlencoded").POST(HttpRequest.BodyPublishers.ofString(corpo.toString())).build());
    }

    private HttpResponse<String> enviar(HttpRequest req) {
        try {
            var resposta = cliente.send(req, HttpResponse.BodyHandlers.ofString());
            resposta.headers().firstValue("Set-Cookie").ifPresent(c -> cookieDeSessao = c.split(";", 2)[0]);
            return resposta;
        } catch (IOException | InterruptedException e) {
            throw new IllegalStateException(e);
        }
    }

    private void entrar() {
        var r = post("/painel/entrar", Map.of("email", "admin@exemplo.com", "senha", "senha-de-teste-1"));
        Afirmar.igual(303, r.statusCode(), "login redireciona");
    }

    private String csrf() {
        Matcher m = CSRF.matcher(get("/painel/materias/nova").body());
        Afirmar.verdadeiro(m.find(), "a página do painel traz o token CSRF");
        return m.group(1);
    }

    private Map<String, String> materia(String titulo, String csrf) {
        Map<String, String> m = new LinkedHashMap<>();
        m.put("_csrf", csrf);
        m.put("id", "0");
        m.put("titulo", titulo);
        m.put("editoria", "saude");
        m.put("video", "https://www.youtube.com/watch?v=476X2Cs6vAM");
        m.put("texto", "Primeiro parágrafo.\n\nSegundo.");
        m.put("referencias", "Fonte A");
        m.put("revisada", "on");
        m.put("acao", "publicar");
        return m;
    }

    @Teste("Páginas públicas respondem 200, têm cabeçalhos de segurança e página 404 própria")
    void publico() {
        for (String caminho : new String[]{"/", "/ucsal", "/sobre-nos", "/editoria/bahia", "/css/app.css", "/js/app.js"}) {
            Afirmar.igual(200, get(caminho).statusCode(), caminho);
        }
        var home = get("/");
        Afirmar.contem(home.headers().firstValue("Content-Security-Policy").orElse(""), "default-src 'self'", "CSP");
        Afirmar.igual("nosniff", home.headers().firstValue("X-Content-Type-Options").orElse(""), "nosniff");
        Afirmar.igual(404, get("/nao-existe").statusCode(), "rota inexistente");
        Afirmar.igual(404, get("/materia/999").statusCode(), "matéria inexistente");
        Afirmar.igual(404, get("/editoria/outra").statusCode(), "editoria inexistente");
        Afirmar.contem(get("/nao-existe").body(), "Página não encontrada", "mensagem amigável");
    }

    @Teste("Não escapa do diretório de arquivos estáticos (path traversal)")
    void travessia() {
        Afirmar.igual(404, get("/css/../../Tarefas.java").statusCode(), "travessia com ..");
        Afirmar.igual(404, get("/%2e%2e/%2e%2e/Tarefas.java").statusCode(), "travessia codificada");
    }

    @Teste("RN01: o painel exige login; senha errada recebe mensagem genérica")
    void login() {
        var r = get("/painel");
        Afirmar.igual(303, r.statusCode(), "sem login redireciona");
        Afirmar.contem(r.headers().firstValue("Location").orElse(""), "/painel/entrar", "para a tela de entrada");
        Afirmar.igual(303, get("/painel/sugestoes").statusCode(), "sugestões também protegidas");
        Afirmar.igual(303, get("/painel/demonstracao").statusCode(), "demonstração também");
        var errada = post("/painel/entrar", Map.of("email", "admin@exemplo.com", "senha", "errada"));
        Afirmar.igual(401, errada.statusCode(), "senha errada");
        var inexistente = post("/painel/entrar", Map.of("email", "ninguem@exemplo.com", "senha", "errada"));
        Afirmar.igual(401, inexistente.statusCode(), "e-mail inexistente");
        Afirmar.contem(inexistente.body(), "E-mail ou senha incorretos", "mensagem genérica (não revela se o e-mail existe)");
        entrar();
        Afirmar.igual(200, get("/painel").statusCode(), "logado entra");
    }

    @Teste("O cookie de sessão é HttpOnly e SameSite=Strict")
    void cookie() {
        var r = post("/painel/entrar", Map.of("email", "admin@exemplo.com", "senha", "senha-de-teste-1"));
        String cookie = r.headers().firstValue("Set-Cookie").orElse("");
        Afirmar.contem(cookie, "HttpOnly", "HttpOnly");
        Afirmar.contem(cookie, "SameSite=Strict", "SameSite");
    }

    @Teste("POST do painel sem o token CSRF é recusado (403) e nada é criado")
    void semTokenCsrf() {
        entrar();
        Map<String, String> campos = materia("Sem token", "invalido");
        Afirmar.igual(403, post("/painel/materias", campos).statusCode(), "token inválido");
        campos.remove("_csrf");
        Afirmar.igual(403, post("/painel/materias", campos).statusCode(), "sem token");
        Afirmar.igual(0, sistema.banco().materias().todos().size(), "nada gravado");
    }

    @Teste("Fluxo P3 completo: login → publicar → aparece na home, na editoria e na página da matéria")
    void publicarDePontaAPonta() {
        entrar();
        String token = csrf();
        var invalida = post("/painel/materias", Map.of("_csrf", token, "id", "0", "titulo", "Incompleta", "acao", "publicar"));
        Afirmar.igual(422, invalida.statusCode(), "campos faltando");
        Afirmar.contem(invalida.body(), "Informe o link do vídeo", "mensagem junto do campo");

        var ok = post("/painel/materias", materia("Matéria <b>publicada</b> pelo teste", token));
        Afirmar.igual(303, ok.statusCode(), "publicou e voltou ao painel");
        String home = get("/").body();
        Afirmar.contem(home, "Matéria &lt;b&gt;publicada&lt;/b&gt; pelo teste", "na home, com HTML escapado");
        Afirmar.naoContem(home, "<b>publicada</b>", "sem injeção de HTML");
        Afirmar.contem(get("/editoria/saude").body(), "publicada", "na editoria");
        long id = sistema.banco().materias().todos().getFirst().id();
        var pagina = get("/materia/" + id);
        Afirmar.igual(200, pagina.statusCode(), "página da matéria");
        Afirmar.contem(pagina.body(), "youtube-nocookie.com/embed/476X2Cs6vAM", "vídeo incorporado");
    }

    @Teste("P3: com a gravação falhando, o painel mostra o erro e a home não muda")
    void falhaNaGravacao() {
        entrar();
        String token = csrf();
        sistema.simulador().definir(Falha.GRAVACAO_DA_MATERIA, true);
        var r = post("/painel/materias", materia("Não deve sair", token));
        Afirmar.igual(503, r.statusCode(), "falha do sistema");
        Afirmar.contem(r.body(), "Não foi possível gravar", "mensagem ao usuário");
        Afirmar.naoContem(get("/").body(), "Não deve sair", "home intacta");
    }

    @Teste("P5: sugestão pública é registrada, valida os campos e a equipe a vê no painel")
    void sugestao() throws Exception {
        var invalida = post("/sugestoes", Map.of("nome", "", "email", "x"));
        Afirmar.igual(422, invalida.statusCode(), "dados inválidos");
        var ok = post("/sugestoes", Map.of("nome", "Ana", "sobrenome", "Silva", "email", "ana@exemplo.com", "sugestao", "Falem de <script>alert(1)</script> games"));
        Afirmar.igual(200, ok.statusCode(), "registrada");
        Afirmar.contem(ok.body(), "Sugestão registrada", "confirmação");
        sistema.sugestoes().aguardarEnvios().join();
        Afirmar.igual(1, sistema.caixaDeSaida().enviados().size(), "e-mail à equipe saiu");
        entrar();
        var painel = get("/painel/sugestoes").body();
        Afirmar.contem(painel, "Ana Silva", "aparece no painel");
        Afirmar.naoContem(painel, "<script>alert(1)</script>", "texto do visitante escapado");
    }

    @Teste("P5: se o registro falhar, o visitante vê o aviso e nenhum e-mail sai")
    void sugestaoFalha() {
        sistema.simulador().definir(Falha.REGISTRO_DA_SUGESTAO, true);
        var r = post("/sugestoes", Map.of("nome", "Ana", "sobrenome", "Silva", "email", "ana@exemplo.com", "sugestao", "Ideia"));
        Afirmar.igual(503, r.statusCode(), "falha do sistema");
        Afirmar.contem(r.body(), "Não conseguimos registrar", "aviso");
        Afirmar.igual(0, sistema.caixaDeSaida().enviados().size(), "sem e-mail");
    }

    @Teste("Muitas tentativas de login erradas são bloqueadas (limite de 5 por minuto)")
    void limiteDeTentativas() {
        int ultimo = 0;
        for (int i = 0; i < 7; i++) {
            ultimo = post("/painel/entrar", Map.of("email", "admin@exemplo.com", "senha", "errada" + i)).statusCode();
        }
        Afirmar.igual(401, ultimo, "continua recusando");
        var r = post("/painel/entrar", Map.of("email", "admin@exemplo.com", "senha", "senha-de-teste-1"));
        Afirmar.contem(r.body(), "Muitas tentativas", "bloqueia até a senha certa por um minuto");
    }

    @Teste("POST com Origin de outro site é recusado")
    void origem() throws Exception {
        var req = HttpRequest.newBuilder(URI.create(base + "/sugestoes")).header("Origin", "http://site-malicioso.example")
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(HttpRequest.BodyPublishers.ofString("nome=A&sobrenome=B&email=a@b.co&sugestao=x")).build();
        var r = cliente.send(req, HttpResponse.BodyHandlers.ofString());
        Afirmar.igual(403, r.statusCode(), "origem diferente do host");
        Afirmar.igual(0, sistema.banco().sugestoes().todos().size(), "nada registrado");
    }

    @Teste("Demonstração: só administrador; ligar uma falha pelo painel muda o simulador")
    void demonstracao() {
        entrar();
        Afirmar.contem(get("/painel/demonstracao").body(), "Simulador de falhas", "tela da demonstração");
        var r = post("/painel/demonstracao/falhas", Map.of("_csrf", csrf(), "falha", "GRAVACAO_DA_MATERIA", "ligada", "sim"));
        Afirmar.igual(303, r.statusCode(), "alternou");
        Afirmar.verdadeiro(sistema.simulador().ativa(Falha.GRAVACAO_DA_MATERIA), "falha ligada");
    }

    @Teste("Sair encerra a sessão")
    void sair() {
        entrar();
        var r = post("/painel/sair", Map.of("_csrf", csrf()));
        Afirmar.igual(303, r.statusCode(), "saiu");
        Afirmar.igual(303, get("/painel").statusCode(), "painel voltou a exigir login");
    }

    void fechar() {
        sistema.close();
    }
}
