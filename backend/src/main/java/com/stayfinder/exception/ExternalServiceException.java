package com.stayfinder.exception;

/**
 * Excepție aruncată când un apel către un API extern (Open-Meteo, Frankfurter etc.)
 * eșuează din cauza unui timeout, eroare 5xx sau răspuns neașteptat.
 */
public class ExternalServiceException extends RuntimeException {

    public ExternalServiceException(String message) {
        super(message);
    }

    public ExternalServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
