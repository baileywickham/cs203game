import java.util.*;

import edu.calpoly.spritely.Size;
import edu.calpoly.spritely.Tile;

/**
 * Data structures that hold the model of our virtual world.
 * It consists of a grid.  Each point on the grid is occupied
 * by a background tile, and, optionally, an Entity.
 */
final class WorldModel
{
    private final Size size;
    private final Tile background[][];
    private final Entity occupant[][];
    private final Set<Entity> entities;

    public WorldModel(Size gridSize)
    {
        this.size = gridSize;
        this.background = new Tile[gridSize.height][gridSize.width];
        this.occupant = new Entity[gridSize.height][gridSize.width];
        this.entities = new HashSet<Entity>();
    }
    public Size getSize() { return this.size;    }
    public Tile[][] getBackground() { return this.background;}
    public Entity[][] getOccupant() { return this.occupant;}
    public Set<Entity> getEntities() {return this.entities;}
    public void setBackground(int x, int y, Tile color) {this.background[x][y] = color;}

    public static void moveEntity(WorldModel world, Entity entity, Point pos)
    {
        Point oldPos = entity.getPosition();
        if (withinBounds(world, pos) && !pos.equals(oldPos))
        {
            setOccupantCell(world, oldPos, null);
            removeEntityAt(world, pos);
            setOccupantCell(world, pos, entity);
            entity.setPosition(pos);
        }
    }

    public static void removeEntity(WorldModel world, Entity entity)
    {
        removeEntityAt(world, entity.getPosition());
    }

    public static void removeEntityAt(WorldModel world, Point pos)
    {
        if (withinBounds(world, pos)
                && getOccupantCell(world, pos) != null)
        {
            Entity entity = getOccupantCell(world, pos);

            /* this moves the entity just outside of the grid for
                debugging purposes */
            entity.setPosition(new Point(-1, -1));
            world.entities.remove(entity);
            setOccupantCell(world, pos, null);
        }
    }
    public static Entity findNearest(WorldModel world, Point pos,
                                     EntityKind kind)
    {
        List<Entity> ofType = new LinkedList<>();
        for (Entity entity : world.entities)
        {
            if (entity.getEntityKind() == kind)
            {
                ofType.add(entity);
            }
        }

        return nearestEntity(ofType, pos);
    }
    public static Entity getOccupant(WorldModel world, Point pos)
    {
        if (isOccupied(world, pos)) {
            return getOccupantCell(world, pos);
        } else {
            return null;
        }
    }
    public static void addEntity(WorldModel world, Entity entity)
    {
        if (withinBounds(world, entity.getPosition()))
        {
            setOccupantCell(world, entity.getPosition(), entity);
            world.entities.add(entity);
        }
    }
    public static Entity getOccupantCell(WorldModel world, Point pos)
    {
        return world.occupant[pos.getY()][pos.getX()];
    }

    public static void
    setOccupantCell(WorldModel world, Point pos, Entity entity)
    {
        world.occupant[pos.getY()][pos.getX()] = entity;
    }

    public static Action createAnimationAction(Entity entity, int repeatCount)
    {
        return new Action(ActionKind.ANIMATION, entity, null, repeatCount);
    }

    public static Action createActivityAction(Entity entity, WorldModel world)
    {
        return new Action(ActionKind.ACTIVITY, entity, world, 0);
    }
    public static Entity nearestEntity(List<Entity> entities, Point pos)
    {
        if (entities.isEmpty()) {
            return null;
        } else {
            Entity nearest = entities.get(0);
            int nearestDistance = pos.distanceSquared(nearest.getPosition(), pos);

            for (Entity other : entities)
            {
                int otherDistance = pos.distanceSquared(other.getPosition(), pos);

                if (otherDistance < nearestDistance)
                {
                    nearest = other;
                    nearestDistance = otherDistance;
                }
            }

            return nearest;
        }
    }

    public static boolean withinBounds(WorldModel world, Point pos)
    {
        return pos.getY() >= 0 && pos.getY() < world.size.height &&
                pos.getX() >= 0 && pos.getX() < world.size.width;
    }

    public static boolean isOccupied(WorldModel world, Point pos)
    {
        return withinBounds(world, pos) && getOccupantCell(world, pos) != null;
    }

    public static Point findOpenAround(WorldModel world, Point pos)
    {
        for (int dy = -1; dy <= 1; dy++)
        {
            for (int dx = -1; dx <= 1; dx++)
            {
                Point newPt = new Point(pos.getX() + dx, pos.getY() + dy);
                if (withinBounds(world, newPt) &&
                        !isOccupied(world, newPt))
                {
                    return newPt;
                }
            }
        }

        return null;
    }


}
