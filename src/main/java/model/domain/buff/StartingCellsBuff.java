package model.domain.buff;

import model.domain.BuffType;

/**
 * Buff that reveals more initial cells in Sudoku grids.
 */
public class StartingCellsBuff extends Buff {
    public StartingCellsBuff() {
        super(BuffType.STARTING_CELLS);
    }

    @Override
    public String getDisplayName() {
        return "Starting Cells";
    }

    @Override
    public String getDescription() {
        return "Start levels with more pre-filled cells.";
    }

    @Override
    public int getCost(int level) {
        if (level == 1) return 1000;
        if (level == 2) return 1600;
        if (level == 3) return 3000;
        return -1;
    }

    @Override
    public int getMaxLevel() {
        return 3;
    }
}
