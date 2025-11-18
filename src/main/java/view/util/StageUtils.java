package view.util;

import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public final class StageUtils {
    private StageUtils() {}

    public static void enforceFullscreen(Stage stage) {
        if (stage == null) return;
        stage.setFullScreenExitHint("");
        stage.setFullScreenExitKeyCombination(javafx.scene.input.KeyCombination.NO_MATCH);
        if (!stage.isFullScreen()) {
            stage.setFullScreen(true);
        }
    }

    public static void setSceneRoot(Stage stage, Parent root) {
        if (stage == null || root == null) return;
        Scene scene = stage.getScene();
        if (scene != null) {
            scene.setRoot(root);
        } else {
            stage.setScene(new Scene(root));
        }
    }
}