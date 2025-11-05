### Diagramma di Sequenza: End Run (Game Over e Vittoria)

Modella la conclusione di una run: calcolo punti, aggiornamento delle statistiche utente e pulizia del riferimento alla Run Corrente.

```mermaid
sequenceDiagram
    participant GameLogic as Game Logic
    participant RunService as Run Service
    participant PointService as Point Service
    participant UserService as User Service
    participant DB as Database
    participant UI as User Interface

    alt Final Event Triggered
        Note over GameLogic: Last Life Lost (Game Over) OR Boss Defeated (Victory)
        GameLogic->>RunService: 1. endRun(runId, isVictory)
    end
    
    Note over RunService: Run is now CLOSED
    
    RunService->>PointService: 2. calculateFinalScore(runData, isVictory)
    
    PointService->>PointService: 3. Fetch Run Data:
    PointService->>PointService: - Errors Total, Levels Completed
    PointService->>PointService: - Remaining Items, Frozen Buff State
    
    PointService->>PointService: 4. Calculate Base Points (Level x 10)
    PointService->>PointService: 5. Apply Bonuses (Items, No Errors, Errors Total, No Buffs)
    
    alt isVictory is TRUE
        PointService->>PointService: 6. Add +200 Boss Bonus
    end
    
    PointService->>PointService: 7. Apply Punti Guadagnati Modifier (%, if present)
    PointService-->>RunService: 8. return finalScore

    RunService->>UserService: 9. updateEndState(userId, finalScore, isVictory)
    
    UserService->>DB: 10. update User:
    UserService->>DB: - pointsAvailable += finalScore
    UserService->>DB: - runsCompleted++
    
    alt isVictory is TRUE
        UserService->>DB: 11. update User: runsWon++
    end
    
    UserService->>DB: 12. update User: currentRunId = NULL
    DB-->>UserService: 13. confirm updates
    UserService-->>RunService: 14. confirm update

    RunService->>UI: 15. displayEndScreen(finalScore, isVictory)