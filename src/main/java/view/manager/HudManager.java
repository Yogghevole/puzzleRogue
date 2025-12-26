package view.manager;

import javafx.animation.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import model.db.DatabaseManager;
import model.service.GameDataService;

import java.util.Locale;
import java.util.Map;

/**
 * Manages the Heads-Up Display (HUD) elements like level, difficulty, lives, and buffs.
 * Handles updates and animations for these UI components.
 */
public class HudManager {

    public void hideGameUIForSelection(boolean hide,
                                       Label levelLabel,
                                       Label difficultyLabel,
                                       ImageView characterSpriteView,
                                       ImageView enemySpriteView,
                                       GridPane sudokuGridContainer,
                                       HBox livesHBox,
                                       HBox inputControlHBox,
                                       HBox inventorySlotsHBox,
                                       Button skipButton) {
        if (levelLabel != null) levelLabel.setVisible(!hide);
        if (difficultyLabel != null) difficultyLabel.setVisible(!hide);
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

    public void updateSkipButtonState(Button skipButton, boolean isBossLevel, boolean characterSelected) {
        if (skipButton == null) return;
        boolean enabled = characterSelected;
        skipButton.setDisable(!enabled);
        skipButton.setVisible(true);
    }

    public void renderSelectedBuffs(HBox selectedBuffsHBox) {
        try {
            String nick = model.service.SessionService.getCurrentNick();
            if (nick == null || nick.isEmpty() || selectedBuffsHBox == null) return;
            var user = new model.dao.UserDAO(DatabaseManager.getInstance()).getUserByNick(nick);
            if (user == null) return;
            Map<String, Integer> buffs = user.getPermanentBuffLevels();
            selectedBuffsHBox.getChildren().clear();
            for (Map.Entry<String, Integer> e : buffs.entrySet()) {
                if (e.getValue() != null && e.getValue() > 0) {
                    String path = mapBuffIcon(e.getKey());
                    if (path != null) {
                        ImageView badge = new ImageView(new Image(getClass().getResourceAsStream(path), 28, 28, true, true));
                        selectedBuffsHBox.getChildren().add(badge);
                    }
                }
            }
        } catch (Exception ignore) {}
    }

    private String mapBuffIcon(String id) {
        if (id == null) return null;
        switch (id) {
            case "EXTRA_LIVES": return "/assets/icons/buffs/extra_lives.png";
            case "FIRST_ERROR_PROTECT": return "/assets/icons/buffs/first_error_protection.png";
            case "POINT_BONUS": return "/assets/icons/buffs/point_bonus.png";
            case "INVENTORY_CAPACITY": return "/assets/icons/buffs/inventory_capacity.png";
            case "STARTING_CELLS": return "/assets/icons/buffs/starting_cells.png";
            default: return null;
        }
    }

    public void renderBuffInfo(VBox buffInfoBox, GameDataService gameDataService) {
        try {
            if (buffInfoBox == null) return;
            String id = model.service.SessionService.getLastSelectedBuff();
            buffInfoBox.getChildren().clear();
            if (id == null || id.isEmpty()) return;
            javafx.scene.control.Label name = new javafx.scene.control.Label(id.replace('_',' '));
            name.getStyleClass().add("heading-small");
            int level = 1;
            try {
                String nick = model.service.SessionService.getCurrentNick();
                if (nick != null) {
                    var user = new model.dao.UserDAO(DatabaseManager.getInstance()).getUserByNick(nick);
                    if (user != null) level = user.getBuffLevel(id);
                }
            } catch (Exception ignore) {}
            double value = new GameDataService(DatabaseManager.getInstance())
                    .getBuffLevelData(id, Math.max(level,1))
                    .getOrDefault("value", 0).doubleValue();
            javafx.scene.control.Label desc = new javafx.scene.control.Label("Level " + Math.max(level,1) + " • Value " + value);
            desc.getStyleClass().add("hint-label");
            buffInfoBox.getChildren().addAll(name, desc);
        } catch (Exception ignore) {}
    }

    public String applyThemeForCharacter(StackPane mainGameArea, String characterId) {
        if (mainGameArea == null) return null;
        mainGameArea.getStyleClass().remove("theme-crusader");
        mainGameArea.getStyleClass().remove("theme-highwayman");
        mainGameArea.getStyleClass().remove("theme-jester");
        mainGameArea.getStyleClass().remove("theme-occultist");
        mainGameArea.getStyleClass().remove("theme-plague-doctor");

        String toAdd = null;
        if ("CRUSADER".equalsIgnoreCase(characterId)) {
            toAdd = "theme-crusader";
        } else if ("HIGHWAYMAN".equalsIgnoreCase(characterId)) {
            toAdd = "theme-highwayman";
        } else if ("JESTER".equalsIgnoreCase(characterId)) {
            toAdd = "theme-jester";
        } else if ("OCCULTIST".equalsIgnoreCase(characterId)) {
            toAdd = "theme-occultist";
        } else if ("PLAGUEDOCTOR".equalsIgnoreCase(characterId) || "PLAGUE_DOCTOR".equalsIgnoreCase(characterId)) {
            toAdd = "theme-plague-doctor";
        }
        if (toAdd != null) {
            mainGameArea.getStyleClass().add(toAdd);
        }
        return toAdd;
    }

    public void setupSkipButtonHoverEffects(Button skipButton) {
        if (skipButton == null) return;
        skipButton.setOnMouseEntered(e -> {
            if (skipButton.getGraphic() instanceof ImageView) {
                ImageView iv = (ImageView) skipButton.getGraphic();
                javafx.scene.effect.Glow glow = new javafx.scene.effect.Glow(0.35);
                javafx.scene.effect.DropShadow shadow = new javafx.scene.effect.DropShadow();
                shadow.setRadius(6.0);
                shadow.setSpread(0.1);
                shadow.setColor(javafx.scene.paint.Color.web("#ffffff88"));
                iv.setEffect(new javafx.scene.effect.Blend(
                    javafx.scene.effect.BlendMode.SRC_OVER,
                    glow,
                    shadow
                ));
            }
        });
        skipButton.setOnMouseExited(e -> {
            if (skipButton.getGraphic() instanceof ImageView) {
                ImageView iv = (ImageView) skipButton.getGraphic();
                iv.setEffect(null);
            }
        });
    }
}