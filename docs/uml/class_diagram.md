### Diagramma delle Classi

Questo diagramma definisce la struttura dei dati persistenti (`User`, `Run`) e le loro relazioni.

```mermaid
classDiagram
    direction LR

    class User{
        <<Entity>>
        +String nick (PK)
        +int currentRunId (FK, NULLable)
        ---
        +int pointsAvailable
        +int pointsTotal
        +int runsCompleted
        +int runsWon
        ---
        +Map<String, int> permanentBuffLevels
    }

    class Run{
        <<Entity>>
        +int runId (PK)
        +int userId (FK)
        ---
        +int currentLevel (1..10)
        +int livesRemaining
        +String characterSelected
        +String enemySpriteId
        +Map<String, int> frozenBuffs
        +List<Item> inventory
        +int totalErrors
    }

    class SudokuState{
        <<Value Object>>
        -String initialGrid
        -String userGrid
        -String notesData
        -int errorsInCurrentLevel
        -bool protectionUsed
        -String difficultyTier
    }

    class Item{
        <<Value Object>>
        +String typeId // e.g., "LIFE_BOOST_ITEM"
        +int quantity
    }

    class AbstractGameEffect{
        <<Abstract>>
        +String id (PK)
        +String name
        +apply(Run run, User user, int level)
    }
    
    class PermanentBuff extends AbstractGameEffect{
        <<Abstract>>
        +int maxLevel
        +Map<int, int> costPerLevel
    }

    class ConsumableItem extends AbstractGameEffect{
        <<Abstract>>
        +useEffect(Run run, User user)
    }

    User "1" o-- "0..1" Run : has current
    Run "1" *-- "1" SudokuState : contains
    Run "1" *-- "0..N" Item : has in
    
    AbstractGameEffect <|-- PermanentBuff : inherits from
    AbstractGameEffect <|-- ConsumableItem : inherits from
    
    User : permanentBuffLevels = Map<BuffId, Level>
    Run : frozenBuffs = Map<BuffId, Level>