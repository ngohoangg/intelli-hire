package org.aibles.intellihireinterview.entity.enums;

public enum SessionStatus {
    CREATED, ACTIVE, COMPLETED, EXPIRED;

    public boolean isTerminal() {
        return this == COMPLETED || this == EXPIRED;
    }
}
