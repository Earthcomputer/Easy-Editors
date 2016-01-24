package net.earthcomputer.easyeditors.gui.command;

import net.minecraft.block.state.IBlockState;

/**
 * An interface for use with the block selector. Once the user has selected a
 * block, setBlock will be invoked
 * 
 * @author Earthcomputer
 *
 */
public interface IBlockSelectorCallback {

	/**
	 * 
	 * @return The currently selected block state
	 */
	IBlockState getBlock();

	/**
	 * Sets the currently selected block state
	 * 
	 * @param block
	 */
	void setBlock(IBlockState block);

}
