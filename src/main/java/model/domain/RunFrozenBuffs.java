package model.domain;

import java.util.Collections;
import java.util.Map;

/**
 * Rappresenta i Buff che sono stati 'congelati' all'inizio di una Run.
 * TODO: Tommy
 */
public class RunFrozenBuffs {

    private final Map<String, Integer> buffs;

    public RunFrozenBuffs(Map<String, Integer> buffs) {
        this.buffs = buffs != null ? buffs : Collections.emptyMap();
    }

    public int getBuffLevel(String buffId) {
        return buffs.getOrDefault(buffId, 0);
    }
    
}
