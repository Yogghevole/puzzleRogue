package model.service;

import model.dao.RunDAO;
import model.db.DatabaseManager;
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
 * Gestisce la logica di business di ogni singola Run.
 */
public class RunService {

    private final RunDAO runDAO;
    private final GameDataService gameDataService;
    private final SudokuGenerator sudokuGenerator;
    private final DatabaseManager dbManager;
    private Run currentRun;
    private SudokuEngine currentEngine;
    private RunFrozenBuffs frozenBuffs; 
    private int levelsCompletedCount = 0;
    private int zeroErrorLevelCount = 0;
    private int scoreItemPointsAccrued = 0;
    private PointService.ScoreBreakdown lastRunBreakdown;

    public RunService(RunDAO runDAO, GameDataService gameDataService, SudokuGenerator sudokuGenerator) {
        this.runDAO = runDAO;
        this.gameDataService = gameDataService;
        this.sudokuGenerator = sudokuGenerator;
        this.dbManager = DatabaseManager.getInstance();
        this.frozenBuffs = new RunFrozenBuffs(Collections.emptyMap());
    }
    
    public RunService() {
        this.dbManager = DatabaseManager.getInstance();
        this.runDAO = new RunDAO(dbManager);
        this.gameDataService = new GameDataService();
        this.sudokuGenerator = new SudokuGenerator(gameDataService);
        this.frozenBuffs = new RunFrozenBuffs(Collections.emptyMap());
    }

    public boolean startNewRun(User user, String characterId) {
        this.frozenBuffs = freezeBuffs(user); 
        
        int baseLives = gameDataService.getCharacterBaseLives(characterId);
        int extraLives = (int) getBuffValue("EXTRA_LIVES", 0);
        
        this.currentRun = new Run(user.getNick(), baseLives + extraLives, characterId);
        this.levelsCompletedCount = 0;
        this.zeroErrorLevelCount = 0;
        this.scoreItemPointsAccrued = 0;
        this.lastRunBreakdown = null;
        
        return startLevel(1);
    }

    public boolean startNewRunWithCharacter(String characterId) {
        User currentUser = getCurrentUser();
        if (currentUser == null || characterId == null || characterId.isEmpty()) return false;
        return startNewRun(currentUser, characterId);
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

        saveCurrentRun();
        
        return true;
    }

    public boolean handleUserMove(int row, int col, int value) {
        if (currentEngine == null) return false;

        boolean isCorrect = currentEngine.insertValue(row, col, value);

        if (!isCorrect) {
            handleError();
        } 
        
        if (isCorrect && currentEngine.checkWin()) {
             endLevel(true);
        }

        saveCurrentRun();

        return isCorrect;
    }

    private void handleError() {
        RunLevelState state = currentRun.getCurrentLevelState();
        currentRun.incrementTotalErrors();
        
        if (getBuffLevel("FIRST_ERROR_PROTECT") > 0 && !state.isProtectionUsed()) {
            state.setProtectionUsed(true);
            return;
        }

        currentRun.loseLife();
        
        if (currentRun.getLivesRemaining() <= 0) {
            endRun(false);
        }
        
        state.incrementErrorsInLevel(); 
    }

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
                             currentEngine.revealHint().isPresent();
                }
                break;
                    
            case "SCORE_ITEM":
                int currentLevel = currentRun.getCurrentLevelState().getCurrentLevel();
                int pts = currentLevel * 10;
                currentRun.addToScore(pts);
                scoreItemPointsAccrued += pts;
                success = true;
                break;
        }

        if (!success) {
            addItemToInventory(itemId, 1);
        }

        saveCurrentRun();
        return success;
    }

    public void registerErrorEvent(String reason) {
        if (currentRun == null) return;
        RunLevelState state = currentRun.getCurrentLevelState();
        if (state == null) return;
        currentRun.incrementTotalErrors();
        state.incrementErrorsInLevel();
        saveCurrentRun();
    }

    public boolean registerItemUse(String itemId) {
        if (currentRun == null) {
            return false;
        }
        boolean ok = removeItemFromInventory(itemId);
        if (!ok) {
            return false;
        }

        switch (itemId) {
            case "HINT_ITEM":
                break;
            case "LIFE_BOOST_ITEM":
                currentRun.addLife();
                break;
            case "SACRIFICE_ITEM":
                break;
            case "SCORE_ITEM": {
                int lvl = currentRun.getCurrentLevelState() != null ? currentRun.getCurrentLevelState().getCurrentLevel() : 1;
                int pts = lvl * 10;
                currentRun.addToScore(pts);
                scoreItemPointsAccrued += pts;
                break;
            }
            default:
                break;
        }
        saveCurrentRun();
        return true;
    }
    
    public void endLevel(boolean win) {
        if (win) {
            RunLevelState currentState = currentRun.getCurrentLevelState();
            int currentLevel = currentState.getCurrentLevel();
            
            int levelScore = currentLevel * 10;
            
            if (currentState.getErrorsInLevel() == 0) {
                levelScore += 30;
                zeroErrorLevelCount++;
            }
            
            currentRun.addToScore(levelScore);
            levelsCompletedCount++;

            int nextLevel = currentLevel + 1;
            if (nextLevel <= 10) {
                startLevel(nextLevel);
            } else {
                endRun(true);
            }
            
        } else {
            endRun(false);
        }
    }

    public void endLevelWithRemainingOverride(boolean win, int remainingOverride) {
        if (!win) {
            endRunWithRemainingItems(false, Math.max(0, remainingOverride));
            return;
        }

        if (currentRun == null || currentRun.getCurrentLevelState() == null) {
            endRunWithRemainingItems(true, Math.max(0, remainingOverride));
            return;
        }

        RunLevelState currentState = currentRun.getCurrentLevelState();
        int currentLevel = currentState.getCurrentLevel();

        int levelScore = currentLevel * 10;
        if (currentState.getErrorsInLevel() == 0) {
            levelScore += 30;
            zeroErrorLevelCount++;
        }

        currentRun.addToScore(levelScore);
        levelsCompletedCount++;

        int nextLevel = currentLevel + 1;
        if (nextLevel <= 10) {
            startLevel(nextLevel);
        } else {
            endRunWithRemainingItems(true, Math.max(0, remainingOverride));
        }
    }

    public void endRun(boolean win) {
        if (currentRun != null) {
            int baseAccumulated = currentRun.getScore();
            int remainingItems = 0;
            int totalErrors = currentRun.getTotalErrors();
            int pointBonusLevel = getBuffLevel("POINT_BONUS");
            double pointBonusMultiplier = pointBonusLevel > 0 ? getBuffValue("POINT_BONUS", 1.0) : 1.0;
            boolean noBuffsHardMode = pointBonusLevel <= 0 && frozenBuffs.isEmpty();

            PointService pointService = new PointService();
            PointService.ScoreBreakdown bd = pointService.buildBreakdown(
                    levelsCompletedCount,
                    zeroErrorLevelCount,
                    totalErrors,
                    remainingItems,
                    win,
                    scoreItemPointsAccrued,
                    pointBonusLevel,
                    pointBonusMultiplier,
                    noBuffsHardMode
            );

            currentRun.setFinalScore(bd.getTotal());
            lastRunBreakdown = bd;
            saveCurrentRun();
        }
        
        currentRun = null;
        currentEngine = null;
    }

    public void endRunWithRemainingItems(boolean win, int remainingItemsOverride) {
        if (currentRun != null) {
            int baseAccumulated = currentRun.getScore();
            int remainingItems = Math.max(0, remainingItemsOverride);
            int totalErrors = currentRun.getTotalErrors();
            int pointBonusLevel = getBuffLevel("POINT_BONUS");
            double pointBonusMultiplier = pointBonusLevel > 0 ? getBuffValue("POINT_BONUS", 1.0) : 1.0;
            boolean noBuffsHardMode = pointBonusLevel <= 0 && frozenBuffs.isEmpty();

            PointService pointService = new PointService();
            PointService.ScoreBreakdown bd = pointService.buildBreakdown(
                    levelsCompletedCount,
                    zeroErrorLevelCount,
                    totalErrors,
                    remainingItems,
                    win,
                    scoreItemPointsAccrued,
                    pointBonusLevel,
                    pointBonusMultiplier,
                    noBuffsHardMode
            );

            currentRun.setFinalScore(bd.getTotal());
            lastRunBreakdown = bd;
            saveCurrentRun();
        }

        currentRun = null;
        currentEngine = null;
    }

    public PointService.ScoreBreakdown getLastRunBreakdown() { return lastRunBreakdown; }
    
    public boolean startNewRun() {
        try {
            User currentUser = getCurrentUser();
            if (currentUser == null) {
                return false;
            }
            var existing = new model.dao.RunDAO(dbManager).findActiveRunByUser(currentUser.getNick());
            if (existing.isPresent()) {
                return false;
            }
            Run run = new Run(currentUser.getNick(), 3, "DEFAULT");
            this.currentRun = run;
            runDAO.save(run);
            return true;
        } catch (Exception e) {
            System.err.println("Errore nella creazione di una nuova run: " + e.getMessage());
            return false;
        }
    }
    
    public boolean resumeLastRun() {
        try {
            User currentUser = getCurrentUser();
            if (currentUser == null) return false;
            var opt = new model.dao.RunDAO(dbManager).findActiveRunByUser(currentUser.getNick());
            if (opt.isPresent()) {
                this.currentRun = opt.get();
                return true;
            }
            return false;
        } catch (Exception e) {
            System.err.println("Errore nel resume della run: " + e.getMessage());
            return false;
        }
    }
    
    private User getCurrentUser() {
        String nick = SessionService.getCurrentNick();
        if (nick == null || nick.isEmpty()) {
            return null;
        }
        try {
            return new model.dao.UserDAO(dbManager).getUserByNick(nick);
        } catch (Exception e) {
            System.err.println("Errore nel recupero dell'utente corrente: " + e.getMessage());
            return null;
        }
    }
    
    private RunFrozenBuffs freezeBuffs(User user) {
        if (user == null) {
            return new RunFrozenBuffs(Collections.emptyMap());
        }
        
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

    public boolean removeItem(String itemId) {
        boolean ok = removeItemFromInventory(itemId);
        if (ok) saveCurrentRun();
        return ok;
    }

    public void addItem(String itemId) {
        addItemToInventory(itemId, 1);
        saveCurrentRun();
    }

    private void saveCurrentRun() {
        if (currentRun != null) {
            runDAO.save(currentRun);
        }
    }
    
    public void save() {
        saveCurrentRun();
    }
    
    public Run getCurrentRun() {
        return currentRun;
    }

    public SudokuEngine getCurrentEngine() {
        return currentEngine;
    }
}