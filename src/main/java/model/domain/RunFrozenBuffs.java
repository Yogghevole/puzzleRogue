package model.domain;

import java.util.Collections;
import java.util.Map;

/**
 * Snapshot of active buffs for a specific run.
 */
public class RunFrozenBuffs {
    private final Map<String, Integer> frozenBuffLevels;

    public RunFrozenBuffs(Map<String, Integer> buffLevels) {
        this.frozenBuffLevels = Collections.unmodifiableMap(buffLevels);
    }

    public int getBuffLevel(String buffId) {
        return frozenBuffLevels.getOrDefault(buffId, 0);
    }

    public boolean isEmpty() {
        return frozenBuffLevels.isEmpty();
    }

    public Map<String, Integer> getFrozenBuffs() {
        return frozenBuffLevels;
    }
}
