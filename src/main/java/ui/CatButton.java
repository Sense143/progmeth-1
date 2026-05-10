package ui;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;
import java.util.Objects;
import java.util.function.BooleanSupplier;

public class CatButton extends StackPane {

    private boolean isCooldown = false;
    private int cost;
    private Rectangle moneyDim;

    public CatButton(String imagePath, double cooldownTime, int cost, BooleanSupplier onSpawnAction) {
        this.cost = cost;

        double imgW = 100;
        double imgH = 80;

        this.setMinSize(imgW, imgH);
        this.setMaxSize(imgW, imgH);
        this.setPrefSize(imgW, imgH);
        this.setCursor(Cursor.HAND);

        // 1. รูปไอคอน
        ImageView catIcon = new ImageView();
        try {
            catIcon.setImage(new Image(Objects.requireNonNull(getClass().getResourceAsStream(imagePath))));
            catIcon.setFitWidth(imgW);
            catIcon.setFitHeight(imgH);
            catIcon.setPreserveRatio(false);
        } catch (Exception err) {
            System.err.println("หาภาพไม่เจอ: " + imagePath);
        }

        // 2. แผ่นฟิล์มตอนเงินไม่พอ
        moneyDim = new Rectangle(imgW, imgH);
        moneyDim.setFill(Color.BLACK);
        moneyDim.setOpacity(0.65);
        moneyDim.setVisible(true);

        // 3. แผ่นฟิล์มคูลดาวน์
        Rectangle cooldownDim = new Rectangle(imgW, 0);
        cooldownDim.setFill(Color.BLACK);
        cooldownDim.setOpacity(0.65);
        StackPane.setAlignment(cooldownDim, Pos.TOP_CENTER);

        // 4. Clipping
        Rectangle clip = new Rectangle(imgW, imgH);
        this.setClip(clip);

        // 5. สร้างป้ายบอกราคา
        Label priceLabel = new Label("$" + cost);
        priceLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px; -fx-effect: dropshadow(gaussian, black, 2, 1.0, 0, 0);");
        StackPane.setAlignment(priceLabel, Pos.BOTTOM_RIGHT);
        StackPane.setMargin(priceLabel, new Insets(0, 5, 2, 0));

        // นำไปซ้อนกัน
        this.getChildren().addAll(catIcon, moneyDim, cooldownDim, priceLabel);

        // 6. การคลิกปุ่ม
        this.setOnMouseClicked(e -> {
            if (!isCooldown) {
                boolean success = onSpawnAction.getAsBoolean();
                if (success) {
                    isCooldown = true;
                    moneyDim.setVisible(false);
                    cooldownDim.setHeight(imgH);

                    Timeline timeline = new Timeline();
                    KeyValue kv = new KeyValue(cooldownDim.heightProperty(), 0);
                    KeyFrame kf = new KeyFrame(Duration.seconds(cooldownTime), kv);

                    timeline.getKeyFrames().add(kf);
                    timeline.setOnFinished(event -> isCooldown = false);
                    timeline.play();
                }
            }
        });
    }

    public void updateState(int currentMoney) {
        if (isCooldown) {
            moneyDim.setVisible(false);
        } else {
            moneyDim.setVisible(currentMoney < cost);
        }
    }
}