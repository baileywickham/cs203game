import java.util.List;
import edu.calpoly.spritely.Tile;

/**
 * An entity in our virtual world.  An entity occupies a square
 * on the grid.  It might move around, and interact with other
 * entities in the world.
 */
final class Entity
{
    private EntityKind kind;
    private Point position;
    private List<Tile> tiles;
    private int tileIndex;       // Index into tiles for animation
    private int resourceLimit;
    private int resourceCount;
    private int actionPeriod;
    private int animationPeriod;

    public Entity(EntityKind kind, Point position,
                  List<Tile> tiles, int resourceLimit, int resourceCount,
                  int actionPeriod, int animationPeriod)
    {
        this.kind = kind;
        this.position = position;
        this.tiles = tiles;
        this.tileIndex = 0;
        this.resourceLimit = resourceLimit;
        this.resourceCount = resourceCount;
        this.actionPeriod = actionPeriod;
        this.animationPeriod = animationPeriod;
    }
    public int getTileIndex() {return this.tileIndex;}
    public int getResourceLimit() {return this.resourceLimit;}
    public int getResourceCount() {return  this.resourceCount;}
    public int getActionPeriod() {return this.actionPeriod;}
    public EntityKind getEntityKind() {return this.kind;}
    public Point getPosition() {return  this.position;}
    public List<Tile> getTiles() {return this.tiles;}
    public void setPosition(Point position) {this.position = position;}

    public int getAnimationPeriod()
    {
        switch (this.kind)
        {
            case MINER_FULL:
            case MINER_NOT_FULL:
            case ORE_BLOB:
            case QUAKE:
                return this.animationPeriod;
            default:
                throw new UnsupportedOperationException(
                        String.format("getAnimationPeriod not supported for %s",
                                this.kind));
        }
    }
    public void nextImage()
    {
        this.tileIndex = (this.tileIndex + 1) % this.tiles.size();
    }
    public Tile getCurrentTile()
    {
        return this.tiles.get(this.tileIndex);
    }
    public boolean
    transformNotFull(WorldModel world,
                     EventSchedule eventSchedule)
    {
        if (this.resourceCount >= this.resourceLimit)
        {
            Entity miner = createMinerFull(this.resourceLimit,
                    this.position, this.actionPeriod, this.animationPeriod);

            WorldModel.removeEntity(world, this);
            eventSchedule.unscheduleAllEvents(this);

            WorldModel.addEntity(world, miner);
            scheduleActions(miner, eventSchedule, world);

            return true;
        }

        return false;
    }

    public void
    transformFull(WorldModel world, EventSchedule eventSchedule)
    {
        Entity miner = createMinerNotFull(this.resourceLimit,
                this.position, this.actionPeriod, this.animationPeriod);

        WorldModel.removeEntity(world, this);
        eventSchedule.unscheduleAllEvents(this);

        WorldModel.addEntity(world, miner);
        scheduleActions(miner, eventSchedule, world);
    }
    public boolean
    moveToNotFull(WorldModel world,
                  Entity target,  EventSchedule eventSchedule)
    {
        if (Point.adjacent(this.position, target.position))
        {
            this.resourceCount += 1;
            WorldModel.removeEntity(world, target);
            eventSchedule.unscheduleAllEvents(target);

            return true;
        }
        else
        {
            Point nextPos = nextPositionMiner(world, target.position);

            if (!this.position.equals(nextPos))
            {
                WorldModel.moveEntity(world, this, nextPos);
            }
            return false;
        }
    }

    public boolean
    moveToFull(WorldModel world,
               Entity target,  EventSchedule eventSchedule)
    {
        if (Point.adjacent(this.position, target.position))
        {
            return true;
        }
        else
        {
            Point nextPos = nextPositionMiner(world, target.position);

            if (!this.position.equals(nextPos))
            {
                WorldModel.moveEntity(world, this, nextPos);
            }
            return false;
        }
    }
    public static void
    scheduleActions(Entity entity,  EventSchedule eventSchedule,
                    WorldModel world)
    {
        switch (entity.kind)
        {
            case MINER_FULL:
                eventSchedule.scheduleEvent(entity,
                        WorldModel.createActivityAction(entity, world),
                        entity.getActionPeriod());
                eventSchedule.scheduleEvent(entity,
                        WorldModel.createAnimationAction(entity, 0),
                        entity.getActionPeriod());
                break;

            case MINER_NOT_FULL:
                eventSchedule.scheduleEvent(entity,
                        WorldModel.createActivityAction(entity, world),
                        entity.getActionPeriod());
                eventSchedule.scheduleEvent(entity,
                        WorldModel.createAnimationAction(entity, 0), entity.getAnimationPeriod());
                break;

            case ORE:
                eventSchedule.scheduleEvent(entity,
                        WorldModel.createActivityAction(entity, world),
                        entity.actionPeriod);
                break;

            case ORE_BLOB:
                eventSchedule.scheduleEvent(entity,
                        WorldModel.createActivityAction(entity, world),
                        entity.actionPeriod);
                eventSchedule.scheduleEvent(entity,
                        WorldModel.createAnimationAction(entity, 0), entity.getAnimationPeriod());
                break;

            case QUAKE:
                eventSchedule.scheduleEvent(entity,
                        WorldModel.createActivityAction(entity, world),
                        entity.actionPeriod);
                eventSchedule.scheduleEvent(entity,
                        WorldModel.createAnimationAction(entity, 10),
                        entity.getAnimationPeriod());
                break;

            case VEIN:
                eventSchedule.scheduleEvent(entity,
                        WorldModel.createActivityAction(entity, world),
                        entity.actionPeriod);
                break;

            default:
        }
    }
    public boolean
    moveToOreBlob(WorldModel world,
                  Entity target,  EventSchedule eventSchedule)
    {
        if (Point.adjacent(this.position, target.position))
        {
            WorldModel.removeEntity(world, target);
            eventSchedule.unscheduleAllEvents(target);
            return true;
        }
        else
        {
            Point nextPos = nextPositionOreBlob(world, target.position);

            if (!this.position.equals(nextPos))
            {
                Entity occupant = WorldModel.getOccupant(world, nextPos);
                if (occupant != null)
                {
                    eventSchedule.unscheduleAllEvents(occupant);
                }

                WorldModel.moveEntity(world, this, nextPos);
            }
            return false;
        }
    }
    public static Entity createBlacksmith(Point position)
    {
        return new Entity(EntityKind.BLACKSMITH, position,
                VirtualWorld.blacksmithTiles, 0, 0, 0, 0);
    }

    public static Entity
    createMinerFull(int resourceLimit, Point position,
                    int actionPeriod, int animationPeriod)
    {
        return new Entity(EntityKind.MINER_FULL, position,
                VirtualWorld.minerTiles,
                resourceLimit, resourceLimit, actionPeriod,
                animationPeriod);
    }

    public static Entity
    createMinerNotFull(int resourceLimit, Point position, int actionPeriod,
                       int animationPeriod)
    {
        return new Entity(EntityKind.MINER_NOT_FULL, position,
                VirtualWorld.minerTiles,
                resourceLimit, 0, actionPeriod, animationPeriod);
    }

    public static Entity
    createObstacle(Point position)
    {
        return new Entity(EntityKind.OBSTACLE, position,
                VirtualWorld.obstacleTiles, 0, 0, 0, 0);
    }

    public static Entity
    createOre(Point position, int actionPeriod)
    {
        return new Entity(EntityKind.ORE, position,
                VirtualWorld.oreTiles, 0, 0, actionPeriod, 0);
    }

    public static Entity
    createOreBlob(Point position, int actionPeriod, int animationPeriod)
    {
        return new Entity(EntityKind.ORE_BLOB, position,
                VirtualWorld.blobTiles,
                0, 0, actionPeriod, animationPeriod);
    }

    public static Entity createQuake(Point position)
    {
        return new Entity(EntityKind.QUAKE, position,
                VirtualWorld.quakeTiles, 0, 0, 1100, 100);
    }

    public static Entity createVein(Point position, int actionPeriod)
    {
        return new Entity(EntityKind.VEIN, position,
                VirtualWorld.veinTiles, 0, 0, actionPeriod, 0);
    }

    public Point
    nextPositionMiner(WorldModel world, Point destPos)
    {
        int horiz = Integer.signum(destPos.getX() - this.position.getX());
        Point newPos = new Point(this.position.getX() + horiz,
                this.position.getY());

        if (horiz == 0 || WorldModel.isOccupied(world, newPos))
        {
            int vert = Integer.signum(destPos.getY() - this.position.getY());
            newPos = new Point(this.position.getX(),
                    this.position.getY() + vert);

            if (vert == 0 || WorldModel.isOccupied(world, newPos))
            {
                newPos = this.position;
            }
        }

        return newPos;
    }

    public  Point
    nextPositionOreBlob(WorldModel world, Point destPos) {
        int horiz = Integer.signum(destPos.getX() - this.position.getX());
        Point newPos = new Point(this.position.getX() + horiz,
                this.position.getY());

        Entity occupant = WorldModel.getOccupant(world, newPos);

        if (horiz == 0 ||
                (occupant != null && !(occupant.kind == EntityKind.ORE))) {
            int vert = Integer.signum(destPos.getY() - this.position.getY());
            newPos = new Point(this.position.getX(),
                    this.position.getY() + vert);
            occupant = WorldModel.getOccupant(world, newPos);

            if (vert == 0 ||
                    (occupant != null && !(occupant.kind == EntityKind.ORE))) {
                newPos = this.position;
            }
        }
        return newPos;
    }

}
