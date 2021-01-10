package it.polimi.se2.clup.CLupEJB.exceptions;

public class BadTicketException extends Exception {
    private static final long serialVersionUID = 1L;

    public BadTicketException(String message) {
        super(message);
    }
}