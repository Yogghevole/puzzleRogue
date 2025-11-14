-- 1. UTENTE
CREATE TABLE IF NOT EXISTS User (
    nick TEXT PRIMARY KEY,
    current_run_id INTEGER,
    points_available INTEGER DEFAULT 0,
    points_total INTEGER DEFAULT 0,
    runs_completed INTEGER DEFAULT 0,
    runs_won INTEGER DEFAULT 0
);

-- 2. STATO CORRENTE DELLA RUN
CREATE TABLE IF NOT EXISTS Run (
    run_id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_nick TEXT NOT NULL,
    lives_remaining INTEGER NOT NULL,
    character_selected TEXT NOT NULL,
    total_errors INTEGER DEFAULT 0,
    
    FOREIGN KEY (user_nick) REFERENCES User(nick) ON DELETE CASCADE
);

-- 3. STATO LIVELLO CORRENTE
CREATE TABLE IF NOT EXISTS Run_Level_State (
    run_id INTEGER PRIMARY KEY, 
    current_level INTEGER NOT NULL,
    enemy_sprite_id TEXT,
    difficulty_tier TEXT,
    initial_grid TEXT,
    user_grid TEXT,
    notes_data TEXT,
    errors_in_level INTEGER DEFAULT 0,
    protection_used BOOLEAN DEFAULT FALSE,
    
    FOREIGN KEY (run_id) REFERENCES Run(run_id) ON DELETE CASCADE
);

-- 4. COLLEGAMENTO USER <-> BUFFS PERMANENTI
CREATE TABLE IF NOT EXISTS User_Buffs (
    user_nick TEXT NOT NULL,
    buff_id TEXT NOT NULL,
    buff_level INTEGER DEFAULT 0,
    
    PRIMARY KEY (user_nick, buff_id),
    FOREIGN KEY (user_nick) REFERENCES User(nick) ON DELETE CASCADE,
    FOREIGN KEY (buff_id) REFERENCES Game_Buff_Definition(buff_id) ON DELETE CASCADE
);

-- 5. BUFFS CONGELATI ALL'INIZIO DELLA RUN
CREATE TABLE IF NOT EXISTS Run_Frozen_Buffs (
    run_id INTEGER NOT NULL,
    buff_id TEXT NOT NULL,
    buff_level INTEGER NOT NULL,
    
    PRIMARY KEY (run_id, buff_id),
    FOREIGN KEY (run_id) REFERENCES Run(run_id) ON DELETE CASCADE,
    FOREIGN KEY (buff_id) REFERENCES Game_Buff_Definition(buff_id) ON DELETE CASCADE
);

-- 6. INVENTARIO DELLA RUN
CREATE TABLE IF NOT EXISTS Run_Inventory (
    run_id INTEGER NOT NULL,
    item_type_id TEXT NOT NULL,
    capacity INTEGER DEFAULT 1,
    
    PRIMARY KEY (run_id, item_type_id),
    FOREIGN KEY (run_id) REFERENCES Run(run_id) ON DELETE CASCADE,
    FOREIGN KEY (item_type_id) REFERENCES Item_Definition(item_id) ON DELETE CASCADE
);

-- 7. DEFINIZIONE BUFFS PERMANENTI
CREATE TABLE IF NOT EXISTS Game_Buff_Definition (
    buff_id TEXT PRIMARY KEY,
    name TEXT NOT NULL,
    description TEXT,
    max_level INTEGER NOT NULL
);

-- 8. COSTI E VALORI PER LIVELLO DEL BUFF 
CREATE TABLE IF NOT EXISTS Buff_Level_Cost (
    buff_id TEXT NOT NULL,
    level INTEGER NOT NULL,
    cost_points INTEGER NOT NULL,
    effect_value REAL,
    
    PRIMARY KEY (buff_id, level),
    FOREIGN KEY (buff_id) REFERENCES Game_Buff_Definition(buff_id) ON DELETE CASCADE
);

-- 9. DEFINIZIONE OGGETTI MONOUSO 
CREATE TABLE IF NOT EXISTS Item_Definition (
    item_id TEXT PRIMARY KEY,
    name TEXT NOT NULL,
    description TEXT,
    usage_type TEXT,
    slot_cost INTEGER DEFAULT 1
);

-- 10. DEFINIZIONE PERSONAGGI 
CREATE TABLE IF NOT EXISTS Character_Definition (
    char_id TEXT PRIMARY KEY,
    name TEXT NOT NULL,
    sprite_path TEXT,
    base_lives INTEGER DEFAULT 3
);

-- 11. DEFINIZIONE LIVELLI
CREATE TABLE IF NOT EXISTS Level_Definition (
    level_number INTEGER PRIMARY KEY,
    base_difficulty TEXT NOT NULL,
    is_boss_level BOOLEAN DEFAULT FALSE
);