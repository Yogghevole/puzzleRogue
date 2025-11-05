### Diagramma di Sequenza: Ricompensa Fine Livello e Uso Oggetti

Modella la scelta della ricompensa di fine livello (gestione capacità inventario) e l'uso di un oggetto monouso in partita.

```mermaid
sequenceDiagram
    participant GameLogic as Game Logic
    participant Generator as Game Generator
    participant RunService as Run Service
    participant DB as Database
    participant UI as User Interface

    Note over GameLogic: Level Solved Triggered (from Level Interaction)
    
    GameLogic->>Generator: 1. generateRewardOptions(currentLevel)
    Generator->>Generator: Select 2 Random Item Types
    Generator-->>GameLogic: 2. return Options (ItemA, ItemB, Skip)

    GameLogic-->>UI: 3. displayRewardChoice(Options)
    
    UI->>UI: User Selects ItemA/ItemB or Skip
    
    alt User Selects Item (ItemX)
        UI->>RunService: 4. attemptAddItem(runId, ItemX)
        RunService->>DB: 5. query Run.frozenBuffs.CapacitaInventario
        DB-->>RunService: 6. return maxSlots
        
        alt Inventory is NOT Full (currentItems < maxSlots)
            RunService->>DB: 7. update Run.inventory: add ItemX
            DB-->>RunService: 8. confirm update
            RunService-->>UI: 9. itemAdded(ItemX)
        else Inventory is Full
            RunService-->>UI: 10. inventoryFull(ItemX)
            UI->>UI: Display prompt to discard or skip
        end
    
    else User Selects Skip or Fails to Add Item
        UI->>UI: Proceed to Next Level Setup
    end

    Note over UI: **Start Next Level Setup**
    UI->>GameLogic: 11. startNewLevel(runId)
    GameLogic->>GameLogic: Increment currentLevel
    Note over GameLogic: Triggers logic from "New Expedition" (Generate Sudoku, Enemy)
    
    
    loop Item Use During Level
        UI->>UI: User Activates Consumable Item (ItemY)
        UI->>RunService: 12. useItem(runId, ItemY)
        
        RunService->>DB: 13. query Run.inventory
        DB-->>RunService: 14. return inventoryData
        
        alt ItemY is Available in Inventory
            RunService->>RunService: 15. Execute ItemY Effect:
            Note over RunService: e.g., Restore 1 Life, Gain Points, Reveal Grid Cell
            
            RunService->>DB: 16. update Run: (Lives/Points/SudokuState)
            RunService->>DB: 17. update Run.inventory: remove ItemY
            DB-->>RunService: 18. confirm updates
            RunService-->>UI: 19. itemEffectApplied(ItemY)
        else ItemY Not Available
            RunService-->>UI: 20. itemUseFailed()
        end
    end