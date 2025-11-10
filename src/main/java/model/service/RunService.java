package model.service;

import model.dao.RunDAO;
import model.domain.Run;
import model.domain.RunFrozenBuffs;
import model.domain.RunLevelState;
import model.domain.SudokuGrid;
import model.domain.User;
import model.engine.SudokuEngine;
import java.util.Collections;

/**
 * Gestisce la logica di business di una singola Run.
 */
public class RunService {

    private final RunDAO runDAO;
    private final GameDataService gameDataService;
    private final SudokuGenerator sudokuGenerator;
    private Run currentRun;
    private SudokuEngine currentEngine;
    private RunFrozenBuffs frozenBuffs; 

    public RunService(RunDAO runDAO, GameDataService gameDataService, SudokuGenerator sudokuGenerator) {
        this.runDAO = runDAO;
        this.gameDataService = gameDataService;
        this.sudokuGenerator = sudokuGenerator;
        this.frozenBuffs = new RunFrozenBuffs(Collections.emptyMap());
    }

    // --- 1. Avvio/Caricamento Run  ---

    public boolean startNewRun(User user, String characterId) {
        // TODO Tommy (freeze dei buff permanenti) 
        this.frozenBuffs = freezeBuffs(user); 
        
        int baseLives = gameDataService.getCharacterBaseLives(characterId);
        int extraLives = (int) getBuffValue("EXTRA_LIVES", 0);
        
        this.currentRun = new Run(user.getNick(), baseLives + extraLives, characterId);
        
        return startLevel(1);
    }

    public boolean startLevel(int levelNumber) {
        if (currentRun == null) return false;
        
        SudokuGrid newPuzzle = sudokuGenerator.generateNewPuzzle(levelNumber, frozenBuffs);
        
        boolean hasProtection = getBuffLevel("FIRST_ERROR_PROTECT") > 0;
        
        RunLevelState newLevelState = new RunLevelState(
            levelNumber, 
            "DEFAULT_ENEMY",
            newPuzzle, 
            hasProtection
        );
        currentRun.setCurrentLevelState(newLevelState);
        
        currentEngine = new SudokuEngine(newPuzzle);
        
        // TODO Tommy (salvataggio run) 
        
        return true;
    }

    // --- Logica di interazione ---

    public boolean handleUserMove(int row, int col, int value) {
        if (currentEngine == null) return false;

        boolean isCorrect = currentEngine.insertValue(row, col, value);

        if (!isCorrect) {
            handleError();
        } 
        
        if (isCorrect && currentEngine.checkWin()) {
             endLevel(true);
        }
        
        // TODO Tommy (salvataggio stato attuale) 
        return isCorrect;
    }

    private void handleError() {
        RunLevelState state = currentRun.getCurrentLevelState();
        currentRun.incrementTotalErrors();
        
        if (getBuffLevel("FIRST_ERROR_PROTECT") > 0 && !state.isProtectionUsed()) {
            state.setProtectionUsed(true);
            System.out.println("Protezione Primo Errore attivata! Vita salvata.");
            return;
        }

        currentRun.loseLife();
        
        if (currentRun.getLivesRemaining() <= 0) {
            endRun(false);
        }
        
        state.incrementErrorsInLevel(); 
    }

    // --- Uso Oggetti  ---

    public boolean useItem(String itemId) {
        if (!removeItemFromInventory(itemId)) {
            return false;
        }

        switch (itemId) {
            case "HINT_ITEM":
                return currentEngine.revealHint().isPresent();

            case "MISSING_HEART_ITEM":
                currentRun.addLife();
                return true;

            case "SACRIFICE_ITEM":
                currentRun.loseLife();
                System.out.println("Sacrificio effettuato. (Logica Hint temporanea da implementare)");
                return currentRun.getLivesRemaining() > 0;
                
            case "SCORE_ITEM":
                System.out.println("Punti aggiunti! (Logica da implementare nel Servizio Punteggio)");
                return true;

            default:
                addItemToInventory(itemId, 1);
                return false;
        }
        // TODO Tommy (salvataggio stato attuale)
    }
    
    // --- 4. Fine Livello/Run ---

    public void endLevel(boolean win) {
        if (win) {
            int nextLevel = currentRun.getCurrentLevelState().getCurrentLevel() + 1;
            // TODO Tommy (calcolo punteggio)
            System.out.println("Livello " + (nextLevel - 1) + " Completato!");
        } else {
             endRun(false);
        }
    }

    public void endRun(boolean win) {
        // TODO Tommy
        currentRun = null;
        currentEngine = null;
        System.out.println("Run Terminata. Vittoria: " + win);
    }
    
    // TODO Tommy
    private RunFrozenBuffs freezeBuffs(User user) {
        return new RunFrozenBuffs(user.getPermanentBuffLevels());
    }
    
    private int getBuffLevel(String buffId) {
        return frozenBuffs.getBuffLevel(buffId);
    }

    private double getBuffValue(String buffId, double defaultValue) {
        int level = getBuffLevel(buffId);
        if (level > 0) {
            return gameDataService.getBuffLevelData(buffId, level).getOrDefault("value", defaultValue).doubleValue();
        }
        return defaultValue;
    }
    
    private boolean removeItemFromInventory(String itemId) { return true; }
    private void addItemToInventory(String itemId, int quantity) { }

    
    public Run getCurrentRun() {
        return currentRun;
    }

    public SudokuEngine getCurrentEngine() {
        return currentEngine;
    }
}