package com.stayfinder.exception;

/**
 * Aruncată când o resursă cerută nu există (ex. cazare cu id inexistent).
 * GlobalExceptionHandler o transformă automat în HTTP 404 cu JSON uniform,
 * astfel încât serviciile nu trebuie să știe nimic despre HTTP.
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
