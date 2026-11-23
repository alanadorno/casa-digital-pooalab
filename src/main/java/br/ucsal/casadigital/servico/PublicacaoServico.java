package br.ucsal.casadigital.servico;

import br.ucsal.casadigital.dominio.BarramentoEventos;
import br.ucsal.casadigital.dominio.DestaqueManual;
import br.ucsal.casadigital.dominio.Editoria;
import br.ucsal.casadigital.dominio.EventoDominio.DestaqueFixado;
import br.ucsal.casadigital.dominio.EventoDominio.DestaqueRemovido;
import br.ucsal.casadigital.dominio.EventoDominio.MateriaAtualizada;
import br.ucsal.casadigital.dominio.EventoDominio.MateriaDespublicada;
import br.ucsal.casadigital.dominio.EventoDominio.MateriaPublicada;
import br.ucsal.casadigital.dominio.Materia;
import br.ucsal.casadigital.dominio.StatusMateria;
import br.ucsal.casadigital.dominio.TransicaoInvalidaException;
import br.ucsal.casadigital.dominio.Usuario;
import br.ucsal.casadigital.infra.ArmazemDeDestaque;
import br.ucsal.casadigital.infra.Repositorio;
import br.ucsal.casadigital.infra.RepositorioIndisponivelException;
import br.ucsal.casadigital.validacao.Erros;
import br.ucsal.casadigital.validacao.Regra;
import br.ucsal.casadigital.validacao.Validador;

import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Caso de uso P3 (publicar a matéria), seguindo o diagrama BPMN passo a passo:
 * <ol>
 *   <li>valida os dados do formulário (só diz se está correto; não publica nada);</li>
 *   <li>grava a matéria (se falhar, ela continua como estava, rascunho);</li>
 *   <li>só depois de gravada, publica o evento {@code MateriaPublicada}, que faz o catálogo
 *       atualizar a home e a editoria;</li>
 *   <li>e então devolve a confirmação ao editor.</li>
 * </ol>
 */
public final class PublicacaoServico implements Publicacao {

    private final Repositorio<Materia> materias;
    private final ArmazemDeDestaque destaque;
    private final BarramentoEventos eventos;
    private final Clock relogio;
    private final Validador validador;
    private final Regra<MateriaForm> regrasDePublicacao = Regras.paraPublicarMateria();
    private final int diasDoDestaque;
    private final TravasPorId travas = new TravasPorId();

    public PublicacaoServico(Repositorio<Materia> materias, ArmazemDeDestaque destaque, BarramentoEventos eventos,
                             Clock relogio, Validador validador, int diasDoDestaque) {
        this.materias = materias;
        this.destaque = destaque;
        this.eventos = eventos;
        this.relogio = relogio;
        this.validador = validador;
        this.diasDoDestaque = diasDoDestaque;
    }

    @Override
    public Resultado<Materia> salvarRascunho(MateriaForm form, Usuario autor) {
        return gravar(form, autor, StatusMateria.RASCUNHO);
    }

    @Override
    public Resultado<Materia> enviarParaRevisao(MateriaForm form, Usuario autor) {
        return gravar(form, autor, StatusMateria.EM_REVISAO);
    }

    @Override
    public Resultado<Materia> publicar(MateriaForm form, Usuario revisor) {
        return gravar(form, revisor, StatusMateria.PUBLICADA);
    }

    private Resultado<Materia> gravar(MateriaForm form, Usuario usuario, StatusMateria destino) {
        // 1. Validação: só decide se o formulário está correto.
        boolean completo = destino != StatusMateria.RASCUNHO;
        Erros erros = validador.validar(form, !completo);
        if (completo) {
            regrasDePublicacao.verificar(form, erros);
        }
        if (!erros.vazio()) {
            return Resultado.invalido(erros);
        }
        if (form.id() == 0) {
            return gravarNova(form, usuario, destino);
        }
        return travas.executar(form.id(), () -> gravarExistente(form, usuario, destino));
    }

    private Resultado<Materia> gravarNova(MateriaForm form, Usuario usuario, StatusMateria destino) {
        Instant agora = relogio.instant();
        Materia nova = Materia.builder()
                .titulo(form.titulo().trim())
                .editoria(Editoria.porSlug(form.editoria()).orElse(null))
                .resumo(form.resumo())
                .videoUrl(form.video())
                .texto(form.texto())
                .referencias(form.listaDeReferencias())
                .autor(usuario.nome())
                .criadaEm(agora)
                .construir();
        return aplicarEGravar(nova, form, usuario, destino, agora);
    }

    private Resultado<Materia> gravarExistente(MateriaForm form, Usuario usuario, StatusMateria destino) {
        Optional<Materia> existente = materias.porId(form.id());
        if (existente.isEmpty()) {
            return Resultado.falha("Matéria não encontrada.");
        }
        Instant agora = relogio.instant();
        Materia materia = existente.get();
        materia.atualizarConteudo(form.titulo().trim(), Editoria.porSlug(form.editoria()).orElse(null),
                form.resumo(), form.video(), form.texto(), form.listaDeReferencias(), agora);
        return aplicarEGravar(materia, form, usuario, destino, agora);
    }

    private Resultado<Materia> aplicarEGravar(Materia materia, MateriaForm form, Usuario usuario,
                                              StatusMateria destino, Instant agora) {
        boolean jaEstavaPublicada = materia.publicada();
        try {
            if (materia.status() != destino) {
                switch (destino) {
                    case RASCUNHO -> materia.voltarARascunho(agora);
                    case EM_REVISAO -> materia.enviarParaRevisao(agora);
                    case PUBLICADA -> materia.publicar(usuario.nome(), agora);
                }
            }
        } catch (TransicaoInvalidaException e) {
            return Resultado.falha(e.getMessage());
        }
        // 2. Gravação: a matéria só passa a "publicada" se isto terminar com sucesso.
        Materia gravada;
        try {
            gravada = materias.salvar(materia);
        } catch (RepositorioIndisponivelException e) {
            return Resultado.falha("Não foi possível gravar a matéria agora. Ela continua como rascunho; tente de novo.");
        }
        // 3. Só agora a home e a editoria são atualizadas (pelo Observer) e a publicação é confirmada.
        if (gravada.publicada() && !jaEstavaPublicada) {
            eventos.publicar(new MateriaPublicada(agora, gravada.id(), gravada.titulo(), usuario.nome()));
        }
        if (gravada.publicada() && jaEstavaPublicada) {
            eventos.publicar(new MateriaAtualizada(agora, gravada.id(), gravada.titulo(), usuario.nome()));
        }
        if (!gravada.publicada() && jaEstavaPublicada) {
            eventos.publicar(new MateriaDespublicada(agora, gravada.id(), gravada.titulo(), usuario.nome()));
        }
        if (gravada.publicada() && form.destacar()) {
            fixarDestaque(gravada.id(), usuario);
        }
        return Resultado.sucesso(gravada);
    }

    @Override
    public Resultado<Materia> despublicar(long id, Usuario por) {
        return travas.executar(id, () -> {
            Optional<Materia> existente = materias.porId(id);
            if (existente.isEmpty()) {
                return Resultado.falha("Matéria não encontrada.");
            }
            Materia materia = existente.get();
            Instant agora = relogio.instant();
            try {
                materia.voltarARascunho(agora);
                Materia gravada = materias.salvar(materia);
                eventos.publicar(new MateriaDespublicada(agora, id, gravada.titulo(), por.nome()));
                return Resultado.sucesso(gravada);
            } catch (TransicaoInvalidaException e) {
                return Resultado.falha(e.getMessage());
            } catch (RepositorioIndisponivelException e) {
                return Resultado.falha("Não foi possível gravar agora; a matéria continua publicada.");
            }
        });
    }

    @Override
    public Resultado<DestaqueManual> fixarDestaque(long materiaId, Usuario por) {
        Optional<Materia> materia = materias.porId(materiaId).filter(Materia::publicada);
        if (materia.isEmpty()) {
            return Resultado.falha("Só uma matéria publicada pode ser fixada como principal da home.");
        }
        Instant agora = relogio.instant();
        DestaqueManual novo = new DestaqueManual(materiaId, agora, agora.plus(diasDoDestaque, ChronoUnit.DAYS), por.nome());
        destaque.definirDestaque(novo);
        eventos.publicar(new DestaqueFixado(agora, materiaId, por.nome()));
        return Resultado.sucesso(novo);
    }

    @Override
    public void voltarAoAutomatico(Usuario por) {
        destaque.limparDestaque();
        eventos.publicar(new DestaqueRemovido(relogio.instant(), "escolha de " + por.nome()));
    }

    @Override
    public Optional<Materia> buscar(long id) {
        return materias.porId(id);
    }

    @Override
    public List<Materia> listar(Optional<Editoria> editoria, Optional<StatusMateria> status) {
        return materias.stream()
                .filter(m -> editoria.isEmpty() || m.editoria() == editoria.get())
                .filter(m -> status.isEmpty() || m.status() == status.get())
                .sorted(Comparator.comparing(Materia::atualizadaEm).reversed())
                .toList();
    }
}
