package br.ucsal.casadigital.servico;

import br.ucsal.casadigital.dominio.BarramentoEventos;
import br.ucsal.casadigital.dominio.Editoria;
import br.ucsal.casadigital.dominio.EventoDominio.MateriaAtualizada;
import br.ucsal.casadigital.dominio.EventoDominio.MateriaDespublicada;
import br.ucsal.casadigital.dominio.EventoDominio.MateriaPublicada;
import br.ucsal.casadigital.dominio.Materia;
import br.ucsal.casadigital.infra.Repositorio;

import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Visão de leitura das matérias publicadas, mantida pelo <b>Observer</b>: quando o evento
 * {@code MateriaPublicada} ou {@code MateriaDespublicada} chega, o catálogo se refaz. É a etapa
 * "atualizar a home e a listagem da editoria" do processo P3, que só acontece <i>depois</i> da gravação.
 *
 * <p>A lista é imutável e trocada de uma vez por um campo {@code volatile}: quem lê nunca vê uma lista
 * pela metade, mesmo com milhares de acessos simultâneos.
 */
public final class CatalogoPublico {

    private final Repositorio<Materia> materias;
    private volatile List<Materia> recentesPrimeiro = List.of();

    public CatalogoPublico(Repositorio<Materia> materias, BarramentoEventos eventos) {
        this.materias = materias;
        atualizar();
        eventos.inscrever(MateriaPublicada.class, e -> atualizar());
        eventos.inscrever(MateriaAtualizada.class, e -> atualizar());
        eventos.inscrever(MateriaDespublicada.class, e -> atualizar());
    }

    private synchronized void atualizar() {
        List<Materia> crescente = materias.stream()
                .filter(Materia::publicada)
                .sorted(Comparator.comparing(Materia::publicadaEm).thenComparingLong(Materia::id))
                .toList();
        // reversed(): método da SequencedCollection (Java 21); a mais recente fica em getFirst()
        this.recentesPrimeiro = List.copyOf(crescente.reversed());
    }

    /** RN11: da mais recente para a mais antiga. */
    public List<Materia> recentesPrimeiro() {
        return recentesPrimeiro;
    }

    public List<Materia> daEditoria(Editoria editoria) {
        return recentesPrimeiro.stream().filter(m -> m.editoria() == editoria).toList();
    }

    public Optional<Materia> porId(long id) {
        return recentesPrimeiro.stream().filter(m -> m.id() == id).findFirst();
    }

    /** Quantidade de matérias por editoria, agrupada com Streams e {@link EnumMap}. */
    public Map<Editoria, Long> contagemPorEditoria() {
        return recentesPrimeiro.stream().collect(
                Collectors.groupingBy(Materia::editoria, () -> new EnumMap<>(Editoria.class), Collectors.counting()));
    }
}
