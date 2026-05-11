package logic;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MoneyManagerTest {

    private MoneyManager manager;

    @BeforeEach
    void setUp() {
        manager = MoneyManager.getInstance();
        manager.reset();
    }

    // ─── Initial state ────────────────────────────────────────────────────────

    @Test
    void initialState_currentMoneyIsZero() {
        assertEquals(0, manager.getCurrentMoney());
    }

    @Test
    void initialState_maxMoneyIs225() {
        // reset() → previousMoney=150 → maxMoney=(int)(150*1.5)=225
        assertEquals(225, manager.getMaxMoney());
    }

    @Test
    void initialState_moneyLevelIsOne() {
        assertEquals(1, manager.getMoneyLevel());
    }

    @Test
    void initialState_upgradeCostIs135() {
        // upgradeCost = (int)(0.6 * 225) = 135
        assertEquals(135, manager.getUpgradeCost());
    }

    @Test
    void initialState_notMaxLevel() {
        assertFalse(manager.isMaxLevel());
    }

    // ─── update() ─────────────────────────────────────────────────────────────

    @Test
    void update_incrementsCurrentMoney() {
        // generationRate = 225/600 = 0.375; getCurrentMoney() casts to int,
        // so we need ≥ 3 frames for the integer value to exceed 0.
        for (int i = 0; i < 3; i++) manager.update();
        assertTrue(manager.getCurrentMoney() > 0);
    }

    @Test
    void update_moneyNeverExceedsMax() {
        for (int i = 0; i < 10_000; i++) manager.update();
        assertTrue(manager.getCurrentMoney() <= manager.getMaxMoney());
    }

    @Test
    void update_moneyCapsAtMax() {
        for (int i = 0; i < 10_000; i++) manager.update();
        assertEquals(manager.getMaxMoney(), manager.getCurrentMoney());
    }

    @Test
    void update_after600Frames_reachesMaxMoney() {
        // generationRate = maxMoney / 600 → 600 frames fills the wallet exactly
        for (int i = 0; i < 600; i++) manager.update();
        assertEquals(manager.getMaxMoney(), manager.getCurrentMoney());
    }

    @Test
    void update_whenAtMax_doesNotExceedMax() {
        for (int i = 0; i < 10_000; i++) manager.update();
        int before = manager.getCurrentMoney();
        manager.update();
        assertEquals(before, manager.getCurrentMoney());
    }

    // ─── spend() ──────────────────────────────────────────────────────────────

    @Test
    void spend_withSufficientFunds_returnsTrue() {
        for (int i = 0; i < 10_000; i++) manager.update();
        assertTrue(manager.spend(100));
    }

    @Test
    void spend_withSufficientFunds_deductsMoney() {
        for (int i = 0; i < 10_000; i++) manager.update();
        int before = manager.getCurrentMoney();
        manager.spend(100);
        assertEquals(before - 100, manager.getCurrentMoney());
    }

    @Test
    void spend_exactAvailableAmount_returnsTrue() {
        for (int i = 0; i < 10_000; i++) manager.update();
        int max = manager.getMaxMoney();
        assertTrue(manager.spend(max));
    }

    @Test
    void spend_exactAvailableAmount_leavesZero() {
        for (int i = 0; i < 10_000; i++) manager.update();
        int max = manager.getMaxMoney();
        manager.spend(max);
        assertEquals(0, manager.getCurrentMoney());
    }

    @Test
    void spend_withInsufficientFunds_returnsFalse() {
        // currentMoney starts at 0
        assertFalse(manager.spend(100));
    }

    @Test
    void spend_withInsufficientFunds_moneyUnchanged() {
        manager.spend(100);
        assertEquals(0, manager.getCurrentMoney());
    }

    @Test
    void spend_oneMoreThanAvailable_returnsFalse() {
        for (int i = 0; i < 10_000; i++) manager.update();
        int max = manager.getMaxMoney();
        assertFalse(manager.spend(max + 1));
    }

    // ─── upgradeWallet() ──────────────────────────────────────────────────────

    @Test
    void upgradeWallet_withSufficientFunds_returnsTrue() {
        for (int i = 0; i < 10_000; i++) manager.update();
        assertTrue(manager.upgradeWallet());
    }

    @Test
    void upgradeWallet_withSufficientFunds_incrementsLevel() {
        for (int i = 0; i < 10_000; i++) manager.update();
        manager.upgradeWallet();
        assertEquals(2, manager.getMoneyLevel());
    }

    @Test
    void upgradeWallet_withSufficientFunds_deductsCost() {
        for (int i = 0; i < 10_000; i++) manager.update();
        int cost = manager.getUpgradeCost();
        int before = manager.getCurrentMoney();
        manager.upgradeWallet();
        assertEquals(before - cost, manager.getCurrentMoney());
    }

    @Test
    void upgradeWallet_withSufficientFunds_increasesMaxMoney() {
        int oldMax = manager.getMaxMoney();
        for (int i = 0; i < 10_000; i++) manager.update();
        manager.upgradeWallet();
        assertTrue(manager.getMaxMoney() > oldMax);
    }

    @Test
    void upgradeWallet_level2_maxMoneyIs337() {
        // level1 maxMoney=225 → level2 maxMoney=(int)(225*1.5)=337
        for (int i = 0; i < 10_000; i++) manager.update();
        manager.upgradeWallet();
        assertEquals(337, manager.getMaxMoney());
    }

    @Test
    void upgradeWallet_level2_upgradeCostIs202() {
        // (int)(0.6 * 337) = 202
        for (int i = 0; i < 10_000; i++) manager.update();
        manager.upgradeWallet();
        assertEquals(202, manager.getUpgradeCost());
    }

    @Test
    void upgradeWallet_withInsufficientFunds_returnsFalse() {
        assertFalse(manager.upgradeWallet());
    }

    @Test
    void upgradeWallet_withInsufficientFunds_levelUnchanged() {
        manager.upgradeWallet();
        assertEquals(1, manager.getMoneyLevel());
    }

    @Test
    void upgradeWallet_atMaxLevel8_returnsFalse() {
        for (int upgrade = 0; upgrade < 7; upgrade++) {
            for (int i = 0; i < 10_000; i++) manager.update();
            manager.upgradeWallet();
        }
        assertEquals(8, manager.getMoneyLevel());
        for (int i = 0; i < 10_000; i++) manager.update();
        assertFalse(manager.upgradeWallet());
    }

    @Test
    void upgradeWallet_atMaxLevel8_levelUnchanged() {
        for (int upgrade = 0; upgrade < 7; upgrade++) {
            for (int i = 0; i < 10_000; i++) manager.update();
            manager.upgradeWallet();
        }
        for (int i = 0; i < 10_000; i++) manager.update();
        manager.upgradeWallet();
        assertEquals(8, manager.getMoneyLevel());
    }

    // ─── isMaxLevel() ─────────────────────────────────────────────────────────

    @Test
    void isMaxLevel_atLevel1_returnsFalse() {
        assertFalse(manager.isMaxLevel());
    }

    @Test
    void isMaxLevel_atLevel8_returnsTrue() {
        for (int upgrade = 0; upgrade < 7; upgrade++) {
            for (int i = 0; i < 10_000; i++) manager.update();
            manager.upgradeWallet();
        }
        assertTrue(manager.isMaxLevel());
    }

    // ─── reset() ──────────────────────────────────────────────────────────────

    @Test
    void reset_restoresCurrentMoneyToZero() {
        for (int i = 0; i < 10_000; i++) manager.update();
        manager.reset();
        assertEquals(0, manager.getCurrentMoney());
    }

    @Test
    void reset_restoresLevelToOne() {
        for (int i = 0; i < 10_000; i++) manager.update();
        manager.upgradeWallet();
        manager.reset();
        assertEquals(1, manager.getMoneyLevel());
    }

    @Test
    void reset_restoresMaxMoneyTo225() {
        for (int i = 0; i < 10_000; i++) manager.update();
        manager.upgradeWallet();
        manager.reset();
        assertEquals(225, manager.getMaxMoney());
    }

    @Test
    void reset_restoresUpgradeCostTo135() {
        for (int i = 0; i < 10_000; i++) manager.update();
        manager.upgradeWallet();
        manager.reset();
        assertEquals(135, manager.getUpgradeCost());
    }

    @Test
    void reset_notMaxLevelAfterReset() {
        for (int upgrade = 0; upgrade < 7; upgrade++) {
            for (int i = 0; i < 10_000; i++) manager.update();
            manager.upgradeWallet();
        }
        manager.reset();
        assertFalse(manager.isMaxLevel());
    }
}