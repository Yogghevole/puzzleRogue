package model.domain.buff;

import model.domain.BuffType;

/**
 * Buff that increases inventory size.
 */
public class InventoryCapacityBuff extends Buff {
    public InventoryCapacityBuff() {
        super(BuffType.INVENTORY_CAPACITY);
    }

    @Override
    public String getDisplayName() {
        return "Inventory Capacity";
    }

    @Override
    public String getDescription() {
        return "Carry more items in your inventory.";
    }

    @Override
    public int getCost(int level) {
        if (level == 1) return 600;
        if (level == 2) return 1600;
        if (level == 3) return 3200;
        return -1;
    }

    @Override
    public int getMaxLevel() {
        return 3;
    }
}
