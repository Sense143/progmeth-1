package models.base;

import interfaces.Attackable;
import interfaces.Moveable;
import logic.BattleManager;

import java.util.ArrayList;

/**
 * Abstract base class for units that attack one enemy at a time.
 *
 * <p>Each frame the unit checks for a single target in range via
 * {@link BattleManager#findSingleTargetInRange}. If a target is found it
 * locks in and begins the attack animation; damage is delivered on animation
 * frame 3 by the inherited {@link Unit#processAttackDamage()} mechanism.
 * When no target is present, the unit walks forward.
 *
 * <p>Concrete subclasses provide sprites and stat values through the
 * {@link #SingleTargetUnit(String, double, double, double, double, double, double)}
 * constructor.
 */
public class SingleTargetUnit extends Unit implements Attackable, Moveable {

    /**
     * Constructs a single-target unit with the given stats.
     *
     * @param name           display name
     * @param x              initial world X position
     * @param hp             starting hit points
     * @param attackDamage   damage per attack
     * @param attackCooldown milliseconds between attacks
     * @param attackRange    attack range in pixels
     * @param speed          pixels moved per frame (negative = left)
     */
    public SingleTargetUnit(String name, double x, double hp, double attackDamage, double attackCooldown, double attackRange, double speed) {
        super(name, x, hp, attackDamage, attackCooldown, attackRange, speed);
        this.isAoe = false;
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
     * Not used for single-target units; this variant is a no-op.
     *
     * @param targets ignored
     */
    @Override
    public void Attack(ArrayList<Unit> targets) {
        return;
    }

    /**
     * Legacy direct-damage method. Applies {@link #attackDamage} to
     * {@code target} if the cooldown has elapsed.
     *
     * @param target the unit to attack
     */
    @Override
    public void Attack(Unit target) {
        long currentTime = System.currentTimeMillis();
        if(currentTime - lastAttackTime >= attackCooldown){
            target.takeDamage(this.attackDamage);
            lastAttackTime = currentTime;
        }
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
     * Called when no target is in range and the unit is not mid-swing.
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
     *   <li>Finds a target; if found, starts the attack animation and locks
     *       the target for frame-3 damage delivery.</li>
     *   <li>If no target is found, walks forward.</li>
     * </ol>
     */
    @Override
    public void update() {
        updateAnimation();

        if(this.hp <= 0 || currentState == State.DEAD_KNOCKBACK || currentState == State.DEAD_SOUL) {
            return;
        }

        Unit target = BattleManager.getInstance().findSingleTargetInRange(this);
        if(target != null) {
            isAttacking = true;
            long currentTime = System.currentTimeMillis();

            if (currentTime - lastAttackTime >= attackCooldown && currentState != State.ATTACK) {
                setState(State.ATTACK);
                lastAttackTime = currentTime;
                hasDealtDamageThisAttack = false;

                currentTargets.clear();
                currentTargets.add(target);
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