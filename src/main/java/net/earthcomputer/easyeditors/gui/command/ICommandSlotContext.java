package net.earthcomputer.easyeditors.gui.command;

import net.minecraft.command.ICommandSender;
import net.minecraft.util.BlockPos;
import net.minecraft.world.World;

/**
 * This class serves two purposes: as a (kind of) equivalent to
 * {@link ICommandSender}, and as a polymorphic way of detecting things like
 * whether the command slot is hidden by various foreground components at given
 * coordinates
 * 
 * @author Earthcomputer
 *
 */
public interface ICommandSlotContext {

	/**
	 * 
	 * @return The world of the command sender. May be null if unknown
	 */
	World getWorld();

	/**
	 * 
	 * @return The position of the command sender. May be null if unknown
	 */
	BlockPos getPos();

	/**
	 * 
	 * @return The command sender. May be null if unknown
	 */
	ICommandSender getSender();

	/**
	 * Whether components should react to a mouse click at the given coordinates
	 * in the <code>onMouseClick</code> method
	 * 
	 * @param mouseX
	 * @param mouseY
	 * @return
	 */
	boolean isMouseInBounds(int mouseX, int mouseY);

}
