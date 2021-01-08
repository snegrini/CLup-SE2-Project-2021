package it.polimi.se2.clup.CLupEJB.messages;

import it.polimi.se2.clup.CLupEJB.entities.TicketEntity;
import it.polimi.se2.clup.CLupEJB.enums.MessageStatus;

import java.util.List;

public class TicketListMessage extends Message {
    private final List<TicketEntity> tickets;

    public TicketListMessage(MessageStatus status, String message, List<TicketEntity> tickets) {
        super(status, message);
        this.tickets = tickets;
    }

    public List<TicketEntity> getTickets() {
        return tickets;
    }
}