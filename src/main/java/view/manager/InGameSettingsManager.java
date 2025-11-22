package view.manager;

import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.effect.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.Group;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.HBox;
import view.util.ModalUtils;

/**
 * Gestisce il modal delle impostazioni in-game.
 */
public class InGameSettingsManager {
    private final StackPane modalContainer;
    private final StackPane mainGameArea;
    private final Runnable onSaveAndExit;
    private final Runnable onWinLevel;
    private final Runnable onOpened;
    private final Runnable onClosed;

    public InGameSettingsManager(StackPane modalContainer,
                                 StackPane mainGameArea,
                                 Runnable onSaveAndExit,
                                 Runnable onWinLevel,
                                 Runnable onOpened,
                                 Runnable onClosed) {
        this.modalContainer = modalContainer;
        this.mainGameArea = mainGameArea;
        this.onSaveAndExit = onSaveAndExit;
        this.onWinLevel = onWinLevel;
        this.onOpened = onOpened;
        this.onClosed = onClosed;
    }

    public void show() {
        try {
            if (modalContainer == null || mainGameArea == null) return;
            ModalUtils.show(modalContainer, ModalUtils.Type.DEFAULT);
            try { view.manager.SoundManager.getInstance().playSettingsToggle(); } catch (Exception ignore) {}
            try {
                modalContainer.prefWidthProperty().unbind();
                modalContainer.prefHeightProperty().unbind();
            } catch (Exception ignore) {}
            modalContainer.prefWidthProperty().bind(mainGameArea.widthProperty());
            modalContainer.prefHeightProperty().bind(mainGameArea.heightProperty());
            modalContainer.setPickOnBounds(true);
            try { modalContainer.setMouseTransparent(false); } catch (Exception ignore) {}
            try { if (onOpened != null) onOpened.run(); } catch (Exception ignore) {}

            StackPane panel = new StackPane();
            panel.setPickOnBounds(true);
            try {
                panel.prefWidthProperty().bind(mainGameArea.widthProperty());
                panel.prefHeightProperty().bind(mainGameArea.heightProperty());
            } catch (Exception ignore) {}

            Image bg = new Image(getClass().getResourceAsStream("/assets/icons/help/help.background.png"));
            ImageView bgView = new ImageView(bg);
            bgView.setPreserveRatio(true);
            double bgW = bg.getWidth() * 0.8;
            double bgH = bg.getHeight() * 0.8;
            bgView.setFitWidth(bgW);
            bgView.setFitHeight(bgH);

            Group bgGroup = new Group();
            bgGroup.getChildren().add(bgView);

            Image closeImg = new Image(getClass().getResourceAsStream("/assets/icons/utils/x.png"));
            ImageView closeIcon = new ImageView(closeImg);
            closeIcon.setPreserveRatio(true);
            closeIcon.setFitWidth(36);
            closeIcon.setCursor(Cursor.HAND);
            closeIcon.setOnMouseEntered(ev -> {
                Glow glow = new Glow(0.6);
                DropShadow shadow = new DropShadow();
                shadow.setRadius(8);
                shadow.setSpread(0.18);
                shadow.setColor(javafx.scene.paint.Color.web("#ff2b2baa"));
                closeIcon.setEffect(new Blend(BlendMode.SRC_OVER, glow, shadow));
            });
            closeIcon.setOnMouseExited(ev -> closeIcon.setEffect(null));
            closeIcon.setOnMouseClicked(ev -> { hide(); });
            double margin = 16;
            double offsetLeft = 63; 
            double offsetDown = 83; 
            StackPane.setAlignment(closeIcon, Pos.CENTER);
            closeIcon.setTranslateX((bgW / 2.0) - closeIcon.getFitWidth() - margin - offsetLeft);
            closeIcon.setTranslateY((-bgH / 2.0) + margin + offsetDown);

            StackPane.setAlignment(bgGroup, Pos.CENTER);
            panel.getChildren().add(bgGroup);

            VBox content = new VBox(20.0);
            content.setAlignment(Pos.CENTER);

            ImageView saveIcon = createMenuIcon("/assets/menu/save.png", () -> { if (onSaveAndExit != null) onSaveAndExit.run(); });
            ImageView winIcon  = createMenuIcon("/assets/menu/win_level.png", () -> { if (onWinLevel != null) onWinLevel.run(); });
            try { javafx.scene.layout.VBox.setMargin(saveIcon, new javafx.geometry.Insets(20, 0, 0, 0)); } catch (Exception ignore) {}

        HBox sfxRow = new HBox(16.0);
        sfxRow.setAlignment(Pos.CENTER);
        Label sfxLabel = new Label("SFX Volume");
        sfxLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #FFFFFF;");
        sfxLabel.setPrefWidth(170);
        sfxLabel.setMinWidth(170);
        sfxLabel.setMaxWidth(170);
        Slider sfxSlider = new Slider(0.0, 1.0, view.manager.SoundManager.getInstance().getSfxVolume());
        sfxSlider.setPrefWidth(320);
        sfxSlider.setMinWidth(320);
        sfxSlider.setMaxWidth(320);
        sfxSlider.setStyle("-fx-accent: #FFCC00;");
        sfxSlider.setShowTickLabels(false);
        sfxSlider.setShowTickMarks(false);
        sfxSlider.valueProperty().addListener((obs, o, n) -> view.manager.SoundManager.getInstance().setSfxVolume(n.doubleValue()));
        sfxRow.getChildren().addAll(sfxLabel, sfxSlider);

        HBox musicRow = new HBox(16.0);
        musicRow.setAlignment(Pos.CENTER);
        Label musicLabel = new Label("Music Volume");
        musicLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #FFFFFF;");
        musicLabel.setPrefWidth(170);
        musicLabel.setMinWidth(170);
        musicLabel.setMaxWidth(170);
        Slider musicSlider = new Slider(0.0, 1.0, view.manager.SoundManager.getInstance().getMusicVolume());
        musicSlider.setPrefWidth(320);
        musicSlider.setMinWidth(320);
        musicSlider.setMaxWidth(320);
        musicSlider.setStyle("-fx-accent: #FFCC00;");
        musicSlider.setShowTickLabels(false);
        musicSlider.setShowTickMarks(false);
        musicSlider.valueProperty().addListener((obs, o, n) -> view.manager.SoundManager.getInstance().setMusicVolume(n.doubleValue()));
        musicSlider.valueChangingProperty().addListener((obs, was, is) -> view.manager.SoundManager.getInstance().setMusicVolume(musicSlider.getValue()));
        musicRow.getChildren().addAll(musicLabel, musicSlider);

            content.getChildren().addAll(sfxRow, musicRow, saveIcon, winIcon);
            content.setPickOnBounds(false);
            StackPane.setAlignment(content, Pos.CENTER);
            panel.getChildren().add(content);

            double shiftX = 15; 
            double shiftY = -30; 
            bgGroup.setTranslateX(shiftX);
            bgGroup.setTranslateY(shiftY);
            content.setTranslateX(shiftX);
            content.setTranslateY(shiftY);
            closeIcon.setTranslateX(closeIcon.getTranslateX() + shiftX);
            closeIcon.setTranslateY(closeIcon.getTranslateY() + shiftY);

            panel.getChildren().add(closeIcon);

            modalContainer.getChildren().add(panel);
        } catch (Exception ex) {
            System.err.println("Errore apertura impostazioni in-game: " + ex.getMessage());
        }
    }

    public void hide() {
        try { view.manager.SoundManager.getInstance().playSettingsToggle(); } catch (Exception ignore) {}
        try { if (onClosed != null) onClosed.run(); } catch (Exception ignore) {}
        try { ModalUtils.hideAndClear(modalContainer); } catch (Exception ignore) {}
    }

    private ImageView createMenuIcon(String resourcePath, Runnable onClick) {
        Image img = new Image(getClass().getResourceAsStream(resourcePath));
        ImageView icon = new ImageView(img);
        icon.setPreserveRatio(true);
        icon.setFitWidth(240);
        icon.setCursor(Cursor.HAND);
        icon.setOnMouseEntered(ev -> {
            Glow glow = new Glow(0.6);
            DropShadow shadow = new DropShadow();
            shadow.setRadius(9);
            shadow.setSpread(0.2);
            shadow.setColor(javafx.scene.paint.Color.web("#ffffffaa"));
            icon.setEffect(new Blend(BlendMode.SRC_OVER, glow, shadow));
        });
        icon.setOnMouseExited(ev -> icon.setEffect(null));
        icon.setOnMouseClicked(ev -> { if (onClick != null) onClick.run(); });
        return icon;
    }
}