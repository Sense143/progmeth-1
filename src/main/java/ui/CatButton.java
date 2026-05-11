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

/**
 * An interactive unit-deploy button used in the in-game HUD.
 *
 * <p>Shows the unit's icon, its cost label, a black overlay when the player
 * cannot afford it ({@link #moneyDim}), and an animated cooldown sweep that
 * fills from top and drains away after a successful deploy.
 *
 * <p>Call {@link #updateState(int)} every frame with the player's current
 * money so the affordability overlay stays in sync.
 */
public class CatButton extends StackPane {

    /** Whether the button is currently in its post-deploy cooldown. */
    private boolean isCooldown = false;
    /** Spawn cost of the associated unit. */
    private int cost;
    /**
     * Semi-transparent overlay shown when the player cannot afford this unit.
     * Hidden during cooldown because the cooldown overlay takes its place.
     */
    private Rectangle moneyDim;

    /**
     * Constructs a cat deploy button.
     *
     * @param imagePath      classpath resource path to the unit icon image
     * @param cooldownTime   seconds the button is locked after a successful deploy
     * @param cost           money cost to deploy the unit
     * @param onSpawnAction  called when the button is clicked and not on cooldown;
     *                       should attempt to spawn the unit and return {@code true}
     *                       on success (money deducted) or {@code false} on failure
     */
    public CatButton(String imagePath, double cooldownTime, int cost, BooleanSupplier onSpawnAction) {
        this.cost = cost;

        double imgW = 100;
        double imgH = 80;

        this.setMinSize(imgW, imgH);
        this.setMaxSize(imgW, imgH);
        this.setPrefSize(imgW, imgH);
        this.setCursor(Cursor.HAND);

        ImageView catIcon = new ImageView();
        try {
            catIcon.setImage(new Image(Objects.requireNonNull(getClass().getResourceAsStream(imagePath))));
            catIcon.setFitWidth(imgW);
            catIcon.setFitHeight(imgH);
            catIcon.setPreserveRatio(false);
        } catch (Exception err) {
            System.err.println("หาภาพไม่เจอ: " + imagePath);
        }

        moneyDim = new Rectangle(imgW, imgH);
        moneyDim.setFill(Color.BLACK);
        moneyDim.setOpacity(0.65);
        moneyDim.setVisible(true);

        Rectangle cooldownDim = new Rectangle(imgW, 0);
        cooldownDim.setFill(Color.BLACK);
        cooldownDim.setOpacity(0.65);
        StackPane.setAlignment(cooldownDim, Pos.TOP_CENTER);

        Rectangle clip = new Rectangle(imgW, imgH);
        this.setClip(clip);

        Label priceLabel = new Label("$" + cost);
        priceLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px; -fx-effect: dropshadow(gaussian, black, 2, 1.0, 0, 0);");
        StackPane.setAlignment(priceLabel, Pos.BOTTOM_RIGHT);
        StackPane.setMargin(priceLabel, new Insets(0, 5, 2, 0));

        this.getChildren().addAll(catIcon, moneyDim, cooldownDim, priceLabel);

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

    /**
     * Refreshes the affordability overlay based on the player's current money.
     * Must be called once per frame from the game loop.
     *
     * @param currentMoney the player's current money amount
     */
    public void updateState(int currentMoney) {
        if (isCooldown) {
            moneyDim.setVisible(false);
        } else {
            moneyDim.setVisible(currentMoney < cost);
        }
    }
}