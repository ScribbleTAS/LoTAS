package com.minecrafttas.TASk5.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.minecrafttas.TASk5.Countdown;
import com.minecrafttas.TASk5.TASk5;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;

@Mixin(PauseScreen.class)
public class MixinPauseMenu extends Screen{
	
	protected MixinPauseMenu(Component component) {
		super(component);
	}

	@Inject(method = "init", at = @At(value = "HEAD"))
	public void inject_init(CallbackInfo ci) {
		if (TASk5.getInstance().isLoading) {
			TASk5.getInstance().isLoading = false;
			if(TASk5.getInstance().countdown == null) {
				TASk5.getInstance().countdown = new Countdown();
			}
		}
	}
	
	@Inject(method = "render", at = @At(value = "RETURN"))
	public void inject_render(PoseStack poseStack, int i, int j, float f, CallbackInfo ci) {
		if(TASk5.getInstance().countdown!=null) {
			drawCenteredString(poseStack, font, new TextComponent(TASk5.getInstance().countdown.getTimeString()), width/2, 20, 0x55FF55);
			drawCenteredString(poseStack, font, new TextComponent(Integer.toString(TASk5.getInstance().itemhandler.size())), width/2, 30, 0x55FF55);
		}
	}
}
