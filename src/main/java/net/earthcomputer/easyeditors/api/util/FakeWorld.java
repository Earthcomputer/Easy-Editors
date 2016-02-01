package net.earthcomputer.easyeditors.api.util;

import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableSetMultimap;
import com.google.common.collect.Lists;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.IProgressUpdate;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.IWorldAccess;
import net.minecraft.world.MinecraftException;
import net.minecraft.world.NextTickListEntry;
import net.minecraft.world.World;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.WorldSavedData;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.biome.BiomeGenBase.SpawnListEntry;
import net.minecraft.world.border.WorldBorder;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.chunk.storage.IChunkLoader;
import net.minecraft.world.gen.structure.StructureBoundingBox;
import net.minecraft.world.storage.IPlayerFileData;
import net.minecraft.world.storage.ISaveHandler;
import net.minecraft.world.storage.WorldInfo;
import net.minecraftforge.common.ForgeChunkManager.Ticket;

public class FakeWorld extends World {
	
	public FakeWorld() {
		super(new FakeSaveHandler(), new WorldInfo(new NBTTagCompound()), new FakeWorldProvider(),
				Minecraft.getMinecraft().mcProfiler, true);
		this.getWorldInfo().setDifficulty(EnumDifficulty.HARD);
	}

	@Override
	public BiomeGenBase getBiomeGenForCoords(BlockPos pos) {
		return BiomeGenBase.plains;
	}

	@Override
	public BiomeGenBase getBiomeGenForCoordsBody(BlockPos pos) {
		return BiomeGenBase.plains;
	}

	@Override
	protected IChunkProvider createChunkProvider() {
		return new FakeChunkProvider();
	}

	@Override
	public Block getGroundAboveSeaLevel(BlockPos pos) {
		return Blocks.grass;
	}

	@Override
	public boolean isAirBlock(BlockPos pos) {
		return pos.getY() > 63;
	}

	@Override
	public boolean isBlockLoaded(BlockPos pos) {
		return false;
	}

	@Override
	public boolean isBlockLoaded(BlockPos pos, boolean allowEmpty) {
		return false;
	}

	@Override
	public boolean isAreaLoaded(BlockPos center, int radius) {
		return false;
	}

	@Override
	public boolean isAreaLoaded(BlockPos center, int radius, boolean allowEmpty) {
		return false;
	}

	@Override
	public boolean isAreaLoaded(BlockPos from, BlockPos to) {
		return false;
	}

	@Override
	public boolean isAreaLoaded(BlockPos from, BlockPos to, boolean allowEmpty) {
		return false;
	}

	@Override
	public boolean isAreaLoaded(StructureBoundingBox box) {
		return false;
	}

	@Override
	public boolean isAreaLoaded(StructureBoundingBox box, boolean allowEmpty) {
		return false;
	}

	@Override
	protected boolean isChunkLoaded(int x, int z, boolean allowEmpty) {
		return false;
	}

	@Override
	public Chunk getChunkFromBlockCoords(BlockPos pos) {
		return null;
	}

	@Override
	public Chunk getChunkFromChunkCoords(int chunkX, int chunkZ) {
		return null;
	}

	@Override
	public boolean setBlockState(BlockPos pos, IBlockState newState, int flags) {
		return false;
	}

	@Override
	public void markAndNotifyBlock(BlockPos pos, Chunk chunk, IBlockState old, IBlockState new_, int flags) {
	}

	@Override
	public boolean setBlockToAir(BlockPos pos) {
		return false;
	}

	@Override
	public boolean destroyBlock(BlockPos pos, boolean dropBlock) {
		return false;
	}

	@Override
	public boolean setBlockState(BlockPos pos, IBlockState state) {
		return false;
	}

	@Override
	public void markBlockForUpdate(BlockPos pos) {
	}

	@Override
	public void notifyNeighborsRespectDebug(BlockPos pos, Block blockType) {
	}

	@Override
	public void markBlocksDirtyVertical(int x1, int z1, int x2, int z2) {
	}

	@Override
	public void markBlockRangeForRenderUpdate(BlockPos rangeMin, BlockPos rangeMax) {
	}

	@Override
	public void markBlockRangeForRenderUpdate(int x1, int y1, int z1, int x2, int y2, int z2) {
	}

	@Override
	public void notifyNeighborsOfStateChange(BlockPos pos, Block blockType) {
	}

	@Override
	public void notifyNeighborsOfStateExcept(BlockPos pos, Block blockType, EnumFacing skipSide) {
	}

	@Override
	public void notifyBlockOfStateChange(BlockPos pos, Block blockIn) {
	}

	@Override
	public boolean isBlockTickPending(BlockPos pos, Block blockType) {
		return false;
	}

	@Override
	public boolean canSeeSky(BlockPos pos) {
		return pos.getY() >= 63;
	}

	@Override
	public boolean canBlockSeeSky(BlockPos pos) {
		return pos.getY() >= 63;
	}

	@Override
	public int getLight(BlockPos pos) {
		return 15;
	}

	@Override
	public int getLightFromNeighbors(BlockPos pos) {
		return 15;
	}

	@Override
	public int getLight(BlockPos pos, boolean checkNeighbors) {
		return 15;
	}

	@Override
	public BlockPos getHeight(BlockPos pos) {
		return new BlockPos(pos.getX() & 15, 63, pos.getZ() & 15);
	}

	@Override
	public int getChunksLowestHorizon(int x, int z) {
		return 63;
	}

	@Override
	public int getLightFor(EnumSkyBlock type, BlockPos pos) {
		return 15;
	}

	@Override
	public int getLightFromNeighborsFor(EnumSkyBlock type, BlockPos pos) {
		return 15;
	}

	@Override
	public void setLightFor(EnumSkyBlock type, BlockPos pos, int lightValue) {
	}

	@Override
	public void notifyLightSet(BlockPos pos) {
	}

	@Override
	public int getCombinedLight(BlockPos pos, int lightValue) {
		return 15 << 20 | 15 << 4;
	}

	@Override
	public float getLightBrightness(BlockPos pos) {
		return 1;
	}

	@Override
	public IBlockState getBlockState(BlockPos pos) {
		int y = pos.getY();
		return y == 0 ? Blocks.bedrock.getDefaultState()
				: (y < 63 ? Blocks.dirt.getDefaultState()
						: (y == 63 ? Blocks.grass.getDefaultState() : Blocks.air.getDefaultState()));
	}

	@Override
	public boolean isDaytime() {
		return true;
	}

	@Override
	public MovingObjectPosition rayTraceBlocks(Vec3 p_72933_1_, Vec3 p_72933_2_) {
		return null;
	}

	@Override
	public MovingObjectPosition rayTraceBlocks(Vec3 start, Vec3 end, boolean stopOnLiquid) {
		return null;
	}

	@Override
	public MovingObjectPosition rayTraceBlocks(Vec3 vec31, Vec3 vec32, boolean stopOnLiquid,
			boolean ignoreBlockWithoutBoundingBox, boolean returnLastUncollidableBlock) {
		return null;
	}

	@Override
	public void playSoundAtEntity(Entity entityIn, String name, float volume, float pitch) {
	}

	@Override
	public void playSoundToNearExcept(EntityPlayer player, String name, float volume, float pitch) {
	}

	@Override
	public void playSoundEffect(double x, double y, double z, String soundName, float volume, float pitch) {
	}

	@Override
	public void playSound(double x, double y, double z, String soundName, float volume, float pitch,
			boolean distanceDelay) {
	}

	@Override
	public void playRecord(BlockPos pos, String name) {
	}

	@Override
	public void spawnParticle(EnumParticleTypes particleType, double xCoord, double yCoord, double zCoord,
			double xOffset, double yOffset, double zOffset, int... p_175688_14_) {
	}

	@Override
	public void spawnParticle(EnumParticleTypes particleType, boolean p_175682_2_, double xCoord, double yCoord,
			double zCoord, double xOffset, double yOffset, double zOffset, int... p_175682_15_) {
	}

	@Override
	public boolean addWeatherEffect(Entity entityIn) {
		return false;
	}

	@Override
	public boolean spawnEntityInWorld(Entity entityIn) {
		return false;
	}

	@Override
	public void onEntityAdded(Entity entityIn) {
	}

	@Override
	public void onEntityRemoved(Entity entityIn) {
	}

	@Override
	public void removeEntity(Entity entityIn) {
	}

	@Override
	public void removePlayerEntityDangerously(Entity entityIn) {
	}

	@Override
	public void addWorldAccess(IWorldAccess worldAccess) {
	}

	@Override
	public List<AxisAlignedBB> getCollidingBoundingBoxes(Entity entityIn, AxisAlignedBB bb) {
		return Lists.newArrayList();
	}

	@Override
	public boolean isInsideBorder(WorldBorder worldBorderIn, Entity entityIn) {
		return true;
	}

	@Override
	public List<AxisAlignedBB> func_147461_a(AxisAlignedBB bb) {
		return Lists.newArrayList();
	}

	@Override
	public int calculateSkylightSubtracted(float p_72967_1_) {
		return 0;
	}

	@Override
	public float getSunBrightnessFactor(float p_72967_1_) {
		return 1;
	}

	@Override
	public void removeWorldAccess(IWorldAccess worldAccess) {
	}

	@Override
	public float getSunBrightness(float p_72971_1_) {
		return 1;
	}

	@Override
	public float getSunBrightnessBody(float p_72971_1_) {
		return 1;
	}

	@Override
	public Vec3 getSkyColor(Entity entityIn, float partialTicks) {
		return getSkyColorBody(entityIn, partialTicks);
	}

	@Override
	public Vec3 getSkyColorBody(Entity entityIn, float partialTicks) {
		// TODO Auto-generated method stub
		return super.getSkyColorBody(entityIn, partialTicks);
	}

	@Override
	public float getCelestialAngle(float partialTicks) {
		// TODO: Is 0 directly upwards?
		return 0;
	}

	@Override
	public int getMoonPhase() {
		return 0;
	}

	@Override
	public float getCurrentMoonPhaseFactor() {
		return 1;
	}

	@Override
	public float getCurrentMoonPhaseFactorBody() {
		return 1;
	}

	@Override
	public float getCelestialAngleRadians(float partialTicks) {
		// TODO: Is 0 directly upwards?
		return 0;
	}

	@Override
	public Vec3 getCloudColour(float partialTicks) {
		return new Vec3(1, 1, 1);
	}

	@Override
	public Vec3 getFogColor(float partialTicks) {
		return new Vec3(1, 1, 1);
	}

	@Override
	public BlockPos getPrecipitationHeight(BlockPos pos) {
		return new BlockPos(pos.getX(), 64, pos.getZ());
	}

	@Override
	public BlockPos getTopSolidOrLiquidBlock(BlockPos pos) {
		return new BlockPos(pos.getX(), 63, pos.getZ());
	}

	@Override
	public float getStarBrightness(float partialTicks) {
		return 0;
	}

	@Override
	public float getStarBrightnessBody(float partialTicks) {
		return 0;
	}

	@Override
	public void scheduleUpdate(BlockPos pos, Block blockIn, int delay) {
	}

	@Override
	public void updateBlockTick(BlockPos pos, Block blockIn, int delay, int priority) {
	}

	@Override
	public void scheduleBlockUpdate(BlockPos pos, Block blockIn, int delay, int priority) {
	}

	@Override
	public void updateEntities() {
	}

	@Override
	public boolean addTileEntity(TileEntity tile) {
		return false;
	}

	@Override
	public void addTileEntities(Collection<TileEntity> tileEntityCollection) {
	}

	@Override
	public void updateEntity(Entity ent) {
	}

	@Override
	public void updateEntityWithOptionalForce(Entity entityIn, boolean forceUpdate) {
	}

	@Override
	public boolean checkNoEntityCollision(AxisAlignedBB bb) {
		return true;
	}

	@Override
	public boolean checkNoEntityCollision(AxisAlignedBB bb, Entity entityIn) {
		return true;
	}

	@Override
	public boolean checkBlockCollision(AxisAlignedBB bb) {
		return false;
	}

	@Override
	public boolean isAnyLiquid(AxisAlignedBB bb) {
		return false;
	}

	@Override
	public boolean isFlammableWithin(AxisAlignedBB bb) {
		return false;
	}

	@Override
	public boolean handleMaterialAcceleration(AxisAlignedBB bb, Material materialIn, Entity entityIn) {
		return false;
	}

	@Override
	public boolean isMaterialInBB(AxisAlignedBB bb, Material materialIn) {
		return false;
	}

	@Override
	public boolean isAABBInMaterial(AxisAlignedBB bb, Material materialIn) {
		return false;
	}

	@Override
	public float getBlockDensity(Vec3 vec, AxisAlignedBB bb) {
		return 0;
	}

	@Override
	public boolean extinguishFire(EntityPlayer player, BlockPos pos, EnumFacing side) {
		return false;
	}

	@Override
	public String getDebugLoadedEntities() {
		return "No entities in this world - it's fake!";
	}

	@Override
	public String getProviderName() {
		return "Fake worlds don't need to be provided by anything";
	}

	@Override
	public TileEntity getTileEntity(BlockPos pos) {
		return null;
	}

	@Override
	public void setTileEntity(BlockPos pos, TileEntity tileEntityIn) {
	}

	@Override
	public void removeTileEntity(BlockPos pos) {
	}

	@Override
	public void markTileEntityForRemoval(TileEntity tileEntityIn) {
	}

	@Override
	public boolean isBlockFullCube(BlockPos pos) {
		return pos.getY() <= 63;
	}

	@Override
	public boolean isBlockNormalCube(BlockPos pos, boolean _default) {
		return pos.getY() <= 63;
	}

	@Override
	public void calculateInitialSkylight() {
	}

	@Override
	public void setAllowedSpawnTypes(boolean hostile, boolean peaceful) {
	}

	@Override
	public void tick() {
	}

	@Override
	protected void calculateInitialWeather() {
	}

	@Override
	public void calculateInitialWeatherBody() {
	}

	@Override
	protected void updateWeather() {
	}

	@Override
	public void updateWeatherBody() {
	}

	@Override
	protected int getRenderDistanceChunks() {
		return 2;
	}

	@Override
	protected void playMoodSoundAndCheckLight(int p_147467_1_, int p_147467_2_, Chunk chunkIn) {
	}

	@Override
	protected void updateBlocks() {
	}

	@Override
	public void forceBlockUpdateTick(Block blockType, BlockPos pos, Random random) {
	}

	@Override
	public boolean canBlockFreezeWater(BlockPos pos) {
		return false;
	}

	@Override
	public boolean canBlockFreezeNoWater(BlockPos pos) {
		return false;
	}

	@Override
	public boolean canBlockFreeze(BlockPos pos, boolean noWaterAdj) {
		return false;
	}

	@Override
	public boolean canBlockFreezeBody(BlockPos pos, boolean noWaterAdj) {
		return false;
	}

	@Override
	public boolean canSnowAt(BlockPos pos, boolean checkLight) {
		return false;
	}

	@Override
	public boolean canSnowAtBody(BlockPos pos, boolean checkLight) {
		return false;
	}

	@Override
	public boolean checkLight(BlockPos pos) {
		return false;
	}

	@Override
	public boolean checkLightFor(EnumSkyBlock lightType, BlockPos pos) {
		return false;
	}

	@Override
	public boolean tickUpdates(boolean p_72955_1_) {
		return false;
	}

	@Override
	public List<NextTickListEntry> getPendingBlockUpdates(Chunk chunkIn, boolean p_72920_2_) {
		return Lists.newArrayList();
	}

	@Override
	public List<NextTickListEntry> func_175712_a(StructureBoundingBox structureBB, boolean p_175712_2_) {
		return Lists.newArrayList();
	}

	@Override
	public List<Entity> getEntitiesWithinAABBExcludingEntity(Entity entityIn, AxisAlignedBB bb) {
		return Lists.newArrayList();
	}

	@Override
	public List<Entity> getEntitiesInAABBexcluding(Entity entityIn, AxisAlignedBB boundingBox,
			Predicate<? super Entity> predicate) {
		return Lists.newArrayList();
	}

	@Override
	public <T extends Entity> List<T> getEntities(Class<? extends T> entityType, Predicate<? super T> filter) {
		return Lists.newArrayList();
	}

	@Override
	public <T extends Entity> List<T> getPlayers(Class<? extends T> playerType, Predicate<? super T> filter) {
		return Lists.newArrayList();
	}

	@Override
	public <T extends Entity> List<T> getEntitiesWithinAABB(Class<? extends T> classEntity, AxisAlignedBB bb) {
		return Lists.newArrayList();
	}

	@Override
	public <T extends Entity> List<T> getEntitiesWithinAABB(Class<? extends T> clazz, AxisAlignedBB aabb,
			Predicate<? super T> filter) {
		return Lists.newArrayList();
	}

	@Override
	public <T extends Entity> T findNearestEntityWithinAABB(Class<? extends T> entityType, AxisAlignedBB aabb,
			T closestTo) {
		return null;
	}

	@Override
	public Entity getEntityByID(int id) {
		return null;
	}

	@Override
	public List<Entity> getLoadedEntityList() {
		return Lists.newArrayList();
	}

	@Override
	public void markChunkDirty(BlockPos pos, TileEntity unusedTileEntity) {
	}

	@Override
	public int countEntities(Class<?> entityType) {
		return 0;
	}

	@Override
	public void loadEntities(Collection<Entity> entityCollection) {
	}

	@Override
	public void unloadEntities(Collection<Entity> entityCollection) {
	}

	@Override
	public boolean canBlockBePlaced(Block blockIn, BlockPos pos, boolean p_175716_3_, EnumFacing side, Entity entityIn,
			ItemStack itemStackIn) {
		return false;
	}

	@Override
	public int getSeaLevel() {
		return 63;
	}

	@Override
	public void setSeaLevel(int p_181544_1_) {
	}

	@Override
	public int getStrongPower(BlockPos pos, EnumFacing direction) {
		return 0;
	}

	@Override
	public WorldType getWorldType() {
		return WorldType.FLAT;
	}

	@Override
	public int getStrongPower(BlockPos pos) {
		return 0;
	}

	@Override
	public boolean isSidePowered(BlockPos pos, EnumFacing side) {
		return false;
	}

	@Override
	public int getRedstonePower(BlockPos pos, EnumFacing facing) {
		return 0;
	}

	@Override
	public boolean isBlockPowered(BlockPos pos) {
		return false;
	}

	@Override
	public int isBlockIndirectlyGettingPowered(BlockPos pos) {
		return 0;
	}

	@Override
	public EntityPlayer getClosestPlayerToEntity(Entity entityIn, double distance) {
		return null;
	}

	@Override
	public EntityPlayer getClosestPlayer(double x, double y, double z, double distance) {
		return null;
	}

	@Override
	public boolean isAnyPlayerWithinRangeAt(double x, double y, double z, double range) {
		return false;
	}

	@Override
	public EntityPlayer getPlayerEntityByName(String name) {
		return null;
	}

	@Override
	public EntityPlayer getPlayerEntityByUUID(UUID uuid) {
		return null;
	}

	@Override
	public void checkSessionLock() throws MinecraftException {
	}

	@Override
	public void setTotalWorldTime(long worldTime) {
	}

	@Override
	public long getSeed() {
		return 0;
	}

	@Override
	public long getTotalWorldTime() {
		return 0;
	}

	@Override
	public long getWorldTime() {
		return 0;
	}

	@Override
	public void setWorldTime(long time) {
	}

	@Override
	public BlockPos getSpawnPoint() {
		return new BlockPos(8, 64, 8);
	}

	@Override
	public void setSpawnPoint(BlockPos pos) {
	}

	@Override
	public void joinEntityInSurroundings(Entity entityIn) {
	}

	@Override
	public boolean isBlockModifiable(EntityPlayer player, BlockPos pos) {
		return false;
	}

	@Override
	public boolean canMineBlockBody(EntityPlayer player, BlockPos pos) {
		return false;
	}

	@Override
	public void setEntityState(Entity entityIn, byte state) {
	}

	@Override
	public void addBlockEvent(BlockPos pos, Block blockIn, int eventID, int eventParam) {
	}

	@Override
	public void updateAllPlayersSleepingFlag() {
	}

	@Override
	public float getThunderStrength(float delta) {
		return 0;
	}

	@Override
	public void setThunderStrength(float strength) {
	}

	@Override
	public float getRainStrength(float delta) {
		return 0;
	}

	@Override
	public void setRainStrength(float strength) {
	}

	@Override
	public boolean isThundering() {
		return false;
	}

	@Override
	public boolean isRaining() {
		return false;
	}

	@Override
	public boolean canLightningStrike(BlockPos strikePosition) {
		return false;
	}

	@Override
	public boolean isBlockinHighHumidity(BlockPos pos) {
		return false;
	}

	@Override
	public void setItemData(String dataID, WorldSavedData worldSavedDataIn) {
	}

	@Override
	public void playBroadcastSound(int p_175669_1_, BlockPos pos, int p_175669_3_) {
	}

	@Override
	public void playAuxSFX(int p_175718_1_, BlockPos pos, int p_175718_3_) {
	}

	@Override
	public void playAuxSFXAtEntity(EntityPlayer player, int sfxType, BlockPos pos, int p_180498_4_) {
	}

	@Override
	public BlockPos getStrongholdPos(String name, BlockPos pos) {
		return new BlockPos(0, 0, 0);
	}

	@Override
	public double getHorizon() {
		return 0;
	}

	@Override
	public void sendBlockBreakProgress(int breakerId, BlockPos pos, int progress) {
	}

	@Override
	public void makeFireworks(double x, double y, double z, double motionX, double motionY, double motionZ,
			NBTTagCompound compund) {
	}

	@Override
	public void updateComparatorOutputLevel(BlockPos pos, Block blockIn) {
	}

	@Override
	public EnumDifficulty getDifficulty() {
		return EnumDifficulty.HARD;
	}

	@Override
	public int getLastLightningBolt() {
		return 0;
	}

	@Override
	public void setLastLightningBolt(int lastLightningBoltIn) {
	}

	@Override
	public boolean isFindingSpawnPoint() {
		return false;
	}

	@Override
	public boolean isSpawnChunk(int x, int z) {
		return false;
	}

	@Override
	public boolean isSideSolid(BlockPos pos, EnumFacing side) {
		return pos.getY() <= 63;
	}

	@Override
	public boolean isSideSolid(BlockPos pos, EnumFacing side, boolean _default) {
		return pos.getY() <= 63;
	}

	@Override
	public ImmutableSetMultimap<ChunkCoordIntPair, Ticket> getPersistentChunks() {
		return ImmutableSetMultimap.of();
	}

	@Override
	public int getBlockLightOpacity(BlockPos pos) {
		return 15;
	}

	@Override
	public int countEntities(EnumCreatureType type, boolean forSpawnCount) {
		return 0;
	}

	protected static class FakeWorldProvider extends WorldProvider {
		protected FakeWorldProvider() {
		}

		@Override
		public IChunkProvider createChunkGenerator() {
			return new FakeChunkProvider();
		}

		@Override
		public boolean canCoordinateBeSpawn(int x, int z) {
			return true;
		}

		@Override
		public float calculateCelestialAngle(long p_76563_1_, float p_76563_3_) {
			return worldObj.getCelestialAngle(p_76563_3_);
		}

		@Override
		public int getMoonPhase(long p_76559_1_) {
			return worldObj.getMoonPhase();
		}

		@Override
		public boolean isSurfaceWorld() {
			return true;
		}

		@Override
		public Vec3 getFogColor(float p_76562_1_, float p_76562_2_) {
			return worldObj.getFogColor(p_76562_2_);
		}

		@Override
		public boolean canRespawnHere() {
			return true;
		}

		@Override
		public float getCloudHeight() {
			return 128;
		}

		@Override
		public boolean isSkyColored() {
			return false;
		}

		@Override
		public BlockPos getSpawnCoordinate() {
			return worldObj.getSpawnPoint();
		}

		@Override
		public int getAverageGroundLevel() {
			return 63;
		}

		@Override
		public double getVoidFogYFactor() {
			return 0;
		}

		@Override
		public boolean doesXZShowFog(int x, int z) {
			return false;
		}

		@Override
		public String getDimensionName() {
			return "Fake dimension which does not exist";
		}

		@Override
		public String getInternalNameSuffix() {
			return "ThisShouldNeverAppearReportToEarthcomputer";
		}

		@Override
		public boolean doesWaterVaporize() {
			return false;
		}

		@Override
		public boolean getHasNoSky() {
			return false;
		}

		@Override
		public int getDimensionId() {
			return 0;
		}

		@Override
		public void setDimension(int dim) {
		}

		@Override
		public String getSaveFolder() {
			return null;
		}

		@Override
		public double getMovementFactor() {
			return 1;
		}

		@Override
		public BlockPos getRandomizedSpawnPoint() {
			return worldObj.getSpawnPoint();
		}

		@Override
		public boolean shouldMapSpin(String entity, double x, double y, double z) {
			return false;
		}

		@Override
		public int getRespawnDimension(EntityPlayerMP player) {
			return 0;
		}

		@Override
		public BiomeGenBase getBiomeGenForCoords(BlockPos pos) {
			return BiomeGenBase.plains;
		}

		@Override
		public boolean isDaytime() {
			return true;
		}

		@Override
		public float getSunBrightnessFactor(float par1) {
			return worldObj.getSunBrightnessFactor(par1);
		}

		@Override
		public float getCurrentMoonPhaseFactor() {
			return worldObj.getCurrentMoonPhaseFactor();
		}

		@Override
		public Vec3 getSkyColor(Entity cameraEntity, float partialTicks) {
			return worldObj.getSkyColor(cameraEntity, partialTicks);
		}

		@Override
		public Vec3 drawClouds(float partialTicks) {
			return worldObj.drawCloudsBody(partialTicks);
		}

		@Override
		public float getSunBrightness(float par1) {
			return worldObj.getSunBrightness(par1);
		}

		@Override
		public float getStarBrightness(float par1) {
			return worldObj.getStarBrightness(par1);
		}

		@Override
		public void setAllowedSpawnTypes(boolean allowHostile, boolean allowPeaceful) {
		}

		@Override
		public void updateWeather() {
		}

		@Override
		public boolean canBlockFreeze(BlockPos pos, boolean byWater) {
			return false;
		}

		@Override
		public boolean canSnowAt(BlockPos pos, boolean checkLight) {
			return false;
		}

		@Override
		public void setWorldTime(long time) {
		}

		@Override
		public long getSeed() {
			return 0;
		}

		@Override
		public long getWorldTime() {
			return 0;
		}

		@Override
		public BlockPos getSpawnPoint() {
			return worldObj.getSpawnPoint();
		}

		@Override
		public void setSpawnPoint(BlockPos pos) {
		}

		@Override
		public boolean canMineBlock(EntityPlayer player, BlockPos pos) {
			return false;
		}

		@Override
		public boolean isBlockHighHumidity(BlockPos pos) {
			return false;
		}

		@Override
		public int getHeight() {
			return 256;
		}

		@Override
		public int getActualHeight() {
			return 256;
		}

		@Override
		public double getHorizon() {
			return 0;
		}

		@Override
		public void resetRainAndThunder() {
		}

		@Override
		public boolean canDoLightning(Chunk chunk) {
			return false;
		}

		@Override
		public boolean canDoRainSnowIce(Chunk chunk) {
			return false;
		}
	}

	protected static class FakeSaveHandler implements ISaveHandler {
		protected FakeSaveHandler() {
		}

		@Override
		public WorldInfo loadWorldInfo() {
			return null;
		}

		@Override
		public void checkSessionLock() throws MinecraftException {
		}

		@Override
		public IChunkLoader getChunkLoader(WorldProvider provider) {
			return null;
		}

		@Override
		public void saveWorldInfoWithPlayer(WorldInfo worldInformation, NBTTagCompound tagCompound) {
		}

		@Override
		public void saveWorldInfo(WorldInfo worldInformation) {
		}

		@Override
		public IPlayerFileData getPlayerNBTManager() {
			return null;
		}

		@Override
		public void flush() {
		}

		@Override
		public File getWorldDirectory() {
			return null;
		}

		@Override
		public File getMapFileFromName(String mapName) {
			return null;
		}

		@Override
		public String getWorldDirectoryName() {
			return null;
		}
	}

	public static class FakeChunkProvider implements IChunkProvider {
		public FakeChunkProvider() {
		}

		@Override
		public boolean chunkExists(int x, int z) {
			return false;
		}

		@Override
		public Chunk provideChunk(int x, int z) {
			return null;
		}

		@Override
		public Chunk provideChunk(BlockPos blockPosIn) {
			return null;
		}

		@Override
		public void populate(IChunkProvider p_73153_1_, int p_73153_2_, int p_73153_3_) {
		}

		@Override
		public boolean func_177460_a(IChunkProvider p_177460_1_, Chunk p_177460_2_, int p_177460_3_, int p_177460_4_) {
			return false;
		}

		@Override
		public boolean saveChunks(boolean p_73151_1_, IProgressUpdate progressCallback) {
			return false;
		}

		@Override
		public boolean unloadQueuedChunks() {
			return false;
		}

		@Override
		public boolean canSave() {
			return false;
		}

		@Override
		public String makeString() {
			return null;
		}

		@Override
		public List<SpawnListEntry> getPossibleCreatures(EnumCreatureType creatureType, BlockPos pos) {
			return null;
		}

		@Override
		public BlockPos getStrongholdGen(World worldIn, String structureName, BlockPos position) {
			return null;
		}

		@Override
		public int getLoadedChunkCount() {
			return 0;
		}

		@Override
		public void recreateStructures(Chunk p_180514_1_, int p_180514_2_, int p_180514_3_) {
		}

		@Override
		public void saveExtraData() {
		}
	}
}
