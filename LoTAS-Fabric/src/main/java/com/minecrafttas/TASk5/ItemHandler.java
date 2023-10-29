package com.minecrafttas.TASk5;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.io.FileUtils;

import com.minecrafttas.TASk5.mixin.AccessorLevelStorage;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class ItemHandler {

	
	Set<Item> itemList = new HashSet<>();
	
	public boolean lock;
	
	private MinecraftServer server;
	
	public ItemHandler(MinecraftServer server) {
		this.server = server;
		load();
	}
	
	public void onItemAdd(int i, ItemStack stack) {
		if(stack.getItem() == Items.AIR) {
			return;
		}
		if(itemList.add(stack.getItem()) && !lock) {
			Minecraft mc = Minecraft.getInstance();
			mc.gui.getChat().addMessage(new TextComponent(ChatFormatting.GREEN+stack.getHoverName().getString()));
			TASk5.LOGGER.info("Added item {}", stack.getHoverName().getString());
			save();
		}
	}
	
	public void setById(Set<Integer> idSet) {
		idSet.forEach(id->{
			itemList.add(Item.byId(id));
		});
	}

	public void onRender(PoseStack poseStack, int width, int height) {
//		Minecraft mc = Minecraft.getInstance();
//		Gui.drawCenteredString(poseStack, mc.font, ""+itemList.size(), width/2, height-55, ChatFormatting.GOLD.getColor());
	}
	
	public void save() {
		Minecraft mc = Minecraft.getInstance();
		String levelname = ((AccessorLevelStorage)server).getStorageSource().getLevelId();
		File saveLoc = new File(mc.gameDirectory, "saves/"+levelname+"/items.dat");
		File saveLoc2 = new File(mc.gameDirectory, "saves/"+levelname+"/items.txt");
		try {
			FileOutputStream stream = new FileOutputStream(saveLoc);
			ObjectOutputStream oos = new ObjectOutputStream(stream);
			oos.writeObject(getIds());
			oos.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		try {
			FileUtils.writeStringToFile(saveLoc2, getItemsNames(), StandardCharsets.UTF_8);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	private Set<Integer> getIds(){
		Set<Integer> out = new HashSet<>();
		itemList.forEach(item -> {
			out.add(Item.getId(item));
		});
		return out;
	}
	
	public String getItemsNames() {
		Set<Integer> ids = getIds();
		String out ="";
		for (Integer id : ids) {
			out += Item.byId(id).toString()+"\n";
		}
		return out;
	}
	
	@SuppressWarnings("unchecked")
	public void load() {
		Minecraft mc = Minecraft.getInstance();
		String levelname = ((AccessorLevelStorage)server).getStorageSource().getLevelId();
		File saveLoc = new File(mc.gameDirectory, "saves/"+levelname+"/items.dat");
		
		if(!saveLoc.exists()) {
			return;
		}
		
		try {
			FileInputStream stream = new FileInputStream(saveLoc);
			ObjectInputStream ois = new ObjectInputStream(stream);
			Object obj = ois.readObject();
			Set<Integer> idSet = (Set<Integer>) obj;
			setById(idSet);
			ois.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}

	public void onTimerRanOut() {
		Minecraft mc = Minecraft.getInstance();
		mc.gui.getChat().addMessage(new TextComponent(ChatFormatting.RED+"Collected "+ChatFormatting.YELLOW+itemList.size()+ChatFormatting.RED+" items!"));
	}
	
	public int size() {
		return itemList.size();
	}
}
