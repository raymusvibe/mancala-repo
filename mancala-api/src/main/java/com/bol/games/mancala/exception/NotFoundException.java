package com.bol.games.mancala.exception;

public class NotFoundException extends Exception {
    private static final long serialVersionUID = 4L;
    public NotFoundException(String message) {
        super(message);
    }
}
