package br.ucsal.casadigital.web;

import br.ucsal.casadigital.dominio.Editoria;
import br.ucsal.casadigital.dominio.Materia;
import br.ucsal.casadigital.servico.Consulta;
import br.ucsal.casadigital.servico.Resultado;
import br.ucsal.casadigital.servico.SugestaoForm;
import br.ucsal.casadigital.servico.Sugestoes;
import br.ucsal.casadigital.servico.Ucsal;
import br.ucsal.casadigital.web.VisaoPublica.EstadoContato;

import java.util.Optional;

/**
 * Controlador do site público (processos P4 e P5, sem cadastro, RN07). Os métodos anotados com {@link Rota}
 * são encontrados pelo {@link Roteador} por reflexão. Cada método só traduz HTTP: a regra de negócio
 * fica nos serviços (GRASP: Controller fino, <b>High Cohesion</b>).
 */
public final class SiteControlador {

    private final Consulta consulta;
    private final Sugestoes sugestoes;
    private final Ucsal ucsal;

    public SiteControlador(Consulta consulta, Sugestoes sugestoes, Ucsal ucsal) {
        this.consulta = consulta;
        this.sugestoes = sugestoes;
        this.ucsal = ucsal;
    }

    @Rota(caminho = "/")
    public Resposta home(Requisicao req) {
        return Resposta.html(new VisoesPublicas.PaginaInicial(consulta.home()).renderizar());
    }

    @Rota(caminho = "/editoria/{slug}")
    public Resposta editoria(Requisicao req) {
        Optional<Editoria> editoria = Editoria.porSlug(req.parametro("slug"));
        if (editoria.isEmpty()) {
            return naoEncontrada();
        }
        int pagina = (int) req.parametroLong("pagina", 1);
        return Resposta.html(new VisoesPublicas.PaginaDeEditoria(editoria.get(), consulta.editoria(editoria.get(), pagina)).renderizar());
    }

    @Rota(caminho = "/materia/{id}")
    public Resposta materia(Requisicao req) {
        Optional<Materia> materia = consulta.materia(req.parametroLong("id", -1));
        return materia.map(m -> Resposta.html(new VisoesPublicas.PaginaDeMateria(m).renderizar()))
                .orElseGet(SiteControlador::naoEncontrada);
    }

    @Rota(caminho = "/ucsal")
    public Resposta ucsal(Requisicao req) {
        return Resposta.html(new VisoesPublicas.PaginaUcsal(ucsal.visiveisPorSubsecao()).renderizar());
    }

    @Rota(caminho = "/sobre-nos")
    public Resposta sobre(Requisicao req) {
        return Resposta.html(new VisoesPublicas.PaginaSobre(consulta.totalDeMaterias()).renderizar());
    }

    /** P5: o switch sobre {@link Resultado} é exaustivo: sucesso, dados inválidos ou falha do sistema. */
    @Rota(metodo = "POST", caminho = "/sugestoes")
    public Resposta enviarSugestao(Requisicao req) {
        SugestaoForm form = Formularios.ligar(req, SugestaoForm.class);
        return switch (sugestoes.registrar(form)) {
            case Resultado.Sucesso<?> s -> Resposta.html(new VisoesPublicas.PaginaDeSugestao(new EstadoContato(
                    SugestaoForm.vazio(), br.ucsal.casadigital.validacao.Erros.novo(), "ok",
                    "Sugestão registrada! Obrigado por ajudar a escolher as próximas pautas.")).renderizar());
            case Resultado.Invalido<?> i -> Resposta.html(422, new VisoesPublicas.PaginaDeSugestao(new EstadoContato(
                    form, i.erros(), "erro", "Revise os campos marcados.")).renderizar());
            case Resultado.Falha<?> f -> Resposta.html(503, new VisoesPublicas.PaginaDeSugestao(new EstadoContato(
                    form, br.ucsal.casadigital.validacao.Erros.novo(), "falha", f.mensagem())).renderizar());
        };
    }

    private static Resposta naoEncontrada() {
        return Resposta.html(404, new VisoesPublicas.Erro(404, "Página não encontrada",
                "O endereço pode ter mudado ou a matéria não está mais no ar.").renderizar());
    }
}
