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

// คลาสนี้สืบทอดจาก StackPane เพื่อให้มันทำหน้าที่เป็น UI Component ตัวนึงได้เลย
public class CatButton extends StackPane {

    private boolean isCooldown = false; // ตัวแปรเช็คว่ากำลังคูลดาวน์อยู่ไหม

    // Constructor รับค่า: พาทรูปภาพ, เวลาคูลดาวน์, และ คำสั่งที่จะให้ทำตอนกดเสกแมว
    public CatButton(String imagePath, double cooldownTime, Runnable onSpawnAction) {

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

        // 2. แผ่นฟิล์มคูลดาวน์
        Rectangle cooldownDim = new Rectangle(imgW, 0);
        cooldownDim.setFill(Color.BLACK);
        cooldownDim.setOpacity(0.65);
        StackPane.setAlignment(cooldownDim, Pos.TOP_CENTER);

        // 3. Clipping
        Rectangle clip = new Rectangle(imgW, imgH);
        this.setClip(clip);

        this.getChildren().addAll(catIcon, cooldownDim);

        // 4. การคลิกปุ่ม
        this.setOnMouseClicked(e -> {
            if (!isCooldown) {
                // รันคำสั่งเสกแมวที่ส่งเข้ามา
                onSpawnAction.run();

                // เริ่มคูลดาวน์
                isCooldown = true;
                cooldownDim.setHeight(imgH);

                Timeline timeline = new Timeline();
                KeyValue kv = new KeyValue(cooldownDim.heightProperty(), 0);
                KeyFrame kf = new KeyFrame(Duration.seconds(cooldownTime), kv);

                timeline.getKeyFrames().add(kf);
                timeline.setOnFinished(event -> isCooldown = false); // เมื่อเสร็จ ให้กดได้ใหม่
                timeline.play();
            }
        });
    }
}