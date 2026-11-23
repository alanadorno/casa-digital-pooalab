package br.ucsal.casadigital.servico;

import br.ucsal.casadigital.dominio.Editoria;
import br.ucsal.casadigital.dominio.Materia;
import br.ucsal.casadigital.infra.ArmazemDeDestaque;

import java.time.Clock;
import java.util.Optional;

/**
 * Caso de uso P4. Segue o GRASP <b>Controller</b> (coordena o caso de uso sem conter regra de tela) e
 * recebe todas as dependências pelo construtor (<b>Dependency Injection</b>).
 */
public final class ConsultaServico implements Consulta {

    private final CatalogoPublico catalogo;
    private final ArmazemDeDestaque destaque;
    private final Clock relogio;
    private final int porPagina;

    public ConsultaServico(CatalogoPublico catalogo, ArmazemDeDestaque destaque, Clock relogio, int porPagina) {
        this.catalogo = catalogo;
        this.destaque = destaque;
        this.relogio = relogio;
        this.porPagina = porPagina;
    }

    @Override
    public Home home() {
        PoliticaDestaque politica = FabricaDePoliticaDeDestaque.para(destaque.destaque(), relogio.instant());
        return politica.compor(catalogo.recentesPrimeiro(), relogio.instant());
    }

    @Override
    public Pagina<Materia> editoria(Editoria editoria, int pagina) {
        return Pagina.de(catalogo.daEditoria(editoria), pagina, porPagina);
    }

    @Override
    public Optional<Materia> materia(long id) {
        return catalogo.porId(id);
    }

    @Override
    public int totalDeMaterias() {
        return catalogo.recentesPrimeiro().size();
    }
}
