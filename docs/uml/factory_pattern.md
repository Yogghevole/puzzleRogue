### Factory Pattern (Buffs)

The BuffFactory decouples the retrieval of specific Buff metadata from the business logic, allowing for centralized management of game bonuses. The Buff classes act as prototypes/data-holders defining the properties of each upgrade.

```mermaid
classDiagram
    direction LR
    
    class BuffFactory {
        -static Map~BuffType, Buff~ buffs
        +static getBuff(BuffType) Buff
        +static getBuff(String id) Buff
    }

    class BuffType {
        <<Enumeration>>
        EXTRA_LIVES
        FIRST_ERROR_PROTECT
        STARTING_CELLS
        POINT_BONUS
        INVENTORY_CAPACITY
    }

    class Buff {
        <<Abstract>>
        #BuffType type
        +getType() BuffType
        +getId() String
        +getDisplayName() String
        +getDescription() String
        +getCost(int level) int
        +getMaxLevel() int
    }

    class ExtraLivesBuff {
        +getCost(int) int
        +getMaxLevel() int
    }
    class FirstErrorProtectBuff {
        +getCost(int) int
        +getMaxLevel() int
    }
    class StartingCellsBuff {
        +getCost(int) int
        +getMaxLevel() int
    }
    class PointBonusBuff {
        +getCost(int) int
        +getMaxLevel() int
    }
    class InventoryCapacityBuff {
        +getCost(int) int
        +getMaxLevel() int
    }

    BuffFactory ..> Buff : creates/retrieves
    BuffFactory ..> BuffType : uses
    Buff --> BuffType : has type

    ExtraLivesBuff --|> Buff
    FirstErrorProtectBuff --|> Buff
    StartingCellsBuff --|> Buff
    PointBonusBuff --|> Buff
    InventoryCapacityBuff --|> Buff
```
