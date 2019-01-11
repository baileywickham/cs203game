
import java.util.ArrayList;
import java.util.List;

/**
 * This data stucture is used to hold the scheduled events that are queued
 * for later execution.
 * <p>
 * This event queue is used to drive all movement and animation within
 * the miner simulation.
 */
final class EventSchedule {

    /**
     * A list of all pending events.
     */
    private List<Event> pendingEvents;

    /**
     * The current time in ms, according to how far we've advanced.
     */
    private double currentTime;

    /**
     * Create a new EventSchedule.
     */
    public EventSchedule()
    {
        this.pendingEvents = new ArrayList<Event>();
        this.currentTime = 0.0;
    }

    /**
     * Get the time we've advanced to so far.  When an event's action is
     * being executed, this will be the time of that event.
     */
    public double getCurrentTime() {
        return currentTime;
    }

    /**
     * Schedule an event at or after the current time.
     *
     * @param target  The target of the event.  This can be used to
     *                unschedule all events for a given target.  It may
     *                be null.
     * @param action  The action to execute when the event is fired.
     * @param after   How far in the future, in ms.  Must be >= 0.
     *                The event will fire at getCurrentTime() + after.
     */
    public void scheduleEvent(Object target, Action action, double after) {
        assert after >= 0;
        assert target != null;

        double time = getCurrentTime() + after;
        int i = 0;
        Event e = new Event(action, time, target);
        for (i = 0; i < pendingEvents.size(); i++) {
            if (pendingEvents.get(i).getTime() > time) {
                break;
            }
        }
        pendingEvents.add(i, e);
        //System.out.println("TO DO:  Implement add " + action + " to "
        //                  + pendingEvents);
    }

    /**
     * Remove all of the events for the given target from the list of
     * pending events.  This should remove every event where 
     * target.equals(event.getTarget()).  Only events with non-null
     * targets can be unscheduled with this method.
     */
    public void unscheduleAllEvents(Object target)
    {
        assert target != null;
        pendingEvents.removeIf(event -> target.equals(event.getTarget()));
    }


    /**
     * Process all the pending events, in order, until we get to
     * the time advanceToTime.  When this method completes,
     * getCurrentTime() will return advanceToTime.
     */
    public void processEvents(double advanceToTime)
    {
        assert advanceToTime >= currentTime;
        //System.out.println("TO DO:  Implement advance to " + advanceToTime);
        currentTime = advanceToTime;
        while (pendingEvents.size() != 0 && pendingEvents.get(0).getTime() <= currentTime) {
                Event e = pendingEvents.get(0);
                e.execute(this);
                pendingEvents.remove(e);
        }
    }

    /**
     * Give the number of events currently scheduled.
     */
    public int size() {
        return pendingEvents.size();
    }

}
