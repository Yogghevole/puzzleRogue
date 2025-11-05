### Game Data Catalog

#### Permanent Buffs (Referenced by ID in `User.permanentBuffLevels`)

These buffs are unlocked by spending **Points** in the "The Ancestor's Legacy" menu.

| ID | Name | Max Level | Level | Cost (Total Points) | Effect on Run |
| :--- | :--- | :--- | :--- | :--- | :--- |
| **EXTRA_LIVES** | Additional Lives 
| | | 1 | 50 | Starts the Run with **+1** Life. |
| | | 2 | 120 | Starts the Run with **+2** Lives. |
| | | 3 | 250 | Starts the Run with **+3** Lives. |

| **FIRST_ERROR_PROTECT** | First Error Protection 
| | | 1 | 100 | Gains **1 shield** that cancels the first error per Level. |

| **STARTING_HINTS** | Initial Hints 
| | | 1 | 75 | Generates the Sudoku with **+1** pre-filled cell. |
| | | 2 | 160 | Generates the Sudoku with **+2** pre-filled cells. |
| | | 3 | 300 | Generates the Sudoku with **+3** pre-filled cells. |

| **POINT_BONUS** | Point Gain Multiplier 
| | | 1 | 120 | Increases final score gain by **+10%**. |
| | | 2 | 280 | Increases final score gain by **+20%**. |
| | | 3 | 500 | Increases final score gain by **+33%**. |

| **INVENTORY_CAPACITY**| Inventory Capacity 
| | | 1 | 60 | Increases Inventory capacity by **+1** slot (Total: 3). |
| | | 2 | 140 | Increases Inventory capacity by **+2** slots (Total: 4). |
| | | 3 | 320 | Increases Inventory capacity by **+3** slots (Total: 5). |

---

#### Consumable Items (Referenced by ID in `Run.inventory`)

These items are earned at the end of each level and are consumed upon use.

| ID | Name | Effect Logic | Usage Context | Inventory Slot Usage |
| :--- | :--- | :--- | :--- | :--- |
| **LIFE_BOOST_ITEM** | Missing Heart | Restores **1 point** to `Run.livesRemaining`. Cannot exceed maximum life defined by permanent buffs. 
| Mid-level | 1 slot |
| **HINT_ITEM** | Insight Crystal | Reveals **1 correct number** in a randomly chosen empty cell. 
| Mid-level | 1 slot |
| **SCORE_ITEM** | Coin Cache | Adds (**Current Level x 10**) points immediately to the final score calculation. 
| Mid-level | 1 slot |
| **SACRIFICE_ITEM**| Blood Offering | **Decreases** `Run.livesRemaining` by 1; **Reveals 2 correct numbers** in empty cells. Requires at least 2 lives to use. 
| Mid-level | 1 slot |

---

### Final Score Calculation Logic (PointService)

A run concludes either by **Death** (zero lives) or **Victory** (defeating the Final Boss at Level 10). Upon conclusion, **Available Points** are calculated and added to the user's total.

#### 1. Base Point Calculation

The base score is cumulative based on the number of levels successfully completed.

| Component | Logic | Total Points (Max 10 Levels) |
| :--- | :--- | :--- |
| **Level Score** | For each level completed (N): **N x 10 Points**. | (10 + 20 + ... + 100) = **550 Points** |
| **Final Boss Bonus (Victory Only)** | Awarded if the run ends in Victory (Level 10 cleared). | **+200 Points** |

*Formula for Base Points:* $\sum_{N=1}^{Levels Completed} (N \times 10) + (\text{200 if Victory})$

#### 2. Calculation of Additional Bonuses

These bonuses are calculated and added directly to the Base Points.

| Bonus | Condition | Points Awarded | Logic / Motivation |
| :--- | :--- | :--- | :--- |
| **Inventory Items** | For every consumable item remaining in the inventory upon run end. 
| **+20 Points** / item | Rewards prudent resource management. |

| **Zero Error Levels** | For every level completed without committing any errors. 
| **+30 Points** / level | Rewards precision and concentration. |

| **Total Errors** | For every error committed during the entire run. 
| **+5 Points** / error | Provides a small reward for perseverance, ensuring players don't feel penalized for every mistake. |

| **Run Without Buff**| If the user had no **Permanent Buffs** active (all levels were 0). 
| **+50 Points** (Non-stackable) | Rewards "Hard Mode" difficulty. This bonus is mutually exclusive with the **POINT_BONUS** buff. |

#### 3. Final Multiplier Application

The final total score (Base Points + Bonuses) is subject to a percentage increase if the user has unlocked the **POINT_BONUS** permanent buff.

| Multiplier Source | Condition | Multiplier | Interaction |
| :--- | :--- | :--- | :--- |
| **POINT_BONUS** | User's frozen buff level (1, 2, or 3). | +10%, +20%, or +33% | Increase the total score by the corresponding percentage. |

---

