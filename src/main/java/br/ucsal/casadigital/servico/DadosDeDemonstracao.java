package br.ucsal.casadigital.servico;

import br.ucsal.casadigital.dominio.Editoria;
import br.ucsal.casadigital.dominio.ItemUcsal;
import br.ucsal.casadigital.dominio.Materia;
import br.ucsal.casadigital.dominio.Subsecao;
import br.ucsal.casadigital.dominio.Usuario;
import br.ucsal.casadigital.infra.Banco;
import br.ucsal.casadigital.infra.Configuracao;
import br.ucsal.casadigital.infra.Senhas;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * Dados <b>fictícios e seguros</b> para demonstração: nenhuma pessoa real, nenhuma credencial real.
 * Só são criados quando o banco está vazio. Tudo é marcado como "demonstração" no painel.
 */
public final class DadosDeDemonstracao {

    /** Vídeos públicos do YouTube usados só para exibir o player e a miniatura nas matérias de exemplo. */
    private static final List<String> VIDEOS = List.of(
            "476X2Cs6vAM", "aJY-2-8WZmA", "p85EBXLmffw", "thCaQa_sU0U", "dDCfkdXU0cM", "lquBhvxPEH4");

    private DadosDeDemonstracao() {
    }

    public static void garantirAdministrador(Banco banco, Configuracao config) {
        if (banco.usuarios().todos().isEmpty()) {
            String sal = Senhas.novoSal();
            banco.usuarios().salvar(new Usuario(config.administradorNome(), config.administradorEmail(), sal,
                    Senhas.hash(config.administradorSenha(), sal), true));
        }
    }

    public static void carregar(Banco banco, Clock relogio) {
        if (!banco.materias().todos().isEmpty() || !banco.itens().todos().isEmpty()) {
            return;
        }
        Instant agora = relogio.instant();
        String[][] materias = {
                {"ENTRETENIMENTO_E_CULTURA", "Episódio de exemplo 1: a cena cultural de Salvador", "Conversa de demonstração sobre cultura."},
                {"ENTRETENIMENTO_E_CULTURA", "Episódio de exemplo 2: do rádio ao podcast", "Como o áudio ganhou novas telas."},
                {"ENTRETENIMENTO_E_CULTURA", "Episódio de exemplo 3: bastidores de uma gravação", "Um dia no estúdio, contado pela equipe."},
                {"ENTRETENIMENTO_E_CULTURA", "Episódio de exemplo 4: música universitária", "Bandas e coletivos do campus."},
                {"ENTRETENIMENTO_E_CULTURA", "Episódio de exemplo 5: cinema e debate", "O que vimos, o que discutimos."},
                {"ENTRETENIMENTO_E_CULTURA", "Episódio de exemplo 6: teatro no campus", "Ensaios, estreias e plateia."},
                {"ENTRETENIMENTO_E_CULTURA", "Episódio de exemplo 7: festival de curtas", "Produção audiovisual estudantil."},
                {"ESPORTE", "Esporte de exemplo: o campeonato interno", "Equipes, jogos e torcida."},
                {"INOVACAO_E_TECNOLOGIA", "Tecnologia de exemplo: inteligência artificial na sala de aula", "Usos, limites e responsabilidade."},
                {"SAUDE", "Saúde de exemplo: rotina de estudos e bem-estar", "Sono, pausa e equilíbrio."},
                {"BAHIA", "Bahia de exemplo: patrimônio e memória", "Lugares que contam a nossa história."},
                {"UCSAL", "UCSAL de exemplo: a vida no campus", "Bastidores da universidade."},
        };
        for (int i = 0; i < materias.length; i++) {
            Instant publicadaEm = agora.minus(materias.length - i, ChronoUnit.DAYS).minus(i, ChronoUnit.HOURS);
            Materia m = Materia.builder()
                    .titulo(materias[i][1])
                    .editoria(Editoria.valueOf(materias[i][0]))
                    .resumo(materias[i][2])
                    .videoUrl("https://www.youtube.com/watch?v=" + VIDEOS.get(i % VIDEOS.size()))
                    .texto("Texto de demonstração da matéria \"" + materias[i][1] + "\".\n\n"
                            + "Aqui entra o texto aprofundado do episódio: contexto, entrevistados e trechos importantes.\n\n"
                            + "O vídeo acima é incorporado do YouTube; esta matéria existe só para demonstrar o sistema.")
                    .referencias(List.of("Referência de demonstração 1", "Referência de demonstração 2"))
                    .autor("Equipe (demonstração)")
                    .episodio(i < 7 ? i + 1 : 0)
                    .demonstracao(true)
                    .criadaEm(publicadaEm.minus(1, ChronoUnit.DAYS))
                    .construir();
            m.publicar("Equipe (demonstração)", publicadaEm);
            banco.materias().salvar(m);
        }
        Materia rascunho = Materia.builder().titulo("Rascunho de exemplo (ainda não publicado)")
                .editoria(Editoria.SAUDE).autor("Equipe (demonstração)").demonstracao(true).criadaEm(agora).construir();
        banco.materias().salvar(rascunho);

        LocalDate hoje = LocalDate.now(relogio);
        Instant conferido = agora;
        String quem = "Equipe (demonstração)";
        banco.itens().salvar(new ItemUcsal(Subsecao.SOBRE_A_INSTITUICAO, "Universidade Católica do Salvador",
                "Texto de demonstração sobre a instituição. Em uso real, será conferido com a UCSAL antes de ir ao ar.",
                "Demonstração", null, null, quem, conferido, true));
        banco.itens().salvar(new ItemUcsal(Subsecao.LIGAS, "Liga acadêmica de exemplo 1",
                "Descrição fictícia de uma liga acadêmica.", "Demonstração", null, null, quem, conferido, true));
        banco.itens().salvar(new ItemUcsal(Subsecao.LIGAS, "Liga acadêmica de exemplo 2",
                "Descrição fictícia de uma liga acadêmica.", "Demonstração", null, null, quem, conferido, true));
        banco.itens().salvar(new ItemUcsal(Subsecao.PROJETOS, "Projeto de extensão de exemplo",
                "Descrição fictícia de um projeto de extensão.", "Demonstração", null, null, quem, conferido, true));
        banco.itens().salvar(new ItemUcsal(Subsecao.O_QUE_ESTA_ROLANDO, "Evento de exemplo no ar",
                "Visível enquanto a data de fim não passa.", "Demonstração", hoje.minusDays(1), hoje.plusDays(10), quem, conferido, true));
        banco.itens().salvar(new ItemUcsal(Subsecao.O_QUE_ESTA_ROLANDO, "Evento de exemplo agendado",
                "Só aparece a partir da data de início.", "Demonstração", hoje.plusDays(5), hoje.plusDays(9), quem, conferido, true));
    }
}
