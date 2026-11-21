package game;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;


import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.awt.Color;

public class CreatureTest {
    private CreatureAi creatureai;
    private World world;
    private Creature creature;

    @BeforeEach
    void setUp() {
        Tile[][][] tiles = new Tile[3][3][1];

        for (int x = 0; x < 3; x++) {
            for (int y = 0; y < 3; y++) {
                tiles[x][y][0] = Tile.FLOOR;
            }
        }

        world = new World(tiles);

        creature = new Creature(world, '@', Color.GRAY, "josh", 100, 10, 5);
        creatureai = new CreatureAi(creature);
        creature.setCreatureAi(creatureai);
        creature.x = 1;
        creature.y = 1;
        creature.z = 0;
    }

    @AfterEach 
    void tearDown(){
        world=null;
        creature=null;
        creatureai=null;
    }
    @ParameterizedTest 
    @CsvSource ({
        "-100,0, true",
        "-99,1, false",
        "-101,-1, true"
    })
    void testCreatureDiesAt0(int amount, int expectedHp, boolean expectedIsDead) {
        creature.modifyHp(amount, "remove HP");
        assertEquals(creature.hp(), expectedHp);
        if(expectedIsDead){
            assertEquals(creature.name()+" corpse", world.item(1,1,0).name());;
        }else{
            assertNull(world.item(1,1,0));
        }
        
    }
    @ParameterizedTest 
    @CsvSource ({
        "9,99",
        "10,100",
        "11,100"
    })
    void testMaxHPCaps(int amount, int expectedHp) {
        creature.modifyHp(-10, "null");
        creature.modifyHp(amount, "increase");
        assertEquals(creature.hp(), expectedHp);
    }

    @Test
    void creatureDiesWhenHpFallsBelowOne() {
        creature.modifyHp(-100, "Fatal damage");

        assertTrue(creature.hp() < 1);
        assertEquals("Fatal damage", creature.causeOfDeath());
        assertNull(world.creature(1,1,0));
        assertTrue(world.item(1,1,0).name().equals(creature.name()+" corpse"));
        assertTrue(world.item(1,1,0).glyph()=='%');
        assertTrue(world.item(1,1,0).color()==creature.color());
    }

    @Test
    void creatureHandlesItsOwnDeath() {
        world.add(creature);
        creature.modifyHp(-100, "Fatal damage");
        int x = creature.x;
        int y = creature.y;
        int z = creature.z;
        assertTrue(creature.hp() < 1);
        assertEquals("Fatal damage", creature.causeOfDeath());
        assertNull(world.creature(x, y, z));
        assertNotSame(creature, world.creature(x, y, z));
    }

}
