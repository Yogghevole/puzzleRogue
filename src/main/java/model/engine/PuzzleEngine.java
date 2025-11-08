package model.engine;

import java.util.Optional;
import java.util.Set;

/**
 * Interfaccia che definisce il meccanismo di puzzle nel gioco.
 */
public interface PuzzleEngine {

    // --- Operazioni Base ---

    boolean insertValue(int row, int col, int value);

    boolean isCorrect(int row, int col, int value);

    void clearCell(int row, int col);

    boolean checkWin();
    
    // --- Operazioni Note ---

    void toggleNote(int row, int col, int candidate);

    void clearNotes(int row, int col);

    void removeCandidateFromPeers(int row, int col, int value);

    // --- Operazioni Oggetti/Buff ---
    
    Optional<int[]> revealHint();
    
    int getCellValue(int row, int col);
    
    Set<Integer> getCellNotes(int row, int col);
    
}