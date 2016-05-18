import com.flowpowered.math.vector.Vector3i;
import com.google.common.base.Objects;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.google.common.collect.Sets;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.block.BlockState;
import org.spongepowered.api.block.BlockType;
import org.spongepowered.api.block.BlockTypes;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.entity.EntitySnapshot;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.function.Consumer;

class Mend {
    private final ExplosionTime time;
    private final Collection<Transaction<BlockSnapshot>> blockSnapshots = new LinkedHashSet<>(15);
    private final Multimap<Location<World>, EntitySnapshot> recordedDrops = Multimaps.newSetMultimap(Maps.newHashMap(), Sets::newHashSet);
    private final Cause cause;
    private static final Collection<BlockType> REPLACEABLE = new LinkedHashSet<>(3);

    {
        REPLACEABLE.add(BlockTypes.AIR);
        REPLACEABLE.add(BlockTypes.WATER);
        REPLACEABLE.add(BlockTypes.FLOWING_WATER);
    }

    Mend(final Cause cause, final List<Transaction<BlockSnapshot>> bs, ExplosionTime time) {
        this.cause = cause;
        this.blockSnapshots.addAll(bs);
        this.time = time;
    }

    final void heal(final Task t) {
        this.blockSnapshots.stream().map(Mend::customOrOriginal).forEach(this::restore);
    }

    //If another plugin overrides the explosion output, use that.
    private static BlockSnapshot customOrOriginal(Transaction<BlockSnapshot> t){
        return t.getCustom().orElse(t.getOriginal());
    }

    private void restore(final BlockSnapshot bs){
        //MendRepository.diamondDebug(bs,"restore");
        final Vector3i pos = bs.getPosition();
        final World world = bs.getLocation().get().getExtent();
        final BlockState current = world.getBlock(pos);
        //if(REPLACEABLE.contains(current.getType())){
        if(false){
            bs.restore(true, false);
        } else {
            final Collection<EntitySnapshot> entities = this.recordedDrops.get(bs.getLocation().get());
            for (final EntitySnapshot entity : entities) {
                CreeperMend.sLogger().warn("dropping "+entity);
            }
            entities.forEach(EntitySnapshot::restore);
        }
    }

    final boolean contains(final BlockSnapshot snapshot) {
        for(final Transaction<BlockSnapshot> t : this.blockSnapshots) {
            if(snapshot.getLocation().equals(t.getOriginal().getLocation()))
                return true;
        }
        return false;
    }

    final void recordDrops(final BlockSnapshot snapshot, final Collection<EntitySnapshot> items){

        this.recordedDrops.putAll(snapshot.getLocation().get(), items);
    }

    final void visitTransactions(final Consumer<Transaction<BlockSnapshot>> visitor){
        this.blockSnapshots.forEach(visitor);
    }


    @Override public String toString() {
        return Objects.toStringHelper(this)
                .add("blockSnapshots", this.blockSnapshots)
                .add("recordedDrops", this.recordedDrops)
                .add("cause", this.cause)
                .add("time", this.time)
                .toString();
    }

    public ExplosionTime getTime() {
        return time;
    }
}
