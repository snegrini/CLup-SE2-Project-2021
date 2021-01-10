package it.polimi.se2.clup.CLupEJB.messages;

import it.polimi.se2.clup.CLupEJB.entities.TicketEntity;
import it.polimi.se2.clup.CLupEJB.enums.MessageStatus;

public class TicketMessage extends Message {
    private final TicketEntity ticket;

    public TicketMessage(MessageStatus status, String message, TicketEntity ticket) {
        super(status, message);
        this.ticket = ticket;
    }

    public TicketEntity getTicket() {
        return ticket;
    }
}
