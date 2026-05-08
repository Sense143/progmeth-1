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
import javafx.scene.shape.Arc;
import javafx.scene.shape.ArcType;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import javafx.util.Duration;

import models.base.Tower;
import models.base.Unit;
import models.enemies.DogTower;
import models.stages.Japan;
import models.units.Cat1;
import logic.BattleManager;
import models.units.CatTower;

// --- Import คลาสระบบด่านเข้ามาเพิ่ม ---
import models.stages.GameStage;
import models.stages.Korea;

import java.util.ArrayList;
import java.util.Objects;

public class Main extends Application {

    private StackPane root;
    private BorderPane gameUI;

    private ArrayList<Unit> units = new ArrayList<>();
    private Tower catTower;
    private Tower dogTower;

    private volatile boolean isPaused = false;
    private volatile boolean running = false;
    private Thread gameThread;

    private GameStage selectedStage;

    @Override
    public void start(Stage primaryStage) {
        root = new StackPane();
        showMainMenu();
        Scene scene = new Scene(root, 1000, 600);
        primaryStage.setTitle("Battle Cat - Complete Edition");
        primaryStage.setScene(scene);
        primaryStage.setOnCloseRequest(e -> running = false);
        primaryStage.show();
    }

    private void showGameScene() {
        root.getChildren().clear();
        units.clear();
        BattleManager.getInstance().clearAll();
        isPaused = false;
        running = true;

        Canvas canvas = new Canvas(1000, 600);
        GraphicsContext gc = canvas.getGraphicsContext2D();

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

        Label moneyLabel = new Label("40/150");
        moneyLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: black;");

        topBar.getChildren().addAll(pauseButton, stageNameLabel, topSpacer, moneyLabel);
        gameUI.setTop(topBar);

        // --- แถบด้านล่าง ---
        HBox bottomTray = new HBox(10);
        bottomTray.setPadding(new Insets(30, 30, 10, 30));
        bottomTray.setAlignment(Pos.BOTTOM_LEFT);

        // 1. ปุ่ม Level UP
        Button levelUpBtn = createImageButton("/button/moneyBTN_1.png", 150, 150, e -> {
            System.out.println("Level UP!");
        });
        levelUpBtn.setTranslateX(-40);
        levelUpBtn.setTranslateY(23);

        Region spacer1 = new Region();
        HBox.setHgrow(spacer1, Priority.ALWAYS);

        // 2. 🌟 กลุ่มปุ่มแมว (Cooldown แบบหลอดโหลดสี่เหลี่ยมเล็กๆ ด้านล่าง)
        HBox catsBox = new HBox(10);
        catsBox.setAlignment(Pos.BOTTOM_CENTER);

        // --- เริ่มสร้างปุ่มแมว ---
        StackPane cat1BtnContainer = new StackPane();
        cat1BtnContainer.setTranslateY(30);
        cat1BtnContainer.setCursor(Cursor.HAND);

        double imgW = 110;
        double imgH = 110;

        // 2.1 รูปไอคอนแมว
        javafx.scene.image.ImageView catIcon = null;
        try {
            catIcon = new javafx.scene.image.ImageView(new javafx.scene.image.Image(Objects.requireNonNull(getClass().getResourceAsStream("/cat/icon/uni000_f00.png"))));
            catIcon.setFitWidth(imgW);
            catIcon.setFitHeight(imgH);
            catIcon.setPreserveRatio(false);
        } catch (Exception err) { System.err.println("หาภาพไอคอนแมวไม่เจอ!"); }

        // 2.2 แผ่นฟิล์มสีดำจางๆ ทับรูปไว้บางๆ ตอนคูลดาวน์ (ให้หลอดดูเด่นขึ้น)
        javafx.scene.shape.Rectangle cooldownDim = new javafx.scene.shape.Rectangle(imgW, imgH);
        cooldownDim.setFill(Color.color(0, 0, 0, 0.4));
        cooldownDim.setOpacity(0.0); // เริ่มต้นแบบสว่าง (พร้อมกด)

        // 2.3 🌟 พื้นหลังของหลอดคูลดาวน์ (สีเทาดำ)
        javafx.scene.shape.Rectangle barBackground = new javafx.scene.shape.Rectangle(imgW, 10); // สูงแค่ 10px
        barBackground.setFill(Color.color(0, 0, 0, 0.6));
        StackPane.setAlignment(barBackground, Pos.BOTTOM_CENTER); // ให้อยู่ชิดขอบล่าง
        barBackground.setOpacity(0.0); // ซ่อนไว้ก่อน

        // 2.4 🌟 หลอดสีคูลดาวน์ (เช่น สีเหลือง หรือ สีฟ้า)
        javafx.scene.shape.Rectangle cooldownBar = new javafx.scene.shape.Rectangle(imgW, 10);
        cooldownBar.setFill(Color.CYAN); // เปลี่ยนสีหลอดตรงนี้ได้ครับ (เช่น Color.LIGHTGREEN, Color.YELLOW)
        StackPane.setAlignment(cooldownBar, Pos.BOTTOM_LEFT); // ให้จุดเริ่มต้นอยู่ซ้ายล่าง จะได้วิ่งไปขวา

        if (catIcon != null) {
            // เอาทุกอย่างมาซ้อนกัน: รูป -> แผ่นฟิล์ม -> พื้นหลังหลอด -> หลอดสี
            cat1BtnContainer.getChildren().addAll(catIcon, cooldownDim, barBackground, cooldownBar);
        }

        // 2.5 Logic คูลดาวน์
        double cat1Cooldown = 2.5;

        cat1BtnContainer.setOnMouseClicked(e -> {
            // เช็คว่าหลอดกว้างเต็มหรือยัง (ถ้า width == imgW คือกดได้)
            if (cooldownBar.getWidth() == imgW) {
                spawnPlayerUnit(new Cat1()); // เสกแมว

                // เริ่มคูลดาวน์: โชว์แผ่นฟิล์มและหลอดโหลด แล้วเซ็ตหลอดให้ว่างเปล่า
                cooldownDim.setOpacity(1.0);
                barBackground.setOpacity(1.0);
                cooldownBar.setWidth(0);

                Timeline timeline = new Timeline();
                // 🌟 อนิเมตความกว้างของหลอดสี จาก 0 วิ่งไปจนเต็ม (imgW)
                KeyValue kv = new KeyValue(cooldownBar.widthProperty(), imgW);
                KeyFrame kf = new KeyFrame(Duration.seconds(cat1Cooldown), kv);

                timeline.getKeyFrames().add(kf);
                timeline.setOnFinished(event -> {
                    // พอคูลดาวน์เสร็จ ซ่อนแผ่นฟิล์มและหลอดให้กลับมาเป็นปุ่มใสๆ เหมือนเดิม
                    cooldownDim.setOpacity(0.0);
                    barBackground.setOpacity(0.0);
                });
                timeline.play(); // เริ่มแอนิเมชัน
            }
        });

        catsBox.getChildren().add(cat1BtnContainer);

        Region spacer2 = new Region();
        HBox.setHgrow(spacer2, Priority.ALWAYS);

        // 3. ปุ่ม ปืนใหญ่ (CANNON) (ขวาสุด)
        // ⚠️ อย่าลืมเตรียมรูป cannon_icon.png ไว้ใน resources ด้วยนะครับ
        Button cannonBtn = createImageButton( "/button/cannonBTN_1.png", 150, 150, e -> {});
        cannonBtn.setTranslateX(40);
        cannonBtn.setTranslateY(23);

        // นำทุกอย่างประกอบลงแถบด้านล่างตามลำดับ: LevelUP -> สปริง1 -> กลุ่มแมว -> สปริง2 -> ปืนใหญ่
        bottomTray.getChildren().addAll(levelUpBtn, spacer1, catsBox, spacer2, cannonBtn);
        gameUI.setBottom(bottomTray);

        // --- สร้างป้อมทัพ ---
        catTower = new CatTower();
        dogTower = new DogTower(selectedStage.getEnemyTowerHp());

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
        if (selectedStage != null && selectedStage.getBackgroundImage() != null) {
            gc.drawImage(selectedStage.getBackgroundImage(), 0, 0, 1000, 600);
        } else {
            gc.setFill(Color.WHITESMOKE);
            gc.fillRect(0, 0, 1000, 600);
        }

        double groundY = 410;

        for (Unit u : units) {
            // ป้อม
            if (u instanceof Tower) {
                double towerWidth = 80;
                double towerHeight = 150;
                double towerY = groundY - towerHeight;
                if (u instanceof CatTower) gc.setFill(Color.DARKBLUE);
                else gc.setFill(Color.DARKRED);
                gc.fillRect(u.getX(), towerY, towerWidth, towerHeight);
            }
            // ยูนิตทหาร (วาดเป็นแอนิเมชัน)
            else {
                double unitSize = 80; // ปรับความใหญ่ของรูปตัวละครในเกมได้ตรงนี้
                double unitY = groundY - unitSize;

                Image sprite = u.getCurrentSprite();

                if (sprite != null) {
                    // วาดรูปตามสถานะปัจจุบัน
                    gc.drawImage(sprite, u.getX(), unitY, unitSize, unitSize);
                } else {
                    // ถ้าหาภาพไม่เจอจริงๆ ให้วาดสี่เหลี่ยมสำรอง
                    if (u instanceof Cat1) gc.setFill(Color.SKYBLUE);
                    else gc.setFill(Color.TOMATO);
                    gc.fillRect(u.getX(), groundY - 50, 50, 50);
                }
            }
        }
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

    private void spawnPlayerUnit(Unit cat) {
        units.add(cat);
        BattleManager.getInstance().addPlayerUnit(cat);
    }

    // --- เมธอดแสดงหน้าจอจบเกม (ชนะ/แพ้) ---
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