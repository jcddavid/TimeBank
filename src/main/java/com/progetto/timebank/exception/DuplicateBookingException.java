package com.progetto.timebank.exception;

/**
 * Eccezione lanciata quando un utente tenta di prenotare lo stesso tutor
 * per la stessa materia mentre c'è già una richiesta "PENDING".
 */
public class DuplicateBookingException extends Exception {
    public DuplicateBookingException(String message) {
        super(message);
    }
}