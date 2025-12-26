package view.manager;

import javafx.animation.*;
import javafx.scene.Node;
import javafx.scene.effect.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.util.Duration;
import java.util.function.Consumer;

/**
 * Manages the UI representation of the player's lives.
 * Handles rendering of heart icons and animations for losing or gaining lives.
 */
public class LivesUIManager {
    private final HBox livesHBox;
    private final String heartResourcePath;
    private int lives = 0;
    private Consumer<Integer> onLivesChanged;
    private ScaleTransition lowLifePulse;
    private ImageView lowLifeHeartRef;

    public LivesUIManager(HBox livesHBox, String heartResourcePath) {
        this.livesHBox = livesHBox;
        this.heartResourcePath = heartResourcePath;
    }

    public void renderLives(int livesCount) {
        if (livesHBox == null) return;
        if (lowLifePulse != null) {
            try { lowLifePulse.stop(); } catch (Exception ignore) {}
            lowLifePulse = null;
        }
        if (lowLifeHeartRef != null) {
            try {
                lowLifeHeartRef.setScaleX(1.0);
                lowLifeHeartRef.setScaleY(1.0);
                lowLifeHeartRef.setEffect(null);
            } catch (Exception ignore) {}
            lowLifeHeartRef = null;
        }
        livesHBox.getChildren().clear();
        for (int i = 0; i < livesCount; i++) {
            ImageView heart = new ImageView(new Image(
                getClass().getResourceAsStream(heartResourcePath)
            ));
            heart.setPreserveRatio(true);
            heart.setFitHeight(92);
            heart.setSmooth(true);
            heart.setMouseTransparent(true);
            heart.setEffect(createBaseHeartEffect());
            livesHBox.getChildren().add(heart);
        }

        applyLowLifeHighlightIfNeeded();
    }

    public void setOnLivesChanged(Consumer<Integer> callback) {
        this.onLivesChanged = callback;
    }

    public void setLives(int count) {
        this.lives = Math.max(0, count);
        renderLives(this.lives);
        if (onLivesChanged != null) onLivesChanged.accept(this.lives);
    }

    public int getLives() {
        return lives;
    }

    public void animateLoss(Runnable afterAnimation) {
        if (livesHBox == null || livesHBox.getChildren().isEmpty()) {
            if (afterAnimation != null) afterAnimation.run();
            return;
        }
        Node node = livesHBox.getChildren().get(livesHBox.getChildren().size() - 1);
        if (!(node instanceof ImageView heart)) {
            if (afterAnimation != null) afterAnimation.run();
            return;
        }

        DropShadow intenseGlow = new DropShadow();
        intenseGlow.setRadius(18);
        intenseGlow.setColor(Color.rgb(255, 0, 0, 0.75));
        heart.setEffect(intenseGlow);

        ScaleTransition pulseUp = new ScaleTransition(Duration.millis(160), heart);
        pulseUp.setToX(1.35);
        pulseUp.setToY(1.35);

        TranslateTransition shake = new TranslateTransition(Duration.millis(180), heart);
        shake.setFromX(0);
        shake.setToX(12);
        shake.setAutoReverse(true);
        shake.setCycleCount(2);

        FadeTransition fade = new FadeTransition(Duration.millis(220), heart);
        fade.setFromValue(1.0);
        fade.setToValue(0.0);

        SequentialTransition seq = new SequentialTransition(pulseUp, shake, fade);
        seq.setOnFinished(ev -> {
            if (afterAnimation != null) afterAnimation.run();
        });
        seq.play();
    }

    public void loseLifeWithAnimation() {
        if (lives <= 0) return;
        lives = Math.max(0, lives - 1);
        animateLoss(() -> {
            renderLives(lives);
            if (onLivesChanged != null) onLivesChanged.accept(lives);
        });
    }

    public void gainLifeWithAnimation(int maxLives) {
        if (livesHBox == null) return;
        if (lives >= maxLives) return;
        lives = Math.min(maxLives, lives + 1);
        renderLives(lives);
        if (onLivesChanged != null) onLivesChanged.accept(lives);
        if (!livesHBox.getChildren().isEmpty()) {
            Node node = livesHBox.getChildren().get(livesHBox.getChildren().size() - 1);
            if (node instanceof ImageView heart) {
                DropShadow healGlow = new DropShadow();
                healGlow.setRadius(16);
                healGlow.setColor(Color.rgb(80, 255, 140, 0.75));
                Effect base = createBaseHeartEffect();
                healGlow.setInput(base);
                heart.setEffect(healGlow);

                heart.setOpacity(0.0);
                heart.setScaleX(0.6);
                heart.setScaleY(0.6);
                FadeTransition fadeIn = new FadeTransition(Duration.millis(220), heart);
                fadeIn.setToValue(1.0);
                ScaleTransition pop = new ScaleTransition(Duration.millis(220), heart);
                pop.setToX(1.0);
                pop.setToY(1.0);
                SequentialTransition seq = new SequentialTransition(fadeIn, pop);
                seq.setOnFinished(e -> heart.setEffect(createBaseHeartEffect()));
                seq.play();
            }
        }
    }

    private void applyLowLifeHighlightIfNeeded() {
        if (livesHBox == null) return;
        if (lives == 1 && !livesHBox.getChildren().isEmpty()) {
            Node node = livesHBox.getChildren().get(0);
            if (node instanceof ImageView heart) {
                lowLifeHeartRef = heart;
                DropShadow intenseGlow = new DropShadow();
                intenseGlow.setRadius(22);
                intenseGlow.setColor(Color.rgb(255, 40, 40, 0.85));
                Effect base = createBaseHeartEffect();
                intenseGlow.setInput(base);
                heart.setEffect(intenseGlow);

                lowLifePulse = new ScaleTransition(Duration.millis(650), heart);
                lowLifePulse.setFromX(1.0);
                lowLifePulse.setFromY(1.0);
                lowLifePulse.setToX(1.18);
                lowLifePulse.setToY(1.18);
                lowLifePulse.setAutoReverse(true);
                lowLifePulse.setCycleCount(ScaleTransition.INDEFINITE);
                lowLifePulse.play();
            }
        }
    }

    private Effect createBaseHeartEffect() {
        ColorAdjust adjust = new ColorAdjust();
        adjust.setBrightness(0.06);
        adjust.setSaturation(0.12);

        DropShadow edgeShadow = new DropShadow();
        edgeShadow.setRadius(10);
        edgeShadow.setSpread(0.20);
        edgeShadow.setColor(Color.rgb(0, 0, 0, 0.45));
        edgeShadow.setOffsetX(0);
        edgeShadow.setOffsetY(0);
        edgeShadow.setInput(adjust);

        DropShadow subtleGlow = new DropShadow();
        subtleGlow.setRadius(12);
        subtleGlow.setSpread(0.10);
        subtleGlow.setColor(Color.rgb(255, 80, 80, 0.35));
        subtleGlow.setOffsetX(0);
        subtleGlow.setOffsetY(0);
        subtleGlow.setInput(edgeShadow);

        return subtleGlow;
    }
}