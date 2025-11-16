package view.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.animation.ScaleTransition;
import javafx.util.Duration;
import java.util.*;
import model.db.DatabaseManager;
import model.dao.UserDAO;
import model.service.SessionService;

public class LegacyController {
    @FXML private Button backButton;
    @FXML private GridPane buffGrid;
    @FXML private HBox buffIconsHBox;
    @FXML private VBox buffInfoBox;
    @FXML private ImageView backgroundView;

    private final Map<String, String> buffIconMap = new LinkedHashMap<>();
    private final Map<String, Image> preloadedIcons = new HashMap<>();
    private final Set<String> selectedBuffs = new LinkedHashSet<>();

    @FXML
    public void initialize() {
        backButton.setOnAction(e -> navigateHome());
        applyBackground();
        setupBuffIcons();
        preloadIcons();
        buildBuffIconsBottom();
    }

    private void applyBackground() {
        try {
            Image bg = new Image(getClass().getResourceAsStream("/assets/menu/home_screen.png"));
            backgroundView.setPreserveRatio(false);
            backgroundView.setImage(bg);
        } catch (Exception e) {
            System.err.println("Errore caricamento background Legacy: " + e.getMessage());
        }
    }

    private void setupBuffIcons() {
        buffIconMap.put("EXTRA_LIVES", "/assets/icons/buffs/extra_lives.png");
        buffIconMap.put("FIRST_ERROR_PROTECT", "/assets/icons/buffs/first_error_protection.png");
        buffIconMap.put("STARTING_HINTS", "/assets/icons/buffs/extra_hints.png");
        buffIconMap.put("POINT_BONUS", "/assets/icons/buffs/point_bonus.png");
        buffIconMap.put("INVENTORY_CAPACITY", "/assets/icons/buffs/inventory_capacity.png");
        buffIconMap.put("STARTING_CELLS", "/assets/icons/buffs/starting_cells.png");
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
        for (Map.Entry<String, Image> entry : preloadedIcons.entrySet()) {
            String id = entry.getKey();
            Image img = entry.getValue();
            StackPane cell = createBuffCell(id, img);
            buffIconsHBox.getChildren().add(cell);
        }
    }

    private StackPane createBuffCell(String id, Image img) {
        ImageView iv = new ImageView(img);
        iv.setFitWidth(96);
        iv.setFitHeight(96);
        iv.setPreserveRatio(true);
        iv.setSmooth(true);
        Label label = new Label(id.replace('_',' '));
        label.getStyleClass().add("hint-label");
        BorderPane pane = new BorderPane();
        pane.setCenter(iv);
        pane.setBottom(label);
        StackPane cell = new StackPane(pane);
        cell.getStyleClass().add("legacy-grid-cell");
        cell.addEventHandler(MouseEvent.MOUSE_CLICKED, e -> handleBuffClick(id));
        return cell;
    }

    private void handleBuffClick(String id) {
        SessionService.setLastSelectedBuff(id);
        showBuffInfo(id);
        persistUserBuff(id, 1);
    }

    private void showBuffInfo(String id) {
        if (buffInfoBox == null) return;
        buffInfoBox.getChildren().clear();
        Label name = new Label(prettyName(id));
        name.getStyleClass().add("heading-small");
        Label desc = new Label(buffDescription(id));
        desc.getStyleClass().add("hint-label");
        buffInfoBox.getChildren().addAll(name, desc);
    }

    private String prettyName(String id) {
        return id.replace('_', ' ');
    }

    private String buffDescription(String id) {
        return "Selected: " + id;
    }

    private void animateSelection(ImageView badge) {
        ScaleTransition st = new ScaleTransition(Duration.millis(120), badge);
        st.setFromX(0.8);
        st.setFromY(0.8);
        st.setToX(1.0);
        st.setToY(1.0);
        st.play();
    }

    private void persistUserBuff(String buffId, int level) {
        try {
            String nick = SessionService.getCurrentNick();
            if (nick == null || nick.isEmpty()) return;
            UserDAO dao = new UserDAO(DatabaseManager.getInstance());
            var user = dao.getUserByNick(nick);
            if (user == null) return;
            user.upgradeBuff(buffId, level);
            dao.updateUser(user);
        } catch (Exception e) {
            System.err.println("Errore salvataggio buff utente: " + e.getMessage());
        }
    }

    private void navigateHome() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/HomeScreen.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) backButton.getScene().getWindow();
            stage.setScene(new javafx.scene.Scene(root, stage.getWidth(), stage.getHeight()));
            stage.show();
        } catch (Exception ex) {
            System.err.println("Errore nella navigazione alla Home: " + ex.getMessage());
        }
    }
}