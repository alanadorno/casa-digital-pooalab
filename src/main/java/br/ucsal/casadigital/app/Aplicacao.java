package br.ucsal.casadigital.app;

import br.ucsal.casadigital.infra.Configuracao;

import java.time.Clock;

/** Ponto de entrada: {@code java Tarefas.java run}. */
public final class Aplicacao {

    private Aplicacao() {
    }

    public static void main(String[] args) throws Exception {
        Configuracao config = Configuracao.instancia();      // Singleton: só aqui, no main
        Sistema sistema = Sistema.montar(config, Clock.system(config.fusoHorario()));
        Runtime.getRuntime().addShutdownHook(new Thread(sistema::close, "encerramento"));
        sistema.iniciar();
        String endereco = "http://" + (config.host().equals("0.0.0.0") ? "localhost" : config.host()) + ":" + sistema.porta();
        System.out.println("""

                Casa Digital - Isso Nao E Um Radio (POOALAB 2026.2)
                  Site publico : %s/
                  Painel       : %s/painel
                  Acesso de demonstracao (ficticio): %s / %s
                  Dados        : %s
                  Para encerrar: Ctrl+C
                """.formatted(endereco, endereco, config.administradorEmail(), config.administradorSenha(),
                config.pastaDeDados().isBlank() ? "somente em memoria" : config.pastaDeDados() + "/"));
    }
}
