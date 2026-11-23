package br.ucsal.casadigital.infra;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

/** Proteção de senhas (RN01): PBKDF2 com sal aleatório. A senha original nunca é guardada. */
public final class Senhas {

    private static final int ITERACOES = 120_000;
    private static final SecureRandom ALEATORIO = new SecureRandom();

    private Senhas() {
    }

    public static String novoSal() {
        byte[] sal = new byte[16];
        ALEATORIO.nextBytes(sal);
        return Base64.getEncoder().encodeToString(sal);
    }

    public static String hash(String senha, String sal) {
        try {
            PBEKeySpec spec = new PBEKeySpec(senha.toCharArray(), Base64.getDecoder().decode(sal), ITERACOES, 256);
            byte[] derivada = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).getEncoded();
            return Base64.getEncoder().encodeToString(derivada);
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("PBKDF2 indisponível neste JDK", e);
        }
    }

    /** Comparação em tempo constante, para não revelar quantos caracteres coincidiram. */
    public static boolean confere(String senha, String sal, String hashEsperado) {
        byte[] calculado = hash(senha, sal).getBytes(java.nio.charset.StandardCharsets.UTF_8);
        byte[] esperado = hashEsperado.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        return MessageDigest.isEqual(calculado, esperado);
    }

    /** Token aleatório e imprevisível para sessão e CSRF. */
    public static String token() {
        byte[] bytes = new byte[24];
        ALEATORIO.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
