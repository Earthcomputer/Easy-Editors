package net.earthcomputer.easyeditors.api.util;

import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Biomes;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.Biome;

class DummyBlockAccess implements IBlockAccess {

	private IBlockState blockAtCenter;
	private BlockPos blockPos;

	public DummyBlockAccess(IBlockState blockAtCenter) {
		this(blockAtCenter, BlockPos.ORIGIN);
	}

	public DummyBlockAccess(IBlockState blockAtCenter, BlockPos blockPos) {
		this.blockAtCenter = blockAtCenter;
		this.blockPos = blockPos;
	}

	@Override
	public TileEntity getTileEntity(BlockPos pos) {
		return null;
	}

	@Override
	public int getCombinedLight(BlockPos pos, int lightValue) {
		return (15 << 20) | (15 << 4);
	}

	@Override
	public IBlockState getBlockState(BlockPos pos) {
		return pos.equals(blockPos) ? blockAtCenter : Blocks.AIR.getDefaultState();
	}

	@Override
	public boolean isAirBlock(BlockPos pos) {
		return pos.equals(blockPos) ? blockAtCenter.getBlock().isAir(blockAtCenter, this, pos) : true;
	}

	@Override
	public Biome getBiome(BlockPos pos) {
		return Biomes.PLAINS;
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
	public boolean isSideSolid(BlockPos pos, EnumFacing side, boolean _default) {
		IBlockState state = getBlockState(pos);
		return state.getBlock().isSideSolid(state, this, pos, side);
	}

}
