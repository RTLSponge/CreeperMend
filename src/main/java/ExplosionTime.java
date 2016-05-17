import com.google.common.base.Objects;
import com.google.common.collect.ComparisonChain;
import org.spongepowered.api.Sponge;

class ExplosionTime implements Comparable<ExplosionTime>{

    private final int ticks;
    private final int count;

    private ExplosionTime(final int ticks, final int count){
        this.ticks = ticks;
        this.count = count;
    }

    @Override public String toString() {
        return Objects.toStringHelper(this)
                .add("ticks", ticks)
                .add("count", count)
                .toString();
    }

    @Override public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        final ExplosionTime that = (ExplosionTime) o;
        return this.ticks == that.ticks &&
                this.count == that.count;
    }

    @Override public int hashCode() {
        return Objects.hashCode(this.ticks, this.count);
    }

    @Override public int compareTo(final ExplosionTime o) {
        return ComparisonChain
                .start()
                .compare(this.ticks,o.ticks)
                .compare(this.count,o.count)
                .result();
    }

    static class Factory {
        private int i = 0;
        private int lastTick = -1;
        Factory(){
            this.lastTick = Sponge.getServer().getRunningTimeTicks();
        }
        final ExplosionTime create(){
            return new ExplosionTime(lastTick, this.i);
        }

        final void tick(){
            final int currentTick = Sponge.getServer().getRunningTimeTicks();
            if(this.lastTick < currentTick) {
                this.i = 0;
            } else {
                this.i++;
            }
            this.lastTick = currentTick;
        }

        @Override public String toString() {
            return Objects.toStringHelper(this)
                    .add("i", i)
                    .add("lastTick", lastTick)
                    .toString();
        }
    }
}
