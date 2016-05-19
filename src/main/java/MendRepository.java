import com.google.common.base.Objects;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.entity.EntitySnapshot;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

class MendRepository {
    private final Set<Mend> pendingMends = Sets.newHashSet();
    //Should work as long as the block snapshot in the cause of the drops is the same (identity) as the one in the explosion.
    private final Map<Location<World>, Mend> snapshotMendMap = Maps.newHashMap();

    private static Set<BlockSnapshot> debugSnapshots = Sets.newHashSet();

    final void add( final Mend mend ) {
        this.pendingMends.add(mend);
        mend.visitTransactions( trans -> {
            this.snapshotMendMap.put(trans.getOriginal().getLocation().get(), mend);
            debugSnapshots.add(trans.getOriginal());
        } );
        this.scheduleMend(mend);
    }

    final void remove( final Mend mend ) {
        this.pendingMends.remove(mend);
        //At least it won't leak, as removals are based on the same snapshot that gets inserted.
        mend.visitTransactions( trans -> {
            this.snapshotMendMap.remove( trans.getOriginal().getLocation().get() );
            debugSnapshots.remove(trans.getOriginal());
        } );
    }

    private Task scheduleMend(final Mend mend){
        final Task submit = Sponge.getGame().getScheduler().createTaskBuilder()
                .delay(15, TimeUnit.SECONDS)
                .name("Explosion Repair Task")
                .execute(task -> {
                    mend.heal(task);
                    this.pendingMends.remove(mend);
                })
                .submit(CreeperMend.getInstance());
        return submit;
    }

    /**
     * Checks the location of the snapshot against currently mending snapshots.
     * @param snapshot
     * @return whether this location has pending mends.
     */
    final boolean shouldCancelDropsFor( final BlockSnapshot snapshot ) {
        return this.pendingMends.stream().anyMatch( mend -> mend.contains(snapshot) );
    }

    final void recordDrops( final BlockSnapshot snapshot, final List<EntitySnapshot> collect ) {
        //Assumes that the snapshot is the same from the Detonate event to the drop event.
        final Mend mend = this.snapshotMendMap.get(snapshot.getLocation().get());
        CreeperMend.sLogger().warn("Was it in the snapshot? + "+debugSnapshots.contains(snapshot));
        if( null == mend ) return;
        mend.recordDrops(snapshot, collect);
    }


    @SuppressWarnings("DesignForExtension")
    @Override public String toString() {
        return Objects.toStringHelper(this)
                .add("pendingMends", this.pendingMends)
                .add("snapshotMendMap", this.snapshotMendMap)
                .toString();
    }

}
