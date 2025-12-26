package model.domain.buff;

import model.domain.BuffType;

/**
 * Abstract base class for all permanent upgrades.
 */
public abstract class Buff {
    protected final BuffType type;

    public Buff(BuffType type) {
        this.type = type;
    }

    public BuffType getType() {
        return type;
    }

    public String getId() {
        return type.name();
    }

    public abstract String getDisplayName();
    public abstract String getDescription();
    public abstract int getCost(int level);
    public abstract int getMaxLevel();
}
