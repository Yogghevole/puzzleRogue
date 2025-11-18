package view.util;

import javafx.scene.layout.StackPane;

public final class ModalUtils {
    private ModalUtils() {}

    public enum Type { DEFAULT, ENDGAME }

    public static void show(StackPane container, Type type) {
        if (container == null) return;
        container.getChildren().clear();
        container.getStyleClass().clear();
        switch (type) {
            case ENDGAME -> container.getStyleClass().add("endgame-overlay");
            case DEFAULT -> container.getStyleClass().add("modal-overlay");
        }
        container.setVisible(true);
        container.setPickOnBounds(true);
    }

    public static void hideAndClear(StackPane container) {
        if (container == null) return;
        container.getChildren().clear();
        container.getStyleClass().clear();
        container.setVisible(false);
    }
}