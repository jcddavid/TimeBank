package com.progetto.timebank.exception;

/**
 * Eccezione lanciata quando un utente tenta di prenotare una lezione
 * ma non ha abbastanza ore nel saldo (minimo 1 richiesta).
 */
public class InsufficientBalanceException extends Exception {
    public InsufficientBalanceException(String message) {
        super(message);
    }
}