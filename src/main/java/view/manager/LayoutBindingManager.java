package view.manager;

import javafx.scene.Scene;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;

public class LayoutBindingManager {

    public void bindToSceneSize(Scene scene,
                                StackPane mainGameArea,
                                ImageView headerImageView,
                                ImageView footerImageView) {
        if (scene == null) return;
        if (headerImageView != null) {
            headerImageView.setPreserveRatio(false);
            headerImageView.setSmooth(true);
            headerImageView.setFitWidth(scene.getWidth());
        }
        if (footerImageView != null) {
            footerImageView.setPreserveRatio(false);
            footerImageView.setSmooth(true);
            footerImageView.setFitWidth(scene.getWidth());
        }

        scene.widthProperty().addListener((o, ov, nv) -> {
            double w = nv.doubleValue();
            if (headerImageView != null) headerImageView.setFitWidth(w);
            if (footerImageView != null) footerImageView.setFitWidth(w);
        });

        if (mainGameArea != null) {
            if (headerImageView != null && footerImageView != null) {
                mainGameArea.prefHeightProperty().bind(scene.heightProperty()
                    .subtract(headerImageView.fitHeightProperty())
                    .subtract(footerImageView.fitHeightProperty()));
            } else {
                mainGameArea.prefHeightProperty().bind(scene.heightProperty().subtract(220.0));
            }
            mainGameArea.minHeightProperty().bind(mainGameArea.prefHeightProperty());
            mainGameArea.maxHeightProperty().bind(mainGameArea.prefHeightProperty());
        }
    }

    public void bindBackgroundToMainArea(StackPane mainGameArea,
                                         ImageView backgroundImageView,
                                         AnchorPane gameContentPane) {
        if (backgroundImageView == null || mainGameArea == null) return;
        backgroundImageView.setPreserveRatio(false);
        backgroundImageView.setSmooth(true);
        Runnable apply = () -> {
            double w = mainGameArea.getWidth();
            double h = mainGameArea.getHeight();
            if (w > 0 && h > 0) {
                backgroundImageView.setFitWidth(w);
                backgroundImageView.setFitHeight(h);
            }
        };
        apply.run();
        mainGameArea.widthProperty().addListener((o, ov, nv) -> apply.run());
        mainGameArea.heightProperty().addListener((o, ov, nv) -> apply.run());
        mainGameArea.layoutBoundsProperty().addListener((o, ov, nv) -> apply.run());

        if (gameContentPane != null) {
            gameContentPane.prefHeightProperty().bind(mainGameArea.heightProperty());
            gameContentPane.minHeightProperty().bind(gameContentPane.prefHeightProperty());
            gameContentPane.maxHeightProperty().bind(gameContentPane.prefHeightProperty());
        }
    }
}