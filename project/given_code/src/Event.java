
/**
 * A timed event in the virtual world.  Events are queued, and then
 * executed when their time arrives.
 */
final class Event
{
    private final Action action;
    private final double time;
    private final Object target;

    public Event(Action action, double time, Object target)
    {
        this.action = action;
        this.time = time;
        this.target = target;
    }

    public double getTime() {
        return time;
    }

    public Object getTarget() {
        return target;
    }

    public void execute(EventSchedule schedule) {
        action.executeAction(schedule);
    }
}
