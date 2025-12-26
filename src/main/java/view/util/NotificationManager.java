package view.util;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

/**
 * Manages the display of system notifications and in-app popups.
 * Handles creation, animation, and lifecycle of notification windows and toast-like messages.
 */
public class NotificationManager {

    public static void showPurchaseNotification(String title, String message, Image icon) {
        Stage stage = new Stage();
        stage.initStyle(StageStyle.TRANSPARENT);
        stage.setAlwaysOnTop(true);

        StackPane root = createNotificationNode(title, message, icon);
        root.setStyle("-fx-background-color: transparent;");

        Rectangle bg = new Rectangle(300, 80);
        bg.setArcWidth(20);
        bg.setArcHeight(20);
        bg.setFill(Color.rgb(30, 30, 40, 0.9));
        bg.setStroke(Color.rgb(100, 200, 255, 0.5));
        bg.setStrokeWidth(2);
        bg.setEffect(new javafx.scene.effect.DropShadow(10, Color.BLACK));

        
        HBox content = createNotificationContent(title, message, icon);
        content.setMouseTransparent(true);
        
        StackPane windowRoot = new StackPane(bg, content);
        windowRoot.setStyle("-fx-background-color: transparent;");

        Scene scene = new Scene(windowRoot);
        scene.setFill(Color.TRANSPARENT);
        stage.setScene(scene);

        Rectangle2D bounds = Screen.getPrimary().getVisualBounds();
        stage.setX(bounds.getMaxX() - 320);
        stage.setY(bounds.getMaxY() - 100);

        windowRoot.setOpacity(0);
        windowRoot.setTranslateY(20);

        Timeline fadeIn = new Timeline(
            new KeyFrame(Duration.ZERO, 
                new javafx.animation.KeyValue(windowRoot.opacityProperty(), 0),
                new javafx.animation.KeyValue(windowRoot.translateYProperty(), 20)
            ),
            new KeyFrame(Duration.millis(500), 
                new javafx.animation.KeyValue(windowRoot.opacityProperty(), 1),
                new javafx.animation.KeyValue(windowRoot.translateYProperty(), 0)
            )
        );
        
        Timeline fadeOut = new Timeline(
            new KeyFrame(Duration.ZERO, 
                new javafx.animation.KeyValue(windowRoot.opacityProperty(), 1)
            ),
            new KeyFrame(Duration.millis(500), 
                e -> stage.close(),
                new javafx.animation.KeyValue(windowRoot.opacityProperty(), 0)
            )
        );
        
        Timeline autoClose = new Timeline(new KeyFrame(Duration.seconds(4), e -> fadeOut.play()));
        
        stage.show();
        
        fadeIn.play();
        fadeIn.setOnFinished(e -> autoClose.play());

        windowRoot.setOnMouseClicked(e -> {
            autoClose.stop();
            fadeOut.play();
        });
    }

    public static void showInPane(javafx.scene.layout.Pane parentPane, String title, String message, Image icon) {
        if (parentPane == null) return;

        HBox notif = createNotificationContent(title, message, icon);
        notif.setMaxSize(320, 80);
        notif.setAlignment(Pos.CENTER_LEFT);
        notif.setStyle("-fx-background-color: rgba(30, 30, 40, 0.9); -fx-padding: 10; -fx-background-radius: 10; -fx-border-color: #E6C88C; -fx-border-width: 2; -fx-border-radius: 10; -fx-effect: dropshadow(three-pass-box, rgba(230, 200, 140, 0.3), 10, 0, 0, 0);");

        StackPane.setAlignment(notif, Pos.BOTTOM_RIGHT);
        StackPane.setMargin(notif, new javafx.geometry.Insets(0, 20, 20, 0));
        
        notif.setTranslateY(100);
        notif.setOpacity(0);
        
        parentPane.getChildren().add(notif);
        
        javafx.animation.ParallelTransition fadeIn = new javafx.animation.ParallelTransition(
            new javafx.animation.FadeTransition(Duration.millis(500), notif),
            new javafx.animation.TranslateTransition(Duration.millis(500), notif)
        );
        ((javafx.animation.FadeTransition)fadeIn.getChildren().get(0)).setFromValue(0);
        ((javafx.animation.FadeTransition)fadeIn.getChildren().get(0)).setToValue(1);
        ((javafx.animation.TranslateTransition)fadeIn.getChildren().get(1)).setFromY(100);
        ((javafx.animation.TranslateTransition)fadeIn.getChildren().get(1)).setToY(0);
        
        javafx.animation.ParallelTransition fadeOut = new javafx.animation.ParallelTransition(
            new javafx.animation.FadeTransition(Duration.millis(500), notif),
            new javafx.animation.TranslateTransition(Duration.millis(500), notif)
        );
        ((javafx.animation.FadeTransition)fadeOut.getChildren().get(0)).setFromValue(1);
        ((javafx.animation.FadeTransition)fadeOut.getChildren().get(0)).setToValue(0);
        ((javafx.animation.TranslateTransition)fadeOut.getChildren().get(1)).setFromY(0);
        ((javafx.animation.TranslateTransition)fadeOut.getChildren().get(1)).setToY(100);
        
        javafx.animation.SequentialTransition seq = new javafx.animation.SequentialTransition(
            fadeIn,
            new javafx.animation.PauseTransition(Duration.seconds(4)),
            fadeOut
        );
        
        seq.setOnFinished(e -> parentPane.getChildren().remove(notif));
        seq.play();
        
        notif.setOnMouseClicked(e -> {
            seq.stop();
            fadeOut.play();
            fadeOut.setOnFinished(ev -> parentPane.getChildren().remove(notif));
        });
    }

    private static StackPane createNotificationNode(String title, String message, Image icon) {
        return new StackPane();
    }

    private static HBox createNotificationContent(String title, String message, Image icon) {
        HBox content = new HBox(15);
        content.setAlignment(Pos.CENTER_LEFT);        
        ImageView iv = new ImageView(icon);
        iv.setFitWidth(48);
        iv.setFitHeight(48);
        iv.setPreserveRatio(true);

        VBox textBox = new VBox(4);
        textBox.setAlignment(Pos.CENTER_LEFT);
        
        Label titleLbl = new Label(title);
        titleLbl.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px; -fx-font-family: 'Segoe UI', sans-serif;");
        
        Label msgLbl = new Label(message);
        msgLbl.setStyle("-fx-text-fill: #cccccc; -fx-font-size: 12px; -fx-font-family: 'Segoe UI', sans-serif;");
        msgLbl.setWrapText(true);

        textBox.getChildren().addAll(titleLbl, msgLbl);
        content.getChildren().addAll(iv, textBox);
        return content;
    }
}
