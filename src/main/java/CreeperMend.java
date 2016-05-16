import com.google.inject.Inject;
import org.slf4j.Logger;
import org.spongepowered.api.Game;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.block.NotifyNeighborBlockEvent;
import org.spongepowered.api.event.cause.entity.spawn.BlockSpawnCause;
import org.spongepowered.api.event.filter.cause.First;
import org.spongepowered.api.event.filter.cause.Named;
import org.spongepowered.api.event.item.inventory.DropItemEvent;
import org.spongepowered.api.event.world.ExplosionEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.scheduler.Task;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Plugin(id = "CreeperMend", name = "CreeperMend", version = "0.2.0-SNAPSHOT")
public class CreeperMend {

    @Inject
    Logger logger = null;
    @Inject
    Game game = null;

    Set<Mend> pendingMends = new HashSet<Mend>();

    @Listener(order = Order.LAST)
    public void onExplode(final ExplosionEvent.Detonate bang){
        logger.warn("explosion scheduled");
        final List<Transaction<BlockSnapshot>> transactions = bang.getTransactions();
        final Mend mend = new Mend(game, bang.getCause(), bang.getTransactions().stream().map(Transaction::getOriginal).collect(Collectors.toSet()));
        pendingMends.add(mend);
        final Task submit = Sponge.getGame().getScheduler().createTaskBuilder()
                .delay(15, TimeUnit.SECONDS)
                .name("Explosion Repair Task")
                .execute(task -> {
                    mend.heal(task);
                    logger.warn("Explosion healing");
                    pendingMends.remove(mend);
                })
                .submit(this);
    }

    @Listener public void onUpdate(NotifyNeighborBlockEvent blockUpdateEvent, @First BlockSnapshot snapshot){
        logger.warn("onUpdate");
        logger.warn(blockUpdateEvent.getCause().toString());
        if(pendingMends.stream().anyMatch(mend -> mend.contains(snapshot)))
            blockUpdateEvent.setCancelled(true);
    }

    @Listener public void onBlockDrops(DropItemEvent.Destruct dropItemEvent, @Named("Source") BlockSpawnCause cause) {
        logger.warn("onBlockDrops");
        logger.warn(dropItemEvent.getCause().toString());
        final BlockSnapshot snapshot = cause.getBlockSnapshot();
        dropItemEvent.setCancelled(pendingMends.stream().anyMatch(mend->mend.contains(snapshot)));
    }
}
