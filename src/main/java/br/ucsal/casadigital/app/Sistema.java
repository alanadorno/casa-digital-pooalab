package br.ucsal.casadigital.app;

import br.ucsal.casadigital.dominio.BarramentoEventos;
import br.ucsal.casadigital.infra.Atividade;
import br.ucsal.casadigital.infra.Banco;
import br.ucsal.casadigital.infra.Configuracao;
import br.ucsal.casadigital.infra.EmailInstavel;
import br.ucsal.casadigital.infra.EmailSimulado;
import br.ucsal.casadigital.infra.MedidorDeChamadas;
import br.ucsal.casadigital.infra.RepositorioComFalhas;
import br.ucsal.casadigital.infra.SimuladorDeFalhas;
import br.ucsal.casadigital.infra.SimuladorDeFalhas.Falha;
import br.ucsal.casadigital.servico.Agendador;
import br.ucsal.casadigital.servico.Autenticacao;
import br.ucsal.casadigital.servico.AutenticacaoServico;
import br.ucsal.casadigital.servico.CatalogoPublico;
import br.ucsal.casadigital.servico.Consulta;
import br.ucsal.casadigital.servico.ConsultaServico;
import br.ucsal.casadigital.servico.DadosDeDemonstracao;
import br.ucsal.casadigital.servico.LimpezaDoDestaque;
import br.ucsal.casadigital.servico.Publicacao;
import br.ucsal.casadigital.servico.PublicacaoServico;
import br.ucsal.casadigital.servico.Sugestoes;
import br.ucsal.casadigital.servico.SugestoesServico;
import br.ucsal.casadigital.servico.Ucsal;
import br.ucsal.casadigital.servico.UcsalServico;
import br.ucsal.casadigital.validacao.Validador;
import br.ucsal.casadigital.web.FiltroDeAcesso;
import br.ucsal.casadigital.web.FiltroDeArquivosEstaticos;
import br.ucsal.casadigital.web.FiltroDeErros;
import br.ucsal.casadigital.web.FiltroDeLog;
import br.ucsal.casadigital.web.FiltroDeSeguranca;
import br.ucsal.casadigital.web.FiltroDeSessao;
import br.ucsal.casadigital.web.FrontController;
import br.ucsal.casadigital.web.GerenciadorDeSessoes;
import br.ucsal.casadigital.web.Html;
import br.ucsal.casadigital.web.PainelControlador;
import br.ucsal.casadigital.web.Roteador;
import br.ucsal.casadigital.web.ServidorWeb;
import br.ucsal.casadigital.web.SiteControlador;
import br.ucsal.casadigital.web.VisaoPublica;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Clock;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * <b>Raiz de composição</b>: o único lugar que usa {@code new} para ligar as peças do sistema.
 * Todas as outras classes recebem o que precisam pelo construtor e dependem de interfaces
 * (<b>Inversão de Dependência</b>, <b>Baixo Acoplamento</b>). Os testes usam esta mesma montagem
 * com um relógio controlável, banco em memória e porta 0.
 */
public final class Sistema implements AutoCloseable {

    private final ServidorWeb servidor;
    private final Agendador agendador;
    private final ExecutorService executorDeTarefas;
    private final Banco banco;
    private final Sugestoes sugestoes;
    private final SimuladorDeFalhas simulador;
    private final EmailSimulado caixaDeSaida;
    private final Roteador roteador;

    private Sistema(ServidorWeb servidor, Agendador agendador, ExecutorService executorDeTarefas, Banco banco, Sugestoes sugestoes,
                    SimuladorDeFalhas simulador, EmailSimulado caixaDeSaida, Roteador roteador) {
        this.servidor = servidor;
        this.agendador = agendador;
        this.executorDeTarefas = executorDeTarefas;
        this.banco = banco;
        this.sugestoes = sugestoes;
        this.simulador = simulador;
        this.caixaDeSaida = caixaDeSaida;
        this.roteador = roteador;
    }

    public static Sistema montar(Configuracao config, Clock relogio) throws IOException {
        // --- infraestrutura
        String pasta = config.pastaDeDados();
        boolean emDisco = !pasta.isBlank();
        Banco banco = emDisco ? Banco.abrir(Path.of(pasta, "banco.json")) : Banco.emMemoria();
        SimuladorDeFalhas simulador = new SimuladorDeFalhas();
        BarramentoEventos eventos = new BarramentoEventos();
        Atividade atividade = new Atividade();
        eventos.inscreverEmTodos(atividade::registrar);               // Observer: o painel de atividade ouve tudo
        ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();   // e-mails em threads virtuais

        EmailSimulado caixa = new EmailSimulado(emDisco ? Path.of(pasta, "emails") : null, relogio);
        var emailDaEquipe = new EmailInstavel(caixa, () -> simulador.ativa(Falha.ENVIO_DO_EMAIL_DA_EQUIPE));   // Decorator
        var emailDeResposta = new EmailInstavel(caixa, () -> simulador.ativa(Falha.ENVIO_DA_RESPOSTA));

        // Proxy dinâmico: as gravações podem ser derrubadas pelo simulador, sem tocar no repositório real
        var materias = RepositorioComFalhas.envolver(banco.materias(), () -> simulador.ativa(Falha.GRAVACAO_DA_MATERIA), false);
        var repoSugestoes = RepositorioComFalhas.envolver(banco.sugestoes(), () -> simulador.ativa(Falha.REGISTRO_DA_SUGESTAO), true);

        DadosDeDemonstracao.garantirAdministrador(banco, config);
        if (config.carregarDadosDeDemonstracao()) {
            DadosDeDemonstracao.carregar(banco, relogio);
        }

        // --- serviços
        Validador validador = Validador.padrao();
        CatalogoPublico catalogo = new CatalogoPublico(banco.materias(), eventos);
        new LimpezaDoDestaque(eventos, banco);

        MedidorDeChamadas medidor = new MedidorDeChamadas();
        Consulta consulta = medidor.envolver(Consulta.class, new ConsultaServico(catalogo, banco, relogio, config.materiasPorPagina()));
        Publicacao publicacao = medidor.envolver(Publicacao.class,
                new PublicacaoServico(materias, banco, eventos, relogio, validador, config.diasDoDestaqueManual()));
        Sugestoes sugestoes = medidor.envolver(Sugestoes.class, new SugestoesServico(repoSugestoes, emailDaEquipe, emailDeResposta, eventos,
                relogio, validador, executor, config.emailDaEquipe(), config.intervaloDeReenvio(), config.maximoDeReenvios()));
        Ucsal ucsal = medidor.envolver(Ucsal.class, new UcsalServico(banco.itens(), eventos, relogio, validador));
        Autenticacao autenticacao = medidor.envolver(Autenticacao.class, new AutenticacaoServico(banco.usuarios(), eventos, relogio, validador));

        // --- web
        Html.definirFuso(config.fusoHorario());
        VisaoPublica.definirRedes(new VisaoPublica.Redes(config.instagram(), config.youtubeDoPodcast()));
        GerenciadorDeSessoes sessoes = new GerenciadorDeSessoes(relogio, java.time.Duration.ofHours(config.horasDeSessao()));
        Roteador roteador = new Roteador();
        roteador.registrar(new SiteControlador(consulta, sugestoes, ucsal));
        roteador.registrar(new PainelControlador(autenticacao, sessoes, publicacao, consulta, sugestoes, ucsal, simulador, caixa,
                atividade, medidor, roteador, relogio));

        // Chain of Responsibility: a ordem importa (erros envolve tudo; acesso vem depois da sessão)
        FrontController frente = new FrontController(roteador, List.of(
                new FiltroDeErros(), new FiltroDeLog(), new FiltroDeSeguranca(), new FiltroDeArquivosEstaticos(),
                new FiltroDeSessao(sessoes, autenticacao), new FiltroDeAcesso(roteador)));
        ServidorWeb servidor = new ServidorWeb(config.host(), config.porta(), frente);
        Agendador agendador = new Agendador(sugestoes, ucsal, config.intervaloDoAgendador());
        return new Sistema(servidor, agendador, executor, banco, sugestoes, simulador, caixa, roteador);
    }

    public void iniciar() {
        servidor.iniciar();
    }

    public int porta() {
        return servidor.porta();
    }

    public Banco banco() {
        return banco;
    }

    public Sugestoes sugestoes() {
        return sugestoes;
    }

    public SimuladorDeFalhas simulador() {
        return simulador;
    }

    public EmailSimulado caixaDeSaida() {
        return caixaDeSaida;
    }

    public Roteador roteador() {
        return roteador;
    }

    @Override
    public void close() {
        agendador.close();
        servidor.close();
        executorDeTarefas.close();
    }
}
