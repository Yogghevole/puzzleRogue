package view.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.control.Tooltip;
import javafx.scene.effect.Glow;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.Blend;
import javafx.scene.effect.BlendMode;
import java.util.*;
import model.db.DatabaseManager;
import model.dao.UserDAO;
import model.domain.User;
import model.domain.BuffType;
import model.domain.buff.Buff;
import model.domain.buff.BuffFactory;
import model.service.SessionService;
import view.util.StageUtils;
import javafx.scene.shape.Circle;
import javafx.scene.paint.Color;
import javafx.geometry.Pos;
import javafx.geometry.Insets;

/**
 * Controller for the Legacy (progression) screen.
 * Allows users to purchase permanent upgrades using points.
 */
public class LegacyController {
    @FXML private ImageView backButton;
    @FXML private GridPane buffGrid;
    @FXML private HBox buffIconsHBox;
    @FXML private VBox buffInfoBox;
    @FXML private ImageView backgroundView;
    @FXML private StackPane rootPane;
    @FXML private Label nickLabel;
    @FXML private Label pointsLabel;

    private final view.manager.UserInfoManager userInfoManager = new view.manager.UserInfoManager();

    private final Map<String, String> buffIconMap = new LinkedHashMap<>();
    private final Map<String, Image> preloadedIcons = new LinkedHashMap<>();
    
    @FXML
    public void initialize() {
        backButton.setOnMouseClicked(e -> {
            try { view.manager.SoundManager.getInstance().playSettingsToggle(); } catch (Exception ignore) {}
            navigateHome();
        });
        
        backButton.setFitWidth(75);
        backButton.setFitHeight(75);
        backButton.setPreserveRatio(true);
        backButton.setOnMouseEntered(e -> {
            Glow glow = new Glow(0.35);
            DropShadow shadow = new DropShadow();
            shadow.setRadius(6.0);
            shadow.setSpread(0.1);
            shadow.setColor(Color.web("#ffffff88"));
            backButton.setEffect(new Blend(BlendMode.SRC_OVER, glow, shadow));
        });
        backButton.setOnMouseExited(e -> backButton.setEffect(null));

        applyBackground();
        setupBuffIcons();
        preloadIcons();

        userInfoManager.setup(nickLabel, pointsLabel);

        buildBuffIconsBottom();

        Button cheatBtn = new Button();
        cheatBtn.setOpacity(0);
        cheatBtn.setPrefSize(150, 50);
        cheatBtn.setCursor(javafx.scene.Cursor.DEFAULT);
        StackPane.setAlignment(cheatBtn, Pos.TOP_RIGHT);
        StackPane.setMargin(cheatBtn, new Insets(90, 80, 0, 0));

        cheatBtn.setOnAction(e -> {
             User u = getCurrentUser();
             if (u != null) {
                 u.addPoints(10000);
                 try {
                     UserDAO dao = new UserDAO(DatabaseManager.getInstance());
                     dao.updateUser(u);
                     
                     
                     SessionService.setCurrentUser(u);
                     userInfoManager.updateDisplay();

                     Image coinIcon = preloadedIcons.get(BuffType.POINT_BONUS.name());
                     view.util.NotificationManager.showInPane(rootPane, "Cheat Activated", "+10000 Credits", coinIcon);
                 } catch (Exception ex) {
                     System.err.println("Cheat error: " + ex.getMessage());
                 }
             }
        });
        rootPane.getChildren().add(cheatBtn);
    }

    private void applyBackground() {
        try {
            Image bg = new Image(getClass().getResourceAsStream("/assets/menu/the_ancestors_legacy.png"));
            backgroundView.setPreserveRatio(true);
            backgroundView.fitWidthProperty().bind(rootPane.widthProperty());
            backgroundView.fitHeightProperty().bind(rootPane.heightProperty());
            backgroundView.setImage(bg);
            rootPane.setStyle("-fx-background-color: black;");
        } catch (Exception e) {
            System.err.println("Errore caricamento background Legacy: " + e.getMessage());
        }
    }

    private void setupBuffIcons() {
        buffIconMap.put(BuffType.POINT_BONUS.name(), "/assets/icons/buffs/point_bonus.png");
        buffIconMap.put(BuffType.EXTRA_LIVES.name(), "/assets/icons/buffs/extra_lives.png");
        buffIconMap.put(BuffType.STARTING_CELLS.name(), "/assets/icons/buffs/starting_cells.png");
        buffIconMap.put(BuffType.FIRST_ERROR_PROTECT.name(), "/assets/icons/buffs/first_error_protection.png");
        buffIconMap.put(BuffType.INVENTORY_CAPACITY.name(), "/assets/icons/buffs/inventory_capacity.png");
    }

    private void preloadIcons() {
        buffIconMap.forEach((id, path) -> {
            try {
                preloadedIcons.put(id, new Image(getClass().getResourceAsStream(path), 64, 64, true, true));
            } catch (Exception e) {
                System.err.println("Errore preload icona buff " + id + ": " + e.getMessage());
            }
        });
    }

    private void buildBuffIconsBottom() {
        if (buffIconsHBox == null) return;
        buffIconsHBox.getChildren().clear();
        buffIconsHBox.setAlignment(Pos.CENTER);

        VBox rowsContainer = new VBox(60);
        rowsContainer.setAlignment(Pos.CENTER);

        HBox row1 = new HBox(40);
        row1.setAlignment(Pos.TOP_CENTER);
        
        HBox row2 = new HBox(40);
        row2.setAlignment(Pos.TOP_CENTER);


        addBuffToRow(BuffType.EXTRA_LIVES.name(), row1);
        addBuffToRow(BuffType.STARTING_CELLS.name(), row1);
        addBuffToRow(BuffType.FIRST_ERROR_PROTECT.name(), row1);
        addBuffToRow(BuffType.POINT_BONUS.name(), row2);
        addBuffToRow(BuffType.INVENTORY_CAPACITY.name(), row2);

        rowsContainer.getChildren().addAll(row1, row2);
        buffIconsHBox.getChildren().add(rowsContainer);
    }

    private void addBuffToRow(String id, HBox row) {
        if (preloadedIcons.containsKey(id)) {
             User user = getCurrentUser();
             Buff buff = BuffFactory.getBuff(id);
             if (buff == null) return;

             int currentLevel = user != null ? user.getBuffLevel(BuffType.valueOf(id)) : 0;
             StackPane cell = createBuffCell(buff, preloadedIcons.get(id), currentLevel);
             row.getChildren().add(cell);
        }
    }

    private StackPane createBuffCell(Buff buff, Image img, int currentLevel) {
        ImageView iv = new ImageView(img);
        iv.setFitWidth(74);
        iv.setFitHeight(74);
        iv.setPreserveRatio(true);
        iv.setSmooth(true);
        
        Label label = new Label(buff.getDisplayName());
        label.setStyle("-fx-font-family: 'Georgia'; -fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #e0e0e0;");
        label.setMinWidth(150);
        label.setMaxWidth(150);
        label.setAlignment(Pos.CENTER);
        label.setWrapText(true);
        label.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);
        
        VBox content = new VBox(8);
        content.setAlignment(Pos.TOP_CENTER);
        content.getChildren().add(iv);

        int maxDots = buff.getMaxLevel();
        
        HBox dotsBox = new HBox(4);
        dotsBox.setAlignment(Pos.CENTER);
        for (int i = 0; i < maxDots; i++) {
            Circle dot = new Circle(5.5);
            if (i < currentLevel) {
                dot.setFill(Color.web("#FFCC00"));
                dot.setEffect(new javafx.scene.effect.Glow(0.8));
            } else {
                dot.setFill(Color.DARKGRAY);
            }
            dotsBox.getChildren().add(dot);
        }
        content.getChildren().add(dotsBox);
        content.getChildren().add(label);

        StackPane cell = new StackPane(content);
        cell.getStyleClass().add("legacy-grid-cell");
        cell.setMinWidth(150);
        cell.setPrefWidth(150);
        cell.setMaxWidth(150);
        Tooltip tp = new Tooltip(buff.getDescription());
        Tooltip.install(cell, tp);

        cell.setOnMouseEntered(e -> {
            Glow glow = new Glow(0.3);
            DropShadow shadow = new DropShadow();
            shadow.setRadius(15.0);
            shadow.setSpread(0.2);
            shadow.setColor(Color.web("#ffffff66"));
            cell.setEffect(new Blend(BlendMode.SRC_OVER, glow, shadow));
            cell.setScaleX(1.05);
            cell.setScaleY(1.05);
            cell.setCursor(javafx.scene.Cursor.HAND);
        });

        cell.setOnMouseExited(e -> {
            cell.setEffect(null);
            cell.setScaleX(1.0);
            cell.setScaleY(1.0);
            cell.setCursor(javafx.scene.Cursor.DEFAULT);
        });
        
        cell.addEventHandler(MouseEvent.MOUSE_CLICKED, e -> handleBuffClick(buff));
        return cell;
    }

    private void handleBuffClick(Buff buff) {
        String id = buff.getId();
        SessionService.setLastSelectedBuff(id);

        User user = getCurrentUser();
        if (user == null) return;

        int currentLevel = user.getBuffLevel(BuffType.valueOf(id));

        if (currentLevel >= buff.getMaxLevel()) {
            if (buff.getMaxLevel() == 1) {
                showAlert("Already Owned", "You already have the " + buff.getDisplayName() + " buff.");
            } else {
                showAlert("Max Level", "This buff is already at maximum level.");
            }
            return;
        }

        int nextLevel = currentLevel + 1;
        int cost = buff.getCost(nextLevel);
        
        if (cost < 0) {
             showAlert("Error", "Cannot purchase this item.");
             return;
        }

        String description = buff.getDescription();
        String prompt = "Purchase " + buff.getDisplayName() + " (Level " + nextLevel + ")?\n\n" + 
                        description + "\n\n" +
                        "Cost: " + cost + " pts";
        
        boolean canAfford = true;
        if (user.getPointsAvailable() < cost) {
             prompt = "Insufficient Points\n\n" + 
                      "Cost: " + cost + " pts\n" +
                      "Available: " + user.getPointsAvailable() + " pts";
             canAfford = false;
        }

        showCustomConfirmation(prompt, canAfford, () -> {
            try { view.manager.SoundManager.getInstance().playWinItemSelection(); } catch (Exception ignore) {}
            
            if (performPurchase(buff, nextLevel, cost)) {
                Image icon = preloadedIcons.get(id);
                view.util.NotificationManager.showInPane(rootPane, 
                    "Purchase Successful", 
                    "You acquired " + buff.getDisplayName() + " (Level " + nextLevel + ")", 
                    icon
                );
                
                buildBuffIconsBottom();
                userInfoManager.updateDisplay();
            } else {
                showAlert("Error", "Transaction failed.");
            }
        });
    }

    private boolean performPurchase(Buff buff, int level, int cost) {
        try {
            User user = getCurrentUser();
            if (user == null) return false;
            
            if (user.tryPurchase(cost)) {
                user.upgradeBuff(buff.getType(), level);
                
                UserDAO dao = new UserDAO(DatabaseManager.getInstance());
                if (dao.updateUser(user)) {
                    SessionService.setCurrentUser(user);
                    return true;
                }
            }
            return false;
        } catch (Exception e) {
            System.err.println("Purchase error: " + e.getMessage());
            return false;
        }
    }

    private void showCustomConfirmation(String rawMessage, boolean showBuyButton, Runnable onConfirm) {
        if (rootPane == null) return;

        StackPane overlay = new StackPane();
        overlay.setStyle("-fx-background-color: rgba(0,0,0,0.85);");
        VBox dialog = new VBox(20);
        dialog.setMaxSize(500, 320);
        dialog.setAlignment(Pos.CENTER);
        
        dialog.setStyle(
            "-fx-background-color: linear-gradient(to bottom, #2a2a2a, #151515);" +
            "-fx-padding: 30;" +
            "-fx-background-radius: 12;" +
            "-fx-border-color: #E6C88C;" +
            "-fx-border-width: 2;" +
            "-fx-border-radius: 12;" +
            "-fx-effect: dropshadow(three-pass-box, rgba(230, 200, 140, 0.3), 20, 0, 0, 0);"
        );


        String titleText = "";
        String contentText = rawMessage;
        if (rawMessage.contains("\n")) {
            int idx = rawMessage.indexOf("\n");
            titleText = rawMessage.substring(0, idx);
            contentText = rawMessage.substring(idx + 1);
        }

        if (!titleText.isEmpty()) {
            Label titleLabel = new Label(titleText.toUpperCase());
            titleLabel.setStyle("-fx-text-fill: #E6C88C; -fx-font-family: 'Georgia'; -fx-font-size: 26px; -fx-font-weight: bold; -fx-effect: dropshadow(gaussian, black, 2, 1, 0, 0);");
            titleLabel.setAlignment(Pos.CENTER);
            titleLabel.setWrapText(true);
            titleLabel.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);
            dialog.getChildren().add(titleLabel);
        }
        
        Label msgLabel = new Label(contentText);
        msgLabel.setWrapText(true);
        msgLabel.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);
        msgLabel.setStyle("-fx-text-fill: #f0f0f0; -fx-font-family: 'Georgia'; -fx-font-size: 18px;");
        msgLabel.setAlignment(Pos.CENTER);
        
        HBox buttons = new HBox(25);
        buttons.setAlignment(Pos.CENTER);
        
        Button backBtn = new Button("BACK");
        styleThemedButton(backBtn);
        backBtn.setOnAction(e -> rootPane.getChildren().remove(overlay));
        
        buttons.getChildren().add(backBtn);

        if (showBuyButton) {
            Button buyBtn = new Button("BUY");
            styleThemedButton(buyBtn);
            buyBtn.setOnAction(e -> {
                rootPane.getChildren().remove(overlay);
                onConfirm.run();
            });
            buttons.getChildren().add(buyBtn);
        }
        
        dialog.getChildren().addAll(msgLabel, buttons);
        overlay.getChildren().add(dialog);
        

        overlay.setOnMouseClicked(e -> {
            if (e.getTarget() == overlay) rootPane.getChildren().remove(overlay);
        });
        
        rootPane.getChildren().add(overlay);
    }

    private void styleThemedButton(Button btn) {
        String baseStyle = "-fx-background-color: #1a1a1a; -fx-text-fill: #E6C88C; -fx-border-color: #E6C88C; -fx-border-radius: 6; -fx-background-radius: 6; -fx-font-family: 'Georgia'; -fx-font-weight: bold; -fx-font-size: 14px; -fx-cursor: hand;";
        String hoverStyle = "-fx-background-color: #E6C88C; -fx-text-fill: #1a1a1a; -fx-border-color: #E6C88C; -fx-border-radius: 6; -fx-background-radius: 6; -fx-font-family: 'Georgia'; -fx-font-weight: bold; -fx-font-size: 14px; -fx-cursor: hand;";
        
        btn.setStyle(baseStyle);
        btn.setPrefWidth(120);
        
        btn.setOnMouseEntered(e -> btn.setStyle(hoverStyle));
        btn.setOnMouseExited(e -> btn.setStyle(baseStyle));
    }


    
    private void showAlert(String title, String content) {
        showCustomConfirmation(title + "\n" + content, false, () -> {});
    }

    private User getCurrentUser() {
        try {
            String nick = SessionService.getCurrentNick();
            if (nick == null || nick.isEmpty()) return null;
            UserDAO dao = new UserDAO(DatabaseManager.getInstance());
            return dao.getUserByNick(nick);
        } catch (Exception e) {
            System.err.println("Error fetching user: " + e.getMessage());
            return null;
        }
    }

    private void navigateHome() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/HomeScreen.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) backButton.getScene().getWindow();
            StageUtils.setSceneRoot(stage, root);
        } catch (Exception ex) {
            System.err.println("Errore nella navigazione alla Home: " + ex.getMessage());
        }
    }
}
