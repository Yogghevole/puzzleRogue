package model.domain.buff;

import model.domain.BuffType;

/**
 * Buff that grants additional lives at the start of a run.
 */
public class ExtraLivesBuff extends Buff {
    public ExtraLivesBuff() {
        super(BuffType.EXTRA_LIVES);
    }

    @Override
    public String getDisplayName() {
        return "Extra Lives";
    }

    @Override
    public String getDescription() {
        return "Start each run with additional lives.";
    }

    @Override
    public int getCost(int level) {
        if (level == 1) return 500;
        if (level == 2) return 1200;
        if (level == 3) return 2000;
        return -1;
    }

    @Override
    public int getMaxLevel() {
        return 3;
    }
}
