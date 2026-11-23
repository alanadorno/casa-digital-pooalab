package br.ucsal.casadigital;

import br.ucsal.casadigital.servico.DestaqueAutomatico;
import br.ucsal.casadigital.servico.DestaqueFixado;
import br.ucsal.casadigital.servico.FabricaDePoliticaDeDestaque;
import br.ucsal.casadigital.dominio.DestaqueManual;

import java.time.Duration;
import java.util.Optional;

/** RN05: destaque automático, destaque manual de 7 dias e a Strategy escolhida pela Factory Method. */
public class DestaqueTest {

    private final Cenario c = new Cenario();

    @Teste("Automático: a principal é a mais recente e as secundárias são as 3 seguintes")
    void automatico() {
        for (int i = 1; i <= 6; i++) {
            c.publicar("M" + i);
        }
        var home = c.consulta.home();
        Afirmar.igual("M6", home.principal().orElseThrow().titulo(), "principal");
        Afirmar.igual(3, home.secundarias().size(), "três secundárias");
        Afirmar.igual("M5", home.secundarias().getFirst().titulo(), "a primeira secundária");
    }

    @Teste("Manual: fixar uma matéria antiga a coloca como principal")
    void manual() {
        long antiga = c.publicar("Antiga");
        c.publicar("Recente");
        c.publicacao.fixarDestaque(antiga, c.equipe);
        var home = c.consulta.home();
        Afirmar.igual("Antiga", home.principal().orElseThrow().titulo(), "fixada vira principal");
        Afirmar.verdadeiro(home.destaqueManual().isPresent(), "home sabe que é manual");
        Afirmar.falso(home.secundarias().stream().anyMatch(m -> m.id() == antiga), "não repete nas secundárias");
    }

    @Teste("Manual expira em 7 dias e a home volta ao automático")
    void expira() {
        long antiga = c.publicar("Antiga");
        c.publicar("Recente");
        c.publicacao.fixarDestaque(antiga, c.equipe);
        c.relogio.avancar(Duration.ofDays(6));
        Afirmar.igual("Antiga", c.consulta.home().principal().orElseThrow().titulo(), "no sexto dia ainda vale");
        c.relogio.avancar(Duration.ofDays(2));
        Afirmar.igual("Recente", c.consulta.home().principal().orElseThrow().titulo(), "depois de 7 dias volta ao automático");
    }

    @Teste("Despublicar a matéria fixada remove o destaque (Observer)")
    void despublicarFixada() {
        c.publicar("Base");
        long fixada = c.publicar("Fixada");
        c.publicacao.fixarDestaque(fixada, c.equipe);
        c.publicacao.despublicar(fixada, c.equipe);
        Afirmar.verdadeiro(c.banco.destaque().isEmpty(), "destaque limpo");
        Afirmar.igual("Base", c.consulta.home().principal().orElseThrow().titulo(), "home automática");
    }

    @Teste("Só matéria publicada pode ser fixada")
    void soPublicada() {
        var r = c.publicacao.fixarDestaque(999, c.equipe);
        Afirmar.falso(r.ok(), "matéria inexistente");
    }

    @Teste("Factory Method devolve a Strategy certa")
    void fabrica() {
        var agora = c.relogio.instant();
        Afirmar.verdadeiro(FabricaDePoliticaDeDestaque.para(Optional.empty(), agora) instanceof DestaqueAutomatico, "sem destaque → automático");
        var vigente = new DestaqueManual(1, agora, agora.plus(Duration.ofDays(7)), "x");
        Afirmar.verdadeiro(FabricaDePoliticaDeDestaque.para(Optional.of(vigente), agora) instanceof DestaqueFixado, "vigente → fixado");
        var vencido = new DestaqueManual(1, agora.minus(Duration.ofDays(8)), agora.minus(Duration.ofDays(1)), "x");
        Afirmar.verdadeiro(FabricaDePoliticaDeDestaque.para(Optional.of(vencido), agora) instanceof DestaqueAutomatico, "vencido → automático");
    }
}
