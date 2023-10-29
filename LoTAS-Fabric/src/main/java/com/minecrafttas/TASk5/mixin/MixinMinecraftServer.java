package com.minecrafttas.TASk5.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.minecrafttas.TASk5.TASk5;

import net.minecraft.server.MinecraftServer;

@Mixin(MinecraftServer.class)
public class MixinMinecraftServer {
	
	@Inject(method = "runServer", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;initServer()Z", shift = Shift.AFTER))
	public void inject_runServer(CallbackInfo ci) {
		TASk5.getInstance().onServerInitialize((MinecraftServer)(Object)this);
	}
	
	
	@Inject(method = "stopServer", at = @At("HEAD"))
	public void inject_stopServer(CallbackInfo ci) {
//		TASk5.getInstance().onServerStop((MinecraftServer)(Object)this);
	}
	
	
	@Inject(method = "tickServer", at = @At("HEAD"))
	public void inject_tickServer(CallbackInfo ci) {
//		TASk5.getInstance().onTick();
	}
}
