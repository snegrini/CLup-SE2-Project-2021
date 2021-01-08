package it.polimi.se2.clup.CLupEJB.messages;

import it.polimi.se2.clup.CLupEJB.enums.MessageStatus;

public class TokenMessage extends Message {
    private final String token;

    public TokenMessage(MessageStatus status, String message, String token) {
        super(status, message);
        this.token = token;
    }

    public String getToken() {
        return token;
    }
}
