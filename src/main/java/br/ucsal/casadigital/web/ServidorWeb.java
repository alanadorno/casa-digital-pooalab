package br.ucsal.casadigital.web;

import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Servidor HTTP do JDK ({@code com.sun.net.httpserver}) usando <b>Virtual Threads</b>: cada requisição roda em
 * uma thread virtual, barata o bastante para existir uma por pedido sem formar uma fila de espera, mesmo quando
 * muitos visitantes acessam ao mesmo tempo ou algum pedido fica esperando o serviço de e-mail.
 */
public final class ServidorWeb implements AutoCloseable {

    private static final Logger LOG = Logger.getLogger(ServidorWeb.class.getName());

    private final HttpServer servidor;
    private final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();

    public ServidorWeb(String host, int porta, FrontController frente) throws IOException {
        this.servidor = HttpServer.create(new InetSocketAddress(host, porta), 0);
        this.servidor.setExecutor(executor);
        this.servidor.createContext("/", troca -> {
            try {
                Requisicao requisicao = Requisicao.de(troca);
                frente.tratar(requisicao).enviar(troca, troca.getRequestMethod().equals("HEAD"));
            } catch (IOException | RuntimeException e) {
                LOG.log(Level.WARNING, "Falha ao responder", e);
                try {
                    troca.sendResponseHeaders(400, -1);
                } catch (IOException ignorada) {
                    // a conexão já caiu
                }
                troca.close();
            }
        });
    }

    public void iniciar() {
        servidor.start();
    }

    /** Porta realmente usada (útil quando se pede a porta 0 nos testes). */
    public int porta() {
        return servidor.getAddress().getPort();
    }

    @Override
    public void close() {
        servidor.stop(0);
        executor.close();
    }
}
