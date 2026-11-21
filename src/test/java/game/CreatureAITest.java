package game;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CreatureAITest {
    private Tile[][][] tiles;
    private World world;
    private Creature creature;
    private CreatureAi ai;

    @BeforeEach
    void setUp() {
        tiles = new Tile[7][7][2];
        for (int x = 0; x < tiles.length; x++) {
            for (int y = 0; y < tiles[0].length; y++) {
                for (int z = 0; z < tiles[0][0].length; z++) {
                    tiles[x][y][z] = Tile.FLOOR;
                }
            }
        }

        world = new World(tiles);
        creature = new Creature(world, 'g', Color.RED, "goblin", 20, 5, 2);
        ai = new CreatureAi(creature);
        creature.x = 3;
        creature.y = 3;
        creature.z = 0;
        world.add(creature);
    }

    @Test
    void nameUsesAppearanceUntilItIsLearned() {
        Item item = new Item('*', Color.WHITE, "teddy bear", "a toy");

        assertEquals("a toy", ai.getName(item));

        ai.setName(item, "teddy bear");

        assertEquals("teddy bear", ai.getName(item));
    }

    @Test
    void onEnterMovesOntoGroundAndReportsWall() {
        ai.onEnter(4, 3, 0, Tile.FLOOR);

        assertEquals(4, creature.x);
        assertEquals(3, creature.y);

        ai.onEnter(5, 3, 0, Tile.WALL);

        assertEquals(4, creature.x);
        assertEquals(3, creature.y);
    }

    @Test
    void onEnterWallReportsActionThroughCreatureAiNotification() {
        List<String> messages = new ArrayList<String>();
        CreatureAi recordingAi = new CreatureAi(creature) {
            @Override
            public void onNotify(String message) {
                messages.add(message);
            }
        };

        recordingAi.onEnter(5, 3, 0, Tile.WALL);

        assertTrue(messages.size() > 0);
        assertTrue(messages.get(0).contains("bump into a wall"));
    }

    @Test
    void rememberedTileIsUnknown() {
        assertEquals(Tile.UNKNOWN, ai.rememberedTile(0, 0, 0));
    }

    @Test
    void canSeeRespectsDepthDistanceAndLineOfSight() {
        assertTrue(ai.canSee(4, 3, 0));
        assertFalse(ai.canSee(4, 3, 1));
        assertFalse(ai.canSee(3, 13, 0));

        tiles[4][3][0] = Tile.WALL;
        assertFalse(ai.canSee(5, 3, 0));
    }

    @Test
    void canSeeIncludesExactVisionRadiusBoundary() {
        creature.modifyVisionRadius(-6);

        assertTrue(ai.canSee(6, 3, 0));
        assertFalse(ai.canSee(7, 3, 0));
    }

    @Test
    void getWeaponToThrowReturnsStrongestNonEquippedWeapon() {
        Item weak = new Item(')', Color.WHITE, "weak", null);
        weak.modifyThrownAttackValue(2);
        Item strong = new Item(')', Color.WHITE, "strong", null);
        strong.modifyThrownAttackValue(5);
        creature.inventory().add(weak);
        creature.inventory().add(strong);

        assertSame(strong, ai.getWeaponToThrow());
    }

    @Test
    void equippedWeaponAndArmorAreNotThrown() {
        Item weapon = new Item(')', Color.WHITE, "dagger", null);
        weapon.modifyAttackValue(5);
        Item armor = new Item('[', Color.WHITE, "armor", null);
        armor.modifyDefenseValue(5);
        creature.inventory().add(weapon);
        creature.inventory().add(armor);
        creature.equip(weapon);
        creature.equip(armor);

        assertNull(ai.getWeaponToThrow());
    }

    @Test
    void rangedAttackRequirementRequiresRangedWeaponAndVisibility() {
        Creature player = new Creature(world, '@', Color.WHITE, "player", 20, 5, 2);
        player.setCreatureAi(new CreatureAi(player));
        player.x = 4;
        player.y = 3;
        player.z = 0;
        world.add(player);

        assertFalse(ai.canRangedWeaponAttack(player));

        Item bow = new Item(')', Color.WHITE, "bow", null);
        bow.modifyRangedAttackValue(4);
        creature.inventory().add(bow);
        creature.equip(bow);

        assertTrue(ai.canRangedWeaponAttack(player));
    }

    @Test
    void throwRequirementRequiresVisibleCreatureAndThrowWeapon() {
        Creature player = new Creature(world, '@', Color.WHITE, "player", 20, 5, 2);
        player.setCreatureAi(new CreatureAi(player));
        player.x = 4;
        player.y = 3;
        player.z = 0;
        world.add(player);

        assertFalse(ai.canThrowAt(player));

        Item dagger = new Item(')', Color.WHITE, "dagger", null);
        dagger.modifyThrownAttackValue(3);
        creature.inventory().add(dagger);

        assertTrue(ai.canThrowAt(player));
    }

    @Test
    void pickupRequirementRequiresGroundItemAndAvailableCapacity() {
        assertFalse(ai.canPickup());

        Item item = new Item('*', Color.WHITE, "item", null);
        world.addAtEmptySpace(item, 3, 3, 0);

        assertTrue(ai.canPickup());
    }

    @Test
    void betterEquipmentDetectsWeaponsAndArmor() {
        assertFalse(ai.canUseBetterEquipment());

        Item sword = new Item(')', Color.WHITE, "sword", null);
        sword.modifyAttackValue(8);
        creature.inventory().add(sword);

        assertTrue(ai.canUseBetterEquipment());

        Item armor = new Item('[', Color.WHITE, "armor", null);
        armor.modifyDefenseValue(7);
        creature.inventory().add(armor);

        assertTrue(ai.canUseBetterEquipment());
    }

    @Test
    void equalRatedEquipmentIsNotConsideredBetter() {
        Item sword = new Item(')', Color.WHITE, "sword", null);
        sword.modifyAttackValue(5);
        creature.inventory().add(sword);
        creature.equip(sword);

        Item equalSword = new Item(')', Color.WHITE, "equal sword", null);
        equalSword.modifyAttackValue(5);
        creature.inventory().add(equalSword);

        assertFalse(ai.canUseBetterEquipment());

        Item armor = new Item('[', Color.WHITE, "armor", null);
        armor.modifyDefenseValue(5);
        creature.inventory().add(armor);
        creature.equip(armor);

        Item equalArmor = new Item('[', Color.WHITE, "equal armor", null);
        equalArmor.modifyDefenseValue(5);
        creature.inventory().add(equalArmor);

        assertFalse(ai.canUseBetterEquipment());
    }

    @Test
    void balancedItemIsNotClassifiedAsArmor() {
        Item strongWeapon = new Item(')', Color.WHITE, "strong weapon", null);
        strongWeapon.modifyAttackValue(10);
        creature.inventory().add(strongWeapon);
        creature.equip(strongWeapon);

        Item balanced = new Item('=', Color.WHITE, "balanced", null);
        balanced.modifyAttackValue(5);
        balanced.modifyDefenseValue(5);
        creature.inventory().add(balanced);

        assertFalse(ai.canUseBetterEquipment());
    }

    @Test
    void useBetterEquipmentEquipsWeaponAndArmor() {
        Item sword = new Item(')', Color.WHITE, "sword", null);
        sword.modifyAttackValue(8);
        Item armor = new Item('[', Color.WHITE, "armor", null);
        armor.modifyDefenseValue(7);
        creature.inventory().add(sword);
        creature.inventory().add(armor);

        ai.useBetterEquipment();

        assertSame(sword, creature.weapon());
        assertSame(armor, creature.armor());
        assertEquals(13, creature.attackValue());
        assertEquals(9, creature.defenseValue());
    }

    @Test
    void useBetterEquipmentDoesNotReplaceWithEqualRatedItems() {
        Item weapon = new Item(')', Color.WHITE, "weapon", null);
        weapon.modifyAttackValue(5);
        creature.inventory().add(weapon);
        creature.equip(weapon);

        Item equalWeapon = new Item(')', Color.WHITE, "equal weapon", null);
        equalWeapon.modifyAttackValue(5);
        creature.inventory().add(equalWeapon);

        ai.useBetterEquipment();

        assertSame(weapon, creature.weapon());
    }

    @Test
    void huntMovesAlongValidPathTowardTarget() {
        Creature target = new Creature(world, 'g', Color.RED, "enemy", 20, 4, 2);
        target.setCreatureAi(new CreatureAi(target));
        target.x = 4;
        target.y = 3;
        target.z = 0;
        world.add(target);

        ai.hunt(target);

        assertTrue(creature.x == 3 || creature.x == 4);
        assertEquals(3, creature.y);
    }

    @Test
    void wanderMovesOnlyAcrossGroundOrReturnsWithoutMoving() {
        creature.x = 0;
        creature.y = 0;
        creature.z = 0;

        for (int x = -1; x <= 1; x++) {
            for (int y = -1; y <= 1; y++) {
                int wx = creature.x + x;
                int wy = creature.y + y;
                if (wx >= 0 && wy >= 0) {
                    if (wx != 0 || wy != 0) {
                        tiles[wx][wy][0] = Tile.WALL;
                    }
                }
            }
        }

        ai.wander();

        assertEquals(0, creature.x);
        assertEquals(0, creature.y);
    }

    @Test
    void playerAiNotifiesMessagesAndUsesFieldOfView() {
        List<String> messages = new ArrayList<String>();
        FieldOfView fov = new FieldOfView(world);
        fov.update(3, 3, 0, 2);
        PlayerAi playerAi = new PlayerAi(creature, messages, fov);

        playerAi.onNotify("hello");
        playerAi.onEnter(4, 3, 0, Tile.FLOOR);

        assertEquals(List.of("hello"), messages);
        assertEquals(4, creature.x);
        assertEquals(3, creature.y);
        assertTrue(playerAi.canSee(3, 3, 0));
        assertEquals(fov.tile(3, 3, 0), playerAi.rememberedTile(3, 3, 0));
    }

    @Test
    void playerAiDigActionHandlesDiggableTile() {
        List<String> messages = new ArrayList<String>();
        FieldOfView fov = new FieldOfView(world);
        PlayerAi playerAi = new PlayerAi(creature, messages, fov);
        tiles[4][3][0] = Tile.WALL;

        playerAi.onEnter(4, 3, 0, Tile.WALL);

        assertEquals(3, creature.x);
        assertEquals(3, creature.y);
        assertEquals(Tile.FLOOR, world.tile(4, 3, 0));
    }

    @Test
    void batAiUpdateRunsWanderWithoutMovingBehindWalls() {
        creature.x = 0;
        creature.y = 0;
        creature.z = 0;
        for (int x = 0; x < 2; x++) {
            for (int y = 0; y < 2; y++) {
                if (x != 0 || y != 0) {
                    tiles[x][y][0] = Tile.WALL;
                }
            }
        }

        BatAi batAi = new BatAi(creature);
        batAi.onUpdate();

        assertEquals(0, creature.x);
        assertEquals(0, creature.y);
    }

    @Test
    void goblinPrioritizesBetterEquipment() {
        Creature player = new Creature(world, '@', Color.WHITE, "player", 20, 5, 2);
        player.setCreatureAi(new CreatureAi(player));
        player.x = 6;
        player.y = 6;
        player.z = 0;
        world.add(player);

        Item sword = new Item(')', Color.WHITE, "sword", null);
        sword.modifyAttackValue(8);
        creature.inventory().add(sword);

        GoblinAi goblinAi = new GoblinAi(creature, player);
        goblinAi.onUpdate();

        assertSame(sword, creature.weapon());
    }

    @Test
    void zombieAiWithoutVisibleTargetWandersOrSkipsWithoutMovingIntoWall() {
        Creature player = new Creature(world, '@', Color.WHITE, "player", 20, 5, 2);
        player.setCreatureAi(new CreatureAi(player));
        player.x = 4;
        player.y = 3;
        player.z = 1;
        world.add(player);

        for (int x = 2; x <= 4; x++) {
            for (int y = 2; y <= 4; y++) {
                if (x != 3 || y != 3) {
                    tiles[x][y][0] = Tile.WALL;
                }
            }
        }

        ZombieAi zombieAi = new ZombieAi(creature, player);

        for (int i = 0; i < 20; i++) {
            zombieAi.onUpdate();
        }

        assertTrue(creature.x >= 3);
        assertEquals(3, creature.y);
    }

    @Test
    void fungusAiUpdateDoesNotCreateChildOnBlockedUpdate() {
        StuffFactory factory = new StuffFactory(world);
        FungusAi fungusAi = new FungusAi(creature, factory);

        for (int i = 0; i < 20; i++) {
            fungusAi.onUpdate();
        }

        assertEquals(3, creature.x);
        assertEquals(3, creature.y);
    }
}
