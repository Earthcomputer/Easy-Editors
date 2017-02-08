package net.earthcomputer.easyeditors.gui.command;

import net.minecraft.command.ICommandSender;
import net.minecraft.util.math.BlockPos;
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

	/**
	 * Moves the horizontal scroll bar the minimum amount possible so that the
	 * given x-position is in view
	 * 
	 * @param x
	 *            - The x-position, measured as seen on screen, not the actual
	 *            virtual coordinates
	 */
	void ensureXInView(int x);

	/**
	 * Moves the vertical scroll bar the minimum amount possible so that the
	 * given y-position is in view
	 * 
	 * @param y
	 *            - The y-position, measured as seen on screen, not the actual
	 *            virtual coordinates
	 */
	void ensureYInView(int y);

}
