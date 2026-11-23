package br.ucsal.casadigital.infra;

import br.ucsal.casadigital.dominio.DestaqueManual;
import br.ucsal.casadigital.dominio.ItemUcsal;
import br.ucsal.casadigital.dominio.Materia;
import br.ucsal.casadigital.dominio.Sugestao;
import br.ucsal.casadigital.dominio.Usuario;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Conjunto dos repositórios + persistência em arquivo JSON ({@code data/banco.json}).
 *
 * <p>Toda mudança grava o arquivo inteiro de forma atômica (arquivo temporário + {@code ATOMIC_MOVE}),
 * então uma queda no meio da gravação não corrompe o banco. Com {@code arquivo == null} tudo fica só
 * em memória (usado nos testes).
 */
public final class Banco implements ArmazemDeDestaque {

    /** Retrato do banco gravado no arquivo. É lido e escrito por reflexão pela classe {@link Json}. */
    private static final class Instantaneo {
        private long ultimoId;
        private List<Materia> materias = new ArrayList<>();
        private List<Sugestao> sugestoes = new ArrayList<>();
        private List<ItemUcsal> itens = new ArrayList<>();
        private List<Usuario> usuarios = new ArrayList<>();
        private DestaqueManual destaque;

        private Instantaneo() {
        }
    }

    private final Path arquivo;
    private final AtomicLong ultimoId = new AtomicLong();
    private volatile DestaqueManual destaque;

    private final RepositorioEmMemoria<Materia> materias = new RepositorioEmMemoria<>(this::proximoId, this::persistir);
    private final RepositorioEmMemoria<Sugestao> sugestoes = new RepositorioEmMemoria<>(this::proximoId, this::persistir);
    private final RepositorioEmMemoria<ItemUcsal> itens = new RepositorioEmMemoria<>(this::proximoId, this::persistir);
    private final RepositorioEmMemoria<Usuario> usuarios = new RepositorioEmMemoria<>(this::proximoId, this::persistir);

    private Banco(Path arquivo) {
        this.arquivo = arquivo;
    }

    public static Banco emMemoria() {
        return new Banco(null);
    }

    /** Abre (ou cria) o banco no arquivo indicado. */
    public static Banco abrir(Path arquivo) {
        Banco banco = new Banco(arquivo);
        if (Files.isRegularFile(arquivo)) {
            banco.carregar();
        }
        return banco;
    }

    public RepositorioEmMemoria<Materia> materias() {
        return materias;
    }

    public RepositorioEmMemoria<Sugestao> sugestoes() {
        return sugestoes;
    }

    public RepositorioEmMemoria<ItemUcsal> itens() {
        return itens;
    }

    public RepositorioEmMemoria<Usuario> usuarios() {
        return usuarios;
    }

    @Override
    public Optional<DestaqueManual> destaque() {
        return Optional.ofNullable(destaque);
    }

    @Override
    public void definirDestaque(DestaqueManual novo) {
        this.destaque = novo;
        persistir();
    }

    @Override
    public void limparDestaque() {
        this.destaque = null;
        persistir();
    }

    public boolean vazio() {
        return usuarios.todos().isEmpty() && materias.todos().isEmpty() && itens.todos().isEmpty();
    }

    private long proximoId() {
        return ultimoId.incrementAndGet();
    }

    private synchronized void persistir() {
        if (arquivo == null) {
            return;
        }
        Instantaneo retrato = new Instantaneo();
        retrato.ultimoId = ultimoId.get();
        retrato.materias = materias.todos();
        retrato.sugestoes = sugestoes.todos();
        retrato.itens = itens.todos();
        retrato.usuarios = usuarios.todos();
        retrato.destaque = destaque;
        try {
            Files.createDirectories(arquivo.toAbsolutePath().getParent());
            Path temporario = arquivo.resolveSibling(arquivo.getFileName() + ".tmp");
            Files.writeString(temporario, Json.escrever(retrato), StandardCharsets.UTF_8);
            Files.move(temporario, arquivo, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (IOException e) {
            throw new UncheckedIOException("Não foi possível gravar " + arquivo, e);
        }
    }

    private void carregar() {
        try {
            Instantaneo retrato = Json.ler(Files.readString(arquivo, StandardCharsets.UTF_8), Instantaneo.class);
            ultimoId.set(retrato.ultimoId);
            materias.carregar(retrato.materias);
            sugestoes.carregar(retrato.sugestoes);
            itens.carregar(retrato.itens);
            usuarios.carregar(retrato.usuarios);
            destaque = retrato.destaque;
        } catch (IOException e) {
            throw new UncheckedIOException("Não foi possível ler " + arquivo, e);
        }
    }
}
