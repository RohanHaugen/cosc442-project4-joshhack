package game;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
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
    private Tile[][][] tiles;

    @BeforeEach
    void setUp() {
        tiles = new Tile[3][3][1];

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

    @Test
    void initializesCreatureState() {
        assertEquals(100, creature.maxHp());
        assertEquals(100, creature.hp());
        assertEquals(creature.maxMana(), creature.mana());
        assertEquals(666, creature.food());
        assertEquals(1, creature.level());
        assertEquals(0, creature.xp());
        assertEquals(10, creature.attackValue());
        assertEquals(5, creature.defenseValue());
        assertEquals(9, creature.visionRadius());
        assertNotNull(creature.inventory());
    }

    @Test
    void manaChangesRespectMinimumAndMaximum() {
        creature.modifyMana(-100);
        assertEquals(0, creature.mana());

        creature.modifyMana(100);
        assertEquals(5, creature.mana());

        creature.modifyMana(2);
        assertEquals(5, creature.mana());
    }

    @Test
    void statModifiersUpdateCreatureValues() {
        creature.modifyMaxHp(25);
        creature.modifyVisionRadius(3);
        creature.modifyAttackValue(4);
        creature.modifyDefenseValue(2);

        assertEquals(125, creature.maxHp());
        assertEquals(12, creature.visionRadius());
        assertEquals(14, creature.attackValue());
        assertEquals(7, creature.defenseValue());
    }

    @Test
    void healingCapsAtMaximumHp() {
        creature.modifyHp(-40, "damage");
        creature.modifyHp(100, "healing");

        assertEquals(creature.maxHp(), creature.hp());
    }

    @Test
    void equipmentChangesDerivedAttackAndDefense() {
        Item sword = new Item(')', Color.WHITE, "sword", null);
        sword.modifyAttackValue(6);
        Item armor = new Item('[', Color.WHITE, "armor", null);
        armor.modifyDefenseValue(4);

        creature.inventory().add(sword);
        creature.inventory().add(armor);

        creature.equip(sword);
        assertSame(sword, creature.weapon());
        assertEquals(16, creature.attackValue());

        creature.equip(armor);
        assertSame(armor, creature.armor());
        assertEquals(9, creature.defenseValue());

        creature.unequip(sword);
        assertNull(creature.weapon());

        creature.unequip(armor);
        assertNull(creature.armor());
    }

    @Test
    void positiveXpCanAdvanceLevel() {
        world.add(creature);

        creature.modifyXp(26);

        assertEquals(26, creature.xp());
        assertEquals(2, creature.level());
    }

    @Test
    void xpBelowLevelThresholdDoesNotAdvanceLevel() {
        creature.modifyXp(25);

        assertEquals(25, creature.xp());
        assertEquals(1, creature.level());
    }

    @Test
    void movingOntoFloorMovesCreatureAndConsumesFood() {
        int startingFood = creature.food();

        creature.moveBy(1, 0, 0);

        assertEquals(2, creature.x);
        assertEquals(1, creature.y);
        assertEquals(startingFood - 1, creature.food());
    }

    @Test
    void movingIntoWallDoesNotMoveCreature() {
        tiles[2][1][0] = Tile.WALL;

        creature.moveBy(1, 0, 0);

        assertEquals(1, creature.x);
        assertEquals(1, creature.y);
    }

    @Test
    void invalidUpAndDownStairMovementDoesNotMoveCreature() {
        Tile[][][] multiLevelTiles = new Tile[3][3][2];
        for (int x = 0; x < 3; x++) {
            for (int y = 0; y < 3; y++) {
                for (int z = 0; z < 2; z++) {
                    multiLevelTiles[x][y][z] = Tile.FLOOR;
                }
            }
        }
        World multiLevelWorld = new World(multiLevelTiles);
        Creature stairCreature = new Creature(
                multiLevelWorld, '@', Color.GRAY, "josh", 100, 10, 5);
        stairCreature.setCreatureAi(new CreatureAi(stairCreature));
        stairCreature.x = 1;
        stairCreature.y = 1;
        stairCreature.z = 1;

        stairCreature.moveBy(0, 0, -1);

        assertEquals(1, stairCreature.z);
    }

    @Test
    void pickupAddsGroundItemToInventoryAndRemovesItFromWorld() {
        Item item = new Item('*', Color.WHITE, "teddy bear", null);
        world.addAtEmptySpace(item, 1, 1, 0);

        creature.pickup();

        assertSame(item, creature.inventory().get(0));
        assertNull(world.item(1, 1, 0));
    }

    @Test
    void droppingItemMovesItFromInventoryToWorld() {
        Item item = new Item('*', Color.WHITE, "teddy bear", null);
        creature.inventory().add(item);

        creature.drop(item);

        assertSame(item, world.item(1, 1, 0));
        assertNull(creature.inventory().get(0));
    }

    @Test
    void eatingFoodModifiesFoodAndRemovesItem() {
        Item item = new Item('%', Color.WHITE, "ration", null);
        item.modifyFoodValue(100);
        creature.inventory().add(item);
        int startingFood = creature.food();

        creature.eat(item);

        assertEquals(startingFood + 100, creature.food());
        assertFalse(creature.inventory().contains(item));
    }

    @Test
    void playerStarvationKillsCreature() {
        creature.modifyFood(-666);

        assertTrue(creature.hp() < 1);
        assertTrue(creature.causeOfDeath().contains("Starved"));
    }

    @Test
    void effectsAreUpdatedAndRemovedWhenFinished() {
        Effect effect = new Effect(1);
        creature.effects().add(effect);

        creature.update();

        assertTrue(creature.effects().isEmpty());
    }

    @Test
    void castingSpellWithoutEnoughManaDoesNotConsumeManaOrApplyEffect() {
        Effect effect = new Effect(3);
        Spell spell = new Spell("fire", 10, effect);
        Creature target = new Creature(
                world, 'g', Color.RED, "goblin", 20, 4, 2);
        target.setCreatureAi(new CreatureAi(target));
        target.x = 1;
        target.y = 2;
        target.z = 0;

        creature.castSpell(spell, target.x, target.y);

        assertEquals(5, creature.mana());
        assertTrue(target.effects().isEmpty());
    }

}
