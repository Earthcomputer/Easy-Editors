package net.earthcomputer.easyeditors.gui.command;

import net.minecraft.block.state.IBlockState;

public interface IBlockSelectorCallback {

	IBlockState getBlock();
	
	void setBlock(IBlockState block);
	
}
