package br.ucsal.casadigital.dominio;

/** Membro da equipe do podcast, com acesso ao painel (RN01). A senha nunca é guardada: só sal e hash. */
public final class Usuario extends EntidadeBase<Usuario> {

    private String nome;
    private String email;
    private String sal;
    private String hashSenha;
    private boolean administrador;

    private Usuario() {
    }

    public Usuario(String nome, String email, String sal, String hashSenha, boolean administrador) {
        this.nome = nome;
        this.email = email.toLowerCase();
        this.sal = sal;
        this.hashSenha = hashSenha;
        this.administrador = administrador;
    }

    public String nome() {
        return nome;
    }

    public String email() {
        return email;
    }

    public String sal() {
        return sal;
    }

    public String hashSenha() {
        return hashSenha;
    }

    public boolean administrador() {
        return administrador;
    }
}
