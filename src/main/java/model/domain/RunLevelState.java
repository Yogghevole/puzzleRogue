package model.domain;

/**
 * Rappresenta lo stato specifico del livello Sudoku attualmente in corso nella Run.
 */
public class RunLevelState {

    private final Integer runId; 
    private int currentLevel;
    private final String enemySpriteId;
    private final String difficultyTier;
    private final String initialGridData;
    private String userGridData;      
    private String notesData;         
    private int errorsInLevel;
    private boolean protectionUsed;

    public RunLevelState(Integer runId, int currentLevel, String enemySpriteId, String difficultyTier, 
                         String initialGridData, String userGridData, String notesData, 
                         int errorsInLevel, boolean protectionUsed) {
        this.runId = runId;
        this.currentLevel = currentLevel;
        this.enemySpriteId = enemySpriteId;
        this.difficultyTier = difficultyTier;
        this.initialGridData = initialGridData;
        this.userGridData = userGridData;
        this.notesData = notesData;
        this.errorsInLevel = errorsInLevel;
        this.protectionUsed = protectionUsed;
    }

    public RunLevelState(int currentLevel, String enemySpriteId, SudokuGrid newPuzzle, boolean hasProtectionBuff) {
        this(null, 
             currentLevel, 
             enemySpriteId, 
             newPuzzle.getDifficultyTier(), 
             convertGridToString(newPuzzle.getInitialGrid()), 
             convertGridToString(newPuzzle.getInitialGrid()),
             "", 
             0, 
             !hasProtectionBuff
        );
    }
    
    // --- (Griglia <-> String) ---

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


    // --- Metodi Getters e Setters ---

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
    
    public Integer getRunId() { return runId; }
    public String getEnemySpriteId() { return enemySpriteId; }
    public String getDifficultyTier() { return difficultyTier; }
}