package logic;

import models.base.Unit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class BattleManagerTest {

    /**
     * Minimal Unit stub — no JavaFX image loading, suitable for all BattleManager tests.
     * speed > 0  → moving right (enemy-like rim = x + width/2)
     * speed < 0  → moving left  (player-like rim = x - width/2)
     */
    static class StubUnit extends Unit {
        StubUnit(double x, double hp, double attackRange, double speed) {
            super("Stub", x, hp, 10, 500, attackRange, speed);
        }

        @Override public void update() {}
        @Override public void Attack(ArrayList<Unit> targets) {}
        @Override public void Attack(Unit target) {}
        @Override public boolean isDead() { return completelyDead; }
        @Override public double getRenderWidth() { return 50; }
        @Override public double getRenderHeight() { return 50; }
    }

    private BattleManager manager;

    @BeforeEach
    void setUp() {
        manager = BattleManager.getInstance();
        manager.clearAll();
    }

    // ─── addPlayerUnit / addEnemyUnit ─────────────────────────────────────────

    @Test
    void addPlayerUnit_appearsInPlayerList() {
        StubUnit unit = new StubUnit(100, 100, 50, -3);
        manager.addPlayerUnit(unit);
        assertTrue(manager.getPlayerUnits().contains(unit));
    }

    @Test
    void addPlayerUnit_doesNotAppearInEnemyList() {
        StubUnit unit = new StubUnit(100, 100, 50, -3);
        manager.addPlayerUnit(unit);
        assertFalse(manager.getEnemyUnits().contains(unit));
    }

    @Test
    void addEnemyUnit_appearsInEnemyList() {
        StubUnit unit = new StubUnit(100, 100, 50, 3);
        manager.addEnemyUnit(unit);
        assertTrue(manager.getEnemyUnits().contains(unit));
    }

    @Test
    void addEnemyUnit_doesNotAppearInPlayerList() {
        StubUnit unit = new StubUnit(100, 100, 50, 3);
        manager.addEnemyUnit(unit);
        assertFalse(manager.getPlayerUnits().contains(unit));
    }

    // ─── removeUnit ───────────────────────────────────────────────────────────

    @Test
    void removeUnit_removesFromPlayerList() {
        StubUnit unit = new StubUnit(100, 100, 50, -3);
        manager.addPlayerUnit(unit);
        manager.removeUnit(unit);
        assertFalse(manager.getPlayerUnits().contains(unit));
    }

    @Test
    void removeUnit_removesFromEnemyList() {
        StubUnit unit = new StubUnit(100, 100, 50, 3);
        manager.addEnemyUnit(unit);
        manager.removeUnit(unit);
        assertFalse(manager.getEnemyUnits().contains(unit));
    }

    @Test
    void removeUnit_nonExistent_doesNotThrow() {
        StubUnit unit = new StubUnit(100, 100, 50, 3);
        assertDoesNotThrow(() -> manager.removeUnit(unit));
    }

    // ─── clearAll ─────────────────────────────────────────────────────────────

    @Test
    void clearAll_emptiesPlayerList() {
        manager.addPlayerUnit(new StubUnit(100, 100, 50, -3));
        manager.clearAll();
        assertTrue(manager.getPlayerUnits().isEmpty());
    }

    @Test
    void clearAll_emptiesEnemyList() {
        manager.addEnemyUnit(new StubUnit(100, 100, 50, 3));
        manager.clearAll();
        assertTrue(manager.getEnemyUnits().isEmpty());
    }

    // ─── findSingleTargetInRange ──────────────────────────────────────────────

    // Player at x=100, range=50.
    // Enemy at x=120, speed=3, renderWidth=50 → rimPos = 120+25 = 145
    // distance = |100 - 145| = 45 ≤ 50  →  found
    @Test
    void findSingleTarget_playerFindsEnemyInRange() {
        StubUnit player = new StubUnit(100, 100, 50, -3);
        StubUnit enemy  = new StubUnit(120, 100, 50,  3);
        manager.addPlayerUnit(player);
        manager.addEnemyUnit(enemy);
        assertEquals(enemy, manager.findSingleTargetInRange(player));
    }

    // Enemy at x=200, speed=3 → rimPos = 225; distance = |100 - 225| = 125 > 50
    @Test
    void findSingleTarget_playerFindsNoEnemyOutOfRange() {
        StubUnit player = new StubUnit(100, 100, 50, -3);
        StubUnit enemy  = new StubUnit(200, 100, 50,  3);
        manager.addPlayerUnit(player);
        manager.addEnemyUnit(enemy);
        assertNull(manager.findSingleTargetInRange(player));
    }

    @Test
    void findSingleTarget_noEnemiesPresent_returnsNull() {
        StubUnit player = new StubUnit(100, 100, 200, -3);
        manager.addPlayerUnit(player);
        assertNull(manager.findSingleTargetInRange(player));
    }

    // Dead enemy (hp=0, not Tower) → skipped
    @Test
    void findSingleTarget_skipsDeadEnemies() {
        StubUnit player    = new StubUnit(100, 100, 200, -3);
        StubUnit deadEnemy = new StubUnit(120,   0, 50,   3);
        manager.addPlayerUnit(player);
        manager.addEnemyUnit(deadEnemy);
        assertNull(manager.findSingleTargetInRange(player));
    }

    // Enemy at x=100, range=200; Player at x=80, speed=-3, renderWidth=50 → rimPos = 80-25 = 55
    // distance = |100 - 55| = 45 ≤ 200  →  found
    @Test
    void findSingleTarget_enemyFindsPlayerInRange() {
        StubUnit player = new StubUnit(80,  100, 50,  -3);
        StubUnit enemy  = new StubUnit(100, 100, 200,  3);
        manager.addPlayerUnit(player);
        manager.addEnemyUnit(enemy);
        assertEquals(player, manager.findSingleTargetInRange(enemy));
    }

    @Test
    void findSingleTarget_enemyFindsNoPlayerOutOfRange() {
        StubUnit player = new StubUnit(80,  100, 50, -3);
        StubUnit enemy  = new StubUnit(500, 100, 30,  3);
        // player rimPos = 80-25 = 55; distance = |500-55| = 445 > 30
        manager.addPlayerUnit(player);
        manager.addEnemyUnit(enemy);
        assertNull(manager.findSingleTargetInRange(enemy));
    }

    // ─── findMultipleTargetsInRange ───────────────────────────────────────────

    // Two enemies both within player's large range
    @Test
    void findMultipleTargets_returnsAllEnemiesInRange() {
        StubUnit player = new StubUnit(100, 100, 300, -3);
        StubUnit e1     = new StubUnit(120, 100,  50,  3); // rimPos=145, dist=45
        StubUnit e2     = new StubUnit(130, 100,  50,  3); // rimPos=155, dist=55
        manager.addPlayerUnit(player);
        manager.addEnemyUnit(e1);
        manager.addEnemyUnit(e2);
        ArrayList<Unit> targets = manager.findMultipleTargetsInRange(player);
        assertEquals(2, targets.size());
        assertTrue(targets.contains(e1));
        assertTrue(targets.contains(e2));
    }

    @Test
    void findMultipleTargets_excludesEnemiesOutOfRange() {
        StubUnit player    = new StubUnit(100, 100,  50, -3);
        StubUnit inRange   = new StubUnit(120, 100,  50,  3); // dist=45 ≤ 50
        StubUnit outOfRange= new StubUnit(300, 100,  50,  3); // dist=225 > 50
        manager.addPlayerUnit(player);
        manager.addEnemyUnit(inRange);
        manager.addEnemyUnit(outOfRange);
        ArrayList<Unit> targets = manager.findMultipleTargetsInRange(player);
        assertEquals(1, targets.size());
        assertTrue(targets.contains(inRange));
    }

    @Test
    void findMultipleTargets_skipsDeadUnits() {
        StubUnit player    = new StubUnit(100, 100, 300, -3);
        StubUnit deadEnemy = new StubUnit(120,   0,  50,  3);
        manager.addPlayerUnit(player);
        manager.addEnemyUnit(deadEnemy);
        assertTrue(manager.findMultipleTargetsInRange(player).isEmpty());
    }

    @Test
    void findMultipleTargets_noEnemies_returnsEmptyList() {
        StubUnit player = new StubUnit(100, 100, 300, -3);
        manager.addPlayerUnit(player);
        assertTrue(manager.findMultipleTargetsInRange(player).isEmpty());
    }

    // ─── getEveryTargetOnBoard ────────────────────────────────────────────────

    @Test
    void getEveryTargetOnBoard_containsAddedEnemies() {
        StubUnit enemy = new StubUnit(100, 100, 50, 3);
        manager.addEnemyUnit(enemy);
        assertTrue(manager.getEveryTargetOnBoard().contains(enemy));
    }

    @Test
    void getEveryTargetOnBoard_emptyWhenNoEnemies() {
        assertTrue(manager.getEveryTargetOnBoard().isEmpty());
    }

    @Test
    void getEveryTargetOnBoard_doesNotContainPlayerUnits() {
        StubUnit player = new StubUnit(100, 100, 50, -3);
        manager.addPlayerUnit(player);
        assertFalse(manager.getEveryTargetOnBoard().contains(player));
    }
}