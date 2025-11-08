package model.domain;

import java.util.HashMap;
import java.util.Map;


/**
 * Rappresenta l'entità utente e il suo stato permanente (punti, statistiche, buff permanenti sbloccati).
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

    public int getPointsTotal() {
        return pointsTotal;
    }
    
    public int getRunsCompleted() {
        return runsCompleted;
    }

    public void incrementRunsCompleted() {
        this.runsCompleted++;
    }

    public int getRunsWon() {
        return runsWon;
    }

    public void incrementRunsWon() {
        this.runsWon++;
    }

    public Map<String, Integer> getPermanentBuffLevels() {
        return permanentBuffLevels;
    }

    public int getBuffLevel(String buffId) {
        return permanentBuffLevels.getOrDefault(buffId, 0);
    }
    
    public void upgradeBuff(String buffId, int newLevel) {
        if (newLevel > 0) {
            permanentBuffLevels.put(buffId, newLevel);
        } else {
             permanentBuffLevels.remove(buffId); 
        }
    }
}