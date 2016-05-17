package config;

import ninja.leaping.configurate.objectmapping.Setting;
import org.spongepowered.api.block.BlockState;

import java.util.Collection;
import java.util.Map;


public class Config {

    @Setting(comment = "Name a config to mirror from, if this is set, this config will copy defaults from another config")
    private String mirror;

    @Setting(comment = "Time before rollback starts")
    private int rollbackDelay;

    @Setting(comment = "A collection of blockstates that will never heal e.g. TNT")
    private Collection<BlockState> blacklist;

    @Setting(comment = "A collection of blockstates that will always drop their items.")
    private Collection<BlockState> alwaysDrop;

    @Setting(comment = "A collection of blockstates that will never explode")
    private Collection<BlockState> protectedBlocks;

    @Setting(comment = "A block with a higher priority will win over a block with lower priority.")
    private Map<Integer, BlockState> replaceable;

    @Setting(comment = "A map of transformations to make when healing or dropping items.")
    private Map<BlockState, BlockState> transformations;

}
