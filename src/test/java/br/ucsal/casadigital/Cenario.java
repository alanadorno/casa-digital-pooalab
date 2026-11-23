package br.ucsal.casadigital;

import br.ucsal.casadigital.dominio.BarramentoEventos;
import br.ucsal.casadigital.dominio.EventoDominio;
import br.ucsal.casadigital.dominio.Usuario;
import br.ucsal.casadigital.infra.Banco;
import br.ucsal.casadigital.infra.EmailInstavel;
import br.ucsal.casadigital.infra.EmailSimulado;
import br.ucsal.casadigital.infra.RepositorioComFalhas;
import br.ucsal.casadigital.infra.Senhas;
import br.ucsal.casadigital.infra.SimuladorDeFalhas;
import br.ucsal.casadigital.infra.SimuladorDeFalhas.Falha;
import br.ucsal.casadigital.servico.CatalogoPublico;
import br.ucsal.casadigital.servico.ConsultaServico;
import br.ucsal.casadigital.servico.Consulta;
import br.ucsal.casadigital.servico.LimpezaDoDestaque;
import br.ucsal.casadigital.servico.MateriaForm;
import br.ucsal.casadigital.servico.PublicacaoServico;
import br.ucsal.casadigital.servico.Publicacao;
import br.ucsal.casadigital.servico.Sugestoes;
import br.ucsal.casadigital.servico.SugestoesServico;
import br.ucsal.casadigital.servico.Ucsal;
import br.ucsal.casadigital.servico.UcsalServico;
import br.ucsal.casadigital.validacao.Validador;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executor;

/**
 * Monta um sistema completo, mas em memória e com relógio controlável, para os testes de serviço.
 * É uma mini "raiz de composição" de teste: as mesmas classes de produção, ligadas por construtor.
 */
public final class Cenario {

    public final RelogioDeTeste relogio = new RelogioDeTeste(Instant.parse("2026-11-23T12:00:00Z"));
    public final Banco banco = Banco.emMemoria();
    public final SimuladorDeFalhas falhas = new SimuladorDeFalhas();
    public final BarramentoEventos eventos = new BarramentoEventos();
    public final List<EventoDominio> ocorridos = Collections.synchronizedList(new ArrayList<>());
    public final EmailSimulado caixa = new EmailSimulado(null, relogio);
    public final Publicacao publicacao;
    public final Consulta consulta;
    public final Sugestoes sugestoes;
    public final Ucsal ucsal;
    public final Usuario equipe;

    public Cenario() {
        this(Runnable::run);
    }

    public Cenario(Executor executor) {
        eventos.inscreverEmTodos(ocorridos::add);
        String sal = Senhas.novoSal();
        equipe = banco.usuarios().salvar(new Usuario("Alana (teste)", "alana@exemplo.com", sal, Senhas.hash("senha-de-teste", sal), true));
        Validador validador = Validador.padrao();
        var materias = RepositorioComFalhas.envolver(banco.materias(), () -> falhas.ativa(Falha.GRAVACAO_DA_MATERIA), false);
        var repoSugestoes = RepositorioComFalhas.envolver(banco.sugestoes(), () -> falhas.ativa(Falha.REGISTRO_DA_SUGESTAO), true);
        CatalogoPublico catalogo = new CatalogoPublico(banco.materias(), eventos);
        new LimpezaDoDestaque(eventos, banco);
        consulta = new ConsultaServico(catalogo, banco, relogio, 6);
        publicacao = new PublicacaoServico(materias, banco, eventos, relogio, validador, 7);
        sugestoes = new SugestoesServico(repoSugestoes,
                new EmailInstavel(caixa, () -> falhas.ativa(Falha.ENVIO_DO_EMAIL_DA_EQUIPE)),
                new EmailInstavel(caixa, () -> falhas.ativa(Falha.ENVIO_DA_RESPOSTA)),
                eventos, relogio, validador, executor, "equipe@exemplo.com", Duration.ofMinutes(15), 3);
        ucsal = new UcsalServico(banco.itens(), eventos, relogio, validador);
    }

    /** Formulário de matéria completo e válido para publicar. */
    public static MateriaForm materiaValida(String titulo) {
        return new MateriaForm(0, titulo, "esporte", "Resumo", "https://www.youtube.com/watch?v=476X2Cs6vAM",
                "Texto da matéria.", "Fonte 1", true, false);
    }

    /** Publica uma matéria válida e devolve o id. */
    public long publicar(String titulo) {
        var r = publicacao.publicar(materiaValida(titulo), equipe);
        if (r instanceof br.ucsal.casadigital.servico.Resultado.Sucesso<br.ucsal.casadigital.dominio.Materia> s) {
            relogio.avancar(Duration.ofMinutes(1));
            return s.valor().id();
        }
        throw new AssertionError("Não consegui publicar: " + r);
    }
}
