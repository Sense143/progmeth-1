package logic;

import models.base.Unit;

import java.util.ArrayList;

public class BattleManager {
    private static BattleManager instance;
    private ArrayList<Unit> playerUnits = new ArrayList<>();
    private ArrayList<Unit> enemyUnits = new ArrayList<>();

    public static BattleManager getInstance() {
        if (instance == null) instance = new BattleManager();
        return instance;
    }

    public ArrayList<Unit> findMultipleTargetsInRange(Unit attacker) {
        ArrayList<Unit> potentialTargets = (isPlayerUnit(attacker)) ? enemyUnits : playerUnits;
        ArrayList<Unit> targets = new ArrayList<>();
        for (Unit target : potentialTargets) {
            double distance = Math.abs(attacker.getX() - target.getX());
            if (distance <= attacker.getAttackRange()) {
                targets.add(target);
            }
        }
        return targets;
    }

    public Unit findSingleTargetInRange(Unit attacker) {
        ArrayList<Unit> Targets = (isPlayerUnit(attacker)) ? enemyUnits : playerUnits;
        for (Unit target : Targets) {
            double distance = Math.abs(attacker.getX() - target.getX());
            if (distance <= attacker.getAttackRange()) {
                return target;
            }
        }
        return null;
    }

    private boolean isPlayerUnit(Unit unit) {
        return playerUnits.contains(unit);
    }

    public void addPlayerUnit(Unit unit) {
        playerUnits.add(unit);
    }

    public void addEnemyUnit(Unit unit) {
        enemyUnits.add(unit);
    }

    public void removeUnit(Unit unit) {
        playerUnits.remove(unit);
        enemyUnits.remove(unit);
    }

    public void clearAll() {
        playerUnits.clear();
        enemyUnits.clear();
    }
}
