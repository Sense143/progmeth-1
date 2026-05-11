package logic;

import models.base.Tower;
import models.base.Unit;
import models.enemies.Stickman;
import models.units.TofuCat;
import models.units.UFOCat;

import java.util.ArrayList;

/**
 * Singleton that owns all living units on the battlefield and provides
 * target-lookup queries used by unit AI every frame.
 *
 * <p>Player units (cats) and enemy units (dogs/bosses) are stored in separate
 * lists. A unit attacks the opposite list. The singleton is reset between
 * stages via {@link #clearAll()}.
 */
public class BattleManager {
    private static BattleManager instance;
    private ArrayList<Unit> playerUnits = new ArrayList<>();
    private ArrayList<Unit> enemyUnits = new ArrayList<>();

    /** Private constructor — use {@link #getInstance()} to obtain the singleton. */
    private BattleManager() {}

    /**
     * Returns the shared singleton instance, creating it on first call.
     *
     * @return the global {@code BattleManager}
     */
    public static BattleManager getInstance() {
        if (instance == null) instance = new BattleManager();
        return instance;
    }

    /**
     * Returns all enemy units within the attacker's attack-range band.
     *
     * <p>Towers are included even at 0 HP (so units keep attacking a dying
     * tower). Distance is normally measured from the attacker's X to the
     * target's rim position, with special-case overrides for {@link TofuCat}
     * and {@link Stickman}/{@link UFOCat} pairs.
     *
     * @param attacker the unit performing the search
     * @return list of valid targets; never {@code null} but may be empty
     */
    public ArrayList<Unit> findMultipleTargetsInRange(Unit attacker) {
        ArrayList<Unit> potentialTargets = (isPlayerUnit(attacker)) ? enemyUnits : playerUnits;
        ArrayList<Unit> targets = new ArrayList<>();
        for (Unit target : potentialTargets) {
            if(target.getHp() <= 0 && !(target instanceof models.base.Tower)) continue;
            double distance = Math.abs(attacker.getX() - target.getRimPosition());
            if(target instanceof TofuCat){
                distance = Math.abs(attacker.getX() - target.getX());
            }
            if(target instanceof Stickman && attacker instanceof UFOCat){
                distance = Math.abs(attacker.getX() - target.getX());
            }
            if (distance <= attacker.getAttackRange() && distance >= attacker.getAttackRangeMin()) {
                targets.add(target);
            }
        }
        return targets;
    }

    /**
     * Returns the full enemy-unit list, including units at 0 HP (used by
     * the cannon-wave sweep to hit everything on the board).
     *
     * @return the live enemy list (direct reference, do not modify)
     */
    public ArrayList<Unit> getEveryTargetOnBoard(){
        return enemyUnits;
    }

    /**
     * Returns the first enemy unit within the attacker's attack range, or
     * {@code null} if no target is in range.
     *
     * @param attacker the unit performing the search
     * @return the nearest valid target, or {@code null}
     */
    public Unit findSingleTargetInRange(Unit attacker) {
        ArrayList<Unit> Targets = (isPlayerUnit(attacker)) ? enemyUnits : playerUnits;
        for (Unit target : Targets) {
            if(target.getHp() <= 0 && !(target instanceof models.base.Tower)) continue;
            double distance = Math.abs(attacker.getX() - target.getRimPosition());
            if(target instanceof TofuCat){
                distance = Math.abs(attacker.getX() - target.getX());
            }
            if (distance <= attacker.getAttackRange()) {
                return target;
            }
        }
        return null;
    }

    /**
     * Returns {@code true} if the given unit belongs to the player side.
     */
    private boolean isPlayerUnit(Unit unit) {
        return playerUnits.contains(unit);
    }

    /**
     * Registers a unit as a player (cat) unit.
     *
     * @param unit the unit to register
     */
    public void addPlayerUnit(Unit unit) {
        playerUnits.add(unit);
    }

    /**
     * Registers a unit as an enemy (dog/boss) unit.
     *
     * @param unit the unit to register
     */
    public void addEnemyUnit(Unit unit) {
        enemyUnits.add(unit);
    }

    /**
     * Removes a unit from whichever side it belongs to.
     *
     * @param unit the unit to remove
     */
    public void removeUnit(Unit unit) {
        playerUnits.remove(unit);
        enemyUnits.remove(unit);
    }

    /**
     * Returns the list of all registered player units.
     *
     * @return player unit list (direct reference)
     */
    public ArrayList<Unit> getPlayerUnits() { return playerUnits; }

    /**
     * Returns the list of all registered enemy units.
     *
     * @return enemy unit list (direct reference)
     */
    public ArrayList<Unit> getEnemyUnits()  { return enemyUnits; }

    /**
     * Clears both unit lists. Called when a stage ends to reset state
     * before loading the next stage.
     */
    public void clearAll() {
        playerUnits.clear();
        enemyUnits.clear();
    }
}