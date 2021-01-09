package it.polimi.se2.clup.CLupEJB.exceptions;

public class BadOpeningHourException extends Exception {
    private static final long serialVersionUID = 1L;

    public BadOpeningHourException(String message) {
        super(message);
    }
}
