import java.util.Random;

/**
 * An action data structure records information about
 * an action that is to be performed on an entity.  It 
 * is attached to an Event data structure.
 */

final class Action
{
    private ActionKind kind;
    private Entity entity;
    private WorldModel world;
    private int repeatCount;     // A repeat count of 0 means to repeat forever
    public static final Random rand = new Random();


    public Action(ActionKind kind, Entity entity, WorldModel world,
                  int repeatCount)
    {
        this.kind = kind;
        this.entity = entity;
        this.world = world;
        this.repeatCount = repeatCount;
    }
    public void executeAction(EventSchedule eventSchedule)
    {
        switch (this.kind)
        {
            case ACTIVITY:
                executeActivityAction(eventSchedule);
                break;

            case ANIMATION:
                executeAnimationAction(eventSchedule);
                break;
        }
    }

    private void
    executeAnimationAction(EventSchedule eventSchedule)
    {
        this.entity.nextImage();

        if (this.repeatCount != 1)
        {
            eventSchedule.scheduleEvent(this.entity,
                    WorldModel.createAnimationAction(this.entity,
                            Math.max(this.repeatCount - 1, 0)),
                    this.entity.getAnimationPeriod());
        }
    }
    public void
    executeActivityAction(EventSchedule eventSchedule)
    {
        switch (this.entity.getEntityKind())
        {
            case MINER_FULL:
                executeMinerFullActivity(this.world,
                        eventSchedule);
                break;

            case MINER_NOT_FULL:
                executeMinerNotFullActivity(this.world,
                        eventSchedule);
                break;

            case ORE:
                executeOreActivity(this.entity, this.world, eventSchedule);
                break;

            case ORE_BLOB:
                executeOreBlobActivity(this.world, eventSchedule);
                break;

            case QUAKE:
                executeQuakeActivity(this.world, eventSchedule);
                break;

            case VEIN:
                executeVeinActivity(this.world, eventSchedule);
                break;

            default:
                throw new UnsupportedOperationException(
                        String.format("executeActivityAction not supported for %s",
                                this.entity.getEntityKind()));
        }
    }
    public void
    executeMinerFullActivity(WorldModel world,
                             EventSchedule eventSchedule)
    {
        Entity fullTarget
                = world.findNearest(world, this.entity.getPosition(), EntityKind.BLACKSMITH);

        if (fullTarget != null  &&
                this.entity.moveToFull(world, fullTarget, eventSchedule))
        {
            this.entity.transformFull(world, eventSchedule);
        }
        else
        {
            eventSchedule.scheduleEvent(this.entity,
                    WorldModel.createActivityAction(entity, world),
                    entity.getActionPeriod());
        }
    }

    public void
    executeMinerNotFullActivity(WorldModel world,
                                EventSchedule eventSchedule)
    {
        Entity notFullTarget = world.findNearest(world, entity.getPosition(),
                EntityKind.ORE);

        if (notFullTarget == null ||
                !entity.moveToNotFull(world, notFullTarget, eventSchedule) ||
                !entity.transformNotFull(world, eventSchedule))
        {
            eventSchedule.scheduleEvent(entity,
                    WorldModel.createActivityAction(entity, world),
                    entity.getActionPeriod());
        }
    }

    public static void
    executeOreActivity(Entity entity, WorldModel world,
                       EventSchedule eventSchedule)
    {
        Point pos = entity.getPosition();    // store current position before removing

        WorldModel.removeEntity(world, entity);
        eventSchedule.unscheduleAllEvents(entity);

        Entity blob = Entity.createOreBlob(pos, entity.getActionPeriod() / 4,
                50 + rand.nextInt(100));

        WorldModel.addEntity(world, blob);
        Entity.scheduleActions(blob, eventSchedule, world);
    }

    public void
    executeOreBlobActivity(WorldModel world,
                           EventSchedule eventSchedule)
    {
        Entity blobTarget = WorldModel.findNearest(world,
                this.entity.getPosition(), EntityKind.VEIN);
        long nextPeriod = entity.getActionPeriod();

        if (blobTarget != null)
        {
            Point tgtPos = blobTarget.getPosition();

            if (this.entity.moveToOreBlob(world, blobTarget, eventSchedule))
            {
                Entity quake = Entity.createQuake(tgtPos);

                WorldModel.addEntity(world, quake);
                nextPeriod += this.entity.getActionPeriod();
                Entity.scheduleActions(quake, eventSchedule, world);
            }
        }

        eventSchedule.scheduleEvent(entity,
                WorldModel.createActivityAction(entity, world),
                nextPeriod);
    }

    public void
    executeQuakeActivity(WorldModel world,
                         EventSchedule eventSchedule)
    {
        eventSchedule.unscheduleAllEvents(entity);
        WorldModel.removeEntity(world, entity);
    }

    public void
    executeVeinActivity(WorldModel world,
                        EventSchedule eventSchedule)
    {
        Point openPt = WorldModel.findOpenAround(world, entity.getPosition());

        if (openPt != null) {
            Entity ore = Entity.createOre(openPt, 20000 + rand.nextInt(10000));
            WorldModel.addEntity(world, ore);
            Entity.scheduleActions(ore, eventSchedule, world);
        }

        eventSchedule.scheduleEvent(entity,
                WorldModel.createActivityAction(entity, world),
                entity.getActionPeriod());
    }


}
