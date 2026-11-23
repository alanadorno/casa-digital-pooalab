package br.ucsal.casadigital.dominio;

/**
 * RN10: toda sugestão tem um de três estados, que só avançam nesta ordem: nova, avaliada, respondida.
 *
 * <p>Padrão <b>State</b> implementado com enum: cada constante sobrescreve só as transições que
 * permite. Qualquer outra transição cai no comportamento padrão e lança
 * {@link TransicaoInvalidaException}, então nenhum estado é pulado nem desfeito.
 *
 * <p>Obs.: o "e-mail pendente" da RN09 <i>não</i> é um estado; é uma marca ({@code emailPendente}) na
 * {@link Sugestao}, que pode acompanhar qualquer um dos três.
 */
public enum EstadoSugestao {

    NOVA("nova") {
        @Override
        public EstadoSugestao aoAvaliar() {
            return AVALIADA;
        }
    },
    AVALIADA("avaliada") {
        @Override
        public EstadoSugestao aoResponder() {
            return RESPONDIDA;
        }
    },
    RESPONDIDA("respondida");

    private final String rotulo;

    EstadoSugestao(String rotulo) {
        this.rotulo = rotulo;
    }

    public String rotulo() {
        return rotulo;
    }

    public EstadoSugestao aoAvaliar() {
        throw new TransicaoInvalidaException("Uma sugestão %s não pode ser avaliada.".formatted(rotulo));
    }

    public EstadoSugestao aoResponder() {
        throw new TransicaoInvalidaException("Uma sugestão %s não pode ser respondida agora.".formatted(rotulo));
    }

    public boolean podeAvaliar() {
        return this == NOVA;
    }

    public boolean podeResponder() {
        return this == AVALIADA;
    }
}
