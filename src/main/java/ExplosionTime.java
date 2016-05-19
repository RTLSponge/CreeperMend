import com.google.common.base.Objects;
import com.google.common.collect.ComparisonChain;
import org.spongepowered.api.Sponge;

final class ExplosionTime implements Comparable<ExplosionTime>{

    private final int ticks;
    private final int count;

    private ExplosionTime(final int ticks, final int count){
        this.ticks = ticks;
        this.count = count;
    }

    @Override public String toString() {
        return Objects.toStringHelper(this)
                .add("ticks", this.ticks)
                .add("count", this.count)
                .toString();
    }

    @Override public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if ((null == obj) || (this.getClass() != obj.getClass())) {
            return false;
        }
        final ExplosionTime that = (ExplosionTime) obj;
        return (this.ticks == that.ticks) &&
                (this.count == that.count);
    }

    @Override public int hashCode() {
        return Objects.hashCode(this.ticks, this.count);
    }

    @Override public int compareTo(final ExplosionTime o) {
        return ComparisonChain
                .start()
                .compare(this.ticks, o.ticks)
                .compare(this.count, o.count)
                .result();
    }

    static class Factory {
        private int explosionCounter = 0;
        private int lastTick = -1;
        Factory(){
            this.lastTick = Sponge.getServer().getRunningTimeTicks();
        }
        final ExplosionTime create(){
            this.tick();
            return new ExplosionTime(this.lastTick, this.explosionCounter);
        }

        //Nicer name to make code more readable.
        private void tick(){
            this.isNewTick();
        }

        private boolean isNewTick(){
            final int currentTick = Sponge.getServer().getRunningTimeTicks();
            boolean retval = false;
            if(this.lastTick < currentTick) {
                this.explosionCounter = 0;
                retval = true;
            }
            this.lastTick = currentTick;
            return retval;
        }

        final void countExplosion(){
            if(!this.isNewTick()) {
                this.explosionCounter++;
            }
        }

        @Override public final String toString() {
            return Objects.toStringHelper(this)
                    .add("explosionCounter", this.explosionCounter)
                    .add("lastTick", this.lastTick)
                    .toString();
        }
    }
}
