package it.polimi.se2.clup.CLupEJB.messages;

import it.polimi.se2.clup.CLupEJB.enums.MessageStatus;

public class Message {
    private final MessageStatus status;
    private final String message;

    public Message(MessageStatus status, String message) {
        this.status = status;
        this.message = message;
    }

    public MessageStatus getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }
}
