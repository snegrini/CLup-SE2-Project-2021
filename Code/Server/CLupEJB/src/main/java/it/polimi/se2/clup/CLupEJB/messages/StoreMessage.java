package it.polimi.se2.clup.CLupEJB.messages;

import it.polimi.se2.clup.CLupEJB.entities.StoreEntity;
import it.polimi.se2.clup.CLupEJB.enums.MessageStatus;

public class StoreMessage extends Message {
    private final StoreEntity store;

    public StoreMessage(MessageStatus status, String message, StoreEntity store) {
        super(status, message);
        this.store = store;
    }

    public StoreEntity getStore() {
        return store;
    }
}
