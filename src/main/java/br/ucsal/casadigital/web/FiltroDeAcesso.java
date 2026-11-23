package br.ucsal.casadigital.web;

/**
 * RN01 e CSRF. Descobre qual rota atende a requisição, lê (por reflexão) se ela tem {@link ExigeLogin} e:
 * sem login, manda para a tela de entrada; sem ser administrador, recusa; em POST do painel, exige o token CSRF
 * da sessão. As rotas públicas passam direto.
 */
public final class FiltroDeAcesso implements Filtro {

    private final Roteador roteador;

    public FiltroDeAcesso(Roteador roteador) {
        this.roteador = roteador;
    }

    @Override
    public Resposta aplicar(Requisicao requisicao, Cadeia proxima) {
        var rota = roteador.resolver(requisicao);
        if (rota.isEmpty() || !rota.get().exigeLogin()) {
            return proxima.seguir(requisicao);
        }
        if (requisicao.usuario().isEmpty()) {
            return Resposta.redirecionar("/painel/entrar");
        }
        if (rota.get().apenasAdministrador() && !requisicao.usuario().get().administrador()) {
            return Resposta.html(403, new VisoesPublicas.Erro(403, "Acesso restrito",
                    "Esta área é só para administradores da equipe.").renderizar());
        }
        if (requisicao.metodo().equals("POST")) {
            String esperado = requisicao.sessao().map(Sessao::csrf).orElse("");
            if (esperado.isEmpty() || !esperado.equals(requisicao.parametro("_csrf"))) {
                return Resposta.html(403, new VisoesPublicas.Erro(403, "Pedido recusado",
                        "A página ficou aberta por muito tempo ou veio de outro lugar. Volte ao painel e tente de novo.").renderizar());
            }
        }
        // o resolver() já guardou os parâmetros do caminho na requisição
        return proxima.seguir(requisicao);
    }
}
