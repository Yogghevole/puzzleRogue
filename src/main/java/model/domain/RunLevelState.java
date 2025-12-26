package model.domain;

/**
 * Snapshot of the current level's state, including grid and progress.
 */
public class RunLevelState {

    private final Integer runId; 
    private int currentLevel;
    private String enemySpriteId;
    private String backgroundId;
    private final String difficultyTier;
    private final String initialGridData;
    private final String solvedGridData;
    private String userGridData;      
    private String notesData;         
    private int errorsInLevel;
    private boolean protectionUsed;
    private String bonusCellsData;

    public RunLevelState(Integer runId, int currentLevel, String enemySpriteId, String backgroundId, String difficultyTier, 
                         String initialGridData, String solvedGridData, String userGridData, String notesData, 
                         int errorsInLevel, boolean protectionUsed, String bonusCellsData) {
        this.runId = runId;
        this.currentLevel = currentLevel;
        this.enemySpriteId = enemySpriteId;
        this.backgroundId = backgroundId;
        this.difficultyTier = difficultyTier;
        this.initialGridData = initialGridData;
        this.solvedGridData = solvedGridData;
        this.userGridData = userGridData;
        this.notesData = notesData;
        this.errorsInLevel = errorsInLevel;
        this.protectionUsed = protectionUsed;
        this.bonusCellsData = bonusCellsData;
    }
    

    public RunLevelState(Integer runId, int currentLevel, String enemySpriteId, String difficultyTier, 
                         String initialGridData, String solvedGridData, String userGridData, String notesData, 
                         int errorsInLevel, boolean protectionUsed, String bonusCellsData) {
        this(runId, currentLevel, enemySpriteId, null, difficultyTier, initialGridData, solvedGridData, userGridData, notesData, errorsInLevel, protectionUsed, bonusCellsData);
    }
    

    public RunLevelState(Integer runId, int currentLevel, String enemySpriteId, String difficultyTier, 
                         String initialGridData, String solvedGridData, String userGridData, String notesData, 
                         int errorsInLevel, boolean protectionUsed) {
        this(runId, currentLevel, enemySpriteId, null, difficultyTier, initialGridData, solvedGridData, userGridData, notesData, errorsInLevel, protectionUsed, "");
    }

    public RunLevelState(int currentLevel, String enemySpriteId, SudokuGrid newPuzzle, boolean hasProtectionBuff) {
        this(null, 
             currentLevel, 
             enemySpriteId, 
             null,
             newPuzzle.getDifficultyTier(), 
             convertGridToString(newPuzzle.getInitialGrid()), 
             convertGridToString(newPuzzle.getSolvedGrid()),
             convertGridToString(newPuzzle.getInitialGrid()),
             "", 
             0, 
             !hasProtectionBuff,
             String.join(";", newPuzzle.getBonusCells())
        );
    }
    

    public static String convertGridToString(int[][] grid) {
        StringBuilder sb = new StringBuilder();
        for (int[] row : grid) {
            for (int cell : row) {
                sb.append(cell);
            }
        }
        return sb.toString();
    }
    
    public static int[][] convertStringToGrid(String gridData) {
        int size = 9;
        int[][] grid = new int[size][size];
        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {
                int index = r * size + c;
                if (index < gridData.length()) {
                    grid[r][c] = Character.getNumericValue(gridData.charAt(index));
                }
            }
        }
        return grid;
    }



    public int getCurrentLevel() {
        return currentLevel;
    }

    public void incrementErrorsInLevel() {
        this.errorsInLevel++;
    }

    public int getErrorsInLevel() {
        return errorsInLevel;
    }
    
    public boolean isProtectionUsed() {
        return protectionUsed;
    }

    public void setProtectionUsed(boolean protectionUsed) {
        this.protectionUsed = protectionUsed;
    }

    public String getUserGridData() {
        return userGridData;
    }

    public void setUserGridData(String userGridData) {
        this.userGridData = userGridData;
    }

    public String getNotesData() {
        return notesData;
    }

    public void setNotesData(String notesData) {
        this.notesData = notesData;
    }

    public String getInitialGridData() {
        return initialGridData;
    }
    
    public String getSolvedGridData() {
        return solvedGridData;
    }
    
    public Integer getRunId() { return runId; }
    public String getEnemySpriteId() { return enemySpriteId; }
    public void setEnemySpriteId(String enemySpriteId) { this.enemySpriteId = enemySpriteId; }
    
    public String getBackgroundId() { return backgroundId; }
    public void setBackgroundId(String backgroundId) { this.backgroundId = backgroundId; }
    
    public String getDifficultyTier() { return difficultyTier; }
    
    public String getBonusCellsData() { return bonusCellsData; }
    public void setBonusCellsData(String bonusCellsData) { this.bonusCellsData = bonusCellsData; }
}
