package br.ucsal.casadigital;

import br.ucsal.casadigital.dominio.BarramentoEventos;
import br.ucsal.casadigital.dominio.EstadoSugestao;
import br.ucsal.casadigital.dominio.EventoDominio;
import br.ucsal.casadigital.dominio.Materia;
import br.ucsal.casadigital.dominio.StatusMateria;
import br.ucsal.casadigital.dominio.TransicaoInvalidaException;
import br.ucsal.casadigital.dominio.YouTube;
import br.ucsal.casadigital.infra.Configuracao;
import br.ucsal.casadigital.infra.Senhas;
import br.ucsal.casadigital.servico.Pagina;
import br.ucsal.casadigital.validacao.Erros;
import br.ucsal.casadigital.validacao.RegraComposta;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

/** Padrões de projeto do código: Prototype, Builder, State, Observer, Composite, Singleton e outros. */
public class PadroesTest {

    private static Materia exemplo() {
        return Materia.builder().titulo("T").texto("x").referencias(List.of("r")).autor("a").criadaEm(Instant.parse("2026-11-01T10:00:00Z")).construir();
    }

    @Teste("Prototype: copiar() gera um objeto independente do original")
    void prototype() {
        Materia original = exemplo();
        Materia copia = original.copiar();
        copia.enviarParaRevisao(Instant.now());
        Afirmar.igual(StatusMateria.RASCUNHO, original.status(), "original não muda");
        Afirmar.igual(StatusMateria.EM_REVISAO, copia.status(), "cópia mudou");
    }

    @Teste("Builder: monta a matéria passo a passo e começa como rascunho")
    void builder() {
        Materia m = exemplo();
        Afirmar.igual("T", m.titulo(), "título");
        Afirmar.igual(StatusMateria.RASCUNHO, m.status(), "status inicial");
    }

    @Teste("State: transições de matéria proibidas lançam exceção; RN04 exige revisor")
    void estadoDaMateria() {
        Materia m = exemplo();
        Afirmar.lanca(TransicaoInvalidaException.class, () -> m.publicar("", Instant.now()), "sem revisor não publica");
        m.publicar("Ana", Instant.now());
        Afirmar.igual(StatusMateria.PUBLICADA, m.status(), "publicada");
        Afirmar.lanca(TransicaoInvalidaException.class, () -> m.enviarParaRevisao(Instant.now()), "publicada não volta para revisão");
    }

    @Teste("State (enum com comportamento): EstadoSugestao recusa transições fora de ordem")
    void estadoDaSugestao() {
        Afirmar.lanca(TransicaoInvalidaException.class, EstadoSugestao.NOVA::aoResponder, "NOVA não responde");
        Afirmar.igual(EstadoSugestao.AVALIADA, EstadoSugestao.NOVA.aoAvaliar(), "NOVA → AVALIADA");
        Afirmar.igual(EstadoSugestao.RESPONDIDA, EstadoSugestao.AVALIADA.aoResponder(), "AVALIADA → RESPONDIDA");
        Afirmar.lanca(TransicaoInvalidaException.class, EstadoSugestao.RESPONDIDA::aoAvaliar, "RESPONDIDA não volta");
    }

    @Teste("Observer: ouvintes por tipo de evento; um ouvinte com erro não impede os outros")
    void observer() {
        BarramentoEventos barramento = new BarramentoEventos();
        List<String> visto = new ArrayList<>();
        barramento.inscrever(EventoDominio.MateriaPublicada.class, e -> {
            throw new IllegalStateException("ouvinte quebrado");
        });
        barramento.inscrever(EventoDominio.MateriaPublicada.class, e -> visto.add("publicada:" + e.titulo()));
        barramento.inscrever(EventoDominio.SugestaoRegistrada.class, e -> visto.add("sugestao"));
        barramento.publicar(new EventoDominio.MateriaPublicada(Instant.now(), 1, "X", "Ana"));
        Afirmar.igual(List.of("publicada:X"), visto, "só o ouvinte do tipo certo, e o segundo ouvinte rodou");
    }

    @Teste("Sealed + pattern matching: todo evento tem descrição")
    void eventosSelados() {
        Instant t = Instant.now();
        List<EventoDominio> todos = List.of(
                new EventoDominio.MateriaPublicada(t, 1, "A", "x"), new EventoDominio.MateriaAtualizada(t, 1, "A", "x"),
                new EventoDominio.MateriaDespublicada(t, 1, "A", "x"), new EventoDominio.DestaqueFixado(t, 1, "x"),
                new EventoDominio.DestaqueRemovido(t, "motivo"), new EventoDominio.SugestaoRegistrada(t, 1),
                new EventoDominio.EmailDaSugestaoEnviado(t, 1, 1), new EventoDominio.EmailDaSugestaoFalhou(t, 1, 1, "m"),
                new EventoDominio.SugestaoAvaliada(t, 1, br.ucsal.casadigital.dominio.DecisaoSugestao.ARQUIVADA, "x"),
                new EventoDominio.SugestaoRespondida(t, 1), new EventoDominio.ItemUcsalCadastrado(t, 1, "A", "x"),
                new EventoDominio.ItemUcsalEncerrado(t, 1, "A"), new EventoDominio.AcessoAoPainel(t, "e", true));
        todos.forEach(e -> Afirmar.falso(e.descricao().isBlank(), "descrição de " + e.getClass().getSimpleName()));
        Afirmar.igual(EventoDominio.class.getPermittedSubclasses().length, todos.size(), "testei todos os tipos permitidos");
    }

    @Teste("Composite: RegraComposta junta várias regras e acumula todos os erros")
    void composite() {
        var regra = RegraComposta.<String>de((s, e) -> e.adicionar("a", "erro a"), (s, e) -> e.adicionar("b", "erro b"));
        Erros erros = Erros.novo();
        regra.verificar("x", erros);
        Afirmar.verdadeiro(erros.tem("a") && erros.tem("b"), "as duas regras rodaram");
    }

    @Teste("Singleton: Configuracao.instancia() é sempre o mesmo objeto; de(Map) cria outra isolada")
    void singleton() {
        Afirmar.verdadeiro(Configuracao.instancia() == Configuracao.instancia(), "mesma instância");
        Configuracao outra = Configuracao.de(Map.of("pagina.tamanho", "3"));
        Afirmar.igual(3, outra.materiasPorPagina(), "valor do teste");
        Afirmar.igual(6, Configuracao.de(Map.of()).materiasPorPagina(), "padrão RN11");
    }

    @Teste("YouTube: extrai o id de vários formatos de link e monta as URLs")
    void youtube() {
        Afirmar.igual("476X2Cs6vAM", YouTube.extrairId("https://www.youtube.com/watch?v=476X2Cs6vAM&t=10").orElseThrow(), "watch");
        Afirmar.igual("476X2Cs6vAM", YouTube.extrairId("https://youtu.be/476X2Cs6vAM").orElseThrow(), "youtu.be");
        Afirmar.igual("476X2Cs6vAM", YouTube.extrairId("https://www.youtube.com/embed/476X2Cs6vAM").orElseThrow(), "embed");
        Afirmar.verdadeiro(YouTube.extrairId("https://vimeo.com/123").isEmpty(), "outro site");
        Afirmar.verdadeiro(YouTube.extrairId("javascript:alert(1)").isEmpty(), "esquema perigoso");
    }

    @Teste("Senhas: PBKDF2 confere a senha certa e recusa a errada; tokens são diferentes")
    void senhas() {
        String sal = Senhas.novoSal();
        String hash = Senhas.hash("segredo", sal);
        Afirmar.verdadeiro(Senhas.confere("segredo", sal, hash), "certa");
        Afirmar.falso(Senhas.confere("Segredo", sal, hash), "errada");
        Afirmar.falso(hash.contains("segredo"), "o hash não guarda a senha");
        Afirmar.falso(Senhas.token().equals(Senhas.token()), "tokens únicos");
    }

    @Teste("Pagina: recorta uma lista com skip/limit e ajusta páginas fora do intervalo")
    void pagina() {
        List<Integer> todos = IntStream.rangeClosed(1, 13).boxed().toList();
        Afirmar.igual(List.of(7, 8, 9, 10, 11, 12), Pagina.de(todos, 2, 6).itens(), "página 2");
        Afirmar.igual(3, Pagina.de(todos, 1, 6).totalDePaginas(), "total de páginas");
        Afirmar.igual(3, Pagina.de(todos, 99, 6).numero(), "página alta vira a última");
        Afirmar.igual(1, Pagina.de(todos, -5, 6).numero(), "página negativa vira a primeira");
    }
}
