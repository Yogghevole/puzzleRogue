package view.util;

import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.animation.SequentialTransition;
import javafx.animation.TranslateTransition;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.util.Duration;

public final class ToastUtils {
    private ToastUtils() {}

    public static void showSudokuToast(StackPane sudokuAreaStack, String text, Color color) {
        if (sudokuAreaStack == null) return;
        try {
            Label toast = new Label(text);
            toast.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: white;");
            toast.setEffect(null);
            toast.setOpacity(0.0);
            StackPane.setAlignment(toast, Pos.TOP_CENTER);
            sudokuAreaStack.getChildren().add(toast);

            FadeTransition fadeIn = new FadeTransition(Duration.millis(200), toast);
            fadeIn.setToValue(1.0);

            TranslateTransition translateUp = new TranslateTransition(Duration.millis(1000), toast);
            translateUp.setFromY(40);
            translateUp.setToY(10);

            PauseTransition hold = new PauseTransition(Duration.millis(700));

            FadeTransition fadeOut = new FadeTransition(Duration.millis(950), toast);
            fadeOut.setFromValue(1.0);
            fadeOut.setToValue(0.0);

            SequentialTransition seq = new SequentialTransition(fadeIn, translateUp, hold, fadeOut);
            seq.setOnFinished(e -> sudokuAreaStack.getChildren().remove(toast));
            seq.play();
        } catch (Exception ignore) {}
    }
}