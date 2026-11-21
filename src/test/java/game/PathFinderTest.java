package game;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Color;
import java.util.ArrayList;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PathFinderTest {
    private Tile[][][] tiles;
    private World world;
    private Creature creature;
    private PathFinder finder;

    @BeforeEach
    void setUp() {
        tiles = new Tile[5][5][1];

        for (int x = 0; x < 5; x++) {
            for (int y = 0; y < 5; y++) {
                tiles[x][y][0] = Tile.FLOOR;
            }
        }

        world = new World(tiles);
        creature = new Creature(world, '@', Color.WHITE, "josh",
                100, 10, 5);
        creature.setCreatureAi(new CreatureAi(creature));
        creature.x = 1;
        creature.y = 1;
        creature.z = 0;
        world.add(creature);

        finder = new PathFinder();
    }

    @Test
    void returnsEmptyPathWhenStartEqualsEnd() {
        Point start = new Point(creature.x, creature.y, creature.z);

        ArrayList<Point> path =
                finder.findPath(creature, start, start, 300);

        assertNotNull(path);
        assertTrue(path.isEmpty());
    }

    @Test
    void returnsDestinationForAdjacentPoint() {
        Point start = new Point(1, 1, 0);
        Point end = new Point(2, 1, 0);

        ArrayList<Point> path =
                finder.findPath(creature, start, end, 300);

        assertNotNull(path);
        assertEquals(1, path.size());
        assertEquals(end, path.get(path.size() - 1));
    }

    @Test
    void findsRouteAcrossOpenWorld() {
        Point start = new Point(1, 1, 0);
        Point end = new Point(3, 3, 0);

        ArrayList<Point> path =
                finder.findPath(creature, start, end, 300);

        assertNotNull(path);
        assertEquals(end, path.get(path.size() - 1));

        Point previous = start;
        for (Point point : path) {
            assertTrue(world.tile(point.x, point.y, point.z).isGround());
            assertEquals(start.z, point.z);
            assertTrue(Math.max(
                    Math.abs(previous.x - point.x),
                    Math.abs(previous.y - point.y)) <= 1);
            previous = point;
        }
    }

    @Test
    void findsRouteAroundWall() {
        tiles[2][1][0] = Tile.WALL;
        Point start = new Point(1, 1, 0);
        Point end = new Point(3, 1, 0);

        ArrayList<Point> path =
                finder.findPath(creature, start, end, 300);

        assertNotNull(path);
        assertEquals(end, path.get(path.size() - 1));

        for (Point point : path) {
            assertNotEquals(Tile.WALL,
                    world.tile(point.x, point.y, point.z));
        }
    }

    @Test
    void allowsOccupiedCreatureAsDestination() {
        Creature target = new Creature(
                world, 'g', Color.RED, "goblin", 20, 4, 2);
        target.setCreatureAi(new CreatureAi(target));
        target.x = 2;
        target.y = 2;
        target.z = 0;
        world.add(target);

        Point start = new Point(creature.x, creature.y, creature.z);
        Point end = new Point(target.x, target.y, target.z);

        ArrayList<Point> path =
                finder.findPath(creature, start, end, 300);

        assertNotNull(path);
        assertEquals(end, path.get(path.size() - 1));
    }

    @Test
    void returnsNullWhenDestinationIsUnreachable() {
        for (int x = 0; x < 5; x++) {
            for (int y = 0; y < 5; y++) {
                tiles[x][y][0] = Tile.WALL;
            }
        }

        tiles[1][1][0] = Tile.FLOOR;
        tiles[3][3][0] = Tile.FLOOR;

        ArrayList<Point> path = finder.findPath(
                creature,
                new Point(1, 1, 0),
                new Point(3, 3, 0),
                300);

        assertNull(path);
    }

    @Test
    void returnsNullWhenSearchLimitIsZero() {
        ArrayList<Point> path = finder.findPath(
                creature,
                new Point(1, 1, 0),
                new Point(3, 3, 0),
                0);

        assertNull(path);
    }

    @Test
    void canReuseFinderAcrossSearches() {
        Point firstEnd = new Point(2, 2, 0);
        Point secondEnd = new Point(4, 4, 0);

        ArrayList<Point> first =
                finder.findPath(creature,
                        new Point(1, 1, 0), firstEnd, 300);
        ArrayList<Point> second =
                finder.findPath(creature,
                        new Point(1, 1, 0), secondEnd, 300);

        assertNotNull(first);
        assertNotNull(second);
        assertEquals(firstEnd, first.get(first.size() - 1));
        assertEquals(secondEnd, second.get(second.size() - 1));
    }
}
