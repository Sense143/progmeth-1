package ui;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.Cursor;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

import java.util.Objects;

public class CannonButton extends StackPane {
    private boolean isReady = true;
    private ImageView icon;
    private Image readyImage;
    private Image cooldownImage;

    public CannonButton(String imagePath, double cooldownSeconds, Runnable onFire) {
        this.setPrefSize(90, 95);
        this.setCursor(Cursor.HAND);

        readyImage = new Image(Objects.requireNonNull(getClass().getResourceAsStream(imagePath)));
        String cooldownPath = imagePath.replace("_1.png", "_2.png");
        cooldownImage = new Image(Objects.requireNonNull(getClass().getResourceAsStream(cooldownPath)));

        icon = new ImageView(readyImage);
        icon.setFitWidth(150);
        icon.setFitHeight(150);
        icon.setPreserveRatio(true);

        this.getChildren().add(icon);

        this.setOnMouseClicked(e -> {
            if (isReady) {
                isReady = false;
                onFire.run();
                startCooldown(cooldownSeconds);
            }
        });
    }

    private void startCooldown(double seconds) {
        isReady = false;
        icon.setImage(cooldownImage);

        Timeline timeline = new Timeline(
                new KeyFrame(Duration.seconds(seconds))
        );

        timeline.setOnFinished(e -> {
            isReady = true;
            icon.setImage(readyImage);
        });
        timeline.play();
    }
}