package ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import java.util.Objects;
import java.util.function.BooleanSupplier;

public class LevelUpButton extends Button {

    private int currentLevel = 1;
    private int currentCost;
    private boolean maxLevel = false;
    private Label textLabel;
    private ImageView icon; // 🌟 ย้ายมาประกาศตรงนี้เพื่อให้เรียกใช้ใน updateState ได้

    public LevelUpButton(String imagePath, int startCost, BooleanSupplier onUpgradeAction) {
        this.currentCost = startCost;

        this.setPrefSize(90, 95);
        this.setCursor(Cursor.HAND);
        this.setStyle("-fx-background-color: transparent;");

        this.setOnMousePressed(e -> this.setOpacity(0.6));
        this.setOnMouseReleased(e -> this.setOpacity(1.0));

        StackPane graphicContainer = new StackPane();

        try {
            // 🌟 ใช้ this.icon เพื่อเก็บอ้างอิงตัวรูปไว้
            this.icon = new ImageView(new Image(Objects.requireNonNull(getClass().getResourceAsStream(imagePath))));
            this.icon.setFitWidth(150);
            this.icon.setFitHeight(150);
            this.icon.setPreserveRatio(true);

            textLabel = new Label();
            // ปรับฟอนต์ให้ใหญ่และชัดขึ้นตามที่คุณแก้มา (20px)
            textLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: white; -fx-effect: dropshadow(gaussian, black, 3, 1.0, 0, 0);");

            StackPane.setAlignment(textLabel, Pos.BOTTOM_CENTER);
            StackPane.setMargin(textLabel, new Insets(0, 0, 5, 0));

            graphicContainer.getChildren().addAll(icon, textLabel);
            this.setGraphic(graphicContainer);
            this.setContentDisplay(ContentDisplay.CENTER);

        } catch (Exception e) {
            System.err.println("หาภาพปุ่มไม่เจอ: " + imagePath);
        }

        updateText();

        this.setOnAction(e -> {
            boolean success = onUpgradeAction.getAsBoolean();
            if (success) {
                currentLevel++;
                updateText();
            }
        });
    }

    private void updateText() {
        if (textLabel == null) return;
        if (maxLevel) {
            textLabel.setText("MAX LEVEL");
        } else {
            textLabel.setText("Lv." + currentLevel + " | $" + currentCost);
        }
    }

    public void markMaxLevel() {
        maxLevel = true;
        if (icon != null) icon.setOpacity(1.0);
        this.setDisable(true);
        updateText();
    }

    public void setNextCost(int newCost) {
        this.currentCost = newCost;
        updateText();
    }

    // 🌟 แก้ไข Logic ตรงนี้ใหม่
    public void updateState(int playerMoney) {
        if (maxLevel) return;
        if (playerMoney < currentCost) {
            // ลดความสว่างเฉพาะตัวรูป (Icon)
            if (icon != null) icon.setOpacity(0.5);

            // ตัวปุ่มหลัก (และตัวหนังสือ) ให้คงความชัดไว้ 100%
            this.setOpacity(1.0);
            this.setDisable(true);
        } else {
            // กลับมาสว่างปกติทั้งรูปและปุ่ม
            if (icon != null) icon.setOpacity(1.0);
            this.setDisable(false);
        }
    }
}