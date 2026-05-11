package models.base;

import interfaces.Attackable;

import java.util.ArrayList;

/**
 * Represents an immovable base structure (tower) for either side.
 *
 * <p>Towers are stationary targets with HP that never regenerates. When a
 * tower's HP reaches 0 the game ends. Unlike regular units, towers remain
 * in the battle lists at 0 HP so that attacking units continue to animate
 * against the defeated tower until the game-over sequence fires.
 */
public class Tower extends Unit{

    private double maxHp;

    /**
     * Returns the tower's initial (maximum) HP, used to draw the HP bar.
     *
     * @return max HP
     */
    public double getMaxHp() {
        return maxHp;
    }

    /**
     * Constructs a tower at the given position with the given HP.
     *
     * @param name display name
     * @param x    world X position
     * @param hp   starting (and maximum) hit points
     */
    public Tower(String name, double x, double hp) {
        super(name, x, hp, 0, 0, 0, 0);
        this.maxHp = hp;
    }

    /** Towers have no per-frame AI; this method is intentionally empty. */
    @Override
    public void update() {

    }

    /** @return 0 — subclasses override with their actual sprite width */
    @Override
    public double getRenderWidth() {
        return 0;
    }

    /** @return 0 — subclasses override with their actual sprite height */
    @Override
    public double getRenderHeight() {
        return 0;
    }

    /**
     * Reduces this tower's HP by {@code damage}, clamped to 0.
     *
     * @param damage the amount of damage to apply
     */
    @Override
    public void takeDamage(double damage) {
        this.hp = Math.max(0, this.hp - damage);
    }

    /** Towers do not initiate attacks; this method is intentionally empty. */
    @Override
    public void Attack(ArrayList<Unit> targets) {

    }

    /** Towers do not initiate attacks; this method is intentionally empty. */
    @Override
    public void Attack(Unit target) {

    }

    /**
     * A tower is considered dead when its HP reaches 0, triggering the
     * game-over sequence. Note: unlike regular units, the tower is NOT removed
     * from the board immediately — the game loop handles the end-state.
     *
     * @return {@code true} when HP == 0
     */
    @Override
    public boolean isDead() {
        return this.hp <= 0;
    }
}