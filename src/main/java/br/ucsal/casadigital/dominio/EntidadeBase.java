package br.ucsal.casadigital.dominio;

/**
 * Base de todas as entidades persistidas.
 *
 * <p>Padrões e conceitos usados aqui:
 * <ul>
 *   <li><b>Prototype</b>: {@link #copiar()} devolve uma cópia independente da entidade. Os repositórios
 *       guardam e entregam cópias, então ninguém altera o estado guardado sem chamar {@code salvar}
 *       (isso também torna o acesso concorrente seguro).</li>
 *   <li><b>Template Method</b>: {@link #copiarEstadoMutavel()} é o gancho que cada subclasse
 *       sobrescreve para copiar listas e outros objetos mutáveis.</li>
 *   <li><b>Generics (CRTP)</b>: {@code SELF} garante que {@code copiar()} devolva o tipo concreto,
 *       sem conversões (cast) em quem chama.</li>
 * </ul>
 */
public abstract class EntidadeBase<SELF extends EntidadeBase<SELF>> implements Cloneable {

    protected long id;

    public long id() {
        return id;
    }

    /** Chamado apenas pelo repositório, ao inserir a entidade pela primeira vez. */
    public void atribuirId(long novoId) {
        this.id = novoId;
    }

    @SuppressWarnings("unchecked")
    public final SELF copiar() {
        try {
            SELF copia = (SELF) super.clone();
            copia.copiarEstadoMutavel();
            return copia;
        } catch (CloneNotSupportedException e) {
            throw new IllegalStateException("A entidade deveria ser clonável", e);
        }
    }

    /** Gancho do Template Method: subclasses com coleções mutáveis devem copiá-las aqui. */
    protected void copiarEstadoMutavel() {
        // por padrão não há nada mutável para copiar
    }
}
