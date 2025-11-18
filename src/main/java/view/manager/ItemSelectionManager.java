package view.manager;

import javafx.geometry.Pos;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import java.util.List;
import java.util.Map;
import java.util.Random;
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
        System.out.println("Show Item Selection after level.");
    }

    private StackPane buildPanel(ItemOption opt, Consumer<ItemOption> onSelect) {
        ImageView bg = new ImageView(new Image(
            getClass().getResourceAsStream("/assets/menu/items_selection.png")
        ));
        bg.setPreserveRatio(true);
        bg.setFitHeight(450); 

        ImageView icon = new ImageView(new Image(
            getClass().getResourceAsStream(opt.iconPath)
        ));
        icon.setPreserveRatio(true);
        icon.setFitWidth(70);
        icon.setFitHeight(70);

        VBox content = new VBox(icon);
        content.setAlignment(Pos.CENTER);

        StackPane panel = new StackPane(bg, content);
        panel.setOnMouseClicked(e -> onSelect.accept(opt));
        return panel;
    }
}