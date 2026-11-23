package br.ucsal.casadigital;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.concurrent.atomic.AtomicReference;

/** Relógio que só anda quando o teste manda: permite "passar 7 dias" sem esperar. */
public final class RelogioDeTeste extends Clock {

    private final AtomicReference<Instant> agora;
    private final ZoneId fuso;

    public RelogioDeTeste(Instant inicio) {
        this(inicio, ZoneId.of("America/Bahia"));
    }

    private RelogioDeTeste(Instant inicio, ZoneId fuso) {
        this.agora = new AtomicReference<>(inicio);
        this.fuso = fuso;
    }

    public void avancar(Duration tempo) {
        agora.updateAndGet(i -> i.plus(tempo));
    }

    @Override
    public ZoneId getZone() {
        return fuso;
    }

    @Override
    public Clock withZone(ZoneId zone) {
        return new RelogioDeTeste(agora.get(), zone);
    }

    @Override
    public Instant instant() {
        return agora.get();
    }
}
