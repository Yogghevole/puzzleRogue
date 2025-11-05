### Diagramma di Sequenza: Interazione e Salvataggio a Livello

Modella l'input dell'utente sul Sudoku, il check degli errori, la logica di perdita vita/buff e il salvataggio manuale.

```mermaid
sequenceDiagram
    participant UI as User Interface
    participant GameLogic as Game Logic
    participant RunService as Run Service
    participant DB as Database

    Note over UI: User tries to insert a Number (N) in Cell (C)
    UI->>GameLogic: 1. attemptInput(runId, C, N)

    GameLogic->>GameLogic: 2. Check Input Validity
    
    alt Input is VALID (Correct Number or Note)
        GameLogic->>DB: 3. update Run.SudokuState: userGrid/notesData
        DB-->>GameLogic: 4. confirm update
        GameLogic-->>UI: 5. inputSuccess(C, N)
        
        alt Sudoku is Complete
            GameLogic->>UI: 6. levelSolved()
            Note over UI: Proceeds to Reward Sequence
        end
        
    else Input is INVALID (Error)
        GameLogic->>GameLogic: 7. Check Run.SudokuState.protectionUsed
        
        alt ProtectionUsed is FALSE (Buff is Active)
            GameLogic->>GameLogic: 8. Set protectionUsed = TRUE
            GameLogic->>DB: 9. update Run.SudokuState
            DB-->>GameLogic: 10. confirm update
            GameLogic-->>UI: 11. errorHandled(message: "Protection Consumed")
            
        else ProtectionUsed is TRUE (Buff is Consumed or Not Active)
            GameLogic->>GameLogic: 12. Decrement Run.livesRemaining
            GameLogic->>GameLogic: Increment Run.totalErrors and Run.errorsInCurrentLevel
            
            GameLogic->>DB: 13. update Run (Lives, Errors)
            DB-->>GameLogic: 14. confirm update

            alt livesRemaining > 0
                GameLogic-->>UI: 15. errorOccurred(newLivesCount)
            else livesRemaining = 0
                GameLogic->>UI: 16. gameOver()
                Note over UI: **Triggers End Run Sequence**
            end
        end
    end

    UI->>UI: **User Selects Save & Exit** (via Settings Menu)
    UI->>RunService: 17. saveAndExit(runId)
    RunService->>DB: 18. update Run (all current data)
    DB-->>RunService: 19. confirm save
    RunService-->>UI: 20. saveSuccess()
    Note over UI: Return to Main Menu