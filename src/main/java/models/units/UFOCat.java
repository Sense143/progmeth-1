package models.units;

import javafx.scene.image.Image;
import logic.BattleManager;
import models.base.AoeUnit;
import models.base.Unit;
import models.enemies.Stickman;

import java.util.ArrayList;
import java.util.Objects;

/**
 * An AoE cat unit that fires a three-wave rolling explosion attack.
 *
 * <p>Stats: 2300 HP | 300 damage | 3000 ms cooldown | 210 px range | −2.5 speed.
 *
 * <p>When the attack fires, three explosions are launched sequentially with a
 * 150 ms delay between them. Each wave covers a 70 px band of the range:
 * wave 1 hits [0–70 px], wave 2 hits [70–140 px], wave 3 hits [140–210 px].
 * The rolling wave is driven by {@link #isWaveActive} and {@link #currentWaveStep}
 * so it runs independently of the base {@link AoeUnit} attack loop.
 */
public class UFOCat extends AoeUnit {

    /** True while a three-wave attack sequence is in progress. */
    private boolean isWaveActive = false;
    /** Index (0–2) of the wave currently waiting to fire. */
    private int currentWaveStep = 0;
    /** Timestamp of the last wave step, used to space waves 150 ms apart. */
    private long lastWaveTime = 0;
    /** Width in pixels of each individual wave band. */
    private final double WAVE_WIDTH = 70;
    /** Total attack range covered by all three waves combined. */
    private final double MAX_WAVE_RANGE = 210;

    /**
     * Constructs a UFOCat at the default spawn position (X = 2300) and loads
     * its walk, attack, idle, and knockback sprites.
     */
    public UFOCat() {
        super("UFO Cat", 2300, 300, 150, 3000, 210, -2.5);
        this.attackRangeMin = 0;

        try {
            walkSprites[0] = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/cat/animation/UFOCat/005_c_1.png")));
            walkSprites[1] = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/cat/animation/UFOCat/005_c_2.png")));
            walkSprites[2] = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/cat/animation/UFOCat/005_c_3.png")));
            attackSprites[0] = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/cat/animation/UFOCat/005_c_a1.png")));
            attackSprites[1] = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/cat/animation/UFOCat/005_c_a2.png")));
            attackSprites[2] = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/cat/animation/UFOCat/005_c_a3.png")));
            idleSprite = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/cat/animation/UFOCat/005_c_idle.png")));
            knockbackSprite = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/cat/animation/UFOCat/005_c_dead.png")));
        } catch (Exception e) {
            System.out.println("โหลดรูป UFOCat ไม่สำเร็จ: " + e.getMessage());
        }
    }

    /**
     * Per-frame AI loop with two phases:
     * <ul>
     *   <li><b>Wave-active phase:</b> fires one wave step every 150 ms until
     *       all three steps are complete, then clears the wave flag.</li>
     *   <li><b>Normal phase:</b> looks for targets in the full 210 px range;
     *       when found and the cooldown has elapsed, starts the wave sequence
     *       by immediately firing wave step 0.</li>
     * </ul>
     */
    @Override
    public void update() {
        updateAnimation();
        if(this.hp <= 0) return;

        long currentTime = System.currentTimeMillis();

        if (isWaveActive) {
            setState(State.ATTACK);

            if (currentTime - lastWaveTime >= 150) {
                executeWaveStep(currentWaveStep);
                lastWaveTime = currentTime;
                currentWaveStep++;

                if (currentWaveStep >= 3) {
                    isWaveActive = false;
                    currentWaveStep = 0;
                }
            }
            return;
        }

        this.attackRange = MAX_WAVE_RANGE;
        this.attackRangeMin = 0;

        ArrayList<Unit> targets = BattleManager.getInstance().findMultipleTargetsInRange(this);

        if(targets != null && !targets.isEmpty()){
            isAttacking = true;
            long timeSinceLastAttack = currentTime - lastAttackTime;

            if (timeSinceLastAttack < 500) {
                setState(State.ATTACK);
            } else if (timeSinceLastAttack >= attackCooldown) {
                isWaveActive = true;
                currentWaveStep = 0;
                lastWaveTime = currentTime;
                lastAttackTime = currentTime;

                executeWaveStep(currentWaveStep);
                currentWaveStep++;
            } else {
                setState(State.IDLE);
            }
        } else {
            isAttacking = false;
            setState(State.WALK);
            this.move();
        }
    }

    /**
     * Fires a single wave step: collects all enemies in the full range, then
     * filters to those inside the current wave's band, deals damage to each,
     * and spawns an explosion effect at the band's centre.
     *
     * <p>Wave bands (measured from the UFOCat's centre):
     * <ul>
     *   <li>Step 0: 0 – 70 px</li>
     *   <li>Step 1: 70 – 140 px</li>
     *   <li>Step 2: 140 – 210 px</li>
     * </ul>
     *
     * @param cnt the wave step index (0–2); {@code currentWaveStep} is used
     *            inside the method to compute the band boundaries
     */
    private void executeWaveStep(int cnt) {
        this.attackRangeMin = 0;
        this.attackRange = MAX_WAVE_RANGE;
        ArrayList<Unit> allTargets = BattleManager.getInstance().findMultipleTargetsInRange(this);

        ArrayList<Unit> targetsToHitNow = new ArrayList<>();
        double currentMaxRange = (currentWaveStep + 1) * WAVE_WIDTH;

        if (allTargets != null) {
            for (Unit t : allTargets) {
                double distance = Math.abs(this.getX() - t.getRimPosition());
                if(t instanceof Stickman){
                    distance = Math.abs(this.getX() - t.getX());
                }

                if (distance <= currentMaxRange && distance >= currentWaveStep * WAVE_WIDTH) {
                    targetsToHitNow.add(t);
                }
            }
        }

        for (Unit t : targetsToHitNow) {
            t.takeDamage(this.attackDamage);
        }

        double direction = (this.getSpeed() < 0) ? -1 : 1;
        double waveCenterDistance = (currentWaveStep * WAVE_WIDTH) + (WAVE_WIDTH / 2);

        double ufoCenterX = this.getX() + (this.getRenderWidth() / 2);
        double spawnX = ufoCenterX + (direction * waveCenterDistance);

        logic.EffectManager.getInstance().spawnExplosion(spawnX, 410);
    }

    /** @return 100 pixels */
    public double getRenderWidth() { return 100; }

    /** @return 120 pixels */
    public double getRenderHeight() { return 120; }
}