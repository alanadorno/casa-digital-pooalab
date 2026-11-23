package br.ucsal.casadigital.dominio;

import java.time.Instant;
import java.time.LocalDate;

/**
 * Item da seção UCSAL (instituição, ligas, projetos ou eventos).
 *
 * <p>RN13: só é publicado depois de conferido com a fonte; o sistema registra quem conferiu e quando.
 * RN14: itens de "O que está rolando" têm vigência (início e fim); fora dela ficam ocultos.
 */
public final class ItemUcsal extends EntidadeBase<ItemUcsal> {

    private Subsecao subsecao;
    private String titulo;
    private String descricao;
    private String fonte;
    private LocalDate inicio;
    private LocalDate fim;
    private String conferidoPor;
    private Instant conferidoEm;
    private boolean encerrado;
    private boolean demonstracao;

    private ItemUcsal() {
    }

    public ItemUcsal(Subsecao subsecao, String titulo, String descricao, String fonte,
                     LocalDate inicio, LocalDate fim, String conferidoPor, Instant conferidoEm,
                     boolean demonstracao) {
        this.subsecao = subsecao;
        this.titulo = titulo;
        this.descricao = descricao;
        this.fonte = fonte;
        this.inicio = subsecao.temVigencia() ? inicio : null;
        this.fim = subsecao.temVigencia() ? fim : null;
        this.conferidoPor = conferidoPor;
        this.conferidoEm = conferidoEm;
        this.demonstracao = demonstracao;
    }

    /** RN14: agendado antes do início, no ar até o fim do último dia, encerrado depois. */
    public SituacaoItem situacao(LocalDate hoje) {
        if (!subsecao.temVigencia()) {
            return SituacaoItem.NO_AR;
        }
        if (hoje.isBefore(inicio)) {
            return SituacaoItem.AGENDADO;
        }
        return hoje.isAfter(fim) ? SituacaoItem.ENCERRADO : SituacaoItem.NO_AR;
    }

    public boolean visivelNaAreaPublica(LocalDate hoje) {
        return situacao(hoje) == SituacaoItem.NO_AR;
    }

    /** Chamado pelo agendador quando a data de fim passa: marca "encerrado" no painel. */
    public void marcarEncerrado() {
        this.encerrado = true;
    }

    public boolean encerrado() {
        return encerrado;
    }

    public Subsecao subsecao() {
        return subsecao;
    }

    public String titulo() {
        return titulo;
    }

    public String descricao() {
        return descricao;
    }

    public String fonte() {
        return fonte;
    }

    public LocalDate inicio() {
        return inicio;
    }

    public LocalDate fim() {
        return fim;
    }

    public String conferidoPor() {
        return conferidoPor;
    }

    public Instant conferidoEm() {
        return conferidoEm;
    }

    public boolean demonstracao() {
        return demonstracao;
    }
}
