package events;

import com.google.common.base.Objects;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.event.block.TargetBlockEvent;
import org.spongepowered.api.event.cause.Cause;

/**
 * Thrown when a recordable event happens, after initial filtering by config has been performed.
 */
@SuppressWarnings("WeakerAccess")
public class CreeperMendRecordEvent implements TargetBlockEvent {

    private final Cause cause;
    private final BlockSnapshot blockSnapshot;
    private final CreeperMendRecordEvent.Action originalAction;
    private CreeperMendRecordEvent.Action action;


    public CreeperMendRecordEvent(final Cause cause, final BlockSnapshot blockSnapshot, final CreeperMendRecordEvent.Action originalAction) {
        this.cause = cause;
        this.blockSnapshot = blockSnapshot;
        this.originalAction = originalAction;
        this.action = originalAction;
    }

    @Override public Cause getCause() {
        return this.cause;
    }

    @Override public BlockSnapshot getTargetBlock() {
        return this.blockSnapshot;
    }

    public CreeperMendRecordEvent.Action getOriginalAction(){
        return this.originalAction;
    }

    public CreeperMendRecordEvent.Action getAction(){
        return this.action;
    }

    public void setAction(final CreeperMendRecordEvent.Action action) {
        this.action = action;
    }


    public enum Action {
        Protect(), //block doesn't explode
        Heal(), //block explodes, then heals
        Drop(); //block explodes,  drops items
    }

    @Override public String toString() {
        return Objects.toStringHelper(this)
                .add("cause", cause)
                .add("blockSnapshot", blockSnapshot)
                .add("originalAction", originalAction)
                .add("action", action)
                .toString();
    }
}
