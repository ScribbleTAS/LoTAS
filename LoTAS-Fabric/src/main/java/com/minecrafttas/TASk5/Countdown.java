package com.minecrafttas.TASk5;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.time.Duration;

import com.minecrafttas.TASk5.mixin.AccessorLevelStorage;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.server.MinecraftServer;

public class Countdown{

	private Integer ticks = null;
	
	private int duration = 600;
	
	public Countdown() {
		load();
		if(ticks == null) {
			ticks = 0;
		}
	}

	public void onRender(PoseStack poseStack, int screenWidth, int screenHeight) {
		Minecraft mc = Minecraft.getInstance();
		Gui.drawString(poseStack, mc.font, getTimeString(), 10, 20, 0xFFFFF);
	}
	
	public String getTimeString() {
		if(ticks==null) {
			return "Tick is null";
		}
		int time = duration-ticks;
		ChatFormatting color = ChatFormatting.WHITE;
		if(time == 0) {
			color = ChatFormatting.RED;
		}
		return color+getTimer(Duration.ofMillis(time*50));
	}
	
	private String getTimer(Duration d) {
		return String.format("%02d", d.toMinutes()) + ":" + String.format("%02d", d.getSeconds() % 60) + "." + (int) ((d.toMillis() % 1000));
	}

	public void onTick() {
		ItemHandler handler = TASk5.getInstance().itemhandler;
		if(ticks==null)
			return;
		if(ticks < duration && TASk5.getInstance().isLoading==false) {
			ticks++;
			if(ticks == duration) {
				handler.onTimerRanOut();
			}
		}
		handler.lock = ticks == duration;
	}
	
	public void save(MinecraftServer server) {
		Minecraft mc = Minecraft.getInstance();
		String levelname = ((AccessorLevelStorage)server).getStorageSource().getLevelId();
		File saveLoc = new File(mc.gameDirectory, "saves/"+levelname+"/timer.dat");
		try {
			FileOutputStream stream = new FileOutputStream(saveLoc);
			ObjectOutputStream oos = new ObjectOutputStream(stream);
			oos.writeObject(ticks);
			oos.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void load() {
		Minecraft mc = Minecraft.getInstance();
		String levelname = ((AccessorLevelStorage)mc.getSingleplayerServer()).getStorageSource().getLevelId();
		File saveLoc = new File(mc.gameDirectory, "saves/"+levelname+"/timer.dat");
		
		if(!saveLoc.exists()) {
			return;
		}
		
		try {
			FileInputStream stream = new FileInputStream(saveLoc);
			ObjectInputStream ois = new ObjectInputStream(stream);
			Object obj = ois.readObject();
			ticks = (Integer) obj;
			ois.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}

	public void stop(MinecraftServer server) {
		save(server);
		ticks = null;
	}
	
}
