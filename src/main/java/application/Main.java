package application;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import models.base.Tower;
import models.base.Unit;
import models.enemies.DogTower;
import models.stages.Japan;
import models.units.Cat1;
import models.enemies.Dog1;
import logic.BattleManager;
import models.units.CatTower;

// --- Import คลาสระบบด่านเข้ามาเพิ่ม ---
import models.stages.GameStage;
import models.stages.Korea;

import java.util.ArrayList;

public class Main extends Application {

    private StackPane root;
    private BorderPane gameUI;

    private ArrayList<Unit> units = new ArrayList<>();
    private Tower catTower;
    private Tower dogTower;

    // การควบคุม Thread
    private volatile boolean isPaused = false;
    private volatile boolean running = false;
    private Thread gameThread;

    // เก็บ Object ด่านที่เลือก
    private GameStage selectedStage;

    @Override
    public void start(Stage primaryStage) {
        root = new StackPane();

        showMainMenu();

        Scene scene = new Scene(root, 1000, 600);
        // scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());

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

        // --- แถบด้านบน (Top Bar) ---
        HBox topBar = new HBox(10);
        topBar.setPadding(new Insets(15));
        topBar.setAlignment(Pos.CENTER_LEFT);

        Button pauseButton = new Button("||");
        pauseButton.setOnAction(e -> showPauseOverlay());

        // --- แสดงชื่อด่านที่เลือก ---
        Label stageNameLabel = new Label(selectedStage.getStageName());
        stageNameLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: black;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label moneyLabel = new Label("40/150");
        moneyLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: black;");

        topBar.getChildren().addAll(pauseButton, stageNameLabel, spacer, moneyLabel);
        gameUI.setTop(topBar);

        // --- แถบด้านล่าง (Bottom Tray) ---
        HBox bottomTray = new HBox(15);
        bottomTray.setPadding(new Insets(30));
        bottomTray.setAlignment(Pos.BOTTOM_CENTER);

        bottomTray.getChildren().add(createBtn("Level UP\n80", 110, 110, "special-button", null));

        // ปุ่มสร้างแมว
        bottomTray.getChildren().add(createBtn("CAT 1", 90, 95, "unit-button", e -> {
            Unit cat = new Cat1(); // อย่าลืมเซ็ตพิกัด X เริ่มต้นของแมวให้อยู่ฝั่งขวานะครับ
            units.add(cat);
            BattleManager.getInstance().addPlayerUnit(cat);
        }));

        // ปุ่มสร้างหมา (ยังเก็บไว้เผื่อกดเสกเองตอนเทส)
//        bottomTray.getChildren().add(createBtn("DOG 1", 90, 95, "unit-button", e -> {
//            Unit dog = new Dog1();
//            units.add(dog);
//            BattleManager.getInstance().addEnemyUnit(dog);
//        }));

        // --- สร้างป้อมทัพ ---
        catTower = new CatTower(); // สมมติว่าพิกัด X อยู่ฝั่งขวาแล้ว

        // ดึงเลือดป้อมศัตรูมาจากด่านที่เลือก
        dogTower = new DogTower(selectedStage.getEnemyTowerHp());

        units.add(catTower);
        units.add(dogTower);
        BattleManager.getInstance().addPlayerUnit(catTower);
        BattleManager.getInstance().addEnemyUnit(dogTower);

        gameUI.setBottom(bottomTray);
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
        // 1. ให้ด่านจัดการระบบเสกศัตรูตามเวลา (Auto Spawn)
        if (selectedStage != null) {
            selectedStage.updateStage(units);
        }

        // 2. อัปเดตยูนิตทั้งหมด
        for (int i = units.size() - 1; i >= 0; i--) {
            Unit u = units.get(i);
            u.update();

            if (u.isDead()) {
                units.remove(i);
                BattleManager.getInstance().removeUnit(u);
            }
        }

        // 3. เช็คจบเกม
        if (catTower.isDead()) {
            gameOver("YOU LOSE!");
        } else if (dogTower.isDead()) {
            gameOver("YOU WIN!");
        }
    }

    private void render(GraphicsContext gc) {
        // --- 1. วาดพื้นหลังด้วยรูปภาพจาก Object ด่าน ---
        if (selectedStage != null && selectedStage.getBackgroundImage() != null) {
            gc.drawImage(selectedStage.getBackgroundImage(), 0, 0, 1000, 600);
        } else {
            gc.setFill(Color.WHITESMOKE); // สำรองเผื่อรูปโหลดไม่ติด
            gc.fillRect(0, 0, 1000, 600);
        }

        double groundY = 410;

        // --- 3. วาดยูนิตและป้อม ---
        for (Unit u : units) {
            // ป้อมทัพ (Tower)
            if (u instanceof Tower) {
                double towerWidth = 80;
                double towerHeight = 150;
                double towerY = groundY - towerHeight;

                if (u instanceof CatTower) {
                    gc.setFill(Color.DARKBLUE);
                } else {
                    gc.setFill(Color.DARKRED);
                }
                gc.fillRect(u.getX(), towerY, towerWidth, towerHeight);
            }
            // ยูนิตทหาร (Unit)
            else {
                double unitSize = 50;
                double unitY = groundY - unitSize;

                if (u instanceof Cat1) {
                    gc.setFill(Color.SKYBLUE);
                } else {
                    gc.setFill(Color.TOMATO);
                }
                gc.fillRect(u.getX(), unitY, unitSize, unitSize);
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

        // ปุ่มด่านเกาหลี
        Button koreaBtn = new Button("KOREA");
        koreaBtn.setPrefSize(150, 100);
        koreaBtn.setStyle("-fx-font-size: 18px; -fx-base: #e0e0e0;");
        koreaBtn.setOnAction(e -> {
            this.selectedStage = new Korea(); // เรียกคลาส Korea ตรงๆ เลย
            showGameScene();
        });
        stageButtons.getChildren().add(koreaBtn);

        Button japanBtn = new Button("JAPAN");
        japanBtn.setPrefSize(150, 100);
        japanBtn.setStyle("-fx-font-size: 18px; -fx-base: #e0e0e0;");
        japanBtn.setOnAction(e -> {
            this.selectedStage = new Japan(); // ต้องสร้างคลาส Thailand ก่อน
            showGameScene();
        });
        stageButtons.getChildren().add(japanBtn);

        Button backBtn = new Button("BACK");
        backBtn.setOnAction(e -> showMainMenu());

        selectionBox.getChildren().addAll(header, stageButtons, backBtn);
        root.getChildren().add(selectionBox);
    }

    private Button createBtn(String text, double w, double h, String styleClass, javafx.event.EventHandler<javafx.event.ActionEvent> action) {
        Button btn = new Button(text);
        btn.setPrefSize(w, h);
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