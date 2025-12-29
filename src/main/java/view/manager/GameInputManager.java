package view.manager;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;

import java.util.function.IntConsumer;

/**
 * Manages the game input controls (number buttons, pencil, eraser) and keyboard shortcuts.
 * Handles both mouse interactions with the UI and keyboard events.
 */
public class GameInputManager {

    private final Button[] numberButtons = new Button[10];
    private Button pencilButton;
    private Label pencilIndicatorLabel;
    private Button eraserButton;

    public void build(HBox inputControlHBox,
                      IntConsumer onNumberInput,
                      Runnable onToggleNoteMode,
                      Runnable onClearCell) {
        if (inputControlHBox == null) return;
        inputControlHBox.setSpacing(10.0);

        for (int i = 1; i <= 9; i++) {
            Button numButton = new Button();
            numButton.getStyleClass().add("input-number-button");
            numButton.setPrefSize(70, 70);

            ImageView iv = new ImageView(new Image(
                getClass().getResourceAsStream("/assets/numbers/" + i + ".png")
            ));
            iv.setFitWidth(70);
            iv.setFitHeight(70);
            iv.setPreserveRatio(true);
            iv.setSmooth(false);
            numButton.setGraphic(iv);

            final int value = i;
            numButton.setOnAction(e -> { try { view.manager.SoundManager.getInstance().playNumberClick(); } catch (Exception ignore) {} onNumberInput.accept(value); });
            numButton.setOnMouseEntered(e -> {
                javafx.scene.effect.Glow glow = new javafx.scene.effect.Glow(0.6);
                javafx.scene.effect.DropShadow shadow = new javafx.scene.effect.DropShadow();
                shadow.setRadius(9.0);
                shadow.setSpread(0.2);
                shadow.setColor(javafx.scene.paint.Color.web("#ffffffaa"));
                iv.setEffect(new javafx.scene.effect.Blend(
                    javafx.scene.effect.BlendMode.SRC_OVER,
                    glow,
                    shadow
                ));
            });
            numButton.setOnMouseExited(e -> iv.setEffect(null));
            inputControlHBox.getChildren().add(numButton);
            numberButtons[i] = numButton;
        }

        pencilButton = new Button();
        pencilButton.getStyleClass().add("input-number-button");
        pencilButton.setPrefSize(70, 70);

        ImageView pencilIv = new ImageView(new Image(
            getClass().getResourceAsStream("/assets/icons/utils/pencil.png")
        ));
        pencilIv.setFitWidth(70);
        pencilIv.setFitHeight(70);
        pencilIv.setPreserveRatio(true);
        pencilIv.setSmooth(false);

        pencilIndicatorLabel = new Label("OFF");
        pencilIndicatorLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: #FFCC00; -fx-background-color: rgba(0,0,0,0.4); -fx-padding: 0 2 0 2;");

        StackPane pencilPane = new StackPane(pencilIv, pencilIndicatorLabel);
        StackPane.setAlignment(pencilIndicatorLabel, Pos.TOP_RIGHT);
        pencilButton.setGraphic(pencilPane);
        pencilButton.setOnAction(e -> { try { view.manager.SoundManager.getInstance().playNumberClick(); } catch (Exception ignore) {} onToggleNoteMode.run(); });
        pencilButton.setOnMouseEntered(e -> {
            javafx.scene.effect.Glow glow = new javafx.scene.effect.Glow(0.6);
            javafx.scene.effect.DropShadow shadow = new javafx.scene.effect.DropShadow();
            shadow.setRadius(9.0);
            shadow.setSpread(0.2);
            shadow.setColor(javafx.scene.paint.Color.web("#ffffffaa"));
            pencilIv.setEffect(new javafx.scene.effect.Blend(
                javafx.scene.effect.BlendMode.SRC_OVER,
                glow,
                shadow
            ));
        });
        pencilButton.setOnMouseExited(e -> pencilIv.setEffect(null));

        HBox.setMargin(pencilButton, new Insets(0, 0, 0, 40));
        inputControlHBox.getChildren().add(pencilButton);

        eraserButton = new Button();
        eraserButton.getStyleClass().add("input-number-button");
        eraserButton.setPrefSize(70, 70);

        ImageView eraserIv = new ImageView(new Image(
            getClass().getResourceAsStream("/assets/icons/utils/eraser.png")
        ));
        eraserIv.setFitWidth(70);
        eraserIv.setFitHeight(70);
        eraserIv.setPreserveRatio(true);
        eraserIv.setSmooth(false);
        eraserButton.setGraphic(eraserIv);
        eraserButton.setOnAction(e -> { try { view.manager.SoundManager.getInstance().playNumberClick(); } catch (Exception ignore) {} onClearCell.run(); });
        eraserButton.setOnMouseEntered(e -> {
            javafx.scene.effect.Glow glow = new javafx.scene.effect.Glow(0.6);
            javafx.scene.effect.DropShadow shadow = new javafx.scene.effect.DropShadow();
            shadow.setRadius(9.0);
            shadow.setSpread(0.2);
            shadow.setColor(javafx.scene.paint.Color.web("#ffffffaa"));
            eraserIv.setEffect(new javafx.scene.effect.Blend(
                javafx.scene.effect.BlendMode.SRC_OVER,
                glow,
                shadow
            ));
        });
        eraserButton.setOnMouseExited(e -> eraserIv.setEffect(null));

        inputControlHBox.getChildren().add(eraserButton);
    }

    public void setNumberEnabled(int value, boolean enabled) {
        if (value < 1 || value > 9) return;
        Button b = numberButtons[value];
        if (b == null) return;
        b.setDisable(!enabled);
        b.setOpacity(enabled ? 1.0 : 0.5);
    }

    public void setNoteModeActive(boolean active) {
        if (pencilIndicatorLabel != null) {
            pencilIndicatorLabel.setText(active ? "ON" : "OFF");
        }
        if (pencilButton != null) {
            pencilButton.setOpacity(active ? 1.0 : 0.9);
            if (active) {
                if (!pencilButton.getStyleClass().contains("note-mode-active")) {
                    pencilButton.getStyleClass().add("note-mode-active");
                }
            } else {
                pencilButton.getStyleClass().remove("note-mode-active");
            }
        }
    }

    public void setInteractivity(boolean enabled) {
        if (pencilButton != null) {
            pencilButton.setMouseTransparent(!enabled);
        }
        if (eraserButton != null) {
            eraserButton.setMouseTransparent(!enabled);
        }
    }

    public void setupKeyboardShortcuts(StackPane mainGameArea,
                                       IntConsumer onNumberInput,
                                       Runnable onToggleNoteMode,
                                       Runnable onClearCell) {
        if (mainGameArea == null) return;
        Runnable attach = () -> {
            Scene scene = mainGameArea.getScene();
            if (scene == null) return;
            mainGameArea.setFocusTraversable(true);
            mainGameArea.requestFocus();
            scene.addEventFilter(KeyEvent.KEY_PRESSED, e -> {
                if (e.getTarget() instanceof javafx.scene.control.TextInputControl) {
                    return;
                }
                int value = mapKeyToDigit(e.getCode());
                if (value >= 1 && value <= 9) {
                    onNumberInput.accept(value);
                    e.consume();
                    return;
                }
                if (e.getCode() == KeyCode.N) {
                    onToggleNoteMode.run();
                    e.consume();
                    return;
                }
                if (e.getCode() == KeyCode.BACK_SPACE || e.getCode() == KeyCode.DELETE
                        || e.getCode() == KeyCode.DIGIT0 || e.getCode() == KeyCode.NUMPAD0) {
                    onClearCell.run();
                    e.consume();
                }
            });
        };
        if (mainGameArea.getScene() != null) {
            attach.run();
        } else {
            mainGameArea.sceneProperty().addListener((obs, oldScene, newScene) -> {
                if (newScene != null) attach.run();
            });
        }
    }

    private int mapKeyToDigit(KeyCode code) {
        switch (code) {
            case DIGIT1: case NUMPAD1: return 1;
            case DIGIT2: case NUMPAD2: return 2;
            case DIGIT3: case NUMPAD3: return 3;
            case DIGIT4: case NUMPAD4: return 4;
            case DIGIT5: case NUMPAD5: return 5;
            case DIGIT6: case NUMPAD6: return 6;
            case DIGIT7: case NUMPAD7: return 7;
            case DIGIT8: case NUMPAD8: return 8;
            case DIGIT9: case NUMPAD9: return 9;
            default: return 0;
        }
    }

    public void refreshNumberButtonsAvailability(Label[][] cellLabels) {
        if (cellLabels == null) return;
        int[] counts = new int[10];
        int rows = cellLabels.length;
        for (int r = 0; r < rows; r++) {
            Label[] row = cellLabels[r];
            if (row == null) continue;
            for (int c = 0; c < row.length; c++) {
                Label lbl = row[c];
                if (lbl == null) continue;
                if (lbl.isVisible()) {
                    String t = lbl.getText();
                    if (t != null && !t.isEmpty()) {
                        try {
                            int v = Integer.parseInt(t);
                            if (v >= 1 && v <= 9) counts[v]++;
                        } catch (NumberFormatException ignore) { }
                    }
                }
            }
        }
        for (int i = 1; i <= 9; i++) {
            boolean enabled = counts[i] < 9;
            setNumberEnabled(i, enabled);
        }
    }
}