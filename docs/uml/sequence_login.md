### Diagramma di Sequenza: Login e Caricamento Partita

Modella l'accesso dell'utente, la creazione di un nuovo utente e la verifica della Run in corso (`currentRunId`).

```mermaid
sequenceDiagram
    participant UI as User Interface
    participant UserService as User Service
    participant DB as Database

    UI->>UI: User Enters Nickname
    UI->>UserService: 1. checkUser(nick)

    UserService->>DB: 2. query User WHERE nick = X
    DB-->>UserService: 3. return User object or NULL

    alt User Exists
        UserService->>UI: 4. UserExists(object)
        UI->>UI: Check currentRunId
        
        alt currentRunId is NOT NULL
            UI->>UserService: 5. loadCurrentRun(runId)
            UserService->>DB: 6. query Run, SudokuState, Items
            DB-->>UserService: 7. return Run Data
            UserService-->>UI: 8. loadSuccess(Run Data)
            UI->>UI: Display Main Menu (Venture Forth ENABLED)
        
        else currentRunId is NULL
            UI-->>UI: Display Main Menu (Venture Forth DISABLED)
        end
    
    else User Does Not Exist (NULL)
        UserService->>DB: 9. createNewUser(nick)
        DB-->>UserService: 10. return new User object (currentRunId=NULL)
        UserService-->>UI: 11. NewUserCreated(object)
        UI->>UI: Display Main Menu (Venture Forth DISABLED)
    end