package com.minecrafttas.TASk5.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.minecrafttas.TASk5.TASk5;

import net.minecraft.network.Connection;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;

@Mixin(PlayerList.class)
public class MixinPlayerList {
	
	@Inject(method = "placeNewPlayer", at = @At(value = "RETURN"))
	public void inject_placeNewPlayer(Connection connection, ServerPlayer player, CallbackInfo ci) {
		TASk5.getInstance().onPlayerJoined(player);
	}
	
	
	@Inject(method = "remove", at = @At("HEAD"))
	public void inject_remove(ServerPlayer player,CallbackInfo ci) {
		TASk5.getInstance().onPlayerLeave(player);
	}
}
