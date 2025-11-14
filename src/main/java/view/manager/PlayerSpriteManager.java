package view.manager;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class PlayerSpriteManager {

    public void applyTo(ImageView view, Image image, String characterId) {
        if (view == null || image == null) return;
        view.setImage(image);
        view.setPreserveRatio(true);
        view.setSmooth(true);

        double fit;
        if (characterId != null && characterId.equals("OCCULTIST")) {
            fit = 650.0;
        } else {
            fit = 500.0;
        }
        view.setFitHeight(fit);
        view.setVisible(true);
    }
}