package game;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

public class CombatRulesTest {
    CombatRules combatRules;
    @BeforeEach
    void setUp() {
        combatRules = new CombatRules();
    }
    @AfterEach 
    void tearDown() {
        combatRules=null;
    }
    @ParameterizedTest 
    @CsvSource ({
        "10,-9,1, false",
        "10,-10,0, true",
        "10,-11,-1, true"
    })
    void testIsDeadEvaluatesExactly(int currentHp, int amount, int expectedHp, boolean expectedIsDead) {
        int actualHp = combatRules.calculateNewHp(currentHp, 100, amount);
        assertEquals(actualHp, expectedHp);
        assertEquals(combatRules.isDead(actualHp), expectedIsDead);
    }
    @ParameterizedTest 
    @CsvSource ({
        "90,10,100",
        "90,9,99",
        "90,11,100"
    })
    void testExpectedHpCapsAt100(int currentHp, int amount, int expectedHp) {
        int actualHp=combatRules.calculateNewHp(currentHp, 100, amount);
        assertEquals(actualHp, expectedHp);
        assertFalse(combatRules.isDead(actualHp));
    }
}
