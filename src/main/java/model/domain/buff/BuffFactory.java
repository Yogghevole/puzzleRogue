package model.domain.buff;

import model.domain.BuffType;
import java.util.EnumMap;
import java.util.Map;

/**
 * Factory for retrieving Buff instances by type or ID.
 */
public class BuffFactory {
    private static final Map<BuffType, Buff> buffs = new EnumMap<>(BuffType.class);

    static {
        buffs.put(BuffType.EXTRA_LIVES, new ExtraLivesBuff());
        buffs.put(BuffType.FIRST_ERROR_PROTECT, new FirstErrorProtectBuff());
        buffs.put(BuffType.STARTING_CELLS, new StartingCellsBuff());
        buffs.put(BuffType.POINT_BONUS, new PointBonusBuff());
        buffs.put(BuffType.INVENTORY_CAPACITY, new InventoryCapacityBuff());
    }

    public static Buff getBuff(BuffType type) {
        return buffs.get(type);
    }

    public static Buff getBuff(String id) {
        try {
            return buffs.get(BuffType.valueOf(id));
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
