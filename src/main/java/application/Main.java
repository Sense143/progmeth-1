package application;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.application.Platform;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
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

import models.base.CannonWave;
import models.base.Tower;
import models.base.Unit;
import models.enemies.DogTower;
import models.stages.*;
import models.units.*;
import logic.BattleManager;
import ui.CannonButton;
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

    private ArrayList<models.base.CannonWave> activeWaves = new ArrayList<>();
    private boolean isTowerFiring = false;
    private Image towerNormal = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/tower/Catbase.png")));
    private Image towerFire = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/tower/Catbase_firing.png")));

    private final double WORLD_WIDTH = 2500;
    private final double SCREEN_WIDTH = 1000;
    private double cameraX = 0;
    private double dragLastX = 0;

    private ui.LevelUpButton levelUpButton;

    // Tower destruction state
    private boolean catTowerDestroyedHandled = false;
    private boolean dogTowerDestroyedHandled = false;
    private int gameOverCountdown = -1;
    private String pendingGameOverText = null;
    private ArrayList<models.base.TowerBurstEffect> burstEffects = new ArrayList<>();
    private java.util.Random rng = new java.util.Random();
    private boolean isGameOver = false;

    private Clip menuMusicClip;
    private Clip gameMusicClip;
    private int postGameFrame = 0;
    private double catTowerShakeX = 0;
    private double dogTowerShakeX = 0;

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
        stopMenuMusic();
        root.getChildren().clear();
        units.clear();
        catButtons.clear(); // 🌟 เคลียร์ปุ่มเก่าตอนเริ่มด่าน
        BattleManager.getInstance().clearAll();

        logic.MoneyManager.getInstance().reset();
        catTowerDestroyedHandled = false;
        dogTowerDestroyedHandled = false;
        gameOverCountdown = -1;
        pendingGameOverText = null;
        burstEffects.clear();
        isGameOver = false;
        postGameFrame = 0;
        catTowerShakeX = 0;
        dogTowerShakeX = 0;

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

        javafx.scene.image.ImageView pauseIcon = new javafx.scene.image.ImageView(
                new Image(Objects.requireNonNull(getClass().getResourceAsStream("/button/PauseBTN.png")))
        );
        pauseIcon.setFitWidth(40);
        pauseIcon.setFitHeight(40);
        Button pauseButton = new Button();
        pauseButton.setGraphic(pauseIcon);
        pauseButton.setStyle("-fx-background-color: transparent; -fx-padding: 0;");
        pauseButton.setCursor(Cursor.HAND);
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
                if (logic.MoneyManager.getInstance().isMaxLevel()) {
                    levelUpButton.markMaxLevel();
                }
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

//        CannonButton cannonBtn = new CannonButton("/button/cannonBTN_1.png", 30.0, () -> {
//            // 1. สร้างลำแสง
//            activeWaves.add(new models.base.CannonWave(catTower.getX()));
//
//            // 2. สลับรูปฐานเป็นตอนยิง
//            isTowerFiring = true;
//            catTower.setCurrentSprite(towerFire); // ต้องมีเมธอด setCurrentSprite ในคลาส Tower
//
//            // 3. หลังจาก 1 วินาที ให้ฐานกลับเป็นรูปปกติ
//            new Thread(() -> {
//                try { Thread.sleep(1000); } catch (Exception e) {}
//                isTowerFiring = false;
//                catTower.setCurrentSprite(towerNormal);
//            }).start();
//        });
        CannonButton cannonBtn = new CannonButton("/button/cannonBTN_1.png", 30.0, () -> {
            // 1. ยิงคลื่น
            activeWaves.add(new CannonWave(catTower.getX()));

            // 2. สลับรูปฐานเป็นตอนยิง (Optional: ถ้าต้องการความสวยงาม)
            isTowerFiring = true;
            catTower.setCurrentSprite(towerFire);

            // ใช้ Timeline สั้นๆ ใน Main เพื่อเปลี่ยนรูปฐานกลับ
            javafx.animation.PauseTransition towerReset = new javafx.animation.PauseTransition(Duration.seconds(1));
            towerReset.setOnFinished(ev -> {
                isTowerFiring = false;
                catTower.setCurrentSprite(towerNormal);
            });
            towerReset.play();

            // ❌ ไม่ต้องสั่ง cannonBtn.setVisible(false) ที่นี่แล้ว
            // เพราะข้างใน CannonButton.java มันสั่งตัวเองไปแล้วครับ
        });
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

        String stageBgm = (selectedStage instanceof Thailand) ? "/music/004.ogg" : "/music/003.ogg";
        startGameMusic(stageBgm);

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
        if (isGameOver) {
            for (Unit u : units) u.update();
            postGameFrame++;
            if (postGameFrame % 3 == 0) {
                if (catTowerDestroyedHandled) catTowerShakeX = rng.nextDouble() * 12 - 6;
                if (dogTowerDestroyedHandled) dogTowerShakeX = rng.nextDouble() * 12 - 6;
            }
            if (postGameFrame % 12 == 0) {
                if (catTowerDestroyedHandled) for (int i = 0; i < 3; i++) spawnTowerBurst(catTower);
                if (dogTowerDestroyedHandled) for (int i = 0; i < 3; i++) spawnTowerBurst(dogTower);
            }
            return;
        }

        if (selectedStage != null && !dogTowerDestroyedHandled) {
            selectedStage.updateStage(units, dogTower.getHp());
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
                levelUpButton.updateState(currentMoney);
            }
        });

        for (int i = units.size() - 1; i >= 0; i--) {
            Unit u = units.get(i);
            u.update();

            // Towers stay on board even when dead (show destruction effect)
            if (u.isDead() && !(u instanceof models.base.Tower)) {
                units.remove(i);
                BattleManager.getInstance().removeUnit(u);
            }
        }

        for (int i = activeWaves.size() - 1; i >= 0; i--) {
            activeWaves.get(i).update();
            if (!activeWaves.get(i).isActive()) activeWaves.remove(i);
        }

        // First frame a tower dies: knock back all units on that side, start countdown
        if (catTower.isDead() && !catTowerDestroyedHandled) {
            catTowerDestroyedHandled = true;
            for (Unit u : BattleManager.getInstance().getPlayerUnits()) {
                if (!(u instanceof models.base.Tower) && u.getHp() > 0) u.setHp(0);
            }
            pendingGameOverText = "YOU LOSE!";
            gameOverCountdown = 150;
            playResultMusic("/music/009.ogg");
        }
        if (dogTower.isDead() && !dogTowerDestroyedHandled) {
            dogTowerDestroyedHandled = true;
            for (Unit u : BattleManager.getInstance().getEnemyUnits()) {
                if (!(u instanceof models.base.Tower) && u.getHp() > 0) u.setHp(0);
            }
            pendingGameOverText = "YOU WIN!";
            gameOverCountdown = 150;
            playResultMusic("/music/008.ogg");
        }

        // During countdown: spawn burst explosions on dead tower every 12 frames
        if (gameOverCountdown > 0) {
            gameOverCountdown--;
            if (gameOverCountdown % 3 == 0) {
                if (catTowerDestroyedHandled) catTowerShakeX = rng.nextDouble() * 12 - 6;
                if (dogTowerDestroyedHandled) dogTowerShakeX = rng.nextDouble() * 12 - 6;
            }
            if (gameOverCountdown % 12 == 0) {
                if (catTowerDestroyedHandled) for (int i = 0; i < 3; i++) spawnTowerBurst(catTower);
                if (dogTowerDestroyedHandled) for (int i = 0; i < 3; i++) spawnTowerBurst(dogTower);
            }
            if (gameOverCountdown == 0) gameOver(pendingGameOverText);
        }
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
            if (u == catTower && catTowerDestroyedHandled) drawX += catTowerShakeX;
            if (u == dogTower && dogTowerDestroyedHandled) drawX += dogTowerShakeX;

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

        for (models.base.CannonWave wave : activeWaves) {
            wave.draw(gc, cameraX);
        }

        logic.EffectManager.getInstance().drawAll(gc, cameraX);

        burstEffects.removeIf(models.base.TowerBurstEffect::isFinished);
        for (models.base.TowerBurstEffect burst : burstEffects) {
            burst.draw(gc, cameraX);
        }
    }

    // 🌟 เมธอดลองซื้อแมว เปลี่ยนเป็นคืนค่า boolean
    private boolean trySpawnUnit(Unit cat, int cost) {
        if (catTowerDestroyedHandled || dogTowerDestroyedHandled) return false;
        if (logic.MoneyManager.getInstance().spend(cost)) {
            spawnPlayerUnit(cat);
            return true;
        } else {
            return false;
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

        Image pauseBtnImg = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/button.png")));

        StackPane unpauseBtn = makeLabelledButton(pauseBtnImg, "CONTINUE", 220, 50);
        unpauseBtn.setOnMouseClicked(e -> {
            root.getChildren().remove(overlay);
            isPaused = false;
        });

        StackPane mainMenuBtn = makeLabelledButton(pauseBtnImg, "RETURN TO MAP", 220, 50);
        mainMenuBtn.setOnMouseClicked(e -> {
            running = false;
            stopGameMusic();
            showLevelSelection();
        });

        menuBox.getChildren().addAll(unpauseBtn, mainMenuBtn);
        overlay.getChildren().add(menuBox);
        root.getChildren().add(overlay);
    }

    private Clip openClip(String path) throws Exception {
        AudioInputStream raw = AudioSystem.getAudioInputStream(
                Objects.requireNonNull(getClass().getResource(path)));
        AudioFormat base = raw.getFormat();
        AudioFormat pcm = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, base.getSampleRate(),
                16, base.getChannels(), base.getChannels() * 2, base.getSampleRate(), false);
        AudioInputStream pcmStream = AudioSystem.getAudioInputStream(pcm, raw);
        Clip clip = AudioSystem.getClip();
        clip.open(pcmStream);
        FloatControl gain = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
        gain.setValue(-25.0f);
        return clip;
    }

    private void startMenuMusic() {
        if (menuMusicClip != null && menuMusicClip.isRunning()) return;
        try {
            if (menuMusicClip != null) { menuMusicClip.stop(); menuMusicClip.close(); }
            menuMusicClip = openClip("/music/001.ogg");
            menuMusicClip.loop(Clip.LOOP_CONTINUOUSLY);
            menuMusicClip.start();
        } catch (Exception e) {
            System.out.println("Could not play menu music: " + e.getMessage());
        }
    }

    private void stopMenuMusic() {
        if (menuMusicClip != null) {
            menuMusicClip.stop();
            menuMusicClip.close();
            menuMusicClip = null;
        }
    }

    private void startGameMusic(String path) {
        try {
            if (gameMusicClip != null) { gameMusicClip.stop(); gameMusicClip.close(); }
            gameMusicClip = openClip(path);
            gameMusicClip.loop(Clip.LOOP_CONTINUOUSLY);
            gameMusicClip.start();
        } catch (Exception e) {
            System.out.println("Could not play game music: " + e.getMessage());
        }
    }

    private void stopGameMusic() {
        if (gameMusicClip != null) {
            gameMusicClip.stop();
            gameMusicClip.close();
            gameMusicClip = null;
        }
    }

    private void playResultMusic(String path) {
        stopGameMusic();
        try {
            gameMusicClip = openClip(path);
            gameMusicClip.start(); // play once, no loop
        } catch (Exception e) {
            System.out.println("Could not play result music: " + e.getMessage());
        }
    }

    private void showMainMenu() {
        root.getChildren().clear();
        running = false;
        startMenuMusic();

        Pane menuPane = new Pane();
        menuPane.setPrefSize(SCREEN_WIDTH, 600);

        // Background
        Image titleImg = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/titleScreen.png")));
        javafx.scene.image.ImageView bg = new javafx.scene.image.ImageView(titleImg);
        bg.setFitWidth(SCREEN_WIDTH);
        bg.setFitHeight(600);
        bg.setPreserveRatio(false);
        menuPane.getChildren().add(bg);

        // Button image with START label stacked on top
        double btnW = 254, btnH = 60;
        Image btnImg = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/button.png")));
        javafx.scene.image.ImageView btnView = new javafx.scene.image.ImageView(btnImg);
        btnView.setFitWidth(btnW);
        btnView.setFitHeight(btnH);

        Label startLabel = new Label("START");
        startLabel.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: black;");

        StackPane startBtn = new StackPane(btnView, startLabel);
        startBtn.setPrefSize(btnW, btnH);
        startBtn.setCursor(Cursor.HAND);
        startBtn.setOnMouseClicked(e -> showLevelSelection());
        startBtn.setOnMouseEntered(e -> startBtn.setOpacity(0.85));
        startBtn.setOnMouseExited(e -> startBtn.setOpacity(1.0));

        // Center horizontally, center at 3/4 down (1/4 above bottom)
        double btnX = (SCREEN_WIDTH - btnW) / 2;
        double btnY = 600 * 0.75 - btnH / 2;
        startBtn.setLayoutX(btnX);
        startBtn.setLayoutY(btnY);

        menuPane.getChildren().add(startBtn);
        root.getChildren().add(menuPane);
    }

    private void showLevelSelection() {
        root.getChildren().clear();
        startMenuMusic();

        Pane mapPane = new Pane();
        mapPane.setPrefSize(SCREEN_WIDTH, 600);

        // Background worldmap
        Image worldmapImg = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/worldmap.png")));
        javafx.scene.image.ImageView mapView = new javafx.scene.image.ImageView(worldmapImg);
        mapView.setFitWidth(SCREEN_WIDTH);
        mapView.setFitHeight(600);
        mapView.setPreserveRatio(false);
        mapPane.getChildren().add(mapView);

        // Country positions on the 1000x600 scaled map (pixel-sampled from 1752x990 original)
        double[][] positions = {
                {655, 180},  // China  (yellow)
                {834, 163},  // Korea  (reddish-brown)
                {915, 153},  // Japan  (red)
                {663, 328},  // Vietnam (pale brown)
                {621, 342},  // Thailand (pink)
        };
        String[] names = {"CHINA", "KOREA", "JAPAN", "VIETNAM", "THAILAND"};
        Runnable[] actions = {
                () -> { this.selectedStage = new China();   showGameScene(); },
                () -> { this.selectedStage = new Korea();   showGameScene(); },
                () -> { this.selectedStage = new Japan();   showGameScene(); },
                () -> { this.selectedStage = new Vietnam(); showGameScene(); },
                () -> { this.selectedStage = new Thailand(); showGameScene(); },
        };

        Image xImg = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/x.png")));
        for (int i = 0; i < names.length; i++) {
            final int idx = i;
            double cx = positions[i][0];
            double cy = positions[i][1];

            javafx.scene.image.ImageView xView = new javafx.scene.image.ImageView(xImg);
            xView.setFitWidth(40);
            xView.setFitHeight(40);

            Label nameLabel = new Label(names[i]);
            nameLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: white; -fx-effect: dropshadow(gaussian, black, 3, 1.0, 0, 0);");

            VBox pinBox = new VBox(2, xView, nameLabel);
            pinBox.setAlignment(Pos.CENTER);
            pinBox.setCursor(Cursor.HAND);
            pinBox.setOnMouseClicked(e -> actions[idx].run());
            pinBox.setOnMouseEntered(e -> xView.setOpacity(0.75));
            pinBox.setOnMouseExited(e -> xView.setOpacity(1.0));

            // Centre the box on (cx, cy)
            pinBox.setLayoutX(cx - 20);
            pinBox.setLayoutY(cy - 20);

            mapPane.getChildren().add(pinBox);
        }

        double backW = 150, backH = 36;
        Image backBtnImg = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/button.png")));
        javafx.scene.image.ImageView backBtnView = new javafx.scene.image.ImageView(backBtnImg);
        backBtnView.setFitWidth(backW);
        backBtnView.setFitHeight(backH);

        Label backLabel = new Label("BACK");
        backLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: black;");

        StackPane backBtn = new StackPane(backBtnView, backLabel);
        backBtn.setPrefSize(backW, backH);
        backBtn.setCursor(Cursor.HAND);
        backBtn.setLayoutX(10);
        backBtn.setLayoutY(10);
        backBtn.setOnMouseClicked(e -> showMainMenu());
        backBtn.setOnMouseEntered(e -> backBtn.setOpacity(0.85));
        backBtn.setOnMouseExited(e -> backBtn.setOpacity(1.0));
        mapPane.getChildren().add(backBtn);

        root.getChildren().add(mapPane);
    }

    private void spawnTowerBurst(models.base.Tower tower) {
        double tW = tower.getRenderWidth() > 0 ? tower.getRenderWidth() : 80;
        double tH = tower.getRenderHeight() > 0 ? tower.getRenderHeight() : 150;
        double groundY = 410;
        double wx = tower.getX() + rng.nextDouble() * tW - tW / 2;
        double sy = groundY - rng.nextDouble() * tH;
        burstEffects.add(new models.base.TowerBurstEffect(wx, sy));
    }

    private StackPane makeLabelledButton(Image btnImg, String text, double w, double h) {
        javafx.scene.image.ImageView iv = new javafx.scene.image.ImageView(btnImg);
        iv.setFitWidth(w);
        iv.setFitHeight(h);
        Label lbl = new Label(text);
        lbl.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: black;");
        StackPane btn = new StackPane(iv, lbl);
        btn.setPrefSize(w, h);
        btn.setCursor(Cursor.HAND);
        btn.setOnMouseEntered(e -> btn.setOpacity(0.85));
        btn.setOnMouseExited(e -> btn.setOpacity(1.0));
        return btn;
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
        isGameOver = true;
        Platform.runLater(() -> {
            StackPane overlay = new StackPane();
            overlay.setStyle("-fx-background-color: rgba(0, 0, 0, 0.7);");

            VBox box = new VBox(30);
            box.setAlignment(Pos.CENTER);

            Label resultLabel = new Label(resultText);
            resultLabel.setStyle("-fx-font-size: 50px; -fx-text-fill: white; -fx-font-weight: bold;");

            Image menuBtnImg = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/button.png")));
            StackPane menuBtn = makeLabelledButton(menuBtnImg, "RETURN TO MAP", 260, 55);
            menuBtn.setOnMouseClicked(e -> { running = false; stopGameMusic(); showLevelSelection(); });

            box.getChildren().addAll(resultLabel, menuBtn);
            overlay.getChildren().add(box);
            root.getChildren().add(overlay);
        });
    }

    public static void main(String[] args) {
        launch(args);
    }
}