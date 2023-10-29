package com.minecrafttas.TASk5.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.minecrafttas.TASk5.TASk5;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

@Mixin(Inventory.class)
public class MixinInventory {
	
	@Inject(method = "setItem", at = @At("HEAD"))
	public void inject_add(int i, ItemStack stack, CallbackInfo ci) {
		TASk5.getInstance().itemhandler.onItemAdd(i, stack);
	}
}
