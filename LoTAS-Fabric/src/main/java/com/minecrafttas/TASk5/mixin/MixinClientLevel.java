package com.minecrafttas.TASk5.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.minecrafttas.TASk5.TASk5;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;

@Mixin(ClientLevel.class)
public class MixinClientLevel {
	
	
	@Inject(method = "disconnect", at = @At("HEAD"))
	public void inject_disconnect(CallbackInfo ci) {
		TASk5.getInstance().onServerStop(Minecraft.getInstance().getSingleplayerServer());
	}
}
