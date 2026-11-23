package br.ucsal.casadigital.servico;

import br.ucsal.casadigital.dominio.DestaqueManual;

import java.time.Instant;
import java.util.Optional;

/**
 * Padrão <b>Factory Method</b> (fábrica estática): escolhe a política certa para o momento. O destaque manual
 * só vale enquanto não passaram os 7 dias; depois disso a home volta sozinha ao automático.
 */
public final class FabricaDePoliticaDeDestaque {

    private FabricaDePoliticaDeDestaque() {
    }

    public static PoliticaDestaque para(Optional<DestaqueManual> destaque, Instant agora) {
        return destaque.filter(d -> d.vigente(agora))
                .<PoliticaDestaque>map(DestaqueFixado::new)
                .orElseGet(DestaqueAutomatico::new);
    }
}
