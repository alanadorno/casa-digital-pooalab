package br.ucsal.casadigital;

import br.ucsal.casadigital.dominio.Materia;
import br.ucsal.casadigital.dominio.Sugestao;
import br.ucsal.casadigital.infra.SimuladorDeFalhas.Falha;
import br.ucsal.casadigital.servico.MateriaForm;
import br.ucsal.casadigital.servico.Resultado;
import br.ucsal.casadigital.servico.SugestaoForm;

import java.time.Duration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.IntStream;

/** Threads virtuais, travas por entidade e CompletableFuture. */
public class ConcorrenciaTest {

    @Teste("100 sugestões simultâneas em threads virtuais: todas registradas, ids únicos, 100 e-mails")
    void muitasSugestoes() throws Exception {
        try (ExecutorService envios = Executors.newVirtualThreadPerTaskExecutor();
             ExecutorService visitantes = Executors.newVirtualThreadPerTaskExecutor()) {
            Cenario c = new Cenario(envios);
            List<Future<Resultado<Sugestao>>> futuros = IntStream.range(0, 100).mapToObj(i -> visitantes.submit(
                    () -> c.sugestoes.registrar(new SugestaoForm("Pessoa" + i, "Teste", "p" + i + "@exemplo.com", "Ideia " + i)))).toList();
            Set<Long> ids = new HashSet<>();
            for (var f : futuros) {
                ids.add(((Resultado.Sucesso<Sugestao>) f.get()).valor().id());
            }
            c.sugestoes.aguardarEnvios().join();
            Afirmar.igual(100, ids.size(), "ids únicos");
            Afirmar.igual(100, c.banco.sugestoes().todos().size(), "todas gravadas");
            Afirmar.igual(100, c.caixa.enviados().size(), "um e-mail por sugestão");
        }
    }

    @Teste("Reenvio simultâneo da mesma sugestão (travas por id): o e-mail sai uma única vez")
    void reenvioSemDuplicar() throws Exception {
        try (ExecutorService pool = Executors.newVirtualThreadPerTaskExecutor()) {
            Cenario c = new Cenario();
            c.falhas.definir(Falha.ENVIO_DO_EMAIL_DA_EQUIPE, true);
            var s = ((Resultado.Sucesso<Sugestao>) c.sugestoes.registrar(
                    new SugestaoForm("Ana", "Silva", "ana@exemplo.com", "Ideia"))).valor();
            c.falhas.definir(Falha.ENVIO_DO_EMAIL_DA_EQUIPE, false);
            List<? extends Future<?>> tarefas = IntStream.range(0, 30).mapToObj(i -> pool.submit(() -> c.sugestoes.reenviarAgora(s.id()))).toList();
            for (Future<?> t : tarefas) {
                t.get();
            }
            Afirmar.igual(1, c.caixa.enviados().size(), "sem e-mail duplicado");
        }
    }

    @Teste("Edições simultâneas da mesma matéria: nenhuma se perde nem corrompe (trava por id)")
    void edicoesDaMesmaMateria() throws Exception {
        try (ExecutorService pool = Executors.newVirtualThreadPerTaskExecutor()) {
            Cenario c = new Cenario();
            long id = c.publicar("Original");
            List<Future<Resultado<Materia>>> tarefas = IntStream.range(0, 40).mapToObj(i -> pool.submit(() ->
                    c.publicacao.publicar(new MateriaForm(id, "Versão " + i, "esporte", "", "https://youtu.be/476X2Cs6vAM", "Texto", "Fonte", true, false), c.equipe))).toList();
            for (var t : tarefas) {
                Afirmar.verdadeiro(t.get().ok(), "cada edição conclui");
            }
            String titulo = c.consulta.materia(id).orElseThrow().titulo();
            Afirmar.verdadeiro(titulo.startsWith("Versão "), "o título final é de uma das versões: " + titulo);
            Afirmar.igual(1, c.banco.materias().todos().size(), "continua uma matéria só");
        }
    }

    @Teste("Leitores da home durante publicações: nunca veem lista pela metade (snapshot volátil)")
    void leitoresEEscritores() throws Exception {
        try (ExecutorService pool = Executors.newVirtualThreadPerTaskExecutor()) {
            Cenario c = new Cenario();
            c.publicar("Base");
            List<Future<Boolean>> leitores = IntStream.range(0, 200).mapToObj(i -> pool.submit(() -> {
                var home = c.consulta.home();
                return home.principal().isPresent() && home.secundarias().size() <= 3;
            })).toList();
            for (int i = 0; i < 10; i++) {
                c.publicar("Nova " + i);
            }
            for (Future<Boolean> f : leitores) {
                Afirmar.verdadeiro(f.get(), "home sempre consistente");
            }
            Afirmar.igual(11, c.consulta.totalDeMaterias(), "11 publicadas");
        }
    }

    @Teste("Banco: gravações concorrentes de matérias geram ids diferentes")
    void idsUnicos() throws Exception {
        try (ExecutorService pool = Executors.newVirtualThreadPerTaskExecutor()) {
            Cenario c = new Cenario();
            List<Future<Resultado<Materia>>> tarefas = IntStream.range(0, 50).mapToObj(i -> pool.submit(
                    () -> c.publicacao.publicar(Cenario.materiaValida("M" + i), c.equipe))).toList();
            Set<Long> ids = new HashSet<>();
            for (var t : tarefas) {
                ids.add(((Resultado.Sucesso<Materia>) t.get()).valor().id());
            }
            Afirmar.igual(50, ids.size(), "50 ids distintos");
            c.relogio.avancar(Duration.ofSeconds(1));
        }
    }
}
