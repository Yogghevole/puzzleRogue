package view.manager;
import javafx.animation.*;
import javafx.geometry.*;
import javafx.util.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.effect.*;
import javafx.scene.image.*;
import javafx.scene.paint.*;
import javafx.scene.shape.*;
import javafx.scene.text.*;
import view.util.ModalUtils;
import view.util.StageUtils;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.stage.Stage;
import model.service.PointService;

public class EndGameManager {

    private void attachHomeButton(StackPane cardPane, double targetWidth, StackPane modalContainer) {
        if (cardPane == null || modalContainer == null) return;
        try {
            Image homeImg = new Image(getClass().getResourceAsStream("/assets/endgame/home_button.png"));
            ImageView homeBtn = new ImageView(homeImg);
            homeBtn.setPreserveRatio(true);
            homeBtn.setSmooth(true);
            double btnWidth = Math.min(440.0, Math.max(280.0, targetWidth * 0.36));
            homeBtn.setFitWidth(btnWidth);

            StackPane.setAlignment(homeBtn, Pos.BOTTOM_CENTER);
            StackPane.setMargin(homeBtn, new Insets(40, 0, -10, 0));
            homeBtn.setViewOrder(-3);
            homeBtn.setCursor(javafx.scene.Cursor.HAND);

            homeBtn.setOnMouseEntered(ev -> {
                Glow glow = new Glow(0.35);
                DropShadow shadow = new DropShadow();
                shadow.setRadius(12.0);
                shadow.setSpread(0.12);
                shadow.setColor(Color.web("#FFFFFF66"));
                Blend blend = new Blend(BlendMode.SRC_OVER, glow, shadow);
                homeBtn.setEffect(blend);

                ScaleTransition scaleUp = new ScaleTransition(Duration.millis(140), homeBtn);
                scaleUp.setToX(1.08);
                scaleUp.setToY(1.08);
                scaleUp.play();
            });

            homeBtn.setOnMouseExited(ev -> {
                homeBtn.setEffect(null);
                ScaleTransition scaleDown = new ScaleTransition(Duration.millis(140), homeBtn);
                scaleDown.setToX(1.0);
                scaleDown.setToY(1.0);
                scaleDown.play();
            });

            homeBtn.setOnMouseClicked(e -> {
                try {
                    ModalUtils.hideAndClear(modalContainer);
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/HomeScreen.fxml"));
                    Parent root = loader.load();
                    Stage stage = (Stage) modalContainer.getScene().getWindow();
                    StageUtils.setSceneRoot(stage, root);
                } catch (Exception ex) {
                    System.err.println("Errore navigazione alla Home: " + ex.getMessage());
                }
            });

            cardPane.getChildren().add(homeBtn);
            homeBtn.toFront();
        } catch (Exception ignore) {}
    }

    private StackPane buildBreakdownList(StackPane cardPane, double targetWidth,
                                         PointService.ScoreBreakdown breakdown,
                                         boolean isVictory) {
        if (cardPane == null || breakdown == null) return null;

        VBox listBox = new VBox(10);
        listBox.setAlignment(Pos.TOP_CENTER);

        for (PointService.Entry e : breakdown.getEntries()) {
            HBox row = new HBox();
            row.setAlignment(Pos.CENTER_LEFT);
            Label left = new Label(e.title);
            Label right = new Label("+" + e.points);
            left.setTextFill(Color.web("#EAE7E3"));
            right.setTextFill(Color.web("#EAE7E3"));
            left.setFont(Font.font("Serif", FontWeight.SEMI_BOLD, 20));
            right.setFont(Font.font("Serif", FontWeight.BOLD, 20));
            HBox.setHgrow(left, Priority.ALWAYS);
            left.setMaxWidth(Double.MAX_VALUE);
            left.setAlignment(Pos.CENTER_LEFT);
            right.setAlignment(Pos.CENTER_RIGHT);
            row.getChildren().addAll(left, right);
            listBox.getChildren().add(row);
        }

        Rectangle divider = new Rectangle(0, 0, targetWidth - 160, 1);
        divider.setFill(Color.web("#EAE7E380"));
        listBox.getChildren().add(divider);

        HBox totalRow = new HBox();
        totalRow.setAlignment(Pos.CENTER_LEFT);
        Label totalLeft = new Label("Total");
        Label totalRight = new Label(String.valueOf(breakdown.getTotal()));
        totalLeft.setTextFill(Color.web("#FFFFFF"));
        totalRight.setTextFill(Color.web("#FFFFFF"));
        totalLeft.setFont(Font.font("Serif", FontWeight.BOLD, 22));
        totalRight.setFont(Font.font("Serif", FontWeight.EXTRA_BOLD, 22));
        HBox.setHgrow(totalLeft, Priority.ALWAYS);
        totalLeft.setMaxWidth(Double.MAX_VALUE);
        totalLeft.setAlignment(Pos.CENTER_LEFT);
        totalRight.setAlignment(Pos.CENTER_RIGHT);
        totalRow.getChildren().addAll(totalLeft, totalRight);
        listBox.getChildren().add(totalRow);

        StackPane listWrapper = new StackPane();
        listWrapper.setAlignment(Pos.TOP_CENTER);
        StackPane.setAlignment(listWrapper, Pos.TOP_CENTER);
        StackPane.setMargin(listWrapper, new Insets(230, 80, 0, 80));
        double bgWidth = targetWidth - 160;
        listWrapper.setMaxWidth(bgWidth);
        listWrapper.setPrefWidth(bgWidth);

        listWrapper.getChildren().add(listBox);
        cardPane.getChildren().add(listWrapper);
        listWrapper.toFront();
        listWrapper.setViewOrder(-1);
        return listWrapper;
    }

    public void showDefeat(StackPane modalContainer, PointService.ScoreBreakdown breakdown) {
        if (modalContainer == null) return;
        ModalUtils.show(modalContainer, ModalUtils.Type.ENDGAME);

        Image defeatImg;
        try {
            var url = getClass().getResource("/assets/endgame/defeat_summary.png");
            if (url == null) {
                throw new IllegalStateException("Resource /assets/endgame/defeat_summary.png not found on classpath");
            }
            defeatImg = new Image(url.toExternalForm());
        } catch (Exception e) {
            defeatImg = new Image("about:blank", 1, 1, false, false);
        }

        ImageView iv = new ImageView(defeatImg);
        iv.setPreserveRatio(true);
        iv.setSmooth(true);
        iv.setFitHeight(750.0); 
        StackPane cardPane = new StackPane(iv);
        cardPane.setAlignment(Pos.CENTER);

        double targetHeight = 750.0;
        double scale = targetHeight / defeatImg.getHeight();
        double targetWidth = defeatImg.getWidth() * scale;
        Rectangle clip = new Rectangle(targetWidth, targetHeight);
        clip.setArcWidth(46); 
        clip.setArcHeight(46);
        iv.setClip(clip);
        cardPane.setPrefSize(targetWidth, targetHeight);
        cardPane.setMaxSize(targetWidth, targetHeight);
        cardPane.setMinSize(targetWidth, targetHeight);

        Glow glow = new Glow(0.20);
        DropShadow shadow = new DropShadow();
        shadow.setRadius(22.0);
        shadow.setSpread(0.15);
        shadow.setColor(Color.web("#E8E1C966")); 
        Blend blend = new Blend(BlendMode.SRC_OVER, glow, shadow);
        cardPane.setEffect(blend);

        Text defeatTitle = new Text("DEFEAT");
        defeatTitle.setFill(Color.web("#8e0101ff"));
        defeatTitle.setStroke(null);
        defeatTitle.setStrokeWidth(0.0);
        defeatTitle.setFont(Font.font("Serif", FontWeight.BLACK, 34));
        DropShadow redGlow = new DropShadow();
        redGlow.setRadius(10.0);
        redGlow.setSpread(0.08);
        redGlow.setOffsetX(0.0);
        redGlow.setOffsetY(0.0);
        redGlow.setColor(Color.web("#FF000033"));
        defeatTitle.setEffect(redGlow);
        StackPane.setAlignment(defeatTitle, Pos.TOP_CENTER);
        StackPane.setMargin(defeatTitle, new Insets(115, 0, 0, 0));

        modalContainer.getChildren().add(cardPane);
        cardPane.getChildren().add(defeatTitle);
        defeatTitle.setViewOrder(-2);

        if (breakdown != null) {
            buildBreakdownList(cardPane, targetWidth, breakdown, false);
            defeatTitle.toFront();
        }

        attachHomeButton(cardPane, targetWidth, modalContainer);

        modalContainer.setOpacity(0.0);
        FadeTransition fadeContainer = new FadeTransition(Duration.millis(480), modalContainer);
        fadeContainer.setFromValue(0.0);
        fadeContainer.setToValue(1.0);
        fadeContainer.setInterpolator(Interpolator.EASE_BOTH);

        cardPane.setOpacity(0.0);
        ScaleTransition scaleIv = new ScaleTransition(Duration.millis(620), cardPane);
        scaleIv.setFromX(0.92);
        scaleIv.setFromY(0.92);
        scaleIv.setToX(1.0);
        scaleIv.setToY(1.0);
        scaleIv.setInterpolator(Interpolator.EASE_BOTH);

        FadeTransition fadeIv = new FadeTransition(Duration.millis(620), cardPane);
        fadeIv.setFromValue(0.0);
        fadeIv.setToValue(1.0);
        fadeIv.setInterpolator(Interpolator.EASE_BOTH);

        ParallelTransition pt = new ParallelTransition(fadeContainer, scaleIv, fadeIv);
        pt.play();
    }

    public void showVictory(StackPane modalContainer, PointService.ScoreBreakdown breakdown) {
        if (modalContainer == null) return;
        ModalUtils.show(modalContainer, ModalUtils.Type.ENDGAME);

        Image victoryImg;
        try {
            var url = getClass().getResource("/assets/endgame/win_summary.png");
            if (url == null) {
                throw new IllegalStateException("Resource /assets/endgame/win_summary.png not found on classpath");
            }
            victoryImg = new Image(url.toExternalForm());
        } catch (Exception e) {
            victoryImg = new Image("about:blank", 1, 1, false, false);
        }

        ImageView iv = new ImageView(victoryImg);
        iv.setPreserveRatio(true);
        iv.setSmooth(true);
        iv.setFitHeight(750.0);
        StackPane cardPane = new StackPane(iv);
        cardPane.setAlignment(Pos.CENTER);

        double targetHeight = 750.0;
        double scale = targetHeight / victoryImg.getHeight();
        double targetWidth = victoryImg.getWidth() * scale;
        Rectangle clip = new Rectangle(targetWidth, targetHeight);
        clip.setArcWidth(46);
        clip.setArcHeight(46);
        iv.setClip(clip);
        cardPane.setPrefSize(targetWidth, targetHeight);
        cardPane.setMaxSize(targetWidth, targetHeight);
        cardPane.setMinSize(targetWidth, targetHeight);

        Glow glow = new Glow(0.20);
        DropShadow shadow = new DropShadow();
        shadow.setRadius(22.0);
        shadow.setSpread(0.15);
        shadow.setColor(Color.web("#E8E1C966"));
        Blend blend = new Blend(BlendMode.SRC_OVER, glow, shadow);
        cardPane.setEffect(blend);

        Text victoryTitle = new Text("VICTORY");
        victoryTitle.setFill(Color.WHITE);
        victoryTitle.setStroke(null);
        victoryTitle.setStrokeWidth(0.0);
        victoryTitle.setFont(Font.font("Serif", FontWeight.BLACK, 34));
        victoryTitle.setEffect(null);
        StackPane.setAlignment(victoryTitle, Pos.TOP_CENTER);
        StackPane.setMargin(victoryTitle, new Insets(59, 0, 0, 0));
        modalContainer.getChildren().add(cardPane);
        cardPane.getChildren().add(victoryTitle);
        victoryTitle.setViewOrder(-2);

        if (breakdown != null) {
            buildBreakdownList(cardPane, targetWidth, breakdown, true);
            victoryTitle.toFront();
        }

        attachHomeButton(cardPane, targetWidth, modalContainer);

        modalContainer.setOpacity(0.0);
        FadeTransition fadeContainer = new FadeTransition(Duration.millis(480), modalContainer);
        fadeContainer.setFromValue(0.0);
        fadeContainer.setToValue(1.0);
        fadeContainer.setInterpolator(Interpolator.EASE_BOTH);

        cardPane.setOpacity(0.0);
        ScaleTransition scaleIv = new ScaleTransition(Duration.millis(620), cardPane);
        scaleIv.setFromX(0.92);
        scaleIv.setFromY(0.92);
        scaleIv.setToX(1.0);
        scaleIv.setToY(1.0);
        scaleIv.setInterpolator(Interpolator.EASE_BOTH);

        FadeTransition fadeIv = new FadeTransition(Duration.millis(620), cardPane);
        fadeIv.setFromValue(0.0);
        fadeIv.setToValue(1.0);
        fadeIv.setInterpolator(Interpolator.EASE_BOTH);

        ParallelTransition pt = new ParallelTransition(fadeContainer, scaleIv, fadeIv);
        pt.play();
    }
}