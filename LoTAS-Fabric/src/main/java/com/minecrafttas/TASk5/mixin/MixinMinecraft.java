package com.minecrafttas.TASk5.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.minecrafttas.TASk5.Countdown;
import com.minecrafttas.TASk5.TASk5;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

@Mixin(Minecraft.class)
public class MixinMinecraft {
	
	@Inject(method = "setLevel", at = @At("HEAD"))
	public void injectloadWorld(net.minecraft.client.multiplayer.ClientLevel worldClientIn, CallbackInfo ci) {
		TASk5.getInstance().isLoading = worldClientIn != null;
	}
	
	@Inject(method = "setScreen", at = @At(value = "HEAD"))
	public void injectdisplayGuiScreen(Screen guiScreenIn, CallbackInfo ci) {
		if (TASk5.getInstance().isLoading && guiScreenIn == null) {
			TASk5.getInstance().isLoading = false;
			if(TASk5.getInstance().countdown == null) {
				TASk5.getInstance().countdown = new Countdown();
			}
		}
	}
	
	@Inject(method = "tick", at = @At(value = "HEAD"))
	public void injectrunTick(CallbackInfo ci) {
		if(!Minecraft.getInstance().isPaused()) {
//			TASk5.getInstance().onTick();
		}
	}

}
