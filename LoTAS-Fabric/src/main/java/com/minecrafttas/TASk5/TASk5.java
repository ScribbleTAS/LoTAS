package com.minecrafttas.TASk5;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.fabricmc.api.ModInitializer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

public class TASk5 implements ModInitializer {
	public static final Logger LOGGER = LoggerFactory.getLogger("TASk5");

	private static TASk5 instance;
	
	public ItemHandler itemhandler;
	
	public Countdown countdown;
	
	public boolean isLoading;
	
	@Override
	public void onInitialize() {
		instance = this;
	}
	
	public static TASk5 getInstance() {
		return instance;
	}
	
	public void onServerInitialize(MinecraftServer server) {
		LOGGER.info("Items initialized!");
		itemhandler = new ItemHandler(server);
	}

	public void onPlayerJoined(ServerPlayer player) {
		
	}
	
	public void onServerStop(MinecraftServer server) {
		if(countdown!=null) {
			countdown.stop(server);
			countdown = null;
		}
	}

	public void onPlayerLeave(ServerPlayer player) {
	}

	public void onTick() {
		if(countdown!=null) {
			countdown.onTick();
		}
	}
}
