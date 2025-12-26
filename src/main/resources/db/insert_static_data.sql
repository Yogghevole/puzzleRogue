-- Insert base definitions for game buffs.
INSERT INTO Game_Buff_Definition (buff_id, name, description, max_level) VALUES
('EXTRA_LIVES', 'Additional Lives', 'Start your run with more lives.', 3),
('FIRST_ERROR_PROTECT', 'First Error Protection', 'Gain a shield against the first error in each level.', 1),
('STARTING_CELLS', 'Initial Cells', 'Start the Sudoku with more pre-filled cells.', 3),
('POINT_BONUS', 'Point Gain Multiplier', 'Increases final score gain.', 3),
('INVENTORY_CAPACITY', 'Inventory Capacity', 'Increases the maximum size of your inventory.', 3)
ON CONFLICT(buff_id) DO NOTHING;

-- Configure costs and effects for Extra Lives buff.
INSERT INTO Buff_Level_Cost (buff_id, level, cost_points, effect_value) VALUES
('EXTRA_LIVES', 1, 500, 1.0),
('EXTRA_LIVES', 2, 1200, 2.0),
('EXTRA_LIVES', 3, 2000, 3.0)
ON CONFLICT(buff_id, level) DO NOTHING;

-- Configure costs and effects for Point Bonus buff.
INSERT INTO Buff_Level_Cost (buff_id, level, cost_points, effect_value) VALUES
('POINT_BONUS', 1, 600, 0.10), 
('POINT_BONUS', 2, 1300, 0.20),
('POINT_BONUS', 3, 2100, 0.33)
ON CONFLICT(buff_id, level) DO UPDATE SET effect_value = excluded.effect_value;

-- Configure costs and effects for First Error Protection buff.
INSERT INTO Buff_Level_Cost (buff_id, level, cost_points, effect_value) VALUES
('FIRST_ERROR_PROTECT', 1, 2000, 1.0)
ON CONFLICT(buff_id, level) DO NOTHING;

-- Configure costs and effects for Inventory Capacity buff.
INSERT INTO Buff_Level_Cost (buff_id, level, cost_points, effect_value) VALUES
('INVENTORY_CAPACITY', 1, 600, 1.0),
('INVENTORY_CAPACITY', 2, 1600, 2.0),
('INVENTORY_CAPACITY', 3, 3200, 3.0)
ON CONFLICT(buff_id, level) DO NOTHING;

-- Configure costs and effects for Starting Cells buff.
INSERT INTO Buff_Level_Cost (buff_id, level, cost_points, effect_value) VALUES
('STARTING_CELLS', 1, 700, 1.0),
('STARTING_CELLS', 2, 1500, 2.0),
('STARTING_CELLS', 3, 2500, 3.0)
ON CONFLICT(buff_id, level) DO UPDATE SET effect_value = excluded.effect_value;

-- Insert definitions for consumable items.
INSERT INTO Item_Definition (item_id, name, description, usage_type, slot_cost) VALUES
('MISSING_HEART_ITEM', 'Missing Heart', 'Restores 1 life point.', 'MID_LEVEL', 1),
('HINT_ITEM', 'Insight Crystal', 'Reveals 1 correct number.', 'MID_LEVEL', 1),
('SCORE_ITEM', 'Coin Cache', 'Adds points immediately to the final score.', 'MID_LEVEL', 1),
('SACRIFICE_ITEM', 'Blood Offering', 'Sacrifice 1 life for 2 hints.', 'MID_LEVEL', 1)
ON CONFLICT(item_id) DO NOTHING;

-- Insert definitions for playable characters.
INSERT INTO Character_Definition (char_id, name, sprite_path, base_lives) VALUES
('CRUSADER', 'Crusader', '/assets/characters/crusader.png', 3),
('HIGHWAYMAN', 'Highwayman', '/assets/characters/highwayman.png', 3),
('JESTER', 'Jester', '/assets/characters/jester.png', 3),
('OCCULTIST', 'Occultist', '/assets/characters/occultist.png', 3),
('PLAGUEDOCTOR', 'Plaguedoctor', '/assets/characters/plague_doctor.png', 3)
ON CONFLICT(char_id) DO NOTHING;

-- Insert definitions for game levels.
INSERT INTO Level_Definition (level_number, base_difficulty, is_boss_level) VALUES
(1, 'EASY', false),
(2, 'EASY', false),
(3, 'MEDIUM', false),
(4, 'MEDIUM', false),
(5, 'HARD', false),
(6, 'HARD', false),
(7, 'HARD', false),
(8, 'EXPERT', false),
(9, 'EXPERT', false),
(10, 'NIGHTMARE', true)
ON CONFLICT(level_number) DO NOTHING;