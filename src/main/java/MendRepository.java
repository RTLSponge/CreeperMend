import com.google.common.base.Objects;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.data.Transaction;
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
    private final Map<LocationTime, Mend> snapshotMendMap = Maps.newHashMap();

    final void add( final Mend mend ) {
        this.pendingMends.add(mend);
        mend.visitTransactions( trans -> {
            final LocationTime loctime = locTime(trans, mend);
            //diamondDebug(trans.getOriginal(), "adding");
            this.snapshotMendMap.put( loctime, mend);
        } );
        this.scheduleMend(mend);
    }

    final void remove( final Mend mend ) {
        this.pendingMends.remove(mend);
        mend.visitTransactions( trans -> {
            final LocationTime loctime = locTime(trans, mend);
            //diamondDebug(trans.getOriginal(), "remove");
            this.snapshotMendMap.remove( loctime );
        } );
    }

    private static LocationTime locTime(final Transaction<BlockSnapshot> trans, final Mend mend) {
        return new LocationTime(trans.getOriginal().getLocation().get(), mend.getTime());
    }


    private static LocationTime locTime(final BlockSnapshot snapshot, final ExplosionTime time) {
        return new LocationTime(snapshot.getLocation().get(), time);
    }

    private Task scheduleMend(Mend mend){
        return Sponge.getGame().getScheduler().createTaskBuilder()
                .delay(15, TimeUnit.SECONDS)
                .name("Explosion Repair Task")
                .execute(task -> {
                    mend.heal(task);
                    this.remove(mend);
                })
                .submit(CreeperMend.getInstance());
    }

    /**
     * Checks the location of the snapshot against currently mending snapshots.
     * @param snapshot
     * @return whether this location has pending mends.
     */
    final boolean shouldCancelDropsFor( final BlockSnapshot snapshot ) {
        return this.pendingMends.stream().anyMatch( mend -> mend.contains(snapshot) );
    }

    final void recordDrops( final BlockSnapshot snapshot, final List<EntitySnapshot> collect, final ExplosionTime time ) {
        final LocationTime locTime = locTime(snapshot, time);
        //diamondDebug(snapshot, "recordDrops");
        final Mend mend = this.snapshotMendMap.get( locTime );
        if( null == mend ) return;
        mend.recordDrops(snapshot, collect);
    }

    static void diamondDebug(BlockSnapshot bs, String s){
        final Location<World> loc = bs.getLocation().get();
        CreeperMend.sLogger().warn(Boolean.toString(loc.equals(loc)));
        if(bs.getState().getType().equals(BlockTypes.DIAMOND_BLOCK)) {
            CreeperMend.sLogger().warn(s+" + "+loc);
        }
    }

    @SuppressWarnings("DesignForExtension")
    @Override public String toString() {
        return Objects.toStringHelper(this)
                .add("pendingMends", this.pendingMends)
                .add("snapshotMendMap", this.snapshotMendMap)
                .toString();
    }

}
