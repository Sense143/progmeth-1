package interfaces;

import models.base.Unit;

import java.util.ArrayList;

/**
 * Defines the contract for any entity that can participate in combat —
 * receiving damage, dealing damage to targets, and reporting its death state.
 */
public interface Attackable {

    /**
     * Reduces this entity's HP by the given amount.
     *
     * @param damage the amount of damage to apply (always non-negative)
     */
    void takeDamage(double damage);

    /**
     * Attacks a list of targets simultaneously (area-of-effect variant).
     *
     * @param targets the units to attack
     */
    void Attack(ArrayList<Unit> targets);

    /**
     * Attacks a single target (single-target variant).
     *
     * @param target the unit to attack
     */
    void Attack(Unit target);

    /**
     * Returns {@code true} when this entity has finished its death animation
     * and should be removed from the board.
     *
     * @return {@code true} if fully dead and safe to remove
     */
    boolean isDead();
}