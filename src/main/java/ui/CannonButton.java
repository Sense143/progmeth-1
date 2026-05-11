package ui;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.Cursor;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

import java.util.Objects;

/**
 * A special-ability button for the player's cannon attack.
 *
 * <p>Displays a "ready" icon when the cannon can fire and automatically
 * switches to a "cooldown" icon after each use. The cooldown icon reverts
 * to the ready icon once the cooldown timer expires.
 *
 * <p>The cooldown image is derived from the ready image path by replacing
 * {@code _1.png} with {@code _2.png}.
 */
public class CannonButton extends StackPane {
    /** Whether the cannon is ready to fire (not on cooldown). */
    private boolean isReady = true;
    private ImageView icon;
    private Image readyImage;
    private Image cooldownImage;

    /**
     * Constructs a cannon button.
     *
     * @param imagePath       classpath resource path to the ready-state icon
     *                        (must end with {@code _1.png})
     * @param cooldownSeconds seconds the button is locked after each fire
     * @param onFire          action executed when the button is clicked while ready
     */
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

    /**
     * Switches the icon to the cooldown image and schedules a timer to
     * revert to the ready image after {@code seconds}.
     *
     * @param seconds cooldown duration in seconds
     */
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