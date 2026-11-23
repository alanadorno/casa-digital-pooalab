package br.ucsal.casadigital.dominio;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Matéria completa de um episódio: vídeo do YouTube, texto e referências (RQ01).
 *
 * <p>GRASP <b>Information Expert</b>: a própria matéria sabe se pode mudar de estado e faz a mudança.
 * Padrão <b>Builder</b> para construir uma matéria sem um construtor com muitos parâmetros.
 */
public final class Materia extends EntidadeBase<Materia> {

    private String titulo;
    private Editoria editoria;
    private String resumo = "";
    private String videoUrl;
    private String texto;
    private List<String> referencias = new ArrayList<>();
    private StatusMateria status = StatusMateria.RASCUNHO;
    private String autor;
    private String revisadaPor;
    private int episodio;
    private Instant criadaEm;
    private Instant atualizadaEm;
    private Instant publicadaEm;
    private boolean demonstracao;

    /** Usado pela leitura do JSON (reflexão) e pelo Builder. */
    private Materia() {
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    protected void copiarEstadoMutavel() {
        this.referencias = new ArrayList<>(referencias);
    }

    // ---------- comportamento ----------

    /** Atualiza o conteúdo editável; não mexe no estado nem na publicação. */
    public void atualizarConteudo(String titulo, Editoria editoria, String resumo, String videoUrl,
                                  String texto, List<String> referencias, Instant agora) {
        this.titulo = titulo;
        this.editoria = editoria;
        this.resumo = resumo == null ? "" : resumo;
        this.videoUrl = videoUrl;
        this.texto = texto;
        this.referencias = new ArrayList<>(referencias);
        this.atualizadaEm = agora;
    }

    public void enviarParaRevisao(Instant agora) {
        mudarPara(StatusMateria.EM_REVISAO, agora);
    }

    /** RN04: só publica quem informa quem revisou e aprovou a matéria. */
    public void publicar(String revisor, Instant agora) {
        if (revisor == null || revisor.isBlank()) {
            throw new TransicaoInvalidaException("Toda matéria precisa ser revisada antes de publicar (RN04).");
        }
        mudarPara(StatusMateria.PUBLICADA, agora);
        this.revisadaPor = revisor;
        this.publicadaEm = agora;
    }

    public void voltarARascunho(Instant agora) {
        mudarPara(StatusMateria.RASCUNHO, agora);
        this.publicadaEm = null;
    }

    private void mudarPara(StatusMateria destino, Instant agora) {
        if (!status.podeIrPara(destino)) {
            throw new TransicaoInvalidaException(
                    "Uma matéria %s não pode passar para %s.".formatted(status.rotulo(), destino.rotulo()));
        }
        this.status = destino;
        this.atualizadaEm = agora;
    }

    public boolean publicada() {
        return status == StatusMateria.PUBLICADA;
    }

    public Optional<String> videoId() {
        return YouTube.extrairId(videoUrl);
    }

    // ---------- leitura ----------

    public String titulo() {
        return titulo;
    }

    public Editoria editoria() {
        return editoria;
    }

    public String resumo() {
        return resumo;
    }

    public String videoUrl() {
        return videoUrl;
    }

    public String texto() {
        return texto;
    }

    public List<String> referencias() {
        return List.copyOf(referencias);
    }

    public StatusMateria status() {
        return status;
    }

    public String autor() {
        return autor;
    }

    public String revisadaPor() {
        return revisadaPor;
    }

    public int episodio() {
        return episodio;
    }

    public Instant criadaEm() {
        return criadaEm;
    }

    public Instant atualizadaEm() {
        return atualizadaEm;
    }

    public Instant publicadaEm() {
        return publicadaEm;
    }

    public boolean demonstracao() {
        return demonstracao;
    }

    // ---------- Builder ----------

    public static final class Builder {
        private final Materia m = new Materia();

        public Builder titulo(String v) {
            m.titulo = v;
            return this;
        }

        public Builder editoria(Editoria v) {
            m.editoria = v;
            return this;
        }

        public Builder resumo(String v) {
            m.resumo = v == null ? "" : v;
            return this;
        }

        public Builder videoUrl(String v) {
            m.videoUrl = v;
            return this;
        }

        public Builder texto(String v) {
            m.texto = v;
            return this;
        }

        public Builder referencias(List<String> v) {
            m.referencias = new ArrayList<>(v);
            return this;
        }

        public Builder autor(String v) {
            m.autor = v;
            return this;
        }

        public Builder episodio(int v) {
            m.episodio = v;
            return this;
        }

        public Builder demonstracao(boolean v) {
            m.demonstracao = v;
            return this;
        }

        public Builder criadaEm(Instant v) {
            m.criadaEm = v;
            m.atualizadaEm = v;
            return this;
        }

        public Materia construir() {
            if (m.criadaEm == null) {
                throw new IllegalStateException("criadaEm é obrigatório");
            }
            return m.copiar();
        }
    }
}
