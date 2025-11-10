package view.manager;

import javafx.scene.image.Image;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Gestisce lo sfondo di gioco.
 */
public class BackgroundManager {

    private static final Logger LOG = Logger.getLogger(BackgroundManager.class.getName());

    private final Map<String, Image> cacheLevels = new HashMap<>();
    private final Map<String, Image> cacheBoss = new HashMap<>();
    private final Set<String> usedLevels = new HashSet<>();
    private final Set<String> usedBoss = new HashSet<>();
    private final Random rng = new Random();

    public void preloadAll() {
        try {
            List<String> levelPaths = listBackgroundFiles("/assets/backgrounds/levels");
            List<String> bossPaths = listBackgroundFiles("/assets/backgrounds/boss");
            int loaded = 0;

            for (String p : levelPaths) {
                if (!cacheLevels.containsKey(p)) {
                    cacheLevels.put(p, new Image(getClass().getResourceAsStream(p), 0, 0, false, true));
                    loaded++;
                }
            }
            for (String p : bossPaths) {
                if (!cacheBoss.containsKey(p)) {
                    cacheBoss.put(p, new Image(getClass().getResourceAsStream(p), 0, 0, false, true));
                    loaded++;
                }
            }
            LOG.log(Level.INFO, "Background preload completato: {0} immagini (levels={1}, boss={2})",
                    new Object[]{loaded, cacheLevels.size(), cacheBoss.size()});
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Errore nel precaricamento dei background: {0}", e.getMessage());
        }
    }

    public Image selectRandomUnique(boolean boss) {
        Map<String, Image> cache = boss ? cacheBoss : cacheLevels;
        Set<String> used = boss ? usedBoss : usedLevels;

        List<String> all = new ArrayList<>(cache.keySet());
        List<String> available = all.stream().filter(p -> !used.contains(p)).collect(Collectors.toList());

        if (available.isEmpty()) {
            LOG.log(Level.SEVERE, "Pool di background esaurito ({0}). Fallback alla selezione con possibili ripetizioni.", boss ? "boss" : "levels");
            if (all.isEmpty()) return null;
            String pick = all.get(rng.nextInt(all.size()));
            return cache.get(pick);
        }

        String pick = available.get(rng.nextInt(available.size()));
        used.add(pick);
        return cache.get(pick);
    }

    public void resetRun() {
        usedLevels.clear();
        usedBoss.clear();
        LOG.log(Level.INFO, "BackgroundManager reset: pool usati azzerati");
    }

    public void clearCache() {
        cacheLevels.clear();
        cacheBoss.clear();
        LOG.log(Level.INFO, "BackgroundManager cache svuotata");
    }

    private List<String> listBackgroundFiles(String basePath) {
        try {
            URL url = getClass().getResource(basePath);
            if (url == null) return Collections.emptyList();
            Path dir = Paths.get(url.toURI());
            try (Stream<Path> s = Files.list(dir)) {
                return s.filter(Files::isRegularFile)
                        .map(p -> basePath + "/" + p.getFileName().toString())
                        .collect(Collectors.toList());
            }
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Errore nel listare {0}: {1}", new Object[]{basePath, e.getMessage()});
            return Collections.emptyList();
        }
    }
}