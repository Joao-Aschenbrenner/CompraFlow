package br.com.jaaschenbrenner.compraflow.exception;

public class IntegracaoExternaException extends RuntimeException {
    public IntegracaoExternaException(String message) {
        super(message);
    }

    public IntegracaoExternaException(String message, Throwable cause) {
        super(message, cause);
    }
}
