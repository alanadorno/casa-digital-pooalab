package br.ucsal.casadigital.dominio;

import java.time.Duration;
import java.time.Instant;

/**
 * Sugestão de pauta enviada pelo visitante, sem cadastro (RN07, RN08).
 *
 * <p>GRASP <b>Information Expert</b>: a sugestão conhece os próprios estados, as tentativas de e-mail e
 * quando é a próxima tentativa; por isso as regras RN09 e RN10 ficam aqui e não espalhadas.
 */
public final class Sugestao extends EntidadeBase<Sugestao> {

    private String nome;
    private String sobrenome;
    private String email;
    private String texto;
    private EstadoSugestao estado = EstadoSugestao.NOVA;
    private DecisaoSugestao decisao;
    private Instant criadaEm;
    private Instant avaliadaEm;
    private Instant respondidaEm;

    // RN09: controle do envio por e-mail à equipe
    private boolean emailPendente;
    private int falhasDeEnvio;
    private Instant proximaTentativa;
    private Instant emailEnviadoEm;

    // RN10: se a resposta ao visitante falhar, a sugestão continua "avaliada" e o painel avisa
    private boolean falhaNaResposta;

    private Sugestao() {
    }

    /** Cria uma sugestão no estado NOVA (RN10). */
    public Sugestao(String nome, String sobrenome, String email, String texto, Instant agora) {
        this.nome = nome;
        this.sobrenome = sobrenome;
        this.email = email;
        this.texto = texto;
        this.criadaEm = agora;
        this.estado = EstadoSugestao.NOVA;
    }

    // ---------- RN10: estados ----------

    public void avaliar(DecisaoSugestao decisao, Instant agora) {
        this.estado = estado.aoAvaliar();
        this.decisao = decisao;
        this.avaliadaEm = agora;
    }

    /** Só chamado depois que o e-mail de resposta foi realmente enviado. */
    public void registrarRespostaEnviada(Instant agora) {
        this.estado = estado.aoResponder();
        this.respondidaEm = agora;
        this.falhaNaResposta = false;
    }

    public void registrarFalhaNaResposta() {
        this.falhaNaResposta = true;
    }

    // ---------- RN09: e-mail à equipe ----------

    public void registrarEmailEnviado(Instant agora) {
        this.emailPendente = false;
        this.emailEnviadoEm = agora;
        this.proximaTentativa = null;
    }

    /**
     * O e-mail falhou: marca "e-mail pendente" e agenda a próxima tentativa, até o máximo informado.
     * A primeira falha é a tentativa 1; depois de {@code maximoReenvios} reenvios sem sucesso a sugestão
     * continua marcada como pendente, visível no painel, mas o sistema deixa de reenviar sozinho.
     */
    public void registrarEmailFalhou(Instant agora, Duration intervalo, int maximoReenvios) {
        this.emailPendente = true;
        this.falhasDeEnvio++;
        this.proximaTentativa = falhasDeEnvio <= maximoReenvios ? agora.plus(intervalo) : null;
    }

    public boolean aguardandoReenvio(Instant agora) {
        return emailPendente && proximaTentativa != null && !proximaTentativa.isAfter(agora);
    }

    // ---------- leitura ----------

    public String nome() {
        return nome;
    }

    public String sobrenome() {
        return sobrenome;
    }

    public String nomeCompleto() {
        return nome + " " + sobrenome;
    }

    public String email() {
        return email;
    }

    public String texto() {
        return texto;
    }

    public EstadoSugestao estado() {
        return estado;
    }

    public DecisaoSugestao decisao() {
        return decisao;
    }

    public Instant criadaEm() {
        return criadaEm;
    }

    public Instant avaliadaEm() {
        return avaliadaEm;
    }

    public Instant respondidaEm() {
        return respondidaEm;
    }

    public boolean emailPendente() {
        return emailPendente;
    }

    public int falhasDeEnvio() {
        return falhasDeEnvio;
    }

    public Instant proximaTentativa() {
        return proximaTentativa;
    }

    public Instant emailEnviadoEm() {
        return emailEnviadoEm;
    }

    public boolean falhaNaResposta() {
        return falhaNaResposta;
    }
}
