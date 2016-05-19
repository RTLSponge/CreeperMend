import com.google.inject.Inject;
import org.slf4j.Logger;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.entity.Entity;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.block.NotifyNeighborBlockEvent;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.api.event.cause.entity.spawn.BlockSpawnCause;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.event.filter.cause.Named;
import org.spongepowered.api.event.game.state.GamePreInitializationEvent;
import org.spongepowered.api.event.item.inventory.DropItemEvent;
import org.spongepowered.api.event.world.ExplosionEvent;
import org.spongepowered.api.plugin.Plugin;

import java.util.List;
import java.util.stream.Collectors;

@Plugin(id = "creepermend", name = "CreeperMend", version = "0.2.0-SNAPSHOT")
public class CreeperMend {

    @Inject
    private final Logger logger = null;
    private final MendRepository pendingMends = new MendRepository();
    private static CreeperMend instance = null;

    private final ExplosionTime.Factory explosionTimeFactory = new ExplosionTime.Factory();

    @Listener
    public final void preInit(final GamePreInitializationEvent e){
        CreeperMend.instance = this;
    }

    static CreeperMend getInstance() {
        return CreeperMend.instance;
    }

    @Listener(order = Order.LAST)
    public final void onExplode(final ExplosionEvent.Detonate bang){
        final List<Transaction<BlockSnapshot>> transactions = bang.getTransactions();
        //increment the explosion count.
        this.explosionTimeFactory.countExplosion();
        final Mend mend = new Mend(bang.getCause(), transactions, this.explosionTimeFactory.create());
        this.pendingMends.add(mend);
    }

    @Listener(order = Order.LATE)
    public final void onUpdate(final NotifyNeighborBlockEvent blockUpdateEvent, @First final BlockSnapshot snapshot){
        //Cancel any blockupdates caused by currently mending blocks.
        // Should prevent torches etc popping off due to explosion, unless another block is responsible.
        if(this.pendingMends.shouldCancelDropsFor(snapshot))
            blockUpdateEvent.setCancelled(true);
    }

    //Late, read only, cancel allowed. Need to act after other plugins in case drops would be modified for some other reason.
    @Listener(order = Order.LATE)
    public final void onBlockDrops(final DropItemEvent.Destruct dropItemEvent, @Named(NamedCause.SOURCE) final BlockSpawnCause cause) {
        final BlockSnapshot snapshot = cause.getBlockSnapshot();
        dropItemEvent.setCancelled(this.pendingMends.shouldCancelDropsFor(snapshot));
        //recreate snapshots, allows for filtering and possible modifications by other plugins.
        this.pendingMends.recordDrops(
                snapshot,
                dropItemEvent.getEntities().stream()
                    .map(Entity::createSnapshot)
                    .collect(Collectors.toList()),
                this.explosionTimeFactory.create()
        );
    }

    private Logger logger(){
        return this.logger;
    }

    static Logger sLogger(){
        return CreeperMend.instance.logger();
    }
}
