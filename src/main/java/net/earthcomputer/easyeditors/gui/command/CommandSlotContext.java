package net.earthcomputer.easyeditors.gui.command;

import javax.annotation.Nullable;

import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
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
public abstract class CommandSlotContext {

	/**
	 * 
	 * @return The world of the command sender. May be null if unknown
	 */
	@Nullable
	public abstract World getWorld();

	/**
	 * 
	 * @return The position of the command sender. May be null if unknown
	 */
	@Nullable
	public abstract BlockPos getPos();

	/**
	 * 
	 * @return The command sender. May be null if unknown
	 */
	@Nullable
	public abstract ICommandSender getSender();

	/**
	 * 
	 * @return The class of the command sender. May be null if unknown
	 */
	@Nullable
	public Class<? extends ICommandSender> getSenderClass() {
		ICommandSender sender = getSender();
		return sender == null ? null : sender.getClass();
	}

	/**
	 * Returns true if the command sender class could be a player
	 * 
	 * @return
	 */
	public boolean isPlayer() {
		Class<? extends ICommandSender> senderClass = getSenderClass();
		if (senderClass == null) {
			return true;
		}
		return EntityPlayer.class.isAssignableFrom(senderClass);
	}

	/**
	 * Returns true if the command sender class could be an entity
	 * 
	 * @return
	 */
	public boolean isEntity() {
		Class<? extends ICommandSender> senderClass = getSenderClass();
		if (senderClass == null) {
			return true;
		}
		return Entity.class.isAssignableFrom(senderClass);
	}

	/**
	 * Returns true if the stored command can hold formatted text (e.g. a
	 * command block text field), false otherwise (e.g. a chat command with
	 * formatting codes is rejected by the server)
	 * 
	 * @return
	 */
	public boolean canHoldFormatting() {
		return true;
	}

	/**
	 * Whether components should react to a mouse click at the given coordinates
	 * in the <code>onMouseClick</code> method
	 * 
	 * @param mouseX
	 * @param mouseY
	 * @return
	 */
	public abstract boolean isMouseInBounds(int mouseX, int mouseY);

	/**
	 * Moves the horizontal scroll bar the minimum amount possible so that the
	 * given x-position is in view
	 * 
	 * @param x
	 *            - The x-position, measured as seen on screen, not the actual
	 *            virtual coordinates
	 */
	public abstract void ensureXInView(int x);

	/**
	 * Moves the vertical scroll bar the minimum amount possible so that the
	 * given y-position is in view
	 * 
	 * @param y
	 *            - The y-position, measured as seen on screen, not the actual
	 *            virtual coordinates
	 */
	public abstract void ensureYInView(int y);

	/**
	 * Called by
	 * {@link net.earthcomputer.easyeditors.gui.command.slot.CommandSlotCommand
	 * CommandSlotCommand} when the command it is reading from has a syntax
	 * error. Normally used to display a GUI notifying the user
	 */
	public abstract void commandSyntaxError();

}
