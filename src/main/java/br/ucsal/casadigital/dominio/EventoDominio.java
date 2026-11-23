package br.ucsal.casadigital.dominio;

import java.time.Instant;

/**
 * Eventos de negócio que o sistema publica quando algo relevante acontece (padrão <b>Observer</b>).
 *
 * <p>A interface é <b>selada</b> ({@code sealed}): só os tipos listados em {@code permits} existem.
 * Isso permite usar <i>pattern matching</i> em {@code switch} sem {@code default}, e o compilador avisa
 * se um novo evento for criado e algum {@code switch} esquecer de tratá-lo.
 */
public sealed interface EventoDominio {

    Instant quando();

    record MateriaPublicada(Instant quando, long materiaId, String titulo, String por) implements EventoDominio {
    }

    record MateriaAtualizada(Instant quando, long materiaId, String titulo, String por) implements EventoDominio {
    }

    record MateriaDespublicada(Instant quando, long materiaId, String titulo, String por) implements EventoDominio {
    }

    record DestaqueFixado(Instant quando, long materiaId, String por) implements EventoDominio {
    }

    record DestaqueRemovido(Instant quando, String motivo) implements EventoDominio {
    }

    record SugestaoRegistrada(Instant quando, long sugestaoId) implements EventoDominio {
    }

    record EmailDaSugestaoEnviado(Instant quando, long sugestaoId, int tentativa) implements EventoDominio {
    }

    record EmailDaSugestaoFalhou(Instant quando, long sugestaoId, int tentativa, String motivo) implements EventoDominio {
    }

    record SugestaoAvaliada(Instant quando, long sugestaoId, DecisaoSugestao decisao, String por) implements EventoDominio {
    }

    record SugestaoRespondida(Instant quando, long sugestaoId) implements EventoDominio {
    }

    record ItemUcsalCadastrado(Instant quando, long itemId, String titulo, String conferidoPor) implements EventoDominio {
    }

    record ItemUcsalEncerrado(Instant quando, long itemId, String titulo) implements EventoDominio {
    }

    record AcessoAoPainel(Instant quando, String email, boolean sucesso) implements EventoDominio {
    }

    /** Texto curto para o painel de atividade. Um {@code switch} exaustivo: sem {@code default}. */
    default String descricao() {
        return switch (this) {
            case MateriaPublicada e -> "Matéria publicada: \"%s\" (por %s)".formatted(e.titulo(), e.por());
            case MateriaAtualizada e -> "Matéria atualizada: \"%s\" (por %s)".formatted(e.titulo(), e.por());
            case MateriaDespublicada e -> "Matéria despublicada: \"%s\" (por %s)".formatted(e.titulo(), e.por());
            case DestaqueFixado e -> "Destaque manual da home fixado por %s (matéria #%d)".formatted(e.por(), e.materiaId());
            case DestaqueRemovido e -> "Home voltou ao destaque automático (%s)".formatted(e.motivo());
            case SugestaoRegistrada e -> "Sugestão #%d registrada como nova".formatted(e.sugestaoId());
            case EmailDaSugestaoEnviado e -> "E-mail da sugestão #%d enviado à equipe (tentativa %d)".formatted(e.sugestaoId(), e.tentativa());
            case EmailDaSugestaoFalhou e -> "E-mail da sugestão #%d falhou (tentativa %d): %s".formatted(e.sugestaoId(), e.tentativa(), e.motivo());
            case SugestaoAvaliada e -> "Sugestão #%d avaliada como %s por %s".formatted(e.sugestaoId(), e.decisao().rotulo(), e.por());
            case SugestaoRespondida e -> "Sugestão #%d respondida ao visitante".formatted(e.sugestaoId());
            case ItemUcsalCadastrado e -> "Item UCSAL cadastrado: \"%s\" (conferido por %s)".formatted(e.titulo(), e.conferidoPor());
            case ItemUcsalEncerrado e -> "Item UCSAL encerrado automaticamente: \"%s\"".formatted(e.titulo());
            case AcessoAoPainel e -> (e.sucesso() ? "Entrada no painel: " : "Tentativa de entrada recusada: ") + e.email();
        };
    }
}
