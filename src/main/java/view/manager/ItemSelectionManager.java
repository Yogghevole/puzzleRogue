package view.manager;

import javafx.geometry.Pos;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import java.util.List;
import java.util.*;
import java.util.function.Consumer;
import view.util.ModalUtils;

/**
 * Gestisce l'overlay di selezione oggetto mostrato alla fine dei livelli (non boss).
 */
public class ItemSelectionManager {

    public static class ItemOption {
        public final String id;         
        public final String iconPath;   
        public ItemOption(String id, String iconPath) {
            this.id = id; this.iconPath = iconPath;
        }
    }

    private final Random rng = new Random();

    private static final Map<String, String> ITEM_ICON_MAP = Map.of(
        "HINT_ITEM", "/assets/icons/items/hint_item.png",
        "LIFE_BOOST_ITEM", "/assets/icons/items/missing_heart_item.png",
        "SACRIFICE_ITEM", "/assets/icons/items/sacrifice_item.png",
        "SCORE_ITEM", "/assets/icons/items/score_item.png"
    );

    private static final String NO_ITEM_ICON = "/assets/icons/items/noItem.png";

    private static final Map<String, String> ITEM_NAME_MAP = Map.of(
        "HINT_ITEM", "Hint",
        "LIFE_BOOST_ITEM", "Life Heal",
        "SACRIFICE_ITEM", "Sacrifice",
        "SCORE_ITEM", "Score Bonus",
        "NO_ITEM", "No Item"
    );

    private static final Map<String, String> ITEM_DESC_MAP = Map.of(
        "HINT_ITEM", "Reveal a correct cell",
        "LIFE_BOOST_ITEM", "Heal one lost life",
        "SACRIFICE_ITEM", "Trade life for 2 hints",
        "SCORE_ITEM", "Increase score gains",
        "NO_ITEM", ""
    );

    public void show(StackPane modalContainer, Consumer<ItemOption> onSelect) {
        if (modalContainer == null) return;
        ModalUtils.show(modalContainer, ModalUtils.Type.DEFAULT);

        javafx.scene.control.Label title = new javafx.scene.control.Label("Choose your item");
        title.getStyleClass().add("item-select-title");
        javafx.scene.effect.Glow glow = new javafx.scene.effect.Glow(0.35);
        javafx.scene.effect.DropShadow shadow = new javafx.scene.effect.DropShadow();
        shadow.setRadius(16);
        shadow.setSpread(0.2);
        shadow.setColor(javafx.scene.paint.Color.web("#88c9ff66"));
        title.setEffect(new javafx.scene.effect.Blend(
            javafx.scene.effect.BlendMode.SRC_OVER,
            glow,
            shadow
        ));
        title.setTranslateY(-40);

        HBox row = new HBox();
        row.setAlignment(Pos.CENTER);
        row.setSpacing(100); 

        List<String> keys = List.copyOf(ITEM_ICON_MAP.keySet());
        String k1 = keys.get(rng.nextInt(keys.size()));
        String k2 = keys.get(rng.nextInt(keys.size()));
        if (keys.size() > 1) {
            while (k2.equals(k1)) {
                k2 = keys.get(rng.nextInt(keys.size()));
            }
        }

        ItemOption opt1 = new ItemOption(k1, ITEM_ICON_MAP.get(k1));
        ItemOption opt2 = new ItemOption(k2, ITEM_ICON_MAP.get(k2));
        ItemOption opt3 = new ItemOption("NO_ITEM", NO_ITEM_ICON);

        StackPane p1 = buildPanel(opt1, onSelect);
        StackPane p2 = buildPanel(opt2, onSelect);
        StackPane p3 = buildPanel(opt3, onSelect);

        row.getChildren().addAll(p1, p2, p3);

        VBox container = new VBox(title, row);
        container.setAlignment(Pos.CENTER);
        container.setSpacing(24);

        modalContainer.getChildren().clear();
        modalContainer.getChildren().add(container);
        StackPane.setAlignment(container, Pos.CENTER);
    }

    private StackPane buildPanel(ItemOption opt, Consumer<ItemOption> onSelect) {
        ImageView bg = new ImageView(new Image(
            getClass().getResourceAsStream("/assets/menu/items_selection.png")
        ));
        bg.setPreserveRatio(true);
        bg.setFitHeight(560);

        ImageView icon = new ImageView(new Image(
            getClass().getResourceAsStream(opt.iconPath)
        ));
        icon.setPreserveRatio(true);
        icon.setFitWidth(70);
        icon.setFitHeight(70);

        javafx.scene.control.Label nameLabel = new javafx.scene.control.Label(ITEM_NAME_MAP.getOrDefault(opt.id, "Item"));
        nameLabel.getStyleClass().add("item-name-label");
        nameLabel.setTranslateY(-6);

        javafx.scene.control.Label descLabel = new javafx.scene.control.Label(ITEM_DESC_MAP.getOrDefault(opt.id, ""));
        descLabel.getStyleClass().add("item-desc-label");
        descLabel.setWrapText(true);
        descLabel.setTranslateY(6);
        descLabel.setVisible(false);

        VBox content = new VBox(nameLabel, icon, descLabel);
        content.setAlignment(Pos.CENTER);
        content.setSpacing(8);

        StackPane panel = new StackPane(bg, content);
        icon.setOnMouseClicked(e -> onSelect.accept(opt));
        javafx.animation.PauseTransition hoverReveal = new javafx.animation.PauseTransition(javafx.util.Duration.seconds(0.5));
        hoverReveal.setOnFinished(ev -> descLabel.setVisible(true));

        icon.setOnMouseEntered(e -> {
            javafx.scene.effect.Glow glow = new javafx.scene.effect.Glow(0.6);
            javafx.scene.effect.DropShadow shadow = new javafx.scene.effect.DropShadow();
            shadow.setRadius(9.0);
            shadow.setSpread(0.2);
            shadow.setColor(javafx.scene.paint.Color.web("#ffffffaa"));
            icon.setEffect(new javafx.scene.effect.Blend(
                javafx.scene.effect.BlendMode.SRC_OVER,
                glow,
                shadow
            ));
            descLabel.setVisible(false);
            hoverReveal.playFromStart();
        });
        icon.setOnMouseExited(e -> {
            icon.setEffect(null);
            hoverReveal.stop();
            descLabel.setVisible(false);
        });
        return panel;
    }
}