package view.manager;

import javafx.scene.effect.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.layout.StackPane;

/**
 * Manages the inventory UI, including slots, items, and interactions.
 * Handles rendering of inventory slots and item management.
 */
public class InventorySlotsUIManager {
    private final HBox inventoryHBox;
    private final String placeholderResourcePath;
    private int capacityLevel = 0;
    private java.util.List<StackPane> slotContainers = new java.util.ArrayList<>();
    private java.util.List<ImageView> slotViews = new java.util.ArrayList<>();
    private java.util.List<Boolean> slotIsItem = new java.util.ArrayList<>();
    private java.util.List<String> slotImagePaths = new java.util.ArrayList<>();
    public interface ItemClickHandler { void onClick(int index, String resourcePath, javafx.scene.input.MouseEvent event); }
    private ItemClickHandler clickHandler;

    public InventorySlotsUIManager(HBox inventoryHBox, String placeholderResourcePath) {
        this.inventoryHBox = inventoryHBox;
        this.placeholderResourcePath = placeholderResourcePath;
    }

    public void setCapacityLevel(int level) {
        this.capacityLevel = Math.max(0, level);
        renderSlots();
    }

    public int getCapacityLevel() {
        return capacityLevel;
    }

    public void renderSlots() {
        if (inventoryHBox == null) return;
        inventoryHBox.getChildren().clear();
        slotViews.clear();
        slotContainers.clear();
        int qty = capacityLevel + 2;
        slotIsItem.clear();
        slotImagePaths.clear();
        for (int i = 0; i < qty; i++) {
            ImageView slot = new ImageView(new Image(
                getClass().getResourceAsStream(placeholderResourcePath)
            ));
            slot.setFitWidth(61);
            slot.setFitHeight(61);
            slot.setPreserveRatio(true);
            slot.setSmooth(true);
            slot.setMouseTransparent(true);
            slot.setEffect(createBaseSlotEffect());
            slot.getStyleClass().remove("inventory-item");
            slot.getStyleClass().remove("inventory-item-hint");
            slot.getStyleClass().remove("inventory-item-heart");
            StackPane container = new StackPane(slot);
            container.setPrefSize(61, 61);
            StackPane.setAlignment(slot, javafx.geometry.Pos.CENTER);
            inventoryHBox.getChildren().add(container);
            slotContainers.add(container);
            slotViews.add(slot);
            slotIsItem.add(false);
            slotImagePaths.add(placeholderResourcePath);
        }
    }

    public int getSlotCount() {
        return slotViews.size();
    }

    public void setSlotImageAt(int index, String resourcePath) {
        if (index < 0 || index >= slotViews.size()) return;
        ImageView iv = slotViews.get(index);
        iv.setImage(new Image(getClass().getResourceAsStream(resourcePath)));
        slotImagePaths.set(index, resourcePath);

        boolean isPlaceholder = placeholderResourcePath != null && placeholderResourcePath.equals(resourcePath);
        if (isPlaceholder) {
            slotIsItem.set(index, false);
            iv.setMouseTransparent(true);
            iv.setOnMouseEntered(null);
            iv.setOnMouseExited(null);
            iv.setOnMouseClicked(null);
            iv.setOnMousePressed(null);
            iv.setEffect(createBaseSlotEffect());
            iv.getStyleClass().remove("inventory-item");
            iv.getStyleClass().remove("inventory-item-hint");
            iv.getStyleClass().remove("inventory-item-heart");
        } else {
            slotIsItem.set(index, true);
            iv.setMouseTransparent(false);
            iv.setOnMouseEntered(e -> {
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
            iv.setOnMouseExited(e -> iv.setEffect(createBaseSlotEffect()));
            iv.setOnMouseClicked(e -> { if (clickHandler != null) clickHandler.onClick(index, resourcePath, e); });
            iv.setOnMousePressed(e -> { if (clickHandler != null) clickHandler.onClick(index, resourcePath, e); });
            if (!iv.getStyleClass().contains("inventory-item")) iv.getStyleClass().add("inventory-item");
            if (resourcePath != null) {
                if (resourcePath.endsWith("/hint_item.png") || resourcePath.endsWith("hint_item.png")) {
                    if (!iv.getStyleClass().contains("inventory-item-hint")) iv.getStyleClass().add("inventory-item-hint");
                    iv.getStyleClass().remove("inventory-item-heart");
                } else if (resourcePath.endsWith("/missing_heart_item.png") || resourcePath.endsWith("missing_heart_item.png")) {
                    if (!iv.getStyleClass().contains("inventory-item-heart")) iv.getStyleClass().add("inventory-item-heart");
                    iv.getStyleClass().remove("inventory-item-hint");
                } else {
                    iv.getStyleClass().remove("inventory-item-hint");
                    iv.getStyleClass().remove("inventory-item-heart");
                }
            }
        }
    }

    public int findNextEmptySlot() {
        for (int i = 0; i < slotIsItem.size(); i++) {
            if (!slotIsItem.get(i)) return i;
        }
        return -1;
    }

    public void addItemImage(String resourcePath) {
        int idx = findNextEmptySlot();
        if (idx != -1) {
            setSlotImageAt(idx, resourcePath);
            return;
        }
        int n = slotViews.size();
        if (n == 0) return;
        for (int i = 1; i < n; i++) {
            String path = slotImagePaths.get(i);
            setSlotImageAt(i - 1, path);
        }
        setSlotImageAt(n - 1, resourcePath);
    }

    public void clearSlot(int index) {
        setSlotImageAt(index, placeholderResourcePath);
    }

    public void clear() {
        for (int i = 0; i < slotViews.size(); i++) {
            clearSlot(i);
        }
    }

    public void setOnItemClicked(ItemClickHandler handler) {
        this.clickHandler = handler;
    }

    public int getVisibleItemCount() {
        int count = 0;
        for (boolean isItem : slotIsItem) {
            if (isItem) count++;
        }
        return count;
    }

    public void flashFailureOnSlot(int index) { flashGlowOnSlot(index, javafx.scene.paint.Color.rgb(255, 60, 60)); }
    public void flashSuccessOnSlot(int index) { flashGlowOnSlot(index, javafx.scene.paint.Color.rgb(80, 255, 140)); }

    public void showPointsToastOnSlot(int index, String text) {
        if (index < 0 || index >= slotContainers.size()) return;
        StackPane container = slotContainers.get(index);
        javafx.scene.control.Label toast = new javafx.scene.control.Label(text);
        toast.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #2be37a;");
        javafx.scene.effect.DropShadow ds = new javafx.scene.effect.DropShadow();
        ds.setRadius(10);
        ds.setSpread(0.3);
        ds.setColor(javafx.scene.paint.Color.web("#2be37acc"));
        toast.setEffect(ds);
        toast.setOpacity(0.0);
        StackPane.setAlignment(toast, javafx.geometry.Pos.TOP_CENTER);
        container.getChildren().add(toast);

        javafx.animation.FadeTransition fadeIn = new javafx.animation.FadeTransition(javafx.util.Duration.millis(120), toast);
        fadeIn.setToValue(1.0);
        javafx.animation.TranslateTransition translateUp = new javafx.animation.TranslateTransition(javafx.util.Duration.millis(600), toast);
        translateUp.setFromY(0);
        translateUp.setToY(-18);
        javafx.animation.FadeTransition fadeOut = new javafx.animation.FadeTransition(javafx.util.Duration.millis(320), toast);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);

        javafx.animation.SequentialTransition seq = new javafx.animation.SequentialTransition(fadeIn, translateUp, fadeOut);
        seq.setOnFinished(e -> container.getChildren().remove(toast));
        seq.play();
    }

    private void flashGlowOnSlot(int index, javafx.scene.paint.Color color) {
        if (index < 0 || index >= slotViews.size()) return;
        ImageView iv = slotViews.get(index);
        javafx.scene.effect.DropShadow glow = new javafx.scene.effect.DropShadow();
        glow.setRadius(18);
        glow.setSpread(0.35);
        glow.setColor(color.deriveColor(0, 1, 1, 0.85));
        iv.setEffect(glow);
        javafx.animation.PauseTransition pt = new javafx.animation.PauseTransition(javafx.util.Duration.millis(500));
        pt.setOnFinished(e -> iv.setEffect(createBaseSlotEffect()));
        pt.play();
    }

    private Effect createBaseSlotEffect() {
        ColorAdjust adjust = new ColorAdjust();
        adjust.setBrightness(0.04);
        adjust.setSaturation(0.08);

        DropShadow edgeShadow = new DropShadow();
        edgeShadow.setRadius(8);
        edgeShadow.setSpread(0.18);
        edgeShadow.setColor(Color.rgb(0, 0, 0, 0.35));
        edgeShadow.setOffsetX(0);
        edgeShadow.setOffsetY(0);
        edgeShadow.setInput(adjust);

        return edgeShadow;
    }
}