package view.manager;

import javafx.scene.ImageCursor;
import javafx.scene.image.Image;
import javafx.scene.layout.StackPane;

/**
 * Manages the application of custom cursors within the game.
 */
public class CursorManager {
    public void apply(StackPane mainGameArea) {
        if (mainGameArea == null) return;
        try {
            Image img = new Image(getClass().getResourceAsStream("/assets/icons/cursor/arrow.png"));
            ImageCursor cursor = new ImageCursor(img);
            if (mainGameArea.getScene() != null) {
                mainGameArea.getScene().setCursor(cursor);
            }
        } catch (Exception e) {
            System.err.println("Unable to set custom cursor: " + e.getMessage());
        }
    }
}