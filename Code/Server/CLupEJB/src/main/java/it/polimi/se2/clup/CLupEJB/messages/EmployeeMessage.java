package it.polimi.se2.clup.CLupEJB.messages;

import it.polimi.se2.clup.CLupEJB.entities.TicketEntity;
import it.polimi.se2.clup.CLupEJB.enums.MessageStatus;

import java.util.List;

public class EmployeeMessage extends Message {

    private final List<TicketEntity> tickets;
    private final int customersInside;
    private final int customersQueue;

    public EmployeeMessage(MessageStatus status, String message, List<TicketEntity> tickets, int customersInside, int customersQueue) {
        super(status, message);
        this.tickets = tickets;
        this.customersInside = customersInside;
        this.customersQueue = customersQueue;
    }

    public List<TicketEntity> getTickets() {
        return tickets;
    }

    public int getCustomersInside() {
        return customersInside;
    }

    public int getCustomersQueue() {
        return customersQueue;
    }
}
