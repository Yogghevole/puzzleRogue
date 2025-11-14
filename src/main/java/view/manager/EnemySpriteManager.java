package view.manager;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class EnemySpriteManager {

    public void applyTo(ImageView view, Image image, String enemySpritePath) {
        if (view == null || image == null) {
            return;
        }

        view.setImage(image);
        view.setPreserveRatio(true);
        view.setSmooth(true);

        String difficultyDir = extractDifficulty(enemySpritePath);
        String fileNameNoExt = extractFileNameNoExt(enemySpritePath);

        double naturalH = image.getHeight();
        double desiredFit = computeDesiredFitHeight(naturalH, difficultyDir, fileNameNoExt);

        if ("swine_prince".equals(fileNameNoExt)) {
            desiredFit *= 0.8;
        }

        view.setFitHeight(desiredFit);
        view.setVisible(true);

        view.setScaleX(-1);

        double shift = computeHorizontalShift(difficultyDir);
        view.setTranslateX(shift);
    }

    private double computeDesiredFitHeight(double naturalH, String difficultyDir, String fileNameNoExt) {
        double base;

        if ("easy".equalsIgnoreCase(difficultyDir)) {
            base = 450.0; 
            if ("drowned_pirate".equals(fileNameNoExt)) {
                base = 650.0;
            }
        } else if ("medium".equalsIgnoreCase(difficultyDir)) {
            base = 525.0; 
            if ("fishman_shaman".equals(fileNameNoExt)) {
                base = 700.0;
            }
        } else if ("hard".equalsIgnoreCase(difficultyDir)) {
            base = 625.0; 
            if ("errant_flesh_bat".equals(fileNameNoExt)) {
                base = 725.0; 
            }
        } else if ("expert".equalsIgnoreCase(difficultyDir)) {
            base = 700.0; 
            if ("prophet".equals(fileNameNoExt)) {
                base = 750.0; 
            }
            if ("ghoul".equals(fileNameNoExt)) {
                base = 650.0; 
            }
        } else if ("nightmare".equalsIgnoreCase(difficultyDir)) {
            base = 800.0;
            if ("necromancer".equals(fileNameNoExt)) {
                base = 875.0; 
            }
        } else {
            base = 600.0;
        }

        return Math.min(naturalH, base);
    }

    private double computeHorizontalShift(String difficultyDir) {
        if ("easy".equalsIgnoreCase(difficultyDir)) {
            return -20.0;
        }
        if ("nightmare".equalsIgnoreCase(difficultyDir)) {
            return 50.0;
        }
        return 20.0;
    }

    private String extractDifficulty(String path) {
        if (path == null) return "";
        String p = path.replace('\\', '/');
        String[] parts = p.split("/");
        for (int i = parts.length - 1; i >= 0; i--) {
            String s = parts[i].toLowerCase();
            if (s.equals("easy") || s.equals("medium") || s.equals("hard") || s.equals("expert") || s.equals("nightmare")) {
                return s;
            }
        }
        return "";
    }

    private String extractFileNameNoExt(String path) {
        if (path == null) return "";
        String p = path.replace('\\', '/');
        int lastSlash = p.lastIndexOf('/');
        String file = (lastSlash >= 0) ? p.substring(lastSlash + 1) : p;
        int dot = file.lastIndexOf('.');
        return (dot >= 0) ? file.substring(0, dot) : file;
    }
}