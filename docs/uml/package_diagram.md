### Package Diagram (Architecture)

Illustrates the source code organization following the MVC pattern and separation of concerns.

```mermaid
classDiagram
    direction BT

    namespace ViewLayer {
        class view_controller
        class view_manager
        class view_util
    }

    namespace ModelLayer {
        class model_domain
        class model_service
        class model_dao
        class model_db
        class model_engine
    }

    namespace AppLayer {
        class Main
    }

    Main ..> view_controller : Loads FXML
    Main ..> model_db : Initializes DB

    view_controller ..> model_service : Uses Business Logic
    view_controller ..> view_manager : Uses UI Helpers
    
    model_service ..> model_dao : Data Access
    model_service ..> model_domain : Manipulates Entities
    model_service ..> model_engine : Sudoku Logic

    model_dao ..> model_db : SQL Execution
    model_dao ..> model_domain : Returns Entities

    model_engine ..> model_domain : Uses SudokuGrid
```

**Package Description:**
*   **view**: JavaFX graphical interface management.
    *   `controller`: User interaction logic (e.g., `GameController`).
    *   `manager`: UI state managers (e.g., `SoundManager`, `UserInfoManager`).
*   **model**: Business logic and data.
    *   `domain`: Pure entities (e.g., `User`, `Run`, `SudokuGrid`).
    *   `service`: Application logic (e.g., `RunService`, `SudokuGenerator`).
    *   `dao`: Data Access Objects for SQL queries.
    *   `db`: SQLite connection and schema management.
    *   `engine`: Sudoku specific logic (validation, generation).
