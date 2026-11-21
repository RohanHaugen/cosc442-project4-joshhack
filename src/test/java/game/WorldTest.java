package game;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Color;
import java.util.Random;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class WorldTest {
    private Tile[][][] tiles;
    private World world;

    @BeforeEach
    void setUp() {
        tiles = new Tile[4][4][2];
        for (int x = 0; x < 4; x++) {
            for (int y = 0; y < 4; y++) {
                for (int z = 0; z < 2; z++) {
                    tiles[x][y][z] = Tile.FLOOR;
                }
            }
        }
        world = new World(tiles, new Random(42));
    }

    @Test
    void reportsWorldDimensions() {
        assertEquals(4, world.width());
        assertEquals(4, world.height());
        assertEquals(2, world.depth());
    }

    @Test
    void reportsBoundsOutsideWorld() {
        assertEquals(Tile.BOUNDS, world.tile(-1, 0, 0));
        assertEquals(Tile.BOUNDS, world.tile(4, 0, 0));
        assertEquals(Tile.BOUNDS, world.tile(0, -1, 0));
        assertEquals(Tile.BOUNDS, world.tile(0, 4, 0));
        assertEquals(Tile.BOUNDS, world.tile(0, 0, -1));
        assertEquals(Tile.BOUNDS, world.tile(0, 0, 2));
    }

    @Test
    void digsOnlyDiggableTiles() {
        tiles[1][1][0] = Tile.WALL;

        world.dig(1, 1, 0);
        world.dig(2, 2, 0);

        assertEquals(Tile.FLOOR, world.tile(1, 1, 0));
        assertEquals(Tile.FLOOR, world.tile(2, 2, 0));
    }

    @Test
    void addsAndRemovesCreatures() {
        Creature creature = new Creature(world, '@', Color.WHITE, "hero", 10, 3, 2);

        world.add(creature);
        creature.x = 1;
        creature.y = 2;
        creature.z = 0;

        assertSame(creature, world.creature(1, 2, 0));

        world.remove(creature);

        assertNull(world.creature(1, 2, 0));
    }

    @Test
    void placesCreatureAtAnEmptyGroundLocation() {
        tiles[0][0][0] = Tile.WALL;
        Creature creature = new Creature(world, '@', Color.WHITE, "hero", 10, 3, 2);

        world.addAtEmptyLocation(creature, 0);

        assertTrue(world.tile(creature.x, creature.y, creature.z).isGround());
        assertSame(creature, world.creature(creature.x, creature.y, creature.z));
    }

    @Test
    void addsAndRemovesItems() {
        Item item = new Item('%', Color.RED, "corpse", null);

        assertTrue(world.addAtEmptySpace(item, 1, 1, 0));
        assertSame(item, world.item(1, 1, 0));

        world.remove(item);

        assertNull(world.item(1, 1, 0));
    }

    @Test
    void movesItemToNearestEmptySpaceWhenRequestedSpaceIsOccupied() {
        Item first = new Item('*', Color.WHITE, "first", null);
        Item second = new Item('#', Color.WHITE, "second", null);

        assertTrue(world.addAtEmptySpace(first, 1, 1, 0));
        assertTrue(world.addAtEmptySpace(second, 1, 1, 0));

        assertSame(first, world.item(1, 1, 0));
    }

    @Test
    void glyphPrioritizesCreatureThenItemThenTile() {
        Creature creature = new Creature(world, '@', Color.WHITE, "hero", 10, 3, 2);
        Item item = new Item('*', Color.WHITE, "item", null);

        creature.setCreatureAi(new CreatureAi(creature));
        world.add(creature);
        creature.x = 1;
        creature.y = 1;
        creature.z = 0;
        world.addAtEmptySpace(item, 1, 1, 0);

        assertEquals('@', world.glyph(1, 1, 0));
    }

    @Test
    void glyphUsesItemGlyphBeforeTileGlyph() {
        Item item = new Item('*', Color.WHITE, "item", null);

        world.addAtEmptySpace(item, 1, 1, 0);

        assertEquals('*', world.glyph(1, 1, 0));
    }
}
