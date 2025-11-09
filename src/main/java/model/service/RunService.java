package model.service;

import model.dao.RunDAO;
import model.domain.Run;
import model.domain.RunFrozenBuffs;
import model.domain.RunLevelState;
import model.domain.SudokuGrid;
import model.domain.User;
import model.engine.SudokuEngine;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

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

        // Save run after starting new level
        saveCurrentRun();
        
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

        // Save run after each move
        saveCurrentRun();

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

        boolean success = false;

        switch (itemId) {
            case "HINT_ITEM":
                success = currentEngine.revealHint().isPresent();
                break;

            case "LIFE_BOOST_ITEM":
                currentRun.addLife();
                success = true;
                break;

            case "SACRIFICE_ITEM":
                if (currentRun.getLivesRemaining() > 1) {
                    currentRun.loseLife();
                    success = currentEngine.revealHint().isPresent() && 
                             currentEngine.revealHint().isPresent(); // Two hints
                }
                break;
                    
            case "SCORE_ITEM":
                int currentLevel = currentRun.getCurrentLevelState().getCurrentLevel();
                currentRun.addToScore(currentLevel * 10);
                success = true;
                break;
        }

        if (!success) {
            // Restore item if action failed
            addItemToInventory(itemId, 1);
        }

        // Save run after using item
        saveCurrentRun();
        return success;
    }
    
    // --- 4. Fine Livello/Run ---

    public void endLevel(boolean win) {
        if (win) {
            RunLevelState currentState = currentRun.getCurrentLevelState();
            int currentLevel = currentState.getCurrentLevel();
            
            // Calculate level score
            int levelScore = currentLevel * 10;
            
            // Add zero error bonus if applicable
            if (currentState.getErrorsInLevel() == 0) {
                levelScore += 30;
            }
            
            currentRun.addToScore(levelScore);
            
            // Start next level
            int nextLevel = currentLevel + 1;
            if (nextLevel <= 10) {
                startLevel(nextLevel);
            } else {
                endRun(true);
            }
            
            System.out.println("Livello " + currentLevel + " Completato! Punteggio: +" + levelScore);
        } else {
            endRun(false);
        }
    }

    public void endRun(boolean win) {
        if (currentRun != null) {
            // Calculate final bonuses
            int finalScore = currentRun.getScore();
            
            // Add inventory item bonus
            // TODO: Add method to get inventory items count
            int remainingItems = currentRun.getInventoryItemCount();
            finalScore += remainingItems * 20;
            
            // Add total errors bonus
            finalScore += currentRun.getTotalErrors() * 5;
            
            // Add victory bonus
            if (win) {
                finalScore += 200;
            }
            
            // Apply point bonus multiplier if active
            int pointBonusLevel = getBuffLevel("POINT_BONUS");
            if (pointBonusLevel > 0) {
                double multiplier = getBuffValue("POINT_BONUS", 1.0);
                finalScore = (int)(finalScore * multiplier);
            } else {
                // Add no-buff bonus if no permanent buffs were active
                if (frozenBuffs.isEmpty()) {
                    finalScore += 50;
                }
            }
            
            currentRun.setFinalScore(finalScore);
            saveCurrentRun();
            
            System.out.println("Run Terminata. Vittoria: " + win + " Punteggio Finale: " + finalScore);
        }
        
        currentRun = null;
        currentEngine = null;
    }
    
    // TODO Tommy
    private RunFrozenBuffs freezeBuffs(User user) {
        if (user == null) {
            return new RunFrozenBuffs(Collections.emptyMap());
        }
        
        // Validate buff levels against max values from game data
        Map<String, Integer> validatedBuffs = user.getPermanentBuffLevels().entrySet().stream()
            .filter(entry -> {
                int maxLevel = gameDataService.getMaxBuffLevel(entry.getKey());
                return entry.getValue() > 0 && entry.getValue() <= maxLevel;
            })
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                Map.Entry::getValue
            ));
        
        return new RunFrozenBuffs(validatedBuffs);
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
    
    private boolean removeItemFromInventory(String itemId) {
        return currentRun != null && currentRun.removeItemFromInventory(itemId);
    }

    private void addItemToInventory(String itemId, int quantity) {
        if (currentRun != null) {
            currentRun.addItemToInventory(itemId, quantity);
        }
    }

    private void saveCurrentRun() {
        if (currentRun != null) {
            runDAO.save(currentRun);
        }
    }
    
    public Run getCurrentRun() {
        return currentRun;
    }

    public SudokuEngine getCurrentEngine() {
        return currentEngine;
    }
}