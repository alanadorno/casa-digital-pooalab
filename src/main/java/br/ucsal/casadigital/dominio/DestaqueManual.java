package br.ucsal.casadigital.dominio;

import java.time.Instant;

/**
 * RN05: matéria fixada manualmente como principal da home. Vale até {@code ate}
 * (7 dias depois da escolha) ou até o editor clicar em "Voltar ao automático".
 */
public final class DestaqueManual {

    private long materiaId;
    private Instant fixadoEm;
    private Instant ate;
    private String fixadoPor;

    private DestaqueManual() {
    }

    public DestaqueManual(long materiaId, Instant fixadoEm, Instant ate, String fixadoPor) {
        this.materiaId = materiaId;
        this.fixadoEm = fixadoEm;
        this.ate = ate;
        this.fixadoPor = fixadoPor;
    }

    public boolean vigente(Instant agora) {
        return agora.isBefore(ate);
    }

    public long materiaId() {
        return materiaId;
    }

    public Instant fixadoEm() {
        return fixadoEm;
    }

    public Instant ate() {
        return ate;
    }

    public String fixadoPor() {
        return fixadoPor;
    }
}
