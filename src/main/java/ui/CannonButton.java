package ui;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.scene.Cursor;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

import java.util.Objects;

public class CannonButton extends StackPane {
    private boolean isReady = true;
    private ImageView icon;
    private Rectangle cooldownOverlay;

    public CannonButton(String imagePath, double cooldownSeconds, Runnable onFire) {
        this.setPrefSize(90, 95);
        this.setCursor(Cursor.HAND);

        icon = new ImageView(new Image(Objects.requireNonNull(getClass().getResourceAsStream(imagePath))));
        icon.setFitWidth(150);
        icon.setFitHeight(150);
        icon.setPreserveRatio(true);

        // แผ่นคูลดาวน์ (จากล่างขึ้นบน)
        cooldownOverlay = new Rectangle(120, 0);
        cooldownOverlay.setFill(Color.BLACK);
        cooldownOverlay.setOpacity(0.6);
        StackPane.setAlignment(cooldownOverlay, javafx.geometry.Pos.BOTTOM_CENTER);

        this.getChildren().addAll(icon, cooldownOverlay);

        this.setOnMouseClicked(e -> {
            if (isReady) {
                isReady = false;
                onFire.run();
                startCooldown(cooldownSeconds);
            }
        });
    }

    // ใน CannonButton.java
    private void startCooldown(double seconds) {
        isReady = false;
        this.setVisible(false); // ซ่อนปุ่ม
        this.setManaged(false); // ไม่จองพื้นที่

        icon.setOpacity(0.5);
        cooldownOverlay.setHeight(120);

        Timeline timeline = new Timeline(
                new KeyFrame(Duration.seconds(seconds), new KeyValue(cooldownOverlay.heightProperty(), 0))
        );

        timeline.setOnFinished(e -> {
            isReady = true;
            icon.setOpacity(1.0);
            // 🌟 เรียกใช้คำสั่งให้ปุ่มกลับมาแสดงผลที่นี่
            this.setVisible(true);
            this.setManaged(true);
        });
        timeline.play();
    }
}