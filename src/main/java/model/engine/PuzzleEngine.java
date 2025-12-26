package model.engine;

import java.util.Optional;
import java.util.Set;

/**
 * Interface defining the core interactions for a puzzle game engine.
 */
public interface PuzzleEngine {

    boolean insertValue(int row, int col, int value);

    boolean isCorrect(int row, int col, int value);

    void clearCell(int row, int col);

    boolean checkWin();

    void toggleNote(int row, int col, int candidate);

    void clearNotes(int row, int col);

    void removeCandidateFromPeers(int row, int col, int value);

    Optional<int[]> revealHint();
    
    int getCellValue(int row, int col);
    
    Set<Integer> getCellNotes(int row, int col);
    
}
