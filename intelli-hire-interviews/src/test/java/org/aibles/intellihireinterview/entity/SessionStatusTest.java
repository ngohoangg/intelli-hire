package org.aibles.intellihireinterview.entity;

import org.aibles.intellihireinterview.entity.enums.SessionStatus;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class SessionStatusTest {
    @Test void createdIsNotTerminal() { assertFalse(SessionStatus.CREATED.isTerminal()); }
    @Test void activeIsNotTerminal()  { assertFalse(SessionStatus.ACTIVE.isTerminal()); }
    @Test void completedIsTerminal()  { assertTrue(SessionStatus.COMPLETED.isTerminal()); }
    @Test void expiredIsTerminal()    { assertTrue(SessionStatus.EXPIRED.isTerminal()); }
}
