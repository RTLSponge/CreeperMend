package events;

import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.entity.EntitySnapshot;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.item.inventory.DropItemEvent;
import org.spongepowered.api.world.World;

import java.util.List;

/**
 * Thrown when block drops are appearing after mending.
 */
@SuppressWarnings("WeakerAccess")
public class DelayedDropEvent implements DropItemEvent.Custom {

    private boolean cancelled;
    private List<EntitySnapshot> snapshots;
    private List<Entity> entities;
    private Cause cause;
    private World world;

    @Override public List<EntitySnapshot> getEntitySnapshots() {
        return snapshots;
    }

    @Override public List<Entity> getEntities() {
        return entities;
    }

    @Override public boolean isCancelled() {
        return cancelled;
    }

    @Override public void setCancelled(final boolean cancel) {
        cancelled = cancel;
    }

    @Override public World getTargetWorld() {
        return world;
    }

    @Override public Cause getCause() {
        return cause;
    }
}
