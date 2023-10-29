package com.minecrafttas.TASk5.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.minecrafttas.TASk5.TASk5;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.gui.Gui;

@Mixin(Gui.class)
public class MixinOverlay {
	
	@Shadow
	private int screenWidth;
	@Shadow
	private int screenHeight;
	
	@Inject(method = "renderHotbar", at = @At("RETURN"))
	public void inject_renderHotbar(float tickDelta, PoseStack poseStack, CallbackInfo ci) {
		TASk5 five = TASk5.getInstance();
		five.itemhandler.onRender(poseStack, screenWidth, screenHeight);
		if(five.countdown != null) {
			five.countdown.onRender(poseStack, screenWidth, screenHeight);
		}
	}
}
