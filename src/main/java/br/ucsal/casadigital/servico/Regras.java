package br.ucsal.casadigital.servico;

import br.ucsal.casadigital.dominio.Editoria;
import br.ucsal.casadigital.dominio.Subsecao;
import br.ucsal.casadigital.validacao.Regra;
import br.ucsal.casadigital.validacao.RegraComposta;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * Regras de negócio que olham o formulário inteiro. Cada uma é uma {@link Regra} pequena e testável
 * isoladamente (SRP); o conjunto é montado com {@link RegraComposta} (Composite).
 */
public final class Regras {

    private Regras() {
    }

    /** RN02, RN03, RN04: o que é exigido para publicar uma matéria, além das anotações do formulário. */
    public static Regra<MateriaForm> paraPublicarMateria() {
        return RegraComposta.de(
                (f, erros) -> {
                    if (f.listaDeReferencias().isEmpty()) {
                        erros.adicionar("referencias", "Inclua ao menos uma referência.");
                    }
                },
                (f, erros) -> {
                    if (f.editoria() != null && !f.editoria().isBlank() && Editoria.porSlug(f.editoria()).isEmpty()) {
                        erros.adicionar("editoria", "Escolha uma das seis editorias.");
                    }
                },
                (f, erros) -> {
                    if (!f.revisada()) {
                        erros.adicionar("revisada", "Marque que a matéria foi revisada e aprovada por um membro da equipe.");
                    }
                });
    }

    /** RN12, RN13, RN14: subseção válida, conferência com a fonte e datas de vigência coerentes. */
    public static Regra<ItemUcsalForm> paraCadastrarItemUcsal(Supplier<LocalDate> hoje) {
        return RegraComposta.de(
                (f, erros) -> {
                    if (f.subsecao() != null && !f.subsecao().isBlank() && Subsecao.porNome(f.subsecao()).isEmpty()) {
                        erros.adicionar("subsecao", "Escolha uma das quatro subseções.");
                    }
                },
                (f, erros) -> {
                    if (!f.conferido()) {
                        erros.adicionar("conferido", "Confirme que a informação foi conferida com a fonte antes de publicar.");
                    }
                },
                (f, erros) -> {
                    Optional<Subsecao> subsecao = Subsecao.porNome(f.subsecao());
                    if (subsecao.isEmpty() || !subsecao.get().temVigencia()) {
                        return;
                    }
                    Optional<LocalDate> inicio = data(f.inicio());
                    Optional<LocalDate> fim = data(f.fim());
                    if (inicio.isEmpty()) {
                        erros.adicionar("inicio", "Informe a data de início.");
                    }
                    if (fim.isEmpty()) {
                        erros.adicionar("fim", "Informe a data de fim.");
                    }
                    if (inicio.isPresent() && fim.isPresent()) {
                        if (fim.get().isBefore(inicio.get())) {
                            erros.adicionar("fim", "A data de fim não pode ser anterior à data de início.");
                        } else if (fim.get().isBefore(hoje.get())) {
                            erros.adicionar("fim", "A data de fim não pode ser anterior a hoje.");
                        }
                    }
                });
    }

    public static Optional<LocalDate> data(String texto) {
        if (texto == null || texto.isBlank()) {
            return Optional.empty();
        }
        try {
            return Optional.of(LocalDate.parse(texto.trim()));
        } catch (DateTimeParseException e) {
            return Optional.empty();
        }
    }
}
