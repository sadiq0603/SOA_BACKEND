package com.bibliotech.rental.exception;

public class BookUnavailableException extends RuntimeException {
    public BookUnavailableException(String message) { super(message); }
}
