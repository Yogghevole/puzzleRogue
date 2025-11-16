package model.service;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class CharacterSelectionPersistenceTest {
    @Test
    void persistsAndReadsLastSelectedCharacter() {
        SessionService.setCurrentNick("persist_user");
        SessionService.setLastSelectedCharacter(null);
        assertNull(SessionService.getLastSelectedCharacter());
        SessionService.setLastSelectedCharacter("CRUSADER");
        assertEquals("CRUSADER", SessionService.getLastSelectedCharacter());
    }
}