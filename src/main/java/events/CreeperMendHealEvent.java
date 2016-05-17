package events;

import com.google.common.base.Objects;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.world.World;

import java.util.List;

/**
 * Thrown just before blocks are healed
 */
@SuppressWarnings("WeakerAccess")
public class CreeperMendHealEvent implements org.spongepowered.api.event.block.ChangeBlockEvent.Place {

    private boolean cancelled;
    private World world;
    private Cause cause;
    private List<Transaction<BlockSnapshot>> transactions;

    public CreeperMendHealEvent(final World world, final Cause cause, final List<Transaction<BlockSnapshot>> transactions) {
        this.world = world;
        this.cause = cause;
        this.transactions = transactions;
    }

    @Override public List<Transaction<BlockSnapshot>> getTransactions() {
        return this.transactions;
    }

    @Override public boolean isCancelled() {
        return this.cancelled;
    }

    @Override public void setCancelled(final boolean cancel) {
        this.cancelled = cancel;
    }

    @Override public World getTargetWorld() {
        return world;
    }

    @Override public Cause getCause() {
        return cause;
    }

    @Override public String toString() {
        return Objects.toStringHelper(this)
                .add("cancelled", cancelled)
                .add("world", world)
                .add("cause", cause)
                .add("transactions", transactions)
                .toString();
    }
}
