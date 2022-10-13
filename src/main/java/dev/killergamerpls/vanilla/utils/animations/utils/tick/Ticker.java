package dev.killergamerpls.vanilla.utils.animations.utils.tick;

import dev.killergamerpls.vanilla.utils.animations.utils.AnimationExecutor;
import dev.killergamerpls.vanilla.utils.animations.utils.collection.AnimationList;
import dev.killergamerpls.vanilla.utils.animations.utils.scheduler.S;

import java.util.concurrent.atomic.AtomicLong;

public class Ticker {

    private final int taskId;
    private final AtomicLong ticks;
    private final AnimationList<ITicked> tickedObjects;
    private final AnimationList<ITicked> newTickedObjects;
    private final AnimationList<String> removeTickedObjects;
    private volatile boolean performingTick;

    /**
     * Default constructor. Ticker is initialized and started.
     */
    public Ticker() {
        this.ticks = new AtomicLong(0);
        this.tickedObjects = new AnimationList<>(1024);
        this.newTickedObjects = new AnimationList<>(64);
        this.removeTickedObjects = new AnimationList<>(64);
        this.performingTick = false;
        this.taskId = S.asyncTask(() -> {
            if (!performingTick) tick();
        }, 1L, 5L).getTaskId();
    }

    /**
     * Stop the ticker and unregister all ticked objects.
     */
    public void destroy() {
        S.stopTask(taskId);
        tickedObjects.clear();
        newTickedObjects.clear();
        removeTickedObjects.clear();
    }

    /**
     * Register a new ticked object.
     *
     * @param ticked The ticked object.
     */
    public void register(ITicked ticked) {
        synchronized (newTickedObjects) {
            if (tickedObjects.contains(ticked)) return;
            if (!newTickedObjects.contains(ticked)) {
                newTickedObjects.add(ticked);
            }
        }
    }

    /**
     * Unregister a ticked object.
     *
     * @param id The id of the ticked object.
     */
    public void unregister(String id) {
        synchronized (removeTickedObjects) {
            if (!removeTickedObjects.contains(id)) {
                removeTickedObjects.add(id);
            }
        }
    }

    private void tick() {
        performingTick = true;

        // Tick all ticked objects
        AnimationExecutor e = AnimationExecutor.create(tickedObjects.size());
        synchronized (tickedObjects) {
            for (ITicked ticked : tickedObjects) {
                e.queue(() -> {
                    if (ticked.shouldTick(ticks.get())) {
                        try {
                            ticked.tick();
                        } catch(Throwable t) {
                            t.printStackTrace();
                        }
                    }
                });
            }
        }

        // Remove ticked objects
        synchronized(removeTickedObjects) {
            while(removeTickedObjects.hasElements()) {
                String id = removeTickedObjects.popRandom();
                for (int i = 0; i < tickedObjects.size(); i++) {
                    if(tickedObjects.get(i).getId().equals(id)) {
                        tickedObjects.remove(i);
                        break;
                    }
                }
            }
        }

        // Add new ticked objects
        synchronized (newTickedObjects) {
            while (newTickedObjects.hasElements()) {
                tickedObjects.add(newTickedObjects.pop());
            }
        }
        performingTick = false;
        ticks.incrementAndGet();
    }

}
