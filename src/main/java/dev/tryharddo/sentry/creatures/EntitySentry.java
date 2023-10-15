package dev.tryharddo.sentry.creatures;

import net.minecraft.world.entity.EntityCreature;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.level.World;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_20_R2.CraftWorld;
import org.jetbrains.annotations.NotNull;

public class EntitySentry extends EntityCreature {

    protected EntitySentry(EntityTypes<? extends EntityCreature> entitytypes, World world) {
        super(entitytypes, world);
    }
}
