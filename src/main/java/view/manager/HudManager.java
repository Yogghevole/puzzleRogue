package view.manager;

import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.SequentialTransition;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.util.Duration;
import java.util.Locale;

public class HudManager {

    public void hideGameUIForSelection(boolean hide,
                                       Label levelLabel,
                                       Label difficultyLabel,
                                       Button menuButton,
                                       ImageView characterSpriteView,
                                       ImageView enemySpriteView,
                                       GridPane sudokuGridContainer,
                                       HBox livesHBox,
                                       HBox inputControlHBox,
                                       HBox inventorySlotsHBox,
                                       Button skipButton) {
        if (levelLabel != null) levelLabel.setVisible(!hide);
        if (difficultyLabel != null) difficultyLabel.setVisible(!hide);
        if (menuButton != null) menuButton.setVisible(!hide);
        if (characterSpriteView != null) characterSpriteView.setVisible(!hide);
        if (enemySpriteView != null) enemySpriteView.setVisible(!hide);
        if (sudokuGridContainer != null) sudokuGridContainer.setVisible(!hide);
        if (livesHBox != null) livesHBox.setVisible(!hide);
        if (inputControlHBox != null) inputControlHBox.setVisible(!hide);
        if (inventorySlotsHBox != null) inventorySlotsHBox.setVisible(!hide);
        if (skipButton != null) skipButton.setVisible(!hide);
    }

    public void updateLevelAndDifficultyUI(Label levelLabel,
                                           Label difficultyLabel,
                                           int currentLevel,
                                           String difficultyText) {
        if (levelLabel != null) {
        levelLabel.setText("Level " + currentLevel);
            applyBannerStyle(levelLabel, true);
            animateBanner(levelLabel);
        }
        if (difficultyLabel != null) {
            String text = difficultyText != null ? difficultyText.toUpperCase(Locale.ITALIAN) : "";
            difficultyLabel.setText(text);
            applyBannerStyle(difficultyLabel, false);
            animateBanner(difficultyLabel);
        }
    }

    private void applyBannerStyle(Label label, boolean isLevel) {
        if (label == null) return;
        label.getStyleClass().removeAll("hud-level-banner", "hud-difficulty-banner", "level-label", "difficulty-label");
        label.getStyleClass().add(isLevel ? "hud-level-banner" : "hud-difficulty-banner");
    }

    private void animateBanner(Label label) {
        if (label == null) return;
        FadeTransition fade = new FadeTransition(Duration.millis(180), label);
        fade.setFromValue(0.0);
        fade.setToValue(1.0);

        ScaleTransition pulse = new ScaleTransition(Duration.millis(140), label);
        pulse.setFromX(0.96);
        pulse.setFromY(0.96);
        pulse.setToX(1.0);
        pulse.setToY(1.0);

        SequentialTransition seq = new SequentialTransition(fade, pulse);
        seq.play();
    }
}