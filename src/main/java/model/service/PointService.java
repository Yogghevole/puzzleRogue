package model.service;

import java.util.ArrayList;
import java.util.List;

/**
 * Service responsible for calculating scores and generating detailed breakdowns.
 */
public class PointService {

    /**
     * Represents a single line item in the score breakdown.
     */
    public static class Entry {
        public final String title;
        public final int points;
        public Entry(String title, int points) { this.title = title; this.points = points; }
    }

    public static class ScoreBreakdown {
        private final List<Entry> entries = new ArrayList<>();
        private int subtotal;
        private int multiplierAdded;
        private int total;

        public void add(String title, int points) {
            if (points == 0) return;
            entries.add(new Entry(title, points));
            subtotal += points;
        }

        public void applyMultiplier(String title, double multiplier) {
            if (multiplier <= 1.0) { total = subtotal; return; }
            int added = (int) Math.round(subtotal * (multiplier - 1.0));
            multiplierAdded = added;
            entries.add(new Entry(title, added));
            total = subtotal + added;
        }

        public void finalizeTotal() { total = subtotal; }

        public List<Entry> getEntries() { return entries; }
        public int getSubtotal() { return subtotal; }
        public int getMultiplierAdded() { return multiplierAdded; }
        public int getTotal() { return total; }
    }

    public ScoreBreakdown buildBreakdown(int levelsCompleted,
                                         int zeroErrorLevelCount,
                                         int totalErrors,
                                         int remainingItems,
                                         boolean victory,
                                         int scoreItemPointsAccrued,
                                         int pointBonusLevel,
                                         double pointBonusMultiplier,
                                         boolean noBuffsHardMode) {
        ScoreBreakdown bd = new ScoreBreakdown();

        int basePoints = levelsCompleted > 0 ? (levelsCompleted * (levelsCompleted + 1) / 2) * 10 : 0;
        bd.add("Levels Completed (x" + levelsCompleted + ")", basePoints);

        bd.add("Zero Error Levels (x" + zeroErrorLevelCount + ")", zeroErrorLevelCount * 30);

        bd.add("Total Errors (x" + totalErrors + ")", totalErrors * 5);

        bd.add("Inventory Items Remaining (x" + remainingItems + ")", remainingItems * 20);

        bd.add("Score Item Uses", scoreItemPointsAccrued);

        if (victory) { bd.add("Final Boss Bonus", 200); }

        if (pointBonusLevel <= 0 && noBuffsHardMode) {
            bd.add("Hard Mode Bonus (No Permanent Buffs)", 50);
        }

        if (pointBonusLevel > 0 && pointBonusMultiplier > 1.0) {
            int pct = (int) Math.round((pointBonusMultiplier - 1.0) * 100);
            bd.applyMultiplier("Score Multiplier (+" + pct + "%)", pointBonusMultiplier);
        } else {
            bd.finalizeTotal();
        }

        return bd;
    }
}
