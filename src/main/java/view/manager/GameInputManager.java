package view.manager;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;

import java.util.function.IntConsumer;

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
            numButton.setMinSize(65, 65);
            numButton.setPrefSize(65, 65);
            numButton.setMaxSize(65, 65);

            ImageView iv = new ImageView(new Image(
                getClass().getResourceAsStream("/assets/numbers/" + i + ".png")
            ));
            iv.setFitWidth(65);
            iv.setFitHeight(65);
            iv.setPreserveRatio(true);
            iv.setSmooth(false);
            numButton.setGraphic(iv);

            final int value = i;
            numButton.setOnAction(e -> onNumberInput.accept(value));
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
        pencilButton.setMinSize(65, 65);
        pencilButton.setPrefSize(65, 65);
        pencilButton.setMaxSize(65, 65);

        ImageView pencilIv = new ImageView(new Image(
            getClass().getResourceAsStream("/assets/icons/utils/pencil.png")
        ));
        pencilIv.setFitWidth(65);
        pencilIv.setFitHeight(65);
        pencilIv.setPreserveRatio(true);
        pencilIv.setSmooth(false);

        pencilIndicatorLabel = new Label("OFF");
        pencilIndicatorLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: #FFCC00; -fx-background-color: rgba(0,0,0,0.4); -fx-padding: 0 2 0 2;");

        StackPane pencilPane = new StackPane(pencilIv, pencilIndicatorLabel);
        StackPane.setAlignment(pencilIndicatorLabel, Pos.TOP_RIGHT);
        pencilButton.setGraphic(pencilPane);
        pencilButton.setOnAction(e -> onToggleNoteMode.run());
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
        eraserButton.setMinSize(65, 65);
        eraserButton.setPrefSize(65, 65);
        eraserButton.setMaxSize(65, 65);

        ImageView eraserIv = new ImageView(new Image(
            getClass().getResourceAsStream("/assets/icons/utils/eraser.png")
        ));
        eraserIv.setFitWidth(65);
        eraserIv.setFitHeight(65);
        eraserIv.setPreserveRatio(true);
        eraserIv.setSmooth(false);
        eraserButton.setGraphic(eraserIv);
        eraserButton.setOnAction(e -> onClearCell.run());
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
}