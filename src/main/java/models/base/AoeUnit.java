package models.base;

import interfaces.Attackable;
import interfaces.Moveable;
import logic.BattleManager;

import java.util.ArrayList;

/**
 * Abstract base class for units that attack all enemies in range simultaneously
 * (area-of-effect / AoE).
 *
 * <p>Each frame the unit collects every target within its attack range via
 * {@link BattleManager#findMultipleTargetsInRange}. When any target is found
 * it begins the attack animation; damage is delivered to the full target list
 * on animation frame 3 by the inherited {@link Unit#processAttackDamage()}
 * mechanism. When no targets are present the unit walks forward.
 *
 * <p>Concrete subclasses provide sprites and stat values via
 * {@link #AoeUnit(String, double, double, double, double, double, double)}.
 */
public class AoeUnit extends Unit implements Attackable, Moveable {

    /**
     * Constructs an AoE unit with the given stats.
     *
     * @param name           display name
     * @param x              initial world X position
     * @param hp             starting hit points
     * @param attackDamage   damage per attack (applied to each target in range)
     * @param attackCooldown milliseconds between attacks
     * @param attackRange    attack range in pixels
     * @param speed          pixels moved per frame (negative = left)
     */
    public AoeUnit(String name, double x, double hp, double attackDamage, double attackCooldown, double attackRange, double speed) {
        super(name, x, hp, attackDamage, attackCooldown, attackRange, speed);
        this.isAoe = true;
    }

    /**
     * Reduces HP by {@code damage}, clamped to 0.
     *
     * @param damage the damage amount
     */
    @Override
    public void takeDamage(double damage) {
        this.hp -= damage;
        this.hp = Math.max(this.hp, 0);
    }

    /**
     * Deals {@link #attackDamage} to every unit in {@code targets} if the
     * attack cooldown has elapsed.
     *
     * @param targets the list of units to damage
     */
    @Override
    public void Attack(ArrayList<Unit> targets) {
        long currentTime = System.currentTimeMillis();
        if(currentTime - lastAttackTime >= attackCooldown){
            for(Unit target : targets){
                target.takeDamage(this.attackDamage);
            }
            lastAttackTime = currentTime;
        }
    }

    /**
     * Not used for AoE units; this single-target variant is a no-op.
     *
     * @param target ignored
     */
    @Override
    public void Attack(Unit target) {
        return;
    }

    /**
     * Returns {@code true} once the unit's death animation has fully completed
     * and the unit can be removed from all lists.
     *
     * @return {@code true} when completely dead
     */
    @Override
    public boolean isDead() {
        return this.completelyDead;
    }

    /**
     * Moves the unit one step in its natural direction.
     * Called when no targets are in range and the unit is not mid-swing.
     */
    @Override
    public void move() {
        this.x += this.speed;
    }

    /**
     * Per-frame AI loop:
     * <ol>
     *   <li>Runs {@link #updateAnimation()} to advance sprites and death phases.</li>
     *   <li>Returns early if the unit is dead or dying.</li>
     *   <li>Collects all targets in range; if any are found, starts the attack
     *       animation and stores the full target list for frame-3 AoE damage.</li>
     *   <li>If no targets are found, walks forward.</li>
     * </ol>
     */
    @Override
    public void update() {
        updateAnimation();

        if(this.hp <= 0 || currentState == State.DEAD_KNOCKBACK || currentState == State.DEAD_SOUL) {
            return;
        }

        ArrayList<Unit> targets = BattleManager.getInstance().findMultipleTargetsInRange(this);

        if(targets != null && !targets.isEmpty()) {
            isAttacking = true;
            long currentTime = System.currentTimeMillis();

            if (currentTime - lastAttackTime >= attackCooldown && currentState != State.ATTACK) {
                setState(State.ATTACK);
                lastAttackTime = currentTime;
                hasDealtDamageThisAttack = false;

                currentTargets.clear();
                currentTargets.addAll(targets);
            }
        } else {
            isAttacking = false;
            if (currentState != State.ATTACK) {
                setState(State.WALK);
                this.move();
            }
        }
    }

    /** @return 0 — concrete subclasses override with the actual sprite width */
    @Override
    public double getRenderWidth() {
        return 0;
    }

    /** @return 0 — concrete subclasses override with the actual sprite height */
    @Override
    public double getRenderHeight() {
        return 0;
    }
}