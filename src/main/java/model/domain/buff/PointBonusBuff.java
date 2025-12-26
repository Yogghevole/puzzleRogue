package model.domain.buff;

import model.domain.BuffType;

/**
 * Buff that increases score gained from levels.
 */
public class PointBonusBuff extends Buff {
    public PointBonusBuff() {
        super(BuffType.POINT_BONUS);
    }

    @Override
    public String getDisplayName() {
        return "Point Bonus";
    }

    @Override
    public String getDescription() {
        return "Earn more points for completing levels.";
    }

    @Override
    public int getCost(int level) {
        if (level == 1) return 600;
        if (level == 2) return 1300;
        if (level == 3) return 2100;
        return -1;
    }

    @Override
    public int getMaxLevel() {
        return 3;
    }
}
