package br.ucsal.casadigital;

import br.ucsal.casadigital.dominio.Materia;
import br.ucsal.casadigital.dominio.Editoria;
import br.ucsal.casadigital.dominio.Sugestao;
import br.ucsal.casadigital.dominio.Usuario;
import br.ucsal.casadigital.infra.Json;
import br.ucsal.casadigital.infra.MedidorDeChamadas;
import br.ucsal.casadigital.servico.Consulta;
import br.ucsal.casadigital.servico.MateriaForm;
import br.ucsal.casadigital.servico.SugestaoForm;
import br.ucsal.casadigital.validacao.Validador;
import br.ucsal.casadigital.web.Formularios;
import br.ucsal.casadigital.web.Requisicao;
import br.ucsal.casadigital.web.Resposta;
import br.ucsal.casadigital.web.Rota;
import br.ucsal.casadigital.web.Roteador;
import br.ucsal.casadigital.web.ExigeLogin;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/** Reflexão e anotações: JSON, validação, formulários, rotas e proxy dinâmico. */
public class ReflexaoTest {

    public static final class ControladorDeTeste {
        @Rota(caminho = "/oi/{nome}")
        public Resposta oi(Requisicao r) {
            return Resposta.texto(200, "oi " + r.parametro("nome"));
        }

        @ExigeLogin(administrador = true)
        @Rota(metodo = "POST", caminho = "/segredo")
        public Resposta segredo(Requisicao r) {
            return Resposta.texto(200, "segredo");
        }

        public Resposta semAnotacao(Requisicao r) {
            return Resposta.texto(200, "x");
        }
    }

    @Teste("Roteador descobre por reflexão só os métodos com @Rota e lê @ExigeLogin")
    void roteador() {
        Roteador roteador = new Roteador().registrar(new ControladorDeTeste());
        Afirmar.igual(2, roteador.rotas().size(), "duas rotas anotadas");
        var rota = roteador.resolver(Requisicao.simulada("GET", "/oi/Ana", Map.of())).orElseThrow();
        Afirmar.falso(rota.exigeLogin(), "rota pública");
        var segredo = roteador.resolver(Requisicao.simulada("POST", "/segredo", Map.of())).orElseThrow();
        Afirmar.verdadeiro(segredo.exigeLogin() && segredo.apenasAdministrador(), "login + administrador");
        Afirmar.verdadeiro(roteador.resolver(Requisicao.simulada("GET", "/segredo", Map.of())).isEmpty(), "método HTTP errado não casa");
    }

    @Teste("Parâmetro do caminho ({nome}) é extraído por expressão regular com grupo nomeado")
    void parametroDoCaminho() {
        Roteador roteador = new Roteador().registrar(new ControladorDeTeste());
        Requisicao req = Requisicao.simulada("GET", "/oi/Mundo", Map.of());
        var rota = roteador.resolver(req).orElseThrow();
        Afirmar.igual("oi Mundo", rota.executar(req).corpoComoTexto(), "executa o método encontrado");
    }

    @Teste("Formularios.ligar preenche um record pelos componentes (getRecordComponents), com boolean e long")
    void ligarFormulario() {
        var req = Requisicao.simulada("POST", "/x", Map.of("id", "7", "titulo", "Olá", "revisada", "on", "editoria", "esporte"));
        MateriaForm form = Formularios.ligar(req, MateriaForm.class);
        Afirmar.igual(7L, form.id(), "long");
        Afirmar.igual("Olá", form.titulo(), "texto");
        Afirmar.verdadeiro(form.revisada(), "checkbox marcado");
        Afirmar.falso(form.destacar(), "checkbox ausente");
        Afirmar.igual("", form.texto(), "campo ausente vira texto vazio");
    }

    @Teste("Validador lê as anotações dos componentes do record")
    void validador() {
        var erros = Validador.padrao().validar(new SugestaoForm("", "Silva", "errado", "x".repeat(2001)), false);
        Afirmar.verdadeiro(erros.tem("nome"), "@Obrigatorio");
        Afirmar.verdadeiro(erros.tem("email"), "@EmailValido");
        Afirmar.verdadeiro(erros.tem("sugestao"), "@Tamanho");
        Afirmar.falso(erros.tem("sobrenome"), "sobrenome está certo");
    }

    @Teste("Json: escreve e lê de volta um objeto de domínio (campos privados, enum, Instant, lista)")
    void jsonIdaEVolta() {
        Materia m = Materia.builder().titulo("Título com \"aspas\" e acentuação ção").editoria(Editoria.BAHIA).texto("Linha 1\nLinha 2")
                .referencias(List.of("A", "B")).autor("Ana").criadaEm(Instant.parse("2026-11-01T10:00:00Z")).construir();
        m.atribuirId(5);
        String json = Json.escrever(m);
        Materia lida = Json.ler(json, Materia.class);
        Afirmar.igual(m.titulo(), lida.titulo(), "título");
        Afirmar.igual(Editoria.BAHIA, lida.editoria(), "enum");
        Afirmar.igual(List.of("A", "B"), lida.referencias(), "lista");
        Afirmar.igual(m.criadaEm(), lida.criadaEm(), "instante");
        Afirmar.igual(5L, lida.id(), "campo herdado");
        Afirmar.igual("Linha 1\nLinha 2", lida.texto(), "quebra de linha");
    }

    @Teste("Json: lê estruturas aninhadas e recusa texto quebrado")
    void jsonErro() {
        Usuario u = Json.ler("{\"nome\":\"A\",\"email\":\"a@b.c\",\"administrador\":true}", Usuario.class);
        Afirmar.verdadeiro(u.administrador(), "boolean");
        Afirmar.lanca(IllegalArgumentException.class, () -> Json.ler("{\"nome\": ", Usuario.class), "JSON incompleto");
    }

    @Teste("Proxy dinâmico mede as chamadas sem alterar o serviço")
    void proxy() {
        MedidorDeChamadas medidor = new MedidorDeChamadas();
        Cenario c = new Cenario();
        Consulta medida = medidor.envolver(Consulta.class, c.consulta);
        medida.home();
        medida.home();
        medida.totalDeMaterias();
        var linhas = medidor.resumo();
        Afirmar.igual(2L, linhas.stream().filter(l -> l.metodo().equals("Consulta.home")).findFirst().orElseThrow().chamadas(), "duas chamadas a home");
        Afirmar.igual(1L, linhas.stream().filter(l -> l.metodo().equals("Consulta.totalDeMaterias")).findFirst().orElseThrow().chamadas(), "uma ao total");
    }
}
