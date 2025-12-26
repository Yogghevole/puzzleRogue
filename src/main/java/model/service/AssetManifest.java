package model.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Registry of static assets paths for enemies and backgrounds.
 */
public class AssetManifest {

    private static final Map<String, List<String>> ENEMIES = new HashMap<>();
    private static final List<String> BACKGROUNDS_LEVELS = new ArrayList<>();
    private static final List<String> BACKGROUNDS_BOSS = new ArrayList<>();

    static {
        ENEMIES.put("easy", Arrays.asList(
            "/assets/enemies/easy/bloated_corpse.png",
            "/assets/enemies/easy/brigand_hunter.png",
            "/assets/enemies/easy/carrion_eater.png",
            "/assets/enemies/easy/crone.png",
            "/assets/enemies/easy/drowned_pirate.png",
            "/assets/enemies/easy/skeleton.png"
        ));

        ENEMIES.put("medium", Arrays.asList(
            "/assets/enemies/medium/brigand_cutthroat.png",
            "/assets/enemies/medium/fishman_shaman.png",
            "/assets/enemies/medium/fungal_artillery.png",
            "/assets/enemies/medium/skeleton_courtier.png",
            "/assets/enemies/medium/skeleton_defender.png",
            "/assets/enemies/medium/swine_skiver.sprite.png"
        ));

        ENEMIES.put("hard", Arrays.asList(
            "/assets/enemies/hard/brigand_blood.png",
            "/assets/enemies/hard/cultist_harpy.png",
            "/assets/enemies/hard/errant_flesh_bat.png",
            "/assets/enemies/hard/gargoyle.png",
            "/assets/enemies/hard/siren.png"
        ));

        ENEMIES.put("expert", Arrays.asList(
            "/assets/enemies/expert/crow.png",
            "/assets/enemies/expert/ghoul.png",
            "/assets/enemies/expert/prophet.png",
            "/assets/enemies/expert/swinetaur.png",
            "/assets/enemies/expert/unclean_giant.png"
        ));

        ENEMIES.put("nightmare", Arrays.asList(
            "/assets/enemies/nightmare/collector.png",
            "/assets/enemies/nightmare/drowned_captain.png",
            "/assets/enemies/nightmare/necromancer.png",
            "/assets/enemies/nightmare/swine_prince.png"
        ));
        BACKGROUNDS_BOSS.addAll(Arrays.asList(
            "/assets/backgrounds/boss/darkestdungeon1.png",
            "/assets/backgrounds/boss/darkestdungeon2.png"
        ));
        for (int i = 1; i <= 5; i++) BACKGROUNDS_LEVELS.add("/assets/backgrounds/levels/cove" + i + ".png");
        for (int i = 1; i <= 5; i++) BACKGROUNDS_LEVELS.add("/assets/backgrounds/levels/crypts" + i + ".png");
        for (int i = 1; i <= 3; i++) BACKGROUNDS_LEVELS.add("/assets/backgrounds/levels/town" + i + ".png");
        for (int i = 1; i <= 5; i++) BACKGROUNDS_LEVELS.add("/assets/backgrounds/levels/warrens" + i + ".png");
        for (int i = 1; i <= 5; i++) BACKGROUNDS_LEVELS.add("/assets/backgrounds/levels/weald" + i + ".png");
    }

    public static List<String> getEnemyPaths(String difficultyTier) {
        if (difficultyTier == null) return Collections.emptyList();
        return ENEMIES.getOrDefault(difficultyTier.toLowerCase(), Collections.emptyList());
    }

    public static List<String> getBackgroundPaths(boolean boss) {
        return boss ? Collections.unmodifiableList(BACKGROUNDS_BOSS) : Collections.unmodifiableList(BACKGROUNDS_LEVELS);
    }
}
