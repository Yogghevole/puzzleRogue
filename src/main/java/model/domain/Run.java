package model.domain;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents a single game run, tracking progress, score, and state.
 */
public class Run {

    private Integer id;
    private final String userNick;
    private final String characterId;
    private int livesRemaining;
    private int totalErrors;
    private int score;
    private int levelsCompleted;
    private int zeroErrorLevels;
    private int scoreItemPoints;
    private RunLevelState currentLevelState;
    private Map<String, Integer> inventory;
    private java.util.Set<String> usedEnemies;

    public Run(String userNick, int livesRemaining, String characterId) {
        this.userNick = userNick;
        this.livesRemaining = livesRemaining;
        this.characterId = characterId;
        this.totalErrors = 0;
        this.score = 0;
        this.levelsCompleted = 0;
        this.zeroErrorLevels = 0;
        this.scoreItemPoints = 0;
        this.inventory = new HashMap<>();
        this.usedEnemies = new java.util.HashSet<>();
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getId() {
        return id;
    }

    public String getUserNick() {
        return userNick;
    }

    public String getCharacterId() {
        return characterId;
    }

    public int getLivesRemaining() {
        return livesRemaining;
    }

    public void loseLife() {
        this.livesRemaining--;
    }

    public void addLife() {
        this.livesRemaining++;
    }

    public int getTotalErrors() {
        return totalErrors;
    }

    public void incrementTotalErrors() {
        this.totalErrors++;
    }

    public void setTotalErrors(int totalErrors) {
        this.totalErrors = totalErrors;
    }

    public int getScore() {
        return score;
    }

    public void addToScore(int points) {
        this.score += points;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public void setFinalScore(int finalScore) {
        this.score = finalScore;
    }

    public int getLevelsCompleted() { return levelsCompleted; }
    public void setLevelsCompleted(int levelsCompleted) { this.levelsCompleted = levelsCompleted; }
    public void incrementLevelsCompleted() { this.levelsCompleted++; }

    public int getZeroErrorLevels() { return zeroErrorLevels; }
    public void setZeroErrorLevels(int zeroErrorLevels) { this.zeroErrorLevels = zeroErrorLevels; }
    public void incrementZeroErrorLevels() { this.zeroErrorLevels++; }

    public int getScoreItemPoints() { return scoreItemPoints; }
    public void setScoreItemPoints(int scoreItemPoints) { this.scoreItemPoints = scoreItemPoints; }

    public java.util.Set<String> getUsedEnemies() { return usedEnemies; }
    public void setUsedEnemies(java.util.Set<String> usedEnemies) { this.usedEnemies = usedEnemies; }
    public void addUsedEnemy(String enemyPath) { 
        if (this.usedEnemies == null) this.usedEnemies = new java.util.HashSet<>();
        this.usedEnemies.add(enemyPath); 
    }

    public void addScoreItemPoints(int points) { this.scoreItemPoints += points; }

    public RunLevelState getCurrentLevelState() {
        return currentLevelState;
    }

    public void setCurrentLevelState(RunLevelState currentLevelState) {
        this.currentLevelState = currentLevelState;
    }

    public Map<String, Integer> getInventory() {
        return inventory;
    }

    public boolean addItemToInventory(String itemId, int quantity) {
        inventory.merge(itemId, quantity, Integer::sum);
        return true;
    }

    public boolean removeItemFromInventory(String itemId) {
        Integer count = inventory.get(itemId);
        if (count == null || count <= 0) return false;
        
        if (count == 1) {
            inventory.remove(itemId);
        } else {
            inventory.put(itemId, count - 1);
        }
        return true;
    }
}
