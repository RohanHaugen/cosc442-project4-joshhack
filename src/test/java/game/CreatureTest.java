package game;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;


import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.awt.Color;

public class CreatureTest {
    private static final int MAX_HP = 20;
    private static final int ATTACK = 5;
    private static final int DEFENSE = 3;
    private Creature creature;
    private World world;
    private CreatureAi creatureai;
    private Tile[][][]tiles;
    @BeforeEach 
    void setUp() {
        tiles= new Tile[2][2][2];
        tiles[0][0][0]=Tile.FLOOR;
        world = new World(tiles);
        creature = new Creature(world, '0', Color.BLACK, "blue", MAX_HP, ATTACK, DEFENSE);
        creatureai = new CreatureAi(creature);
        creature.x=0;
        creature.y=0;
        creature.z=0;
        world.add(creature);
        creature.setCreatureAi(creatureai);
        
    }
    @AfterEach 
    void tearDown(){
        world=null;
        creature=null;
        tiles=null;
        creatureai=null;
    }
    @Test
    void positiveAmountIncreasesHp() {
        creature.modifyHp(-10, "damage");
        creature.modifyHp(5, "healing");

        assertEquals(15, creature.hp());
    }

    @Test
    void hpCannotExceedMaxHp() {
        creature.modifyHp(100, "healing");

        assertEquals(MAX_HP, creature.hp());
    }

    @Test
    void negativeAmountReducesHp() {
        creature.modifyHp(-5, "damage");

        assertEquals(15, creature.hp());
    }

    @Test
    void hpOfOneIsStillAlive() {
        creature.modifyHp(-19, "damage");

        assertEquals(1, creature.hp());
    }


    @Test
    void creatureDiesWhenHpFallsBelowOne() {
        creature.modifyHp(-20, "Fatal damage");

        assertTrue(creature.hp() < 1);
        assertEquals("Fatal damage", creature.causeOfDeath());
        assertNull(world.creature(0,0,0));
        System.out.println(world.item(0,0,0).name());
        assertTrue(world.item(0,0,0).name().equals(creature.name()+" corpse"));
        assertTrue(world.item(0,0,0).glyph()=='%');
        assertTrue(world.item(0,0,0).color()==creature.color());
    }

    @Test
    void creatureHandlesItsOwnDeath() {
    creature.modifyHp(-20, "Fatal damage");
    int x = creature.x;
    int y = creature.y;
    int z = creature.z;
    assertTrue(creature.hp() < 1);
    assertEquals("Fatal damage", creature.causeOfDeath());
    assertNotSame(creature, world.creature(x, y, z));
    }

}
