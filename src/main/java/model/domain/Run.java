package model.domain;

import java.util.HashMap;
import java.util.Map;

/**
 * Rappresenta una singola Run in corso o completata.
 */
public class Run {

    private Integer id;
    private final String userNick;
    private final String characterId;
    private int livesRemaining;
    private int totalErrors;
    private int score;
    private RunLevelState currentLevelState;
    private Map<String, Integer> inventory;

    public Run(String userNick, int livesRemaining, String characterId) {
        this.userNick = userNick;
        this.livesRemaining = livesRemaining;
        this.characterId = characterId;
        this.totalErrors = 0;
        this.score = 0;
        this.inventory = new HashMap<>();
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