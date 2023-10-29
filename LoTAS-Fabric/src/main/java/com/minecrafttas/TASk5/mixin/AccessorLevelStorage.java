package com.minecrafttas.TASk5.mixin;

import org.spongepowered.asm.mixin.Mixin;

import net.minecraft.server.MinecraftServer;

@Mixin(MinecraftServer.class)
public interface AccessorLevelStorage {
	@org.spongepowered.asm.mixin.gen.Accessor
	public net.minecraft.world.level.storage.LevelStorageSource.LevelStorageAccess getStorageSource();
}
