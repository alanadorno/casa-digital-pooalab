package br.ucsal.casadigital;

import br.ucsal.casadigital.dominio.Materia;
import br.ucsal.casadigital.dominio.DestaqueManual;
import br.ucsal.casadigital.infra.Banco;
import br.ucsal.casadigital.servico.DadosDeDemonstracao;
import br.ucsal.casadigital.servico.Resultado;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;

/** O banco em arquivo JSON sobrevive a reiniciar o sistema. */
public class PersistenciaTest {

    @Teste("Dados gravados em data/banco.json voltam ao abrir o banco de novo (incluindo o destaque manual)")
    void sobreviveAoReinicio() throws IOException {
        Path pasta = Files.createTempDirectory("casa-teste");
        try {
            Path arquivo = pasta.resolve("banco.json");
            Banco primeiro = Banco.abrir(arquivo);
            Cenario c = new Cenario();
            var materia = ((Resultado.Sucesso<Materia>) c.publicacao.publicar(Cenario.materiaValida("Persistida"), c.equipe)).valor();
            primeiro.materias().salvar(materia.copiar());
            primeiro.definirDestaque(new DestaqueManual(1, Instant.parse("2026-11-23T12:00:00Z"), Instant.parse("2026-11-30T12:00:00Z"), "Ana"));

            Banco segundo = Banco.abrir(arquivo);
            Afirmar.igual(1, segundo.materias().todos().size(), "matéria recarregada");
            Afirmar.igual("Persistida", segundo.materias().todos().getFirst().titulo(), "título");
            Afirmar.igual("Ana", segundo.destaque().orElseThrow().fixadoPor(), "destaque recarregado");
            Afirmar.falso(Files.exists(pasta.resolve("banco.json.tmp")), "arquivo temporário não sobra");
        } finally {
            try (var arquivos = Files.walk(pasta)) {
                arquivos.sorted(java.util.Comparator.reverseOrder()).forEach(p -> p.toFile().delete());
            }
        }
    }

    @Teste("O repositório devolve cópias: alterar o objeto lido não muda o banco sem salvar")
    void devolveCopias() {
        Banco banco = Banco.emMemoria();
        Cenario c = new Cenario();
        DadosDeDemonstracao.carregar(banco, c.relogio);
        Materia lida = banco.materias().todos().getFirst();
        String original = lida.titulo();
        lida.atualizarConteudo("Mudou na memória", lida.editoria(), "", lida.videoUrl(), lida.texto(), lida.referencias(), Instant.now());
        Afirmar.igual(original, banco.materias().porId(lida.id()).orElseThrow().titulo(), "banco intacto até salvar");
    }

    @Teste("Dados de demonstração: 12 publicadas, 1 rascunho e itens UCSAL, todos marcados como demonstração")
    void dadosDeDemonstracao() {
        Banco banco = Banco.emMemoria();
        DadosDeDemonstracao.carregar(banco, new RelogioDeTeste(Instant.parse("2026-11-23T12:00:00Z")));
        long publicadas = banco.materias().todos().stream().filter(Materia::publicada).count();
        Afirmar.igual(12L, publicadas, "publicadas");
        Afirmar.igual(13, banco.materias().todos().size(), "com o rascunho");
        Afirmar.verdadeiro(banco.materias().todos().stream().allMatch(Materia::demonstracao), "tudo marcado como demonstração");
        Afirmar.verdadeiro(banco.itens().todos().size() >= 4, "itens UCSAL");
        Duration.ZERO.isZero();
    }
}
