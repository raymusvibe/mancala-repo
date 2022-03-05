package com.bol.games.mancala.exception;

public class ValidationException extends Exception{
    private static final long serialVersionUID = 4L;
    public ValidationException(String message) {
        super(message);
    }
}
