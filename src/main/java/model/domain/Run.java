package model.domain;

import java.util.Collections;
import java.util.List;

/**
 * Rappresenta una singola Run in corso o completata.
 */
public class Run {

    private Integer runId;
    private final String userNick;
    private int livesRemaining;
    private final String characterSelected;
    private int totalErrors;
    private RunLevelState currentLevelState; 
    private final List<RunInventoryItem> inventory; 

    public Run(Integer runId, String userNick, int livesRemaining, String characterSelected, int totalErrors, List<RunInventoryItem> inventory) {
        this.runId = runId;
        this.userNick = userNick;
        this.livesRemaining = livesRemaining;
        this.characterSelected = characterSelected;
        this.totalErrors = totalErrors;
        this.inventory = inventory != null ? inventory : Collections.emptyList();
    }
    
    public Run(String userNick, int startingLives, String characterSelected) {
        this(null, userNick, startingLives, characterSelected, 0, null);
    }
    
    // --- Gestione Inventario ---
    
    public static class RunInventoryItem {
        private final String itemTypeId;
        private int quantity;
        private final int slotCost;

        public RunInventoryItem(String itemTypeId, int quantity, int slotCost) {
            this.itemTypeId = itemTypeId;
            this.quantity = quantity;
            this.slotCost = slotCost;
        }

        public String getItemTypeId() { return itemTypeId; }
        public int getQuantity() { return quantity; }
        public int getSlotCost() { return slotCost; }
        public void setQuantity(int quantity) { this.quantity = quantity; }
    }
    
    // --- Metodi Getters e Setters ---

    public Integer getRunId() {
        return runId;
    }

    public void setRunId(Integer runId) {
        this.runId = runId;
    }
    
    public String getUserNick() {
        return userNick;
    }

    public int getLivesRemaining() {
        return livesRemaining;
    }

    public void setLivesRemaining(int livesRemaining) {
        this.livesRemaining = livesRemaining;
    }
    
    public void loseLife() {
        this.livesRemaining = Math.max(0, this.livesRemaining - 1);
    }
    
    public void addLife() {
        this.livesRemaining++;
    }

    public String getCharacterSelected() {
        return characterSelected;
    }

    public int getTotalErrors() {
        return totalErrors;
    }

    public void incrementTotalErrors() {
        this.totalErrors++;
    }

    public RunLevelState getCurrentLevelState() {
        return currentLevelState;
    }

    public void setCurrentLevelState(RunLevelState currentLevelState) {
        this.currentLevelState = currentLevelState;
    }
    
    public List<RunInventoryItem> getInventory() {
        return inventory;
    }
}