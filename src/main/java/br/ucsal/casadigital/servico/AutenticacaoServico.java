package br.ucsal.casadigital.servico;

import br.ucsal.casadigital.dominio.BarramentoEventos;
import br.ucsal.casadigital.dominio.EventoDominio.AcessoAoPainel;
import br.ucsal.casadigital.dominio.Usuario;
import br.ucsal.casadigital.infra.Repositorio;
import br.ucsal.casadigital.infra.Senhas;
import br.ucsal.casadigital.validacao.Erros;
import br.ucsal.casadigital.validacao.Validador;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

/** Caso de uso P2. A mensagem de erro é a mesma para e-mail inexistente e senha errada, para não revelar quem é da equipe. */
public final class AutenticacaoServico implements Autenticacao {

    private static final String SAL_FALSO = Senhas.novoSal();

    private final Repositorio<Usuario> usuarios;
    private final BarramentoEventos eventos;
    private final Clock relogio;
    private final Validador validador;
    private final LimitadorDeTentativas limitador = new LimitadorDeTentativas(5, Duration.ofMinutes(1));

    public AutenticacaoServico(Repositorio<Usuario> usuarios, BarramentoEventos eventos, Clock relogio, Validador validador) {
        this.usuarios = usuarios;
        this.eventos = eventos;
        this.relogio = relogio;
        this.validador = validador;
    }

    @Override
    public Resultado<Usuario> entrar(LoginForm form, String origem) {
        Erros erros = validador.validar(form, false);
        if (!erros.vazio()) {
            return Resultado.invalido(erros);
        }
        Instant agora = relogio.instant();
        String email = form.email().trim().toLowerCase();
        String chave = origem + "|" + email;
        if (limitador.bloqueado(chave, agora)) {
            return Resultado.falha("Muitas tentativas. Aguarde um minuto e tente de novo.");
        }
        Optional<Usuario> usuario = usuarios.stream().filter(u -> u.email().equals(email)).findFirst();
        // O hash é calculado mesmo quando o usuário não existe, para o tempo de resposta não denunciar o e-mail.
        boolean confere = usuario
                .map(u -> Senhas.confere(form.senha(), u.sal(), u.hashSenha()))
                .orElseGet(() -> {
                    Senhas.hash(form.senha(), SAL_FALSO);
                    return false;
                });
        if (!confere) {
            limitador.registrarFalha(chave, agora);
            eventos.publicar(new AcessoAoPainel(agora, email, false));
            return Resultado.falha("E-mail ou senha incorretos.");
        }
        limitador.limpar(chave);
        eventos.publicar(new AcessoAoPainel(agora, email, true));
        return Resultado.sucesso(usuario.orElseThrow());
    }

    @Override
    public Optional<Usuario> porId(long id) {
        return usuarios.porId(id);
    }
}
