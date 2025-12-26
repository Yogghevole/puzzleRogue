package view.manager;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import model.service.AssetManifest;

/**
 * Manages background images for levels and boss fights, including caching and random selection.
 * Handles preloading of assets to ensure smooth transitions during gameplay.
 */
public class BackgroundManager {

    private static final Logger LOG = Logger.getLogger(BackgroundManager.class.getName());

    private final Map<String, Image> cacheLevels = new HashMap<>();
    private final Map<String, Image> cacheBoss = new HashMap<>();
    private final Set<String> usedLevels = new HashSet<>();
    private final Set<String> usedBoss = new HashSet<>();
    private final Random rng = new Random();
    private String lastSelectedPath;

    public void preloadAll() {
        try {
            List<String> levelPaths = AssetManifest.getBackgroundPaths(false);
            List<String> bossPaths = AssetManifest.getBackgroundPaths(true);
            int loaded = 0;

            for (String p : levelPaths) {
                if (!cacheLevels.containsKey(p)) {
                    try {
                        cacheLevels.put(p, new Image(getClass().getResourceAsStream(p), 0, 0, false, true));
                        loaded++;
                    } catch (Exception ex) {
                        LOG.log(Level.WARNING, "Failed to load background: {0}", p);
                    }
                }
            }
            for (String p : bossPaths) {
                if (!cacheBoss.containsKey(p)) {
                    try {
                        cacheBoss.put(p, new Image(getClass().getResourceAsStream(p), 0, 0, false, true));
                        loaded++;
                    } catch (Exception ex) {
                        LOG.log(Level.WARNING, "Failed to load boss background: {0}", p);
                    }
                }
            }
            LOG.log(Level.FINE, "Background preload completed: {0} images (levels={1}, boss={2})",
                    new Object[]{loaded, cacheLevels.size(), cacheBoss.size()});
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Error preloading backgrounds: {0}", e.getMessage());
        }
    }

    public Image selectRandomUnique(boolean boss) {
        Map<String, Image> cache = boss ? cacheBoss : cacheLevels;
        Set<String> used = boss ? usedBoss : usedLevels;

        List<String> all = new ArrayList<>(cache.keySet());
        List<String> available = all.stream().filter(p -> !used.contains(p)).collect(Collectors.toList());

        if (available.isEmpty()) {
            LOG.log(Level.SEVERE, "Background pool exhausted ({0}). Fallback to selection with possible repeats.", boss ? "boss" : "levels");
            if (all.isEmpty()) return null;
            String pick = all.get(rng.nextInt(all.size()));
            return cache.get(pick);
        }

        String pick = available.get(rng.nextInt(available.size()));
        used.add(pick);
        lastSelectedPath = pick;
        return cache.get(pick);
    }

    public void resetRun() {
        usedLevels.clear();
        usedBoss.clear();
        LOG.log(Level.FINE, "BackgroundManager reset: used pool cleared");
    }

    public void clearCache() {
        cacheLevels.clear();
        cacheBoss.clear();
        LOG.log(Level.FINE, "BackgroundManager cache cleared");
    }

    public void applyRandomForLevel(ImageView backgroundImageView, boolean boss) {
        if (backgroundImageView == null) return;
        Image img = selectRandomUnique(boss);
        if (img != null) {
            backgroundImageView.setImage(img);
        }
    }

    public String getLastSelectedCategory() {
        String p = (lastSelectedPath == null) ? "" : lastSelectedPath.toLowerCase();
        if (p.contains("/boss/")) return "boss";
        if (p.contains("cove")) return "cove";
        if (p.contains("warrens")) return "warrens";
        if (p.contains("weald")) return "weald";
        if (p.contains("crypts")) return "crypts";
        if (p.contains("town")) return "town";
        return "levels";
    }

    public String getLastSelectedPath() {
        return lastSelectedPath;
    }

    public boolean applyBackground(ImageView backgroundImageView, String path) {
        if (backgroundImageView == null || path == null) return false;
        
        if (!path.startsWith("/")) {
            path = "/" + path;
        }
        
        Image img = cacheLevels.get(path);
        if (img == null) img = cacheBoss.get(path);
        if (img == null) {
            try {
                java.io.InputStream is = getClass().getResourceAsStream(path);
                if (is != null) {
                    img = new Image(is, 0, 0, false, true);
                } else {
                     LOG.log(Level.WARNING, "Resource stream is null for path: {0}", path);
                }
            } catch (Exception e) {
                LOG.log(Level.WARNING, "Could not load background from path: {0}", path);
            }
        }
        if (img != null) {
            backgroundImageView.setImage(img);
            lastSelectedPath = path;
            return true;
        }
        return false;
    }
}
