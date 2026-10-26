package com.stayfinder.exception;

/**
 * Aruncată când o cerere conține date conflictuale sau invalide din punct de vedere al logicii de business
 * (de exemplu înregistrare cu un email deja existent).
 */
public class BadRequestException extends RuntimeException {

    public BadRequestException(String message) {
        super(message);
    }
}
