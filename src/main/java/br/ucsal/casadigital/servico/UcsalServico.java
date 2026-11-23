package br.ucsal.casadigital.servico;

import br.ucsal.casadigital.dominio.BarramentoEventos;
import br.ucsal.casadigital.dominio.EventoDominio.ItemUcsalCadastrado;
import br.ucsal.casadigital.dominio.EventoDominio.ItemUcsalEncerrado;
import br.ucsal.casadigital.dominio.ItemUcsal;
import br.ucsal.casadigital.dominio.SituacaoItem;
import br.ucsal.casadigital.dominio.Subsecao;
import br.ucsal.casadigital.dominio.Usuario;
import br.ucsal.casadigital.infra.Repositorio;
import br.ucsal.casadigital.infra.RepositorioIndisponivelException;
import br.ucsal.casadigital.validacao.Erros;
import br.ucsal.casadigital.validacao.Regra;
import br.ucsal.casadigital.validacao.Validador;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/** Caso de uso P6. O relógio é injetado ({@link Clock}), então os testes controlam "hoje" sem esperar dias. */
public final class UcsalServico implements Ucsal {

    private final Repositorio<ItemUcsal> itens;
    private final BarramentoEventos eventos;
    private final Clock relogio;
    private final Validador validador;
    private final Regra<ItemUcsalForm> regras;

    public UcsalServico(Repositorio<ItemUcsal> itens, BarramentoEventos eventos, Clock relogio, Validador validador) {
        this.itens = itens;
        this.eventos = eventos;
        this.relogio = relogio;
        this.validador = validador;
        this.regras = Regras.paraCadastrarItemUcsal(this::hoje);
    }

    private LocalDate hoje() {
        return LocalDate.now(relogio);
    }

    @Override
    public Resultado<ItemUcsal> cadastrar(ItemUcsalForm form, Usuario conferente) {
        Erros erros = validador.validar(form, false);
        regras.verificar(form, erros);
        if (!erros.vazio()) {
            return Resultado.invalido(erros);
        }
        Instant agora = relogio.instant();
        Subsecao subsecao = Subsecao.porNome(form.subsecao()).orElseThrow();
        ItemUcsal item = new ItemUcsal(subsecao, form.titulo().trim(), form.descricao().trim(), form.fonte().trim(),
                Regras.data(form.inicio()).orElse(null), Regras.data(form.fim()).orElse(null),
                conferente.nome(), agora, false);
        try {
            ItemUcsal gravado = itens.salvar(item);
            eventos.publicar(new ItemUcsalCadastrado(agora, gravado.id(), gravado.titulo(), conferente.nome()));
            return Resultado.sucesso(gravado);
        } catch (RepositorioIndisponivelException e) {
            return Resultado.falha("Não foi possível gravar o item agora. Tente de novo.");
        }
    }

    @Override
    public Map<Subsecao, List<ItemUcsal>> visiveisPorSubsecao() {
        LocalDate hoje = hoje();
        Map<Subsecao, List<ItemUcsal>> agrupado = itens.stream()
                .filter(i -> i.visivelNaAreaPublica(hoje))
                .sorted(Comparator.comparing(ItemUcsal::conferidoEm).reversed())
                .collect(Collectors.groupingBy(ItemUcsal::subsecao, () -> new EnumMap<>(Subsecao.class), Collectors.toList()));
        for (Subsecao s : Subsecao.values()) {
            agrupado.putIfAbsent(s, List.of());
        }
        return agrupado;
    }

    @Override
    public List<ItemUcsal> todos() {
        return itens.stream().sorted(Comparator.comparing(ItemUcsal::conferidoEm).reversed()).toList();
    }

    @Override
    public int encerrarVencidos() {
        LocalDate hoje = hoje();
        List<ItemUcsal> vencidos = itens.stream()
                .filter(i -> i.situacao(hoje) == SituacaoItem.ENCERRADO && !i.encerrado())
                .toList();
        for (ItemUcsal item : vencidos) {
            item.marcarEncerrado();
            itens.salvar(item);
            eventos.publicar(new ItemUcsalEncerrado(relogio.instant(), item.id(), item.titulo()));
        }
        return vencidos.size();
    }
}
