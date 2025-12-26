package model.domain;

import java.util.HashMap;
import java.util.Map;


/**
 * Represents a player profile with statistics and permanent progression.
 */
public class User {

    private String nick;
    private Integer currentRunId;
    private int pointsAvailable;
    private int pointsTotal;
    private int runsCompleted;
    private int runsWon;

    private final Map<String, Integer> permanentBuffLevels; 

    public User(String nick, Integer currentRunId, int pointsAvailable, int pointsTotal, int runsCompleted, int runsWon, Map<String, Integer> permanentBuffLevels) {
        this.nick = nick;
        this.currentRunId = currentRunId;
        this.pointsAvailable = pointsAvailable;
        this.pointsTotal = pointsTotal;
        this.runsCompleted = runsCompleted;
        this.runsWon = runsWon;
        this.permanentBuffLevels = permanentBuffLevels != null ? permanentBuffLevels : new HashMap<>();
    }
    
    public User(String nick) {
        this(nick, null, 0, 0, 0, 0, null);
    }

    public String getNick() {
        return nick;
    }
    
    public void setNick(String nick) {
        this.nick = nick;
    }
    
    public Integer getCurrentRunId() {
        return currentRunId;
    }

    public void setCurrentRunId(Integer currentRunId) {
        this.currentRunId = currentRunId;
    }

    public int getPointsAvailable() {
        return pointsAvailable;
    }

    public void setPointsAvailable(int pointsAvailable) {
        this.pointsAvailable = pointsAvailable;
    }
    
    public void addPoints(int points) {
        this.pointsAvailable += points;
        this.pointsTotal += points;
    }

    public boolean spendPoints(int points) {
        if (pointsAvailable >= points) {
            pointsAvailable -= points;
            return true;
        }
        return false;
    }

    public boolean tryPurchase(int cost) {
        return spendPoints(cost);
    }

    public int getPointsTotal() {
        return pointsTotal;
    }
    
    public int getRunsCompleted() {
        return runsCompleted;
    }

    public void incrementRunsCompleted() {
        this.runsCompleted++;
    }
    
    public void setRunsCompleted(int runsCompleted) {
        this.runsCompleted = runsCompleted;
    }

    public int getRunsWon() {
        return runsWon;
    }

    public void incrementRunsWon() {
        this.runsWon++;
    }
    
    public void setRunsWon(int runsWon) {
        this.runsWon = runsWon;
    }

    public Map<String, Integer> getPermanentBuffLevels() {
        return permanentBuffLevels;
    }

    public int getBuffLevel(String buffId) {
        return permanentBuffLevels.getOrDefault(buffId, 0);
    }

    public int getBuffLevel(BuffType type) {
        return getBuffLevel(type.name());
    }
    
    public void upgradeBuff(String buffId, int newLevel) {
        if (newLevel > 0) {
            permanentBuffLevels.put(buffId, newLevel);
        } else {
             permanentBuffLevels.remove(buffId); 
        }
    }

    public void upgradeBuff(BuffType type, int newLevel) {
        upgradeBuff(type.name(), newLevel);
    }
}