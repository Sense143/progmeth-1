package application;

import javafx.animation.PauseTransition;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
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
import ui.CatButton;

import java.util.ArrayList;
import java.util.Objects;

/**
 * Main application class and top-level controller for Battle Cat - Complete Edition.
 *
 * <p>Responsibilities:
 * <ul>
 *   <li><b>Scene management</b> – owns the primary {@link Stage} and drives
 *       transitions between the title screen, world-map level selection, and
 *       the battle scene ({@link #showMainMenu()}, {@link #showLevelSelection()},
 *       {@link #showGameScene()}, {@link #gameOver(String)}).</li>
 *   <li><b>Game loop</b> – runs a ~60 FPS daemon thread that calls
 *       {@link #updateLogic()} each tick and schedules {@link #render(GraphicsContext)}
 *       on the JavaFX Application Thread via {@link Platform#runLater}.</li>
 *   <li><b>Rendering</b> – draws the scrollable world, all units, cannon waves,
 *       and visual effects onto a {@link Canvas}.</li>
 *   <li><b>Audio</b> – plays MP3 music through {@link MediaPlayer} (JavaFX Media).
 *       Resources are extracted to temp files at runtime because JavaFX Media
 *       requires {@code file:} URIs and cannot read {@code jar:} URIs directly.</li>
 * </ul>
 */
public class Main extends Application {

    /** Root container; the active scene's content is swapped into this pane. */
    private StackPane root;
    /** HUD label displaying current / max money and wallet level. */
    private Label moneyLabel;
    /** All active unit-deploy buttons; updated each frame for affordability state. */
    private ArrayList<CatButton> catButtons = new ArrayList<>();
    /** All living units on the battlefield, including both towers. */
    private ArrayList<Unit> units = new ArrayList<>();
    /** Player's home base tower (right side of the world). */
    private Tower catTower;
    /** Enemy base tower (left side of the world). */
    private Tower dogTower;

    /** When {@code true} the game loop skips {@link #updateLogic()} each tick. */
    private volatile boolean isPaused = false;
    /** When {@code false} the game loop thread exits cleanly. */
    private volatile boolean running = false;
    private Thread gameThread;

    /** The stage (level) currently loaded or being played. */
    private GameStage selectedStage;

    /** Cannon-wave projectiles currently sweeping across the battlefield. */
    private ArrayList<models.base.CannonWave> activeWaves = new ArrayList<>();
    /** Cat-tower sprite displayed when idle. */
    private Image towerNormal = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/tower/Catbase.png")));
    /** Cat-tower sprite displayed briefly when the cannon fires. */
    private Image towerFire = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/tower/Catbase_firing.png")));

    /** Total width of the scrollable game world in pixels. */
    private final double WORLD_WIDTH = 2500;
    /** Visible viewport width in pixels. */
    private final double SCREEN_WIDTH = 1000;
    /** Horizontal scroll offset: the world-X coordinate shown at the left edge of the screen. */
    private double cameraX = 0;
    /** X position of the last mouse-drag event, used to compute drag delta. */
    private double dragLastX = 0;

    /** Wallet-upgrade button in the bottom HUD. */
    private ui.LevelUpButton levelUpButton;

    /** Guards the one-shot cat-tower destruction handler. */
    private boolean catTowerDestroyedHandled = false;
    /** Guards the one-shot dog-tower destruction handler. */
    private boolean dogTowerDestroyedHandled = false;
    /** Frames remaining before the game-over overlay appears; {@code -1} while inactive. */
    private int gameOverCountdown = -1;
    /** Result string ({@code "YOU WIN!"} or {@code "YOU LOSE!"}) queued for the overlay. */
    private String pendingGameOverText = null;
    /** Tower-destruction burst particles currently being rendered. */
    private ArrayList<models.base.TowerBurstEffect> burstEffects = new ArrayList<>();
    private java.util.Random rng = new java.util.Random();
    /** {@code true} once the game-over overlay has been shown; freezes logic updates. */
    private boolean isGameOver = false;

    /** Looping background music player used on the menu and level-selection screens. */
    private MediaPlayer menuMusicPlayer;
    /** Background music player for the active stage, or the result jingle after the battle. */
    private MediaPlayer gameMusicPlayer;
    /** Frame counter that drives the post-game burst-explosion animation. */
    private int postGameFrame = 0;
    /** Screen-space shake offset applied to the cat tower while it is being destroyed. */
    private double catTowerShakeX = 0;
    /** Screen-space shake offset applied to the dog tower while it is being destroyed. */
    private double dogTowerShakeX = 0;

    /** No-arg constructor required by the JavaFX {@link Application} launch mechanism. */
    public Main() {}

    /**
     * JavaFX entry point called after the toolkit is initialised.
     * Creates the root pane, shows the title screen, and presents the primary window.
     *
     * @param primaryStage the top-level window supplied by the JavaFX runtime
     */
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

    /**
     * Builds and activates the in-game battle scene for the currently selected stage.
     *
     * <p>Clears all previous state (units, buttons, effects), resets
     * {@link logic.MoneyManager} and {@link logic.BattleManager}, constructs the
     * HUD (top bar with money label, bottom tray with unit buttons, wallet-upgrade
     * button, and cannon button), places both towers, starts stage-appropriate
     * background music, then launches the game loop.
     */
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

        BorderPane gameUI = new BorderPane();
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

        CannonButton cannonBtn = new CannonButton("/button/cannonBTN_1.png", 30.0, () -> {
            activeWaves.add(new CannonWave(catTower.getX()));
            catTower.setCurrentSprite(towerFire);
            PauseTransition towerReset = new PauseTransition(Duration.seconds(1));
            towerReset.setOnFinished(ev -> catTower.setCurrentSprite(towerNormal));
            towerReset.play();
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

        String stageBgm = (selectedStage instanceof Thailand) ? "/music/004.mp3" : "/music/003.mp3";
        startGameMusic(stageBgm);

        startGameThread(gc);
    }

    /**
     * Spawns a daemon background thread that drives the game loop at approximately 60 FPS.
     * Each tick calls {@link #updateLogic()}, then posts {@link #render(GraphicsContext)}
     * to the JavaFX Application Thread via {@link Platform#runLater}.
     * The loop exits when {@link #running} is set to {@code false}.
     *
     * @param gc the {@link GraphicsContext} of the battle canvas to render into
     */
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

    /**
     * Advances all game state by one frame. Invoked from the game loop thread.
     *
     * <p>Execution order each frame:
     * <ol>
     *   <li>If {@link #isGameOver}: animate surviving units and spawn burst explosions
     *       on the destroyed tower, then return early.</li>
     *   <li>Advance the selected stage's enemy-spawn schedule.</li>
     *   <li>Tick {@link logic.MoneyManager}; post a HUD-button affordability update
     *       to the JavaFX thread.</li>
     *   <li>Update every unit; remove dead non-tower units from the board and
     *       from {@link BattleManager}.</li>
     *   <li>Advance all active {@link CannonWave}s; remove finished ones.</li>
     *   <li>On the first frame a tower reaches 0 HP: zero out all allied units on
     *       that side, set the result string, start the result jingle, and begin
     *       the 150-frame game-over countdown.</li>
     *   <li>While the countdown is active: emit burst particles every 12 frames and
     *       shake the dead tower every 3 frames. When the counter reaches 0, call
     *       {@link #gameOver(String)}.</li>
     * </ol>
     */
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
            playResultMusic("/music/009.mp3");
        }
        if (dogTower.isDead() && !dogTowerDestroyedHandled) {
            dogTowerDestroyedHandled = true;
            for (Unit u : BattleManager.getInstance().getEnemyUnits()) {
                if (!(u instanceof models.base.Tower) && u.getHp() > 0) u.setHp(0);
            }
            pendingGameOverText = "YOU WIN!";
            gameOverCountdown = 150;
            playResultMusic("/music/008.mp3");
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

    /**
     * Draws the current frame onto the battle canvas.
     * Must be called on the JavaFX Application Thread.
     *
     * <p>Drawing order:
     * <ol>
     *   <li>Stage background image, or a solid {@link Color#WHITESMOKE} fallback.</li>
     *   <li>Each unit: towers are drawn with their HP overlay and an optional
     *       horizontal shake; regular units use their current sprite and Y-offset,
     *       with a smaller soul sprite substituted when dead.</li>
     *   <li>All active {@link CannonWave} projectiles.</li>
     *   <li>UFOCat explosion particles via {@link logic.EffectManager}.</li>
     *   <li>Tower-destruction burst particles.</li>
     * </ol>
     *
     * @param gc the {@link GraphicsContext} of the battle canvas
     */
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

    /**
     * Deducts {@code cost} from the player's wallet and spawns {@code cat} on the
     * battlefield. Returns {@code false} without spawning if the game has already
     * ended or the player cannot afford the cost.
     *
     * @param cat  the pre-constructed unit instance to deploy
     * @param cost the amount of money to deduct on success
     * @return {@code true} if the unit was spawned and money was spent
     */
    private boolean trySpawnUnit(Unit cat, int cost) {
        if (catTowerDestroyedHandled || dogTowerDestroyedHandled) return false;
        if (logic.MoneyManager.getInstance().spend(cost)) {
            spawnPlayerUnit(cat);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Adds a player-side unit to the live unit list and registers it with
     * {@link BattleManager} so that enemy units can target it.
     *
     * @param cat the unit to add to the battlefield
     */
    private void spawnPlayerUnit(Unit cat) {
        units.add(cat);
        BattleManager.getInstance().addPlayerUnit(cat);
    }

    /**
     * Pauses the game loop and shows a semi-transparent overlay with
     * <em>CONTINUE</em> and <em>RETURN TO MAP</em> buttons.
     * Pressing CONTINUE removes the overlay and resumes the loop.
     * Pressing RETURN TO MAP stops music and navigates to level selection.
     */
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

    /**
     * Loads a classpath MP3 resource into a {@link MediaPlayer}.
     *
     * <p>JavaFX {@link Media} requires a {@code file:} URI and cannot read
     * {@code jar:} URIs, so the resource is first copied to a temporary file.
     * The temp file is registered for deletion on JVM exit.
     * Volume is set to 15 % of maximum to prevent audio clipping.
     *
     * @param path classpath-relative path to the MP3 (e.g. {@code "/music/001.mp3"})
     * @return a configured {@link MediaPlayer} ready for playback,
     *         or {@code null} if the resource cannot be found or loaded
     */
    private MediaPlayer openPlayer(String path) {
        try {
            java.io.InputStream in = Objects.requireNonNull(getClass().getResourceAsStream(path));
            java.nio.file.Path tmp = java.nio.file.Files.createTempFile("battlecat_", ".mp3");
            tmp.toFile().deleteOnExit();
            java.nio.file.Files.copy(in, tmp, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            in.close();
            MediaPlayer player = new MediaPlayer(new Media(tmp.toUri().toString()));
            player.setVolume(0.15);
            return player;
        } catch (Exception e) {
            System.out.println("Could not load audio: " + e.getMessage());
            return null;
        }
    }

    /**
     * Starts looping background music for the title and level-selection screens
     * ({@code /music/001.mp3}). No-ops if the track is already playing.
     * Disposes the previous player before creating a new one.
     */
    private void startMenuMusic() {
        if (menuMusicPlayer != null && menuMusicPlayer.getStatus() == MediaPlayer.Status.PLAYING) return;
        if (menuMusicPlayer != null) { menuMusicPlayer.stop(); menuMusicPlayer.dispose(); }
        menuMusicPlayer = openPlayer("/music/001.mp3");
        if (menuMusicPlayer != null) {
            menuMusicPlayer.setCycleCount(MediaPlayer.INDEFINITE);
            menuMusicPlayer.play();
        }
    }

    /**
     * Stops and disposes the menu music player.
     * Safe to call when no menu music is currently playing.
     */
    private void stopMenuMusic() {
        if (menuMusicPlayer != null) {
            menuMusicPlayer.stop();
            menuMusicPlayer.dispose();
            menuMusicPlayer = null;
        }
    }

    /**
     * Starts looping background music for the current battle stage.
     * Any previously playing game music is stopped and disposed before the new
     * track begins.
     *
     * @param path classpath-relative path to the MP3 track (e.g. {@code "/music/003.mp3"})
     */
    private void startGameMusic(String path) {
        if (gameMusicPlayer != null) { gameMusicPlayer.stop(); gameMusicPlayer.dispose(); }
        gameMusicPlayer = openPlayer(path);
        if (gameMusicPlayer != null) {
            gameMusicPlayer.setCycleCount(MediaPlayer.INDEFINITE);
            gameMusicPlayer.play();
        }
    }

    /**
     * Stops and disposes the game music player.
     * Safe to call when no game music is currently playing.
     */
    private void stopGameMusic() {
        if (gameMusicPlayer != null) {
            gameMusicPlayer.stop();
            gameMusicPlayer.dispose();
            gameMusicPlayer = null;
        }
    }

    /**
     * Stops the stage music and plays a one-shot win/lose result jingle.
     * The jingle plays once with no loop and stops naturally at the end.
     *
     * <p>This method may be called from the game loop thread; it wraps all
     * {@link MediaPlayer} operations inside {@link Platform#runLater} to satisfy
     * JavaFX's thread requirement.
     *
     * @param path classpath-relative path to the MP3 result track
     *             (e.g. {@code "/music/008.mp3"} for win, {@code "/music/009.mp3"} for lose)
     */
    private void playResultMusic(String path) {
        Platform.runLater(() -> {
            stopGameMusic();
            gameMusicPlayer = openPlayer(path);
            if (gameMusicPlayer != null) gameMusicPlayer.play();
        });
    }

    /**
     * Builds and displays the title screen with a <em>START</em> button.
     * Stops the game loop if one is running and starts menu background music.
     */
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

    /**
     * Builds and displays the world-map level-selection screen.
     * Five clickable stage pins are positioned at their geographic locations on the
     * map image. Clicking a pin sets {@link #selectedStage} and calls
     * {@link #showGameScene()}. A <em>BACK</em> button returns to the title screen.
     * Menu music continues (or restarts) on this screen.
     */
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

    /**
     * Adds a new {@link models.base.TowerBurstEffect} at a random position within
     * the given tower's sprite bounds. Called every 12 frames during the
     * post-destruction countdown to simulate the tower crumbling.
     *
     * @param tower the tower whose bounds are used to position the burst effect
     */
    private void spawnTowerBurst(models.base.Tower tower) {
        double tW = tower.getRenderWidth() > 0 ? tower.getRenderWidth() : 80;
        double tH = tower.getRenderHeight() > 0 ? tower.getRenderHeight() : 150;
        double groundY = 410;
        double wx = tower.getX() + rng.nextDouble() * tW - tW / 2;
        double sy = groundY - rng.nextDouble() * tH;
        burstEffects.add(new models.base.TowerBurstEffect(wx, sy));
    }

    /**
     * Creates a button {@link StackPane} composed of a background image and a
     * centred text label, with a hover-dim effect.
     * Used for pause-menu and game-over overlay buttons.
     *
     * @param btnImg the image to use as the button background
     * @param text   the label text displayed on the button
     * @param w      desired button width in pixels
     * @param h      desired button height in pixels
     * @return the assembled {@link StackPane} button node
     */
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

    /**
     * Sets {@link #isGameOver} and posts a game-over overlay to the JavaFX
     * Application Thread. The overlay shows the result text and a
     * <em>RETURN TO MAP</em> button that stops music and navigates to level selection.
     * The render loop continues after this call so burst animations keep playing.
     *
     * @param resultText the result message to display ({@code "YOU WIN!"} or {@code "YOU LOSE!"})
     */
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

    /**
     * Application entry point. Delegates to {@link Application#launch(String[])}.
     *
     * @param args command-line arguments (not used)
     */
    public static void main(String[] args) {
        launch(args);
    }
}
