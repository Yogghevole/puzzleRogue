package model.domain.buff;

import model.domain.BuffType;

/**
 * Buff that prevents the first mistake in each level.
 */
public class FirstErrorProtectBuff extends Buff {
    public FirstErrorProtectBuff() {
        super(BuffType.FIRST_ERROR_PROTECT);
    }

    @Override
    public String getDisplayName() {
        return "First Error Protect";
    }

    @Override
    public String getDescription() {
        return "The first mistake in a level is ignored.";
    }

    @Override
    public int getCost(int level) {
        if (level == 1) return 2000;
        return -1;
    }

    @Override
    public int getMaxLevel() {
        return 1;
    }
}
