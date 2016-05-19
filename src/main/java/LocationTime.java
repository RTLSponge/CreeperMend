import com.google.common.base.Objects;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

final class LocationTime {

    private final Location<World> location;
    private final ExplosionTime ticks;

    LocationTime(final Location<World> location, final ExplosionTime time) {
        this.location = location;
        this.ticks = time;
    }

    @Override public String toString() {
        return Objects.toStringHelper(this)
                .add("location", this.location)
                .add("ticks", this.ticks)
                .toString();
    }

    @Override public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if ((null == obj) || (this.getClass() != obj.getClass())) {
            return false;
        }
        final LocationTime that = (LocationTime) obj;
        return (this.ticks == that.ticks) &&
                Objects.equal(this.location, that.location);
    }

    @Override public int hashCode() {
        return Objects.hashCode(this.location, this.ticks);
    }
}
