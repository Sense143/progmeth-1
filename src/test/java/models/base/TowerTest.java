package models.base;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for Tower — a concrete Unit subclass with no attack/movement.
 * Tower's constructor only calls Unit's constructor which wraps image loading
 * in try-catch, so these tests run without a JavaFX toolkit.
 */
class TowerTest {

    // ─── Construction ─────────────────────────────────────────────────────────

    @Test
    void constructor_setsHp() {
        Tower tower = new Tower("TestTower", 100, 2000);
        assertEquals(2000, tower.getHp(), 0.001);
    }

    @Test
    void constructor_setsMaxHp() {
        Tower tower = new Tower("TestTower", 100, 2000);
        assertEquals(2000, tower.getMaxHp(), 0.001);
    }

    @Test
    void constructor_setsX() {
        Tower tower = new Tower("TestTower", 250, 1000);
        assertEquals(250, tower.getX(), 0.001);
    }

    @Test
    void constructor_speedIsZero() {
        Tower tower = new Tower("TestTower", 0, 1000);
        assertEquals(0, tower.getSpeed(), 0.001);
    }

    @Test
    void constructor_attackRangeIsZero() {
        Tower tower = new Tower("TestTower", 0, 1000);
        assertEquals(0, tower.getAttackRange(), 0.001);
    }

    // ─── takeDamage ───────────────────────────────────────────────────────────

    @Test
    void takeDamage_reducesHp() {
        Tower tower = new Tower("T", 0, 1000);
        tower.takeDamage(200);
        assertEquals(800, tower.getHp(), 0.001);
    }

    @Test
    void takeDamage_doesNotGoBelowZero() {
        Tower tower = new Tower("T", 0, 1000);
        tower.takeDamage(2000);
        assertEquals(0, tower.getHp(), 0.001);
    }

    @Test
    void takeDamage_exactKill_leavesZeroHp() {
        Tower tower = new Tower("T", 0, 500);
        tower.takeDamage(500);
        assertEquals(0, tower.getHp(), 0.001);
    }

    @Test
    void takeDamage_zeroDamage_hpUnchanged() {
        Tower tower = new Tower("T", 0, 500);
        tower.takeDamage(0);
        assertEquals(500, tower.getHp(), 0.001);
    }

    // ─── isDead ───────────────────────────────────────────────────────────────

    @Test
    void isDead_withFullHp_returnsFalse() {
        Tower tower = new Tower("T", 0, 1000);
        assertFalse(tower.isDead());
    }

    @Test
    void isDead_afterLethalDamage_returnsTrue() {
        Tower tower = new Tower("T", 0, 1000);
        tower.takeDamage(1000);
        assertTrue(tower.isDead());
    }

    @Test
    void isDead_afterOverkillDamage_returnsTrue() {
        Tower tower = new Tower("T", 0, 500);
        tower.takeDamage(10000);
        assertTrue(tower.isDead());
    }

    @Test
    void isDead_constructedWithZeroHp_returnsTrue() {
        Tower tower = new Tower("T", 0, 0);
        assertTrue(tower.isDead());
    }

    // ─── update / Attack / render size ────────────────────────────────────────

    @Test
    void update_doesNotChangeHp() {
        Tower tower = new Tower("T", 0, 1000);
        tower.update();
        assertEquals(1000, tower.getHp(), 0.001);
    }

    @Test
    void update_doesNotChangeX() {
        Tower tower = new Tower("T", 200, 1000);
        tower.update();
        assertEquals(200, tower.getX(), 0.001);
    }

    @Test
    void getRenderWidth_returnsZero() {
        Tower tower = new Tower("T", 0, 1000);
        assertEquals(0, tower.getRenderWidth(), 0.001);
    }

    @Test
    void getRenderHeight_returnsZero() {
        Tower tower = new Tower("T", 0, 1000);
        assertEquals(0, tower.getRenderHeight(), 0.001);
    }

    @Test
    void attackWithList_doesNotThrow() {
        Tower tower = new Tower("T", 0, 1000);
        assertDoesNotThrow(() -> tower.Attack(new java.util.ArrayList<>()));
    }

    @Test
    void attackWithUnit_doesNotThrow() {
        Tower tower = new Tower("T", 0, 1000);
        assertDoesNotThrow(() -> tower.Attack((Unit) null));
    }

    // ─── maxHp stays fixed after damage ───────────────────────────────────────

    @Test
    void maxHp_unchangedAfterDamage() {
        Tower tower = new Tower("T", 0, 1000);
        tower.takeDamage(400);
        assertEquals(1000, tower.getMaxHp(), 0.001);
    }

    // ─── multiple damage applications ─────────────────────────────────────────

    @Test
    void multipleDamageApplications_accumulateCorrectly() {
        Tower tower = new Tower("T", 0, 1000);
        tower.takeDamage(300);
        tower.takeDamage(300);
        assertEquals(400, tower.getHp(), 0.001);
    }

    @Test
    void damageBeyondHp_thenCheckIsDead() {
        Tower tower = new Tower("T", 0, 500);
        tower.takeDamage(200);
        assertFalse(tower.isDead());
        tower.takeDamage(300);
        assertTrue(tower.isDead());
    }
}