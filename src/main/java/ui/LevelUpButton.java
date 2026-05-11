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

/**
 * A wallet-upgrade button displayed in the in-game HUD.
 *
 * <p>Shows the current wallet level and upgrade cost. When the player cannot
 * afford the upgrade, the icon dims to 50% opacity while the cost label
 * remains fully visible. Once the wallet reaches its maximum level the button
 * is permanently disabled and labelled "MAX LEVEL".
 *
 * <p>Call {@link #updateState(int)} every frame with the player's current
 * money to keep the affordability state in sync. Call {@link #markMaxLevel()}
 * after a successful upgrade that reaches the cap.
 */
public class LevelUpButton extends Button {

    /** Current wallet level (1-indexed, displayed in the label). */
    private int currentLevel = 1;
    /** Cost of the next upgrade, shown in the label. */
    private int currentCost;
    /** {@code true} once the wallet has reached the maximum upgrade level. */
    private boolean maxLevel = false;
    private Label textLabel;
    private ImageView icon;

    /**
     * Constructs a wallet-upgrade button.
     *
     * @param imagePath       classpath resource path to the wallet icon image
     * @param startCost       cost of the first upgrade
     * @param onUpgradeAction called when the button is clicked; should attempt
     *                        the upgrade and return {@code true} on success
     */
    public LevelUpButton(String imagePath, int startCost, BooleanSupplier onUpgradeAction) {
        this.currentCost = startCost;

        this.setPrefSize(90, 95);
        this.setCursor(Cursor.HAND);
        this.setStyle("-fx-background-color: transparent;");

        this.setOnMousePressed(e -> this.setOpacity(0.6));
        this.setOnMouseReleased(e -> this.setOpacity(1.0));

        StackPane graphicContainer = new StackPane();

        try {
            this.icon = new ImageView(new Image(Objects.requireNonNull(getClass().getResourceAsStream(imagePath))));
            this.icon.setFitWidth(150);
            this.icon.setFitHeight(150);
            this.icon.setPreserveRatio(true);

            textLabel = new Label();
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

    /**
     * Refreshes the button label.
     * Shows "MAX LEVEL" when the wallet is maxed; otherwise "Lv.N | $cost".
     */
    private void updateText() {
        if (textLabel == null) return;
        if (maxLevel) {
            textLabel.setText("MAX LEVEL");
        } else {
            textLabel.setText("Lv." + currentLevel + " | $" + currentCost);
        }
    }

    /**
     * Locks the button permanently to indicate the wallet is fully upgraded.
     * Restores the icon to full opacity and disables the button.
     */
    public void markMaxLevel() {
        maxLevel = true;
        if (icon != null) icon.setOpacity(1.0);
        this.setDisable(true);
        updateText();
    }

    /**
     * Updates the cost shown in the label for the next upgrade.
     * Call this after each successful upgrade to show the upcoming cost.
     *
     * @param newCost the cost of the next wallet upgrade
     */
    public void setNextCost(int newCost) {
        this.currentCost = newCost;
        updateText();
    }

    /**
     * Refreshes the button's enabled/disabled state and icon opacity based on
     * whether the player can currently afford the upgrade. No-ops if already
     * at max level.
     *
     * @param playerMoney the player's current money amount
     */
    public void updateState(int playerMoney) {
        if (maxLevel) return;
        if (playerMoney < currentCost) {
            if (icon != null) icon.setOpacity(0.5);
            this.setOpacity(1.0);
            this.setDisable(true);
        } else {
            if (icon != null) icon.setOpacity(1.0);
            this.setDisable(false);
        }
    }
}