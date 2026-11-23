package br.ucsal.casadigital.servico;

import br.ucsal.casadigital.dominio.DestaqueManual;
import br.ucsal.casadigital.dominio.Editoria;
import br.ucsal.casadigital.dominio.Materia;
import br.ucsal.casadigital.dominio.StatusMateria;
import br.ucsal.casadigital.dominio.Usuario;

import java.util.List;
import java.util.Optional;

/** Processos P1 e P3: a equipe escreve, revisa e publica matérias, e controla o destaque da home. */
public interface Publicacao {

    Resultado<Materia> salvarRascunho(MateriaForm form, Usuario autor);

    Resultado<Materia> enviarParaRevisao(MateriaForm form, Usuario autor);

    /** P3: valida, grava e só então publica, atualiza a home e confirma (RN02, RN04). */
    Resultado<Materia> publicar(MateriaForm form, Usuario revisor);

    Resultado<Materia> despublicar(long id, Usuario por);

    /** RN05: fixa a matéria como principal da home por 7 dias. */
    Resultado<DestaqueManual> fixarDestaque(long materiaId, Usuario por);

    /** RN05: "Voltar ao automático". */
    void voltarAoAutomatico(Usuario por);

    Optional<Materia> buscar(long id);

    List<Materia> listar(Optional<Editoria> editoria, Optional<StatusMateria> status);
}
