package game;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Random;

import org.junit.jupiter.api.Test;

class WorldBuilderTest {
    @Test
    void buildCreatesWorldWithRequestedDimensions() {
        World world = new WorldBuilder(20, 20, 2).build();

        assertEquals(20, world.width());
        assertEquals(20, world.height());
        assertEquals(2, world.depth());
    }

    @Test
    void makeCavesCreatesGroundAndExactlyOneExit() {
        WorldBuilder builder = new WorldBuilder(30, 30, 3, new Random(42));
        World world = builder.makeCaves().build();

        int groundCount = 0;
        int exitCount = 0;

        for (int x = 0; x < world.width(); x++) {
            for (int y = 0; y < world.height(); y++) {
                Tile tile = world.tile(x, y, 0);
                if (tile.isGround()) {
                    groundCount++;
                }
                if (tile == Tile.STAIRS_UP) {
                    exitCount++;
                }
            }
        }

        assertTrue(groundCount > 0);
        assertEquals(1, exitCount);
    }

    @Test
    void depthOneCaveBuildsWithAnExitStair() {
        World world = new WorldBuilder(20, 20, 1, new Random(7)).makeCaves().build();

        int exitCount = 0;
        for (int x = 0; x < world.width(); x++) {
            for (int y = 0; y < world.height(); y++) {
                if (world.tile(x, y, 0) == Tile.STAIRS_UP) {
                    exitCount++;
                }
            }
        }

        assertEquals(1, exitCount);
    }

    @Test
    void downStairHasMatchingUpStairOnNextLevel() {
        World world = new WorldBuilder(30, 30, 3, new Random(1234)).makeCaves().build();

        for (int z = 0; z < world.depth() - 1; z++) {
            for (int x = 0; x < world.width(); x++) {
                for (int y = 0; y < world.height(); y++) {
                    if (world.tile(x, y, z) == Tile.STAIRS_DOWN) {
                        assertEquals(Tile.STAIRS_UP, world.tile(x, y, z + 1));
                    }
                }
            }
        }
    }

    @Test
    void everyGeneratedTileIsValid() {
        World world = new WorldBuilder(20, 20, 2, new Random(99)).makeCaves().build();

        for (int x = 0; x < world.width(); x++) {
            for (int y = 0; y < world.height(); y++) {
                for (int z = 0; z < world.depth(); z++) {
                    assertTrue(world.tile(x, y, z) == Tile.FLOOR
                            || world.tile(x, y, z) == Tile.WALL
                            || world.tile(x, y, z) == Tile.STAIRS_DOWN
                            || world.tile(x, y, z) == Tile.STAIRS_UP);
                }
            }
        }
    }
}
