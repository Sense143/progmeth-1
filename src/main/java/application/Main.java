package application;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.Duration;

import models.base.Tower;
import models.base.Unit;
import models.enemies.DogTower;
import models.stages.*;
import models.units.*;
import logic.BattleManager;
import ui.CatButton; // 🌟 Import ปุ่มแมว

import java.util.ArrayList;
import java.util.Objects;

public class Main extends Application {

    private StackPane root;
    private BorderPane gameUI;

    private Label moneyLabel;

    // 🌟 ตัวแปรเก็บปุ่มแมวทั้งหมด เพื่อเอาไว้อัปเดตสถานะตอนเงินเด้ง
    private ArrayList<CatButton> catButtons = new ArrayList<>();

    private ArrayList<Unit> units = new ArrayList<>();
//    private java.util.concurrent.CopyOnWriteArrayList<Unit> units = new java.util.concurrent.CopyOnWriteArrayList<>();
    private Tower catTower;
    private Tower dogTower;

    private volatile boolean isPaused = false;
    private volatile boolean running = false;
    private Thread gameThread;

    private GameStage selectedStage;

    private final double WORLD_WIDTH = 2500;
    private final double SCREEN_WIDTH = 1000;
    private double cameraX = 0;
    private double dragLastX = 0;

    private ui.LevelUpButton levelUpButton;

    @Override
    public void start(Stage primaryStage) {
        root = new StackPane();
        showMainMenu();
        Scene scene = new Scene(root, SCREEN_WIDTH, 600);
        primaryStage.setTitle("Battle Cat - Complete Edition");
        primaryStage.setScene(scene);
        primaryStage.setOnCloseRequest(e -> running = false);
        primaryStage.show();
    }

    private void showGameScene() {
        root.getChildren().clear();
        units.clear();
        catButtons.clear(); // 🌟 เคลียร์ปุ่มเก่าตอนเริ่มด่าน
        BattleManager.getInstance().clearAll();

        logic.MoneyManager.getInstance().reset();

        isPaused = false;
        running = true;
        cameraX = WORLD_WIDTH - SCREEN_WIDTH;

        Canvas canvas = new Canvas(SCREEN_WIDTH, 600);
        GraphicsContext gc = canvas.getGraphicsContext2D();

        canvas.setOnMousePressed(e -> dragLastX = e.getSceneX());
        canvas.setOnMouseDragged(e -> {
            double deltaX = dragLastX - e.getSceneX();
            cameraX += deltaX;
            if (cameraX < 0) cameraX = 0;
            if (cameraX > WORLD_WIDTH - SCREEN_WIDTH) cameraX = WORLD_WIDTH - SCREEN_WIDTH;
            dragLastX = e.getSceneX();
        });

        gameUI = new BorderPane();
        gameUI.setPickOnBounds(false);

        // --- แถบด้านบน ---
        HBox topBar = new HBox(10);
        topBar.setPadding(new Insets(15));
        topBar.setAlignment(Pos.CENTER_LEFT);

        Button pauseButton = new Button("||");
        pauseButton.setOnAction(e -> showPauseOverlay());

        Label stageNameLabel = new Label(selectedStage.getStageName());
        stageNameLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: black;");

        Region topSpacer = new Region();
        HBox.setHgrow(topSpacer, Priority.ALWAYS);

        moneyLabel = new Label("0/150");
        moneyLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: black; -fx-font-size: 18px;");

        topBar.getChildren().addAll(pauseButton, stageNameLabel, topSpacer, moneyLabel);
        gameUI.setTop(topBar);

        // --- แถบด้านล่าง ---
        HBox bottomTray = new HBox(10);
        bottomTray.setPadding(new Insets(30, 30, 10, 30));
        bottomTray.setAlignment(Pos.BOTTOM_LEFT);

        // 🌟 2. เปลี่ยนมาใช้ LevelUpButton ที่เราสร้างไว้
        int startCost = logic.MoneyManager.getInstance().getUpgradeCost();

        levelUpButton = new ui.LevelUpButton("/button/moneyBTN_1.png", startCost, () -> {
            // เมื่อกดปุ่ม ให้ไปเรียกใช้คำสั่งอัปเกรดที่ MoneyManager เตรียมไว้แล้ว
            boolean success = logic.MoneyManager.getInstance().upgradeWallet();

            if (success) {
                // 🌟 สำคัญ: ถ้าอัปเกรดสำเร็จ ให้บอกราคาใหม่กับปุ่มด้วย
                int newCost = logic.MoneyManager.getInstance().getUpgradeCost();
                levelUpButton.setNextCost(newCost);
                return true;
            }
            return false;
        });
        levelUpButton.setTranslateX(-40);
        levelUpButton.setTranslateY(14);

        Region spacer1 = new Region();
        HBox.setHgrow(spacer1, Priority.ALWAYS);

        // กลุ่มปุ่มแมว
        HBox catsBox = new HBox(10);
        catsBox.setAlignment(Pos.BOTTOM_CENTER);

        // 🌟 สร้างปุ่มแมวโดยส่ง พารามิเตอร์: ภาพ, เวลาคูลดาวน์(วินาที), ราคา, เมธอดทำงาน
        CatButton cat1Btn = new CatButton("/cat/icon/uni000_f00.png", 2.5, 50, () -> trySpawnUnit(new Cat1(), 50));
        CatButton tofuBtn = new CatButton("/cat/icon/uni001_c00.png", 3.0, 100, () -> trySpawnUnit(new TofuCat(), 100));
        CatButton knightBtn = new CatButton("/cat/icon/uni002_c00.png", 5.0, 150, () -> trySpawnUnit(new KnightCat(), 150));
        CatButton fishBtn = new CatButton("/cat/icon/uni006_f00.png", 10.0, 400, () -> trySpawnUnit(new FishCat(), 400));
        CatButton ufoBtn = new CatButton("/cat/icon/uni005_c00.png", 15.0, 600, () -> trySpawnUnit(new UFOCat(), 600));

        // 🌟 เก็บลงลิสต์เพื่อให้ Game Loop เอาไปอัปเดตสี
        catButtons.add(cat1Btn);
        catButtons.add(tofuBtn);
        catButtons.add(knightBtn);
        catButtons.add(fishBtn);
        catButtons.add(ufoBtn);

        catsBox.getChildren().addAll(cat1Btn, tofuBtn, knightBtn, fishBtn, ufoBtn);

        Region spacer2 = new Region();
        HBox.setHgrow(spacer2, Priority.ALWAYS);

        Button cannonBtn = createImageButton("/button/cannonBTN_1.png", 150, 150, e -> {});
        cannonBtn.setTranslateX(40);
        cannonBtn.setTranslateY(23);

        bottomTray.getChildren().addAll(levelUpButton, spacer1, catsBox, spacer2, cannonBtn);
        gameUI.setBottom(bottomTray);

        // --- สร้างป้อมทัพ ---
        catTower = new CatTower();
        dogTower = new DogTower(selectedStage.getEnemyTowerHp(), selectedStage.getEnemyTowerImagePath());

        units.add(catTower);
        units.add(dogTower);
        BattleManager.getInstance().addPlayerUnit(catTower);
        BattleManager.getInstance().addEnemyUnit(dogTower);

        root.getChildren().addAll(canvas, gameUI);
        startGameThread(gc);
    }

    private void startGameThread(GraphicsContext gc) {
        gameThread = new Thread(() -> {
            while (running) {
                if (!isPaused) {
                    updateLogic();
                    Platform.runLater(() -> render(gc));
                }
                try {
                    Thread.sleep(16); // ≈ 60 FPS
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        });
        gameThread.setDaemon(true);
        gameThread.start();
    }

    private void updateLogic() {
        if (selectedStage != null) {
            selectedStage.updateStage(units);
        }

        // ระบบเงิน และ เอฟเฟกต์
        logic.MoneyManager.getInstance().update();

        // 🌟 อัปเดตสถานะปุ่มแมว (มืด/สว่าง) แบบ Real-time ตามเงินที่มี
        int currentMoney = logic.MoneyManager.getInstance().getCurrentMoney();
        Platform.runLater(() -> {
            // อัปเดตปุ่มแมวทั้งหมด
            for (CatButton btn : catButtons) {
                btn.updateState(currentMoney);
            }

            // 🌟 3. อัปเดตปุ่ม Level Up ด้วย
            if (levelUpButton != null) {
                // ถ้าอัปเกรดจนตันแล้ว ให้ปุ่มปิดการใช้งานไปเลย
                if (logic.MoneyManager.getInstance().isMaxLevel()) {
                    levelUpButton.setDisable(true);
                    levelUpButton.setText("MAX LVL");
                    levelUpButton.setOpacity(0.5);
                } else {
                    // ถ้ายังไม่ตัน ก็อัปเดตสถานะตามเงินปกติ
                    levelUpButton.updateState(currentMoney);
                }
            }
        });

        for (int i = units.size() - 1; i >= 0; i--) {
            Unit u = units.get(i);
            u.update();

            if (u.isDead()) {
                units.remove(i);
                BattleManager.getInstance().removeUnit(u);
            }
        }

        if (catTower.isDead()) gameOver("YOU LOSE!");
        else if (dogTower.isDead()) gameOver("YOU WIN!");
    }

    private void render(GraphicsContext gc) {
        // อัปเดตตัวเลขกระเป๋าเงิน
        logic.MoneyManager mm = logic.MoneyManager.getInstance();
        String levelStatus = mm.isMaxLevel() ? " (MAX)" : " (Lv." + mm.getMoneyLevel() + ")";
        moneyLabel.setText("Money: " + mm.getCurrentMoney() + " / " + mm.getMaxMoney() + levelStatus);

        if (selectedStage != null && selectedStage.getBackgroundImage() != null) {
            gc.drawImage(selectedStage.getBackgroundImage(), -cameraX, 0, WORLD_WIDTH, 600);
        } else {
            gc.setFill(Color.WHITESMOKE);
            gc.fillRect(0, 0, SCREEN_WIDTH, 600);
        }

        double groundY = 410;

        for (Unit u : units) {
            double drawX = u.getX() - cameraX;

            if (u instanceof Tower) {
                double towerWidth = u.getRenderWidth() > 0 ? u.getRenderWidth() : 80;
                double towerHeight = u.getRenderHeight() > 0 ? u.getRenderHeight() : 150;
                double towerY = groundY - towerHeight;

                Image sprite = u.getCurrentSprite();

                if (sprite != null) {
                    gc.drawImage(sprite, drawX, towerY, towerWidth, towerHeight);
                } else {
                    if (u instanceof CatTower) gc.setFill(Color.DARKBLUE);
                    else gc.setFill(Color.DARKRED);
                    gc.fillRect(drawX, towerY, towerWidth, towerHeight);
                }

                double currentHp = u.getHp();
                double maxHp = ((Tower) u).getMaxHp();
                String hpText = (int)currentHp + " / " + (int)maxHp;

                gc.setFont(javafx.scene.text.Font.font("Arial", javafx.scene.text.FontWeight.BOLD, 16));
                gc.setTextAlign(javafx.scene.text.TextAlignment.CENTER);

                double textX = drawX + (towerWidth / 2);
                double textY = towerY - 10;

                gc.setStroke(Color.BLACK);
                gc.setLineWidth(3);
                gc.strokeText(hpText, textX, textY);
                gc.setFill(Color.WHITE);
                gc.fillText(hpText, textX, textY);
                gc.setTextAlign(javafx.scene.text.TextAlignment.LEFT);
            } else {
                double unitWidth = u.getRenderWidth();
                double unitHeight = u.getRenderHeight();
//                double unitY = groundY - unitHeight;
                if (u.isDeadSoul()) {
                    double soulSize = 60; // 🔧 ปรับขนาดวิญญาณตามต้องการ (เช่น กว้าง/สูง 80 เท่ากันหมด)
                    drawX += (unitWidth - soulSize) / 2; // ขยับตำแหน่งให้วิญญาณลอยตรงกลางตัวละครพอดี
                    unitWidth = soulSize;
                    unitHeight = soulSize;
                }
                double unitY = groundY - unitHeight + u.getYOffset();

                if(u instanceof UFOCat) unitY -= 50;

                Image sprite = u.getCurrentSprite();
                if (sprite != null) {
                    gc.drawImage(sprite, drawX, unitY, unitWidth, unitHeight);
                } else {
                    gc.setFill(Color.TOMATO);
                    gc.fillRect(drawX, unitY, unitWidth, unitHeight);
                }
            }
        }

        logic.EffectManager.getInstance().drawAll(gc, cameraX);
    }

    // 🌟 เมธอดลองซื้อแมว เปลี่ยนเป็นคืนค่า boolean
    private boolean trySpawnUnit(Unit cat, int cost) {
        if (logic.MoneyManager.getInstance().spend(cost)) {
            spawnPlayerUnit(cat);
            return true; // สำเร็จ! ให้ปุ่มเริ่มคูลดาวน์ได้
        } else {
            return false; // ไม่สำเร็จ ไม่ต้องคูลดาวน์
        }
    }

    private void spawnPlayerUnit(Unit cat) {
        units.add(cat);
        BattleManager.getInstance().addPlayerUnit(cat);
    }

    private void showPauseOverlay() {
        isPaused = true;
        StackPane overlay = new StackPane();
        overlay.setStyle("-fx-background-color: rgba(0, 0, 0, 0.5);");

        VBox menuBox = new VBox(20);
        menuBox.setAlignment(Pos.CENTER);

        Button unpauseBtn = new Button("CONTINUE");
        unpauseBtn.setOnAction(e -> {
            root.getChildren().remove(overlay);
            isPaused = false;
        });

        Button mainMenuBtn = new Button("MAIN MENU");
        mainMenuBtn.setOnAction(e -> {
            running = false;
            showMainMenu();
        });

        menuBox.getChildren().addAll(unpauseBtn, mainMenuBtn);
        overlay.getChildren().add(menuBox);
        root.getChildren().add(overlay);
    }

    private void showMainMenu() {
        root.getChildren().clear();
        running = false;
        VBox menuContent = new VBox(20);
        menuContent.setAlignment(Pos.CENTER);
        menuContent.setStyle("-fx-background-color: #ffffff;");
        Label title = new Label("BATTLE CAT");
        title.setStyle("-fx-font-size: 40px; -fx-font-weight: bold;");
        Button startBtn = new Button("START GAME");
        startBtn.setPrefSize(200, 50);
        startBtn.setOnAction(e -> showLevelSelection());
        menuContent.getChildren().addAll(title, startBtn);
        root.getChildren().add(menuContent);
    }

    private void showLevelSelection() {
        root.getChildren().clear();
        VBox selectionBox = new VBox(30);
        selectionBox.setAlignment(Pos.CENTER);
        selectionBox.setStyle("-fx-background-color: #f0f0f0;");
        Label header = new Label("SELECT STAGE");
        header.setStyle("-fx-font-size: 30px; -fx-font-weight: bold;");
        FlowPane stageButtons = new FlowPane(20, 20);
        stageButtons.setAlignment(Pos.CENTER);

        Button koreaBtn = new Button("KOREA");
        koreaBtn.setPrefSize(150, 100);
        koreaBtn.setStyle("-fx-font-size: 18px;");
        koreaBtn.setOnAction(e -> { this.selectedStage = new Korea(); showGameScene(); });
        stageButtons.getChildren().add(koreaBtn);

        Button japanBtn = new Button("JAPAN");
        japanBtn.setPrefSize(150, 100);
        japanBtn.setStyle("-fx-font-size: 18px;");
        japanBtn.setOnAction(e -> { this.selectedStage = new Japan(); showGameScene(); });
        stageButtons.getChildren().add(japanBtn);

        Button chinaBtn = new Button("CHINA");
        chinaBtn.setPrefSize(150, 100);
        chinaBtn.setStyle("-fx-font-size: 18px;");
        chinaBtn.setOnAction(e -> { this.selectedStage = new China(); showGameScene(); });
        stageButtons.getChildren().add(chinaBtn);

        Button vietBtn = new Button("VIETNAM");
        vietBtn.setPrefSize(150, 100);
        vietBtn.setStyle("-fx-font-size: 18px;");
        vietBtn.setOnAction(e -> { this.selectedStage = new Vietnam(); showGameScene(); });
        stageButtons.getChildren().add(vietBtn);

        Button thaiBtn = new Button("THAILAND");
        thaiBtn.setPrefSize(150, 100);
        thaiBtn.setStyle("-fx-font-size: 18px;");
        thaiBtn.setOnAction(e -> { this.selectedStage = new Thailand(); showGameScene(); });
        stageButtons.getChildren().add(thaiBtn);

        Button backBtn = new Button("BACK");
        backBtn.setPrefSize(100, 40);
        backBtn.setStyle("-fx-font-size: 14px;");
        backBtn.setOnAction(e -> showMainMenu());

        selectionBox.getChildren().addAll(header, stageButtons, backBtn);
        root.getChildren().add(selectionBox);
        Platform.runLater(selectionBox::requestFocus);
    }

    private Button createImageButton(String imagePath, int w, int h, javafx.event.EventHandler<javafx.event.ActionEvent> action) {
        Button btn = new Button();
        btn.setPrefSize(90, 95);
        btn.setStyle("-fx-background-color: transparent; -fx-font-size: 14px; -fx-font-weight: bold;");
        btn.setCursor(Cursor.HAND);
        btn.setOnMousePressed(e -> btn.setOpacity(0.6));
        btn.setOnMouseReleased(e -> btn.setOpacity(1.0));

        try {
            javafx.scene.image.ImageView icon = new javafx.scene.image.ImageView(new javafx.scene.image.Image(Objects.requireNonNull(getClass().getResourceAsStream(imagePath))));
            icon.setFitWidth(w);
            icon.setFitHeight(h);
            icon.setPreserveRatio(true);
            btn.setGraphic(icon);
            btn.setContentDisplay(javafx.scene.control.ContentDisplay.TOP);
        } catch (Exception e) {
            System.err.println("หาภาพปุ่มไม่เจอ: " + imagePath);
            btn.setStyle("-fx-background-color: #DDDDDD; -fx-font-size: 14px; -fx-font-weight: bold;");
        }

        if (action != null) btn.setOnAction(action);
        return btn;
    }

    private void gameOver(String resultText) {
        running = false;
        Platform.runLater(() -> {
            StackPane overlay = new StackPane();
            overlay.setStyle("-fx-background-color: rgba(0, 0, 0, 0.7);");

            VBox box = new VBox(30);
            box.setAlignment(Pos.CENTER);

            Label resultLabel = new Label(resultText);
            resultLabel.setStyle("-fx-font-size: 50px; -fx-text-fill: white; -fx-font-weight: bold;");

            Button menuBtn = new Button("RETURN TO MENU");
            menuBtn.setPrefSize(200, 50);
            menuBtn.setOnAction(e -> showMainMenu());

            box.getChildren().addAll(resultLabel, menuBtn);
            overlay.getChildren().add(box);
            root.getChildren().add(overlay);
        });
    }

    public static void main(String[] args) {
        launch(args);
    }
}