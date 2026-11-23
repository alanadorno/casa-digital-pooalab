package br.ucsal.casadigital;

import br.ucsal.casadigital.dominio.EventoDominio;
import br.ucsal.casadigital.dominio.Materia;
import br.ucsal.casadigital.dominio.StatusMateria;
import br.ucsal.casadigital.infra.SimuladorDeFalhas.Falha;
import br.ucsal.casadigital.servico.MateriaForm;
import br.ucsal.casadigital.servico.Resultado;

/** Processo P3 e regras RN02 a RN04: publicar só com tudo certo e só depois de gravar. */
public class PublicacaoTest {

    private final Cenario c = new Cenario();

    @Teste("RN02/RN03: publicar sem vídeo, texto e referência devolve erros por campo e não grava nada")
    void publicarIncompleto() {
        var form = new MateriaForm(0, "Só o título", "esporte", "", "", "", "", true, false);
        var r = c.publicacao.publicar(form, c.equipe);
        var invalido = (Resultado.Invalido<Materia>) r;
        Afirmar.verdadeiro(invalido.erros().tem("video"), "falta o vídeo");
        Afirmar.verdadeiro(invalido.erros().tem("texto"), "falta o texto");
        Afirmar.verdadeiro(invalido.erros().tem("referencias"), "falta a referência");
        Afirmar.igual(0, c.banco.materias().todos().size(), "nada foi gravado");
    }

    @Teste("RN04: sem a confirmação de revisão a matéria não é publicada")
    void semRevisao() {
        var form = new MateriaForm(0, "Titulo", "esporte", "", "https://youtu.be/476X2Cs6vAM", "Texto", "Fonte", false, false);
        var invalido = (Resultado.Invalido<Materia>) c.publicacao.publicar(form, c.equipe);
        Afirmar.verdadeiro(invalido.erros().tem("revisada"), "deve cobrar a revisão");
    }

    @Teste("Link que não é do YouTube é recusado pela anotação @LinkYouTube")
    void linkInvalido() {
        var form = new MateriaForm(0, "Titulo", "esporte", "", "https://exemplo.com/video", "Texto", "Fonte", true, false);
        var invalido = (Resultado.Invalido<Materia>) c.publicacao.publicar(form, c.equipe);
        Afirmar.verdadeiro(invalido.erros().tem("video"), "link inválido");
    }

    @Teste("Rascunho só exige o título (parâmetro paraRascunho das anotações)")
    void rascunhoSoComTitulo() {
        var form = new MateriaForm(0, "Rascunho", "", "", "", "", "", false, false);
        var r = c.publicacao.salvarRascunho(form, c.equipe);
        var materia = ((Resultado.Sucesso<Materia>) r).valor();
        Afirmar.igual(StatusMateria.RASCUNHO, materia.status(), "continua rascunho");
        Afirmar.verdadeiro(c.consulta.home().principal().isEmpty(), "rascunho não aparece na home");
    }

    @Teste("P3: matéria publicada aparece na home e na editoria, e o evento é emitido")
    void publicaEAparece() {
        long id = c.publicar("Gol de placa");
        Afirmar.igual(id, c.consulta.home().principal().orElseThrow().id(), "é a principal da home");
        Afirmar.igual(1, c.consulta.editoria(br.ucsal.casadigital.dominio.Editoria.ESPORTE, 1).total(), "está na editoria");
        Afirmar.verdadeiro(c.ocorridos.stream().anyMatch(e -> e instanceof EventoDominio.MateriaPublicada), "evento publicado");
    }

    @Teste("P3: se a gravação falhar, nada é publicado e a home não muda")
    void falhaNaGravacao() {
        c.publicar("Já no ar");
        c.falhas.definir(Falha.GRAVACAO_DA_MATERIA, true);
        var r = c.publicacao.publicar(Cenario.materiaValida("Nova"), c.equipe);
        Afirmar.verdadeiro(r instanceof Resultado.Falha<Materia>, "deve ser falha do sistema");
        Afirmar.igual("Já no ar", c.consulta.home().principal().orElseThrow().titulo(), "home intacta");
        Afirmar.igual(1, c.consulta.totalDeMaterias(), "só a primeira está publicada");
        c.falhas.definir(Falha.GRAVACAO_DA_MATERIA, false);
        Afirmar.verdadeiro(c.publicacao.publicar(Cenario.materiaValida("Nova"), c.equipe).ok(), "depois que volta, publica");
    }

    @Teste("Despublicar tira da área pública e volta a rascunho")
    void despublicar() {
        long id = c.publicar("Sai do ar");
        c.publicacao.despublicar(id, c.equipe);
        Afirmar.igual(0, c.consulta.totalDeMaterias(), "não há publicadas");
        Afirmar.igual(StatusMateria.RASCUNHO, c.publicacao.buscar(id).orElseThrow().status(), "voltou a rascunho");
        Afirmar.verdadeiro(c.consulta.materia(id).isEmpty(), "a página pública não a encontra mais");
    }

    @Teste("Editar uma matéria publicada atualiza o catálogo público")
    void editarPublicada() {
        long id = c.publicar("Titulo antigo");
        var form = new MateriaForm(id, "Titulo novo", "esporte", "", "https://youtu.be/476X2Cs6vAM", "Texto", "Fonte", true, false);
        Afirmar.verdadeiro(c.publicacao.publicar(form, c.equipe).ok(), "atualizou");
        Afirmar.igual("Titulo novo", c.consulta.materia(id).orElseThrow().titulo(), "catálogo atualizado");
    }

    @Teste("RN11: a editoria pagina de 6 em 6, da mais recente para a mais antiga")
    void paginacao() {
        for (int i = 1; i <= 8; i++) {
            c.publicar("Materia " + i);
        }
        var p1 = c.consulta.editoria(br.ucsal.casadigital.dominio.Editoria.ESPORTE, 1);
        var p2 = c.consulta.editoria(br.ucsal.casadigital.dominio.Editoria.ESPORTE, 2);
        Afirmar.igual(6, p1.itens().size(), "primeira página cheia");
        Afirmar.igual(2, p2.itens().size(), "segunda página com o resto");
        Afirmar.igual(2, p1.totalDePaginas(), "duas páginas");
        Afirmar.igual("Materia 8", p1.itens().getFirst().titulo(), "a mais recente primeiro");
    }
}
