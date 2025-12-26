package view.manager;

import javafx.geometry.*;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.text.TextAlignment;
import javafx.geometry.HPos;
import java.util.Locale;
import java.util.List;
import java.util.function.Consumer;
import view.util.ModalUtils;

/**
 * Manages the character selection screen logic and UI construction.
 * Displays available characters and handles user selection.
 */
public class CharacterSelectionManager {

    public static class Option {
        public final String id;
        public final String name;
        public final String portrait;
        public final String sprite;
        public Option(String id, String name, String portrait, String sprite) {
            this.id = id; this.name = name; this.portrait = portrait; this.sprite = sprite;
        }
    }

    public void show(StackPane modalContainer, Consumer<Option> onSelect) {
        if (modalContainer == null) return;
        ModalUtils.show(modalContainer, ModalUtils.Type.DEFAULT);

        StackPane selectionPanel = new StackPane();
        ImageView panelBackground = new ImageView(new Image(getClass().getResourceAsStream("/assets/menu/character_selection.png")));
        panelBackground.setFitWidth(1400);
        panelBackground.setFitHeight(800);
        panelBackground.setPreserveRatio(false);

        VBox contentBox = new VBox();
        contentBox.setPickOnBounds(false);
        contentBox.setSpacing(16);
        contentBox.setAlignment(Pos.CENTER);
        contentBox.setPadding(new Insets(20, 20, 20, 20));
        contentBox.setTranslateY(56);

        GridPane grid = new GridPane();
        grid.setHgap(14);
        grid.setVgap(8);
        grid.setAlignment(Pos.CENTER);

        for (int c = 0; c < 3; c++) {
            ColumnConstraints cc = new ColumnConstraints();
            cc.setMinWidth(200);
            cc.setPrefWidth(200);
            cc.setMaxWidth(200);
            grid.getColumnConstraints().add(cc);
        }
        for (int r = 0; r < 2; r++) {
            RowConstraints rc = new RowConstraints();
            rc.setMinHeight(150);
            rc.setPrefHeight(150);
            rc.setMaxHeight(150);
            grid.getRowConstraints().add(rc);
        }

        List<Option> defs = List.of(
            new Option("CRUSADER", "Crusader", "/assets/icons/characters/crusader_portrait.png", "/assets/characters/crusader.png"),
            new Option("HIGHWAYMAN", "Highwayman", "/assets/icons/characters/highwayman_portrait.png", "/assets/characters/highwayman.png"),
            new Option("JESTER", "Jester", "/assets/icons/characters/jester_portrait.png", "/assets/characters/jester.png"),
            new Option("OCCULTIST", "Occultist", "/assets/icons/characters/occultist_portrait.png", "/assets/characters/occultist.png"),
            new Option("PLAGUEDOCTOR", "Plaguedoctor", "/assets/icons/characters/plague_doctor_portrait.png", "/assets/characters/plague_doctor.png")
        );

        HBox bottomRowBox = new HBox();
        bottomRowBox.setAlignment(Pos.CENTER);
        bottomRowBox.setSpacing(12);
        bottomRowBox.setTranslateY(-2); 

        for (int i = 0; i < defs.size(); i++) {
            Option d = defs.get(i);
            ImageView portrait = new ImageView(new Image(getClass().getResourceAsStream(d.portrait)));
            portrait.setFitWidth(78);
            portrait.setFitHeight(78);
            portrait.setPreserveRatio(true);
            portrait.getStyleClass().add("character-portrait");

            Label nameLabel = new Label(d.name.toUpperCase(Locale.ITALIAN));
            nameLabel.getStyleClass().add("character-name-label");
            nameLabel.setWrapText(true);
            nameLabel.setTextAlignment(TextAlignment.CENTER);
            nameLabel.setAlignment(Pos.CENTER);
            nameLabel.setMaxWidth(190);

            VBox option = new VBox(portrait, nameLabel);
            option.getStyleClass().add("character-option");
            option.setAlignment(Pos.CENTER);
            option.setSpacing(10);
            option.setPrefWidth(190);
            option.setMinWidth(190);
            option.setMaxWidth(190);
            option.setFillWidth(true);
            option.setPrefHeight(150);
            option.setMinHeight(150);
            option.setMaxHeight(150);

            option.setOnMouseEntered(ev -> {
                portrait.setViewOrder(-1);
            });
            option.setOnMouseExited(ev -> {
                portrait.setViewOrder(0);
            });

            option.setOnMouseClicked(e -> onSelect.accept(d));
            portrait.setOnMouseClicked(e -> onSelect.accept(d));
            nameLabel.setOnMouseClicked(e -> onSelect.accept(d));

            if (i < 3) {
                grid.add(option, i, 0);
                GridPane.setHalignment(option, HPos.CENTER);
            } else {
                bottomRowBox.getChildren().add(option);
            }
        }

        grid.add(bottomRowBox, 0, 1, 3, 1);
        GridPane.setHalignment(bottomRowBox, HPos.CENTER);

        contentBox.getChildren().add(grid);
        selectionPanel.getChildren().addAll(panelBackground, contentBox);

        modalContainer.getChildren().clear();
        modalContainer.getChildren().add(selectionPanel);
        StackPane.setAlignment(selectionPanel, Pos.CENTER);
    }
}