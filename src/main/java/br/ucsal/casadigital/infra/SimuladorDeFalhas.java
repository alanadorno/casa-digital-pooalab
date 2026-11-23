package br.ucsal.casadigital.infra;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Chaves para <b>simular falhas</b> durante a demonstração (painel &gt; Demonstração).
 * Permitem mostrar ao vivo o tratamento de erros dos processos P3 e P5, que seria difícil de
 * provocar de outro jeito: falha na gravação da matéria, no registro da sugestão e no envio de e-mail.
 */
public final class SimuladorDeFalhas {

    public enum Falha {
        GRAVACAO_DA_MATERIA("Falha ao gravar a matéria (P3: a matéria continua rascunho)"),
        REGISTRO_DA_SUGESTAO("Falha ao registrar a sugestão (P5: nada é enviado por e-mail)"),
        ENVIO_DO_EMAIL_DA_EQUIPE("Falha ao enviar o e-mail à equipe (P5: fica \"e-mail pendente\" e reenvia sozinho)"),
        ENVIO_DA_RESPOSTA("Falha ao enviar a resposta ao visitante (P5: a sugestão continua \"avaliada\")");

        private final String descricao;

        Falha(String descricao) {
            this.descricao = descricao;
        }

        public String descricao() {
            return descricao;
        }
    }

    private final Set<Falha> ativas = ConcurrentHashMap.newKeySet();

    public boolean ativa(Falha falha) {
        return ativas.contains(falha);
    }

    public void definir(Falha falha, boolean ligada) {
        if (ligada) {
            ativas.add(falha);
        } else {
            ativas.remove(falha);
        }
    }

    public void desligarTodas() {
        ativas.clear();
    }
}
