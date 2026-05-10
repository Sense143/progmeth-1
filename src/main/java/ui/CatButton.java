package ui;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
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

    // 🌟 เพิ่มแผ่นฟิล์มดำสำหรับเช็คเงิน (มืดเท่าแผ่นคูลดาวน์)
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

        // 🌟 2. แผ่นฟิล์มตอนเงินไม่พอ (มืด 0.65 เท่ากับแผ่นคูลดาวน์)
        moneyDim = new Rectangle(imgW, imgH);
        moneyDim.setFill(Color.BLACK);
        moneyDim.setOpacity(0.65);
        moneyDim.setVisible(true); // เริ่มเกมมาเงินยังเป็น 0 ให้ปิดฟิล์มนี้ไว้เลย

        // 3. แผ่นฟิล์มคูลดาวน์
        Rectangle cooldownDim = new Rectangle(imgW, 0);
        cooldownDim.setFill(Color.BLACK);
        cooldownDim.setOpacity(0.65);
        StackPane.setAlignment(cooldownDim, Pos.TOP_CENTER);

        // 4. Clipping
        Rectangle clip = new Rectangle(imgW, imgH);
        this.setClip(clip);

        // 🌟 นำฟิล์มเงินไปซ้อนก่อนฟิล์มคูลดาวน์
        this.getChildren().addAll(catIcon, moneyDim, cooldownDim);

        // 5. การคลิกปุ่ม
        this.setOnMouseClicked(e -> {
            if (!isCooldown) {
                // ลองรันคำสั่งเสกแมว
                boolean success = onSpawnAction.getAsBoolean();

                if (success) {
                    isCooldown = true;

                    // 🌟 พอซื้อสำเร็จให้ซ่อนแผ่นเงินไปก่อน แล้วให้แผ่นคูลดาวน์กางออกแทน
                    moneyDim.setVisible(false);
                    cooldownDim.setHeight(imgH);

                    Timeline timeline = new Timeline();
                    KeyValue kv = new KeyValue(cooldownDim.heightProperty(), 0);
                    KeyFrame kf = new KeyFrame(Duration.seconds(cooldownTime), kv);

                    timeline.getKeyFrames().add(kf);
                    timeline.setOnFinished(event -> isCooldown = false); // เมื่อเสร็จ ให้กดได้ใหม่
                    timeline.play();
                }
            }
        });
    }

    // 🌟 เมธอดอัปเดตสถานะปุ่ม
    public void updateState(int currentMoney) {
        if (isCooldown) {
            // ถ้ากำลังคูลดาวน์อยู่ ซ่อนแผ่นเงินไปเลย จะได้ไม่มืดซ้อนกัน 2 ชั้น
            moneyDim.setVisible(false);
        } else {
            // ถ้าไม่ได้คูลดาวน์ ให้โชว์/ซ่อน แผ่นเงินตามจำนวนเงินที่มี
            if (currentMoney < cost) {
                moneyDim.setVisible(true);  // เงินไม่พอ กางแผ่นดำ
            } else {
                moneyDim.setVisible(false); // เงินพอแล้ว ซ่อนแผ่นดำ
            }
        }
    }
}