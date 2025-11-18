package view.manager;

import javafx.geometry.Pos;
import javafx.geometry.Insets;
import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.Interpolator;
import javafx.util.Duration;
import javafx.scene.effect.Blend;
import javafx.scene.effect.BlendMode;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.Glow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.effect.InnerShadow;
import view.util.ModalUtils;

public class EndGameManager {

    public void showDefeat(StackPane modalContainer) {
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

        Glow glow = new Glow(0.20);
        DropShadow shadow = new DropShadow();
        shadow.setRadius(22.0);
        shadow.setSpread(0.15);
        shadow.setColor(Color.web("#E8E1C966")); 
        Blend blend = new Blend(BlendMode.SRC_OVER, glow, shadow);
        cardPane.setEffect(blend);

        Text defeatTitle = new Text("Defeat");
        defeatTitle.setFill(Color.web("#F3F3F3")); 
        defeatTitle.setStroke(Color.web("#00000099"));
        defeatTitle.setStrokeWidth(1.2);
        defeatTitle.setFont(Font.font("Serif", FontWeight.BLACK, 26));
        InnerShadow innerDust = new InnerShadow();
        innerDust.setRadius(6.0);
        innerDust.setChoke(0.22);
        innerDust.setColor(Color.web("#FFFFFF33"));
        DropShadow outerShade = new DropShadow();
        outerShade.setRadius(18.0);
        outerShade.setSpread(0.10);
        outerShade.setOffsetY(2.0);
        outerShade.setColor(Color.web("#00000066"));
        defeatTitle.setEffect(new Blend(BlendMode.SRC_OVER, innerDust, outerShade));
        StackPane.setAlignment(defeatTitle, Pos.TOP_CENTER);
        StackPane.setMargin(defeatTitle, new Insets(100, 0, 0, 0));

        modalContainer.getChildren().add(cardPane);
        cardPane.getChildren().add(defeatTitle);

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