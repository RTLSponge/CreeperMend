import com.google.common.base.Objects;
import org.spongepowered.api.world.Location;
import org.spongepowered.api.world.World;

public final class LocationTime {

    private final Location<World> location;
    private final ExplosionTime ticks;

    public LocationTime(final Location<World> location, final ExplosionTime time) {
        this.location = location;
        this.ticks = time;
    }

    @Override public String toString() {
        return Objects.toStringHelper(this)
                .add("location", location)
                .add("ticks", ticks)
                .toString();
    }

    @Override public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || this.getClass() != o.getClass()) {
            return false;
        }
        final LocationTime that = (LocationTime) o;
        return this.ticks == that.ticks &&
                Objects.equal(this.location, that.location);
    }

    @Override public int hashCode() {
        return Objects.hashCode(this.location, this.ticks);
    }
}
