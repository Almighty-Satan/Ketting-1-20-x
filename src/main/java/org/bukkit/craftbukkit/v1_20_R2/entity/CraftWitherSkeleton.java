package org.bukkit.craftbukkit.v1_20_R2.entity;

import net.minecraft.world.entity.monster.AbstractSkeleton;
import org.bukkit.craftbukkit.v1_20_R2.CraftServer;
import org.bukkit.entity.Skeleton.SkeletonType;
import org.bukkit.entity.WitherSkeleton;

public class CraftWitherSkeleton extends CraftAbstractSkeleton implements WitherSkeleton {

    public CraftWitherSkeleton(CraftServer server, net.minecraft.world.entity.monster.WitherSkeleton entity) {
        super(server, (AbstractSkeleton) entity);
    }

    public String toString() {
        return "CraftWitherSkeleton";
    }

    public SkeletonType getSkeletonType() {
        return SkeletonType.WITHER;
    }
}