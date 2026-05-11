package interfaces;

/**
 * Defines the contract for any entity that can move along the battlefield.
 */
public interface Moveable {

    /**
     * Advances this entity's position by one step according to its speed.
     * Called once per game-loop frame while the entity is in the WALK state.
     */
    void move();
}