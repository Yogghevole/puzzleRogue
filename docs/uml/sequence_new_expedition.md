### Diagramma di Sequenza: New Expedition (Avvio e Setup)

Modella il flusso di creazione di una nuova Run, con selezione del personaggio, congelamento dei buff permanenti e generazione dei dati iniziali del livello 1.

```mermaid
sequenceDiagram
    participant UI as User Interface
    participant RunService as Run Service
    participant UserService as User Service
    participant Generator as Game Generator
    participant GameDataService as Game Data Service
    participant DB as Database

    UI->>UI: User Selects "New Expedition"
    
    UI->>UI: **1. Character Selection Screen**
    UI->>UI: User Chooses Character (ID)
    
    UI->>UserService: 2. getCurrentBuffs(userId)
    UserService->>DB: 3. query permanentBuffLevels
    DB-->>UserService: 4. return currentBuffState (B)

    Note over UserService: **Freeze Buff State** (B) for New Run
    
    UI->>Generator: 5. determineDifficulty(Level 1, B)
    Generator->>GameDataService: 6. retrieveBuffValue(B.STARTING_HINTS)
    Note right of GameDataService: Consults PermanentBuff class logic
    GameDataService-->>Generator: 7. return extraHintsValue
    Generator->>UI: 8. return difficultyTier (D_tier)

    UI->>Generator: 9. generateInitialSudoku(D_tier)
    Generator->>Generator: Select Random Enemy Sprite (E_id)
    Generator-->>UI: 10. return SudokuState Initial, Enemy ID (E_id)

    UI->>RunService: 11. createNewRun(userId, Character, B, D_tier, E_id, SudokuState)
    
    RunService->>GameDataService: 12. calculateInitialStats(B)
    Note right of GameDataService: Uses PermanentBuff classes to calculate Lives and Inventory Slots
    RunService->>RunService: - Initial Lives = Base + Buff Value
    RunService->>RunService: - Max Inventory Slots = Base + Buff Value

    RunService->>DB: 13. saveNewRun(Run Data, R_new)
    DB-->>RunService: 14. return newRunId (R_new)
    
    RunService-->>UI: 15. runCreated(R_new)
    
    UI->>UserService: 16. updateCurrentRun(userId, R_new)
    UserService->>DB: 17. update User SET currentRunId = R_new
    DB-->>UserService: 18. confirm update
    
    UI->>UI: Load Game State (R_new) and Start Level 1