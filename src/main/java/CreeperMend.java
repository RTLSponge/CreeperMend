import com.google.inject.Inject;
import org.spongepowered.api.Game;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.block.BlockSnapshot;
import org.spongepowered.api.data.Transaction;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.world.ExplosionEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.scheduler.Task;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Plugin(id = "CreeperMend", name = "CreeperMend", version = "0.1-SNAPSHOT")
public class CreeperMend {

    //@Inject
    //Logger logger = null;
    @Inject
    Game game = null;

    @Listener(order = Order.LAST)
    public void onExplode(final ExplosionEvent.Detonate bang){

        final List<Transaction<BlockSnapshot>> transactions = bang.getTransactions();
        final Mend mend = new Mend(game, bang.getCause(), bang.getTransactions().stream().map(Transaction::getOriginal).collect(Collectors.toSet()));
        final Task submit = Sponge.getGame().getScheduler().createTaskBuilder()
                .delay(15, TimeUnit.SECONDS)
                .name("Explosion Repair Task")
                .execute(task -> mend.heal(task))
                .submit(this);
    }


}
