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
import java.util.Set;
import java.util.stream.Collectors;

@Plugin(id = "creepermend", name = "CreeperMend", version = "0.2.0-SNAPSHOT")
public class CreeperMend {

    @Inject
    private final Logger logger = null;
    private final MendRepository pendingMends = new MendRepository();
    private static CreeperMend instance;

    public static CreeperMend getInstance() {
        return CreeperMend.instance;
    }
    private final ExplosionTime.Factory explosionTimeFactory = new ExplosionTime.Factory();

    @Listener
    public final void preInit(GamePreInitializationEvent e){
        this.instance = this;
    }

    static ExplosionTime debug;
    static Set<LocationTime> debugLTs;

    @Listener(order = Order.LAST)
    public final void onExplode(final ExplosionEvent.Detonate bang){
        this.logger().debug("explosion scheduled : "+ bang.getCause() + "\n"+bang.toString());
        final List<Transaction<BlockSnapshot>> transactions = bang.getTransactions();
        this.explosionTimeFactory.tick();
        final Mend mend = new Mend(bang.getCause(), transactions, this.explosionTimeFactory.create());
        debug = this.explosionTimeFactory.create();
        this.pendingMends.add(mend);
        debugLTs = transactions.stream().map(t->new LocationTime(t.getOriginal().getLocation().get(), debug)).collect(Collectors.toSet());

    }

    @Listener(order = Order.LATE)
    public final void onUpdate(final NotifyNeighborBlockEvent blockUpdateEvent, @First final BlockSnapshot snapshot){
        this.logger().debug("onUpdate@ " + snapshot.toString() + " : " + blockUpdateEvent.getCause().toString());
        //Cancel any blockupdates caused by currently mending blocks.
        // Should prevent torches etc popping off due to explosion, unless another block is responsible.
        if(this.pendingMends.shouldCancelDropsFor(snapshot))
            blockUpdateEvent.setCancelled(true);
    }

    //Late, read only, cancel allowed. Need to act after other plugins in case drops would be modified for some other reason.
    @Listener(order = Order.LATE)
    public final void onBlockDrops(final DropItemEvent.Destruct dropItemEvent,
            @Named(NamedCause.SOURCE) final BlockSpawnCause
                    cause) {
        this.logger().debug("onBlockDrops @ "+ dropItemEvent.getEntities().toString() + " : " + dropItemEvent.getCause().toString());
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
        final ExplosionTime newDebug = this.explosionTimeFactory.create();
        assert debug.equals(this.explosionTimeFactory.create());
        assert debugLTs.contains(new LocationTime(snapshot.getLocation().get(), newDebug));
        //logger().warn("explosionTime Equals "+debug.toString() + ", " + newDebug.toString());

    }

    Logger logger(){
        return this.logger;
    }

    @SuppressWarnings("StaticVariableUsedBeforeInitialization")
    static Logger sLogger(){
        return CreeperMend.instance.logger();
    }
}
