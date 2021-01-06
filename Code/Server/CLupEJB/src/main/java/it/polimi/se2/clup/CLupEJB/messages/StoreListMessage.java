package it.polimi.se2.clup.CLupEJB.messages;

import it.polimi.se2.clup.CLupEJB.entities.StoreEntity;
import it.polimi.se2.clup.CLupEJB.enums.MessageStatus;

import java.util.List;

public class StoreListMessage extends Message {
    private final List<StoreEntity> stores;

    public StoreListMessage(MessageStatus status, String message, List<StoreEntity> stores) {
        super(status, message);
        this.stores = stores;
    }
}
