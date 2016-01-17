package net.earthcomputer.easyeditors.gui.command.slot;

import java.util.List;

import net.earthcomputer.easyeditors.gui.ISizeChangeListener;
import net.earthcomputer.easyeditors.gui.command.CommandSyntaxException;

/**
 * An element of a command syntax. Example implementations of most of these
 * methods can be found in {@link GuiCommandSlotImpl}
 * 
 * @author Earthcomputer
 *
 */
public interface IGuiCommandSlot {

	/**
	 * Adds an {@link ISizeChangeListener} to this command slot
	 * 
	 * @param listener
	 */
	void addSizeChangeListener(ISizeChangeListener listener);

	/**
	 * Removes an {@link ISizeChangeListener} from this command slot
	 * 
	 * @param listener
	 */
	void removeSizeChangeListener(ISizeChangeListener listener);

	/**
	 * Reads this command slot from the given arguments, starting at the given
	 * index
	 * 
	 * @param args
	 * @param index
	 * @return The number of arguments consumed
	 * @throws CommandSyntaxException
	 *             If the command is found to have invalid syntax
	 */
	int readFromArgs(String[] args, int index) throws CommandSyntaxException;

	/**
	 * Adds arguments to args from this command slot
	 * 
	 * @param args
	 */
	void addArgs(List<String> args);

	/**
	 * Draws this command slot
	 * 
	 * @param x
	 * @param y
	 * @param mouseX
	 * @param mouseY
	 * @param partialTicks
	 */
	void draw(int x, int y, int mouseX, int mouseY, float partialTicks);

	/**
	 * 
	 * @return The width of this command slot
	 */
	int getWidth();

	/**
	 * Sets the width of this command slot
	 * 
	 * @param width
	 */
	void setWidth(int width);

	/**
	 * 
	 * @return The height of this command slot
	 */
	int getHeight();

	/**
	 * Sets the height of this command slot
	 * 
	 * @param height
	 */
	void setHeight(int height);

	/**
	 * Called when a key is typed
	 * 
	 * @param typedChar
	 * @param keyCode
	 */
	void onKeyTyped(char typedChar, int keyCode);

	/**
	 * Called when a mouse button is pressed
	 * 
	 * @param mouseX
	 * @param mouseY
	 * @param mouseButton
	 */
	void onMouseClicked(int mouseX, int mouseY, int mouseButton);

	/**
	 * Called when a mouse button is released
	 * 
	 * @param mouseX
	 * @param mouseY
	 * @param mouseButton
	 */
	void onMouseReleased(int mouseX, int mouseY, int mouseButton);

	/**
	 * Called when the mouse is clicked and dragged
	 * 
	 * @param mouseX
	 * @param mouseY
	 * @param clickedMouseButton
	 * @param timeSinceLastClick
	 */
	void onMouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick);

	/**
	 * 
	 * @return The parent of this command slot
	 */
	IGuiCommandSlot getParent();

	/**
	 * Sets the parent of this command slot
	 * 
	 * @param parent
	 */
	void setParent(IGuiCommandSlot parent);

	/**
	 * Draws a tooltip on top of everything else
	 * 
	 * @param x
	 * @param y
	 * @param lines
	 */
	void drawTooltip(int x, int y, List<String> lines);

}
