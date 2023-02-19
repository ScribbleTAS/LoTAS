/**
 * Here is the logic of the savestate mod..
 *
 * The class 'state' represents a savestate. It contains some data such as name, timestamp and more.
 * There is a list of states, which is being synchronized between the client and the server whenever a state action occurs (save load delete)
 *
 * in onClientsidePayload the client reacts to the server, once it sends a packet. The packet contains all states of a server.
 *
 * The client can request and action in requestState().
 */
package com.minecrafttas.lotas.mods;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import java.time.Instant;
import java.util.ArrayList;

import org.apache.commons.io.FileUtils;

import com.minecrafttas.lotas.LoTAS;
import com.minecrafttas.lotas.mods.savestatemod.StateData;
import com.minecrafttas.lotas.mods.savestatemod.StateData.State;
import com.minecrafttas.lotas.system.ModSystem.Mod;

import io.netty.buffer.Unpooled;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundChangeDifficultyPacket;
import net.minecraft.network.protocol.game.ClientboundPlayerAbilitiesPacket;
import net.minecraft.network.protocol.game.ClientboundRespawnPacket;
import net.minecraft.network.protocol.game.ClientboundSetExperiencePacket;
import net.minecraft.network.protocol.game.ClientboundSetHealthPacket;
import net.minecraft.network.protocol.game.ClientboundUpdateMobEffectPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.DistanceManager;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.stats.ServerStatsCounter;
import net.minecraft.util.DirectoryLock;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.storage.RegionFile;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.storage.DerivedLevelData;
import net.minecraft.world.level.storage.LevelData;

// # 1.16.1
//$$import java.util.concurrent.CompletableFuture;
//$$import net.minecraft.Util;
//$$import net.minecraft.commands.Commands;
//$$import net.minecraft.nbt.NbtOps;
//$$import net.minecraft.server.MinecraftServer;
//$$import net.minecraft.server.packs.repository.FolderRepositorySource;
//$$import net.minecraft.server.packs.repository.PackRepository;
//$$import net.minecraft.server.packs.repository.ServerPacksSource;
//$$import net.minecraft.core.RegistryAccess;
//$$import net.minecraft.resources.RegistryReadOps;
//$$import net.minecraft.server.ServerResources;
//$$import net.minecraft.server.packs.repository.Pack;
//$$import net.minecraft.server.packs.repository.PackSource;
//$$import net.minecraft.world.level.biome.BiomeManager;
//$$import net.minecraft.world.level.storage.LevelResource;
//$$import net.minecraft.world.level.DataPackConfig;
//$$import net.minecraft.world.level.storage.LevelStorageSource.LevelStorageAccess;
//$$import net.minecraft.world.level.storage.ServerLevelData;
//$$import net.minecraft.world.level.storage.WorldData;
// # def
//$$import net.minecraft.world.level.storage.LevelStorageSource;
//$$import net.minecraft.server.level.DerivedServerLevel;
//$$import java.nio.file.StandardOpenOption;
//$$import java.nio.file.Files;
// # end

/**
 * Main savestate mod
 * @author Pancake
 */
public class SavestateMod extends Mod {

	public static SavestateMod instance;
	
	/**
	 * Initializes the savestate mod
	 */
	public SavestateMod() {
		super(new ResourceLocation("lotas", "savestatemod"));
		instance = this;
	}

	/**
	 * Mirrored state data
	 */
	private StateData data = new StateData();
	
	/**
	 * Task for next tick
	 */
	private Task task;
	
	/**
	 * Client-Side only state request. Sends a packet to the server contains a save or load int and an index to load
	 * @param state state 0 is save, state 1 is load, state 2 is delete
	 * @param index Index of Load/Deletestate
	 * @param name Name of the Savestate
	 */
	@Environment(EnvType.CLIENT)
	public void requestState(int state, int index, String name) {
		FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
		buf.writeInt(state);
		if (state == 0)
			buf.writeUtf(name);
		else
			buf.writeInt(index);
		this.sendPacketToServer(buf);
	}
	
	/**
	 * Triggers a Load/delete/savestate when a client packet is incoming and then resends the packet to all clients.
	 * @param buf Packet Data
	 */
	@Override
	protected void onServerPayload(FriendlyByteBuf buf) {
		// Prepare task for next tick
		switch (buf.readInt()) {
			case 0:
				String s = buf.readUtf(Short.MAX_VALUE);
				this.task = () -> this.savestate(s);
				break;
			case 1:
				int i = buf.readInt();
				this.task = () -> this.loadstate(i);
				break;
			case 2:
				int j = buf.readInt();
				this.task = () -> this.deletestate(j);
				break;
		}
		
		// Tick the server
		if (TickAdvance.instance.isTickadvanceEnabled())
			TickAdvance.instance.updateTickadvance();
	}

	/**
	 * Savestates/loadstates/deletestates after the tick
	 */
	@Override
	protected void onServerTick() {
		// Run task
		if (this.task != null) {
			try {
				this.data.loadData();
				this.task.run();
			} catch (IOException e) {
				LoTAS.LOGGER.error("State task failed!", e);
			}
			this.task = null;
		}
	}

	/**
	 * Sends all states to the client
	 */
	public void sendStates() {
		byte[] serialized = this.data.serializeData();
		this.mcserver.getPlayerList().getPlayers().forEach(player -> {
			FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
			buf.writeInt(0);
			buf.writeByteArray(serialized);
			this.sendPacketToClient(player, buf);
		});
	}

	/**
	 * Saves a new state of the world
	 * @param name Savestate Name
	 * @throws IOException Filesystem Exception 
	 */
	private void savestate(String name) throws IOException {
		// Save world
		this.mcserver.getPlayerList().saveAll();
		this.mcserver.saveAllChunks(false, true, false);
		
		// Make state
		int latestStateIndex = this.data.getStateCount() - 1;
		int index = latestStateIndex == -1 ? 0 : this.data.getState(latestStateIndex).getIndex() + 1;
		File stateDir = new File(data.getWorldSavestateDir(), index + "");
		FileUtils.copyDirectory(this.data.getWorldDir(), stateDir);
		this.data.addState(new State(name == null ? "Untitled State" : name, Instant.now().getEpochSecond(), index));
		
		// Save data and send to client
		this.data.saveData();
		this.sendStates();
	}

	/**
	 * Loads a state of the world
	 * @param i Index to load
	 * @throws IOException Filesystem Excepion
	 */
	private void loadstate(int i) throws IOException {
		if (!this.data.isValid(i)) {
			LoTAS.LOGGER.warn("Trying to load a state that does not exist: " + i);
			return;
		}
		
		TickAdvance.instance.updateTickadvanceStatus(true);
		
		/*
		 * Fully unload server level
		 */
		
		for (ServerLevel level : this.mcserver.getAllLevels()) {
			ServerChunkCache chunkCache = level.getChunkSource();
			DistanceManager distanceManager = chunkCache.distanceManager;
			ChunkMap map = chunkCache.chunkMap;
			
			// Clear global and future entities
			level.toAddAfterTick.clear();
			// # 1.16.1
//$$			
			// # def
//$$			level.globalEntities.clear();
			// # end
			
			// Despawn existing entities
			for (Entity entity : new ArrayList<>(level.entitiesById.values()))
				if (entity != null)
					level.despawn(entity);

			// Remove chunk loading requests
			distanceManager.tickets.clear();
			distanceManager.chunksToUpdateFutures.clear();
			distanceManager.ticketsToRelease.clear();
			
			// Unload chunks
			map.pendingUnloads.clear();
			map.updatingChunkMap.clear();
			map.visibleChunkMap.clear();
			map.entitiesInLevel.clear();
			map.processUnloads(() -> true);
			
			// # 1.15.2
//$$			
			// # def
//$$			// Unload nether portals
//$$			level.getPortalForcer().cachedPortals.clear();
			// # end
			
			// Clear chunk cache
			chunkCache.clearCache();

			// Close file
			// # 1.15.2
//$$			for (RegionFile file : map.poiManager.worker.storage.regionCache.values())
//$$				file.close();
//$$			map.poiManager.worker.storage.regionCache.clear();
//$$			map.poiManager.worker.pendingWrites.clear();
//$$			
//$$			for (RegionFile file : map.worker.storage.regionCache.values())
//$$				file.close();
//$$			map.worker.storage.regionCache.clear();
//$$			map.worker.pendingWrites.clear();
			// # def
//$$			for (RegionFile file : map.poiManager.regionCache.values())
//$$				file.close();
//$$			map.poiManager.regionCache.clear();
//$$			
//$$			for (RegionFile file : map.regionCache.values())
//$$				file.close();
//$$			map.regionCache.clear();
			// # end
		}
		
		/*
		 * Load state
		 */
		
		
		// Save session.lock
		File worldDir = this.data.getWorldDir();
		// # 1.16.1
//$$		Path levelPath = this.mcserver.storageSource.levelPath;
//$$		this.mcserver.storageSource.lock.close();
		// # def
//$$		Path sessionLockFile = new File(worldDir, "session.lock").toPath();
//$$		byte[] sessionLock = Files.readAllBytes(sessionLockFile);
		// # end
		
		// Delete world
		FileUtils.deleteDirectory(worldDir);

		// Copy state
		File worldSavestateDir = new File(this.data.getWorldSavestateDir(), this.data.getState(i).getIndex() + "");
		FileUtils.copyDirectory(worldSavestateDir, this.data.getWorldDir());

		// Load session.lock
		// # 1.16.1
//$$		this.mcserver.storageSource.lock = DirectoryLock.create(levelPath);
		// # def
//$$ 		Files.write(sessionLockFile, sessionLock, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE);
		// # end
		
		/*
		 * Fully reload world
		 */
		
		// # 1.16.1
//$$		WorldData worldData = loadWorldData(this.mcserver.storageSource);
//$$		ServerLevelData data = worldData.overworldData();
//$$		for (ServerLevel level : this.mcserver.getAllLevels()) {
//$$			if (level.dimension() != Level.OVERWORLD)
//$$				level.levelData = new DerivedLevelData(worldData, data);
//$$			else
//$$				level.levelData = data;
//$$		}
		// # def
//$$		LevelData data = LevelStorageSource.getLevelData(new File(worldDir, "level.dat"), this.mcserver.getFixerUpper());
//$$		for (ServerLevel level : this.mcserver.getAllLevels()) {
//$$			// Load level data
//$$			if (level instanceof DerivedServerLevel)
//$$				level.levelData = new DerivedLevelData(data);
//$$			else
//$$				level.levelData = data;
//$$		}
		// # end
		
		PlayerList playerList = this.mcserver.getPlayerList();
		for (ServerPlayer player : new ArrayList<>(playerList.getPlayers())) {
			
			// Load player data
			Level oldLevel = player.level;
			playerList.load(player);
			ServerLevel newLevel = (ServerLevel) player.level;
			
	        // Update client pre-level
	        LevelData levelData = oldLevel.getLevelData();
	        // # 1.16.1
//$$	        player.connection.send(new ClientboundRespawnPacket(newLevel.dimensionTypeKey(), newLevel.dimension(), BiomeManager.obfuscateSeed(newLevel.getSeed()), player.gameMode.getGameModeForPlayer(), player.gameMode.getPreviousGameModeForPlayer(), newLevel.isDebug(), newLevel.isFlat(), true));        
	        // # 1.15.2
//$$	        player.connection.send(new ClientboundRespawnPacket(player.dimension, LevelData.obfuscateSeed(levelData.getSeed()), levelData.getGeneratorType(), player.gameMode.getGameModeForPlayer()));
	        // # def
//$$	        player.connection.send(new ClientboundRespawnPacket(player.dimension, levelData.getGeneratorType(), player.gameMode.getGameModeForPlayer()));
	        // # end
	        player.connection.send(new ClientboundChangeDifficultyPacket(levelData.getDifficulty(), levelData.isDifficultyLocked()));
	        player.server.getPlayerList().sendPlayerPermissionLevel(player);
	        
	        // Add player to level
	        Vec3 pos = player.position();
	        player.moveTo(pos.x(), pos.y(), pos.z(), player.yRot, player.xRot);
	        player.setLevel(newLevel);
	        newLevel.addDuringPortalTeleport(player);
	        
	        // Update client level
	        player.connection.teleport(pos.x(), pos.y(), pos.z(), player.yRot, player.xRot);
	        player.gameMode.setLevel(newLevel);
	        player.connection.send(new ClientboundPlayerAbilitiesPacket(player.abilities));
	        player.server.getPlayerList().sendLevelInfo(player, newLevel);
	        player.server.getPlayerList().sendAllPlayerInfo(player);
	        player.connection.send(new ClientboundSetHealthPacket(player.getHealth(), player.getFoodData().getFoodLevel(), player.getFoodData().getSaturationLevel()));
	        player.connection.send(new ClientboundSetExperiencePacket(player.experienceProgress, player.totalExperience, player.experienceLevel));
	        for (MobEffectInstance mobEffectInstance : player.getActiveEffects())
	        	player.connection.send(new ClientboundUpdateMobEffectPacket(player.getId(), mobEffectInstance));
		
	        // Update player advancements
			PlayerAdvancements adv = player.getAdvancements();
			// # 1.16.1
//$$			adv.reload(this.mcserver.getAdvancements());
			// # def
//$$			adv.reload();
			// # end
            adv.flushDirty(player);
            
            // Update player stats
            playerList.stats.remove(player.getUUID());
            ServerStatsCounter stats = playerList.getPlayerStats(player);
            player.stats = stats;
            stats.sendStats(player);
            
            // Update dupe mod data
    		FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
    		buf.writeBoolean(true);
            DupeMod.instance.onServerPayload(buf);
            
            // Run quality of life stuff
            player.getCooldowns().cooldowns.clear();
            
            // Run quality of life stuff on the client
    		FriendlyByteBuf buf2 = new FriendlyByteBuf(Unpooled.buffer());
    		buf2.writeInt(-1);
            this.sendPacketToClient(player, buf2);
		}
		
		TickAdvance.instance.updateTickadvanceStatus(false);
	}
	
	// # 1.16.1
//$$	private WorldData loadWorldData(LevelStorageAccess levelStorageAccess) {
//$$        ServerResources serverResources;
//$$		DataPackConfig dataPackConfig = levelStorageAccess.getDataPacks();
//$$        PackRepository<Pack> packRepository = new PackRepository<Pack>(Pack::new, new ServerPacksSource(), new FolderRepositorySource(levelStorageAccess.getLevelPath(LevelResource.DATAPACK_DIR).toFile(), PackSource.WORLD));
//$$        DataPackConfig dataPackConfig2 = MinecraftServer.configurePackRepository(packRepository, dataPackConfig == null ? DataPackConfig.DEFAULT : dataPackConfig, false);
//$$        CompletableFuture<ServerResources> completableFuture = ServerResources.loadResources(packRepository.openAllSelected(), Commands.CommandSelection.DEDICATED, 2, Util.backgroundExecutor(), Runnable::run);
//$$        try {
//$$            serverResources = completableFuture.get();
//$$        }
//$$        catch (Exception exception) {
//$$            packRepository.close();
//$$            return null;
//$$        }
//$$        serverResources.updateGlobals();
//$$        RegistryAccess.RegistryHolder exception = RegistryAccess.builtin();
//$$        RegistryReadOps<net.minecraft.nbt.Tag> registryReadOps = RegistryReadOps.create(NbtOps.INSTANCE, serverResources.getResourceManager(), exception);
//$$        WorldData worldData = levelStorageAccess.getDataTag(registryReadOps, dataPackConfig2);
//$$        return worldData;
//$$	}
	// # end
	
	/**
	 * Deletes a state of the world
	 * @param i Index to delete
	 * @throws IOException Filesystem Exception
	 */
	private void deletestate(int i) throws IOException {
		if (!this.data.isValid(i)) {
			LoTAS.LOGGER.warn("Trying to delete a state that does not exist: " + i);
			return;
		}
		
		// Delete State
		FileUtils.deleteDirectory(new File(this.data.getWorldSavestateDir(), this.data.getState(i).getIndex() + ""));
		this.data.removeState(i);

		// Save data and send to client
		this.data.saveData();
		this.sendStates();
	}
	
	
	/**
	 * Updates the state list on incoming packet
	 * @param buf Packet Data
	 */
	@Override
	@Environment(EnvType.CLIENT)
	protected void onClientsidePayload(FriendlyByteBuf buf) {
		switch (buf.readInt()) {
			case -1: // Update after loadstate
				this.mc.gui.getChat().clearMessages(false);
				this.mc.getToasts().clear();
				this.mc.particleEngine.particles.clear();
				this.mc.player.getCooldowns().cooldowns.clear();
				this.mc.player.attackStrengthTicker = 100;
				this.mc.player.lastItemInMainHand = this.mc.player.getItemBySlot(EquipmentSlot.MAINHAND);
				break;
			case 1: // Load states
				this.data.deserializeData(buf.readByteArray());
				break;
		}
	}
	
	/**
	 * Updates client data on connect
	 */
	public void onConnect(ServerPlayer c) {
		try {
			this.data.loadData();
			this.sendStates();
		} catch (IOException e) {
			LoTAS.LOGGER.warn("Unable to send states to client.", e);
		}
	}
	
	/**
	 * Load save data on initialize
	 */
	@Override
	protected void onServerLoad() {
		this.data.onServerInitialize(this.mcserver);
	}
	
	public interface Task {
		public void run() throws IOException;
	}
}
