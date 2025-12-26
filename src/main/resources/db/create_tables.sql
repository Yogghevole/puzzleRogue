-- Stores user profiles and their global statistics.
CREATE TABLE IF NOT EXISTS User (
    nick TEXT PRIMARY KEY,
    current_run_id INTEGER,
    points_available INTEGER DEFAULT 0,
    points_total INTEGER DEFAULT 0,
    runs_completed INTEGER DEFAULT 0,
    runs_won INTEGER DEFAULT 0
);

-- Represents a single game session (run) for a user.
CREATE TABLE IF NOT EXISTS Run (
    run_id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_nick TEXT NOT NULL,
    lives_remaining INTEGER NOT NULL,
    character_selected TEXT NOT NULL,
    total_errors INTEGER DEFAULT 0,
    levels_completed INTEGER DEFAULT 0,
    zero_error_levels INTEGER DEFAULT 0,
    score_item_points INTEGER DEFAULT 0,
    used_enemies TEXT,
    
    FOREIGN KEY (user_nick) REFERENCES User(nick) ON DELETE CASCADE
);

-- Tracks the state of the current level within an active run.
CREATE TABLE IF NOT EXISTS Run_Level_State (
    run_id INTEGER PRIMARY KEY, 
    current_level INTEGER NOT NULL,
    enemy_sprite_id TEXT,
    difficulty_tier TEXT,
    initial_grid TEXT,
    solved_grid TEXT,
    user_grid TEXT,
    notes_data TEXT,
    errors_in_level INTEGER DEFAULT 0,
    protection_used BOOLEAN DEFAULT FALSE,
    bonus_cells_data TEXT,
    background_id TEXT,
    
    FOREIGN KEY (run_id) REFERENCES Run(run_id) ON DELETE CASCADE
);

-- Maps users to their unlocked buffs and current upgrade levels.
CREATE TABLE IF NOT EXISTS User_Buffs (
    user_nick TEXT NOT NULL,
    buff_id TEXT NOT NULL,
    buff_level INTEGER DEFAULT 0,
    
    PRIMARY KEY (user_nick, buff_id),
    FOREIGN KEY (user_nick) REFERENCES User(nick) ON DELETE CASCADE,
    FOREIGN KEY (buff_id) REFERENCES Game_Buff_Definition(buff_id) ON DELETE CASCADE
);

-- Snapshots the buffs active for a specific run (prevents changes during a run).
CREATE TABLE IF NOT EXISTS Run_Frozen_Buffs (
    run_id INTEGER NOT NULL,
    buff_id TEXT NOT NULL,
    buff_level INTEGER NOT NULL,
    
    PRIMARY KEY (run_id, buff_id),
    FOREIGN KEY (run_id) REFERENCES Run(run_id) ON DELETE CASCADE,
    FOREIGN KEY (buff_id) REFERENCES Game_Buff_Definition(buff_id) ON DELETE CASCADE
);

-- Manages the inventory capacity for specific item types in a run.
CREATE TABLE IF NOT EXISTS Run_Inventory (
    run_id INTEGER NOT NULL,
    item_type_id TEXT NOT NULL,
    capacity INTEGER DEFAULT 1,
    
    PRIMARY KEY (run_id, item_type_id),
    FOREIGN KEY (run_id) REFERENCES Run(run_id) ON DELETE CASCADE,
    FOREIGN KEY (item_type_id) REFERENCES Item_Definition(item_id) ON DELETE CASCADE
);

-- Defines the base properties of available game buffs.
CREATE TABLE IF NOT EXISTS Game_Buff_Definition (
    buff_id TEXT PRIMARY KEY,
    name TEXT NOT NULL,
    description TEXT,
    max_level INTEGER NOT NULL
);

-- Defines the cost and effect values for each level of a buff.
CREATE TABLE IF NOT EXISTS Buff_Level_Cost (
    buff_id TEXT NOT NULL,
    level INTEGER NOT NULL,
    cost_points INTEGER NOT NULL,
    effect_value REAL,
    
    PRIMARY KEY (buff_id, level),
    FOREIGN KEY (buff_id) REFERENCES Game_Buff_Definition(buff_id) ON DELETE CASCADE
);

-- Defines the base properties of available game items.
CREATE TABLE IF NOT EXISTS Item_Definition (
    item_id TEXT PRIMARY KEY,
    name TEXT NOT NULL,
    description TEXT,
    usage_type TEXT,
    slot_cost INTEGER DEFAULT 1
);

-- Defines the playable characters and their attributes.
CREATE TABLE IF NOT EXISTS Character_Definition (
    char_id TEXT PRIMARY KEY,
    name TEXT NOT NULL,
    sprite_path TEXT,
    base_lives INTEGER DEFAULT 3
);

-- Defines the configuration for each game level.
CREATE TABLE IF NOT EXISTS Level_Definition (
    level_number INTEGER PRIMARY KEY,
    base_difficulty TEXT NOT NULL,
    is_boss_level BOOLEAN DEFAULT FALSE
);