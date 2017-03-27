package net.earthcomputer.easyeditors.gui.command.slot;

import java.util.List;

import net.earthcomputer.easyeditors.gui.ISizeChangeListener;
import net.earthcomputer.easyeditors.gui.command.CommandSyntaxException;
import net.earthcomputer.easyeditors.gui.command.CommandSlotContext;
import net.earthcomputer.easyeditors.gui.command.UIInvalidException;

/**
 * A basic component of a command GUI. There are a number of reasons these are
 * used instead of the standard components such as GuiButton:<br/>
 * 1: These do not provide a consistent way of getting the width and height of a
 * given component, each component has a separately-declared field<br/>
 * 2: They do not provide other functionality for dealing with dimensions such
 * as being able to listen for size changes<br/>
 * 3: There is no consistent way to draw these components<br/>
 * 4: There is no consistent way to let these components listen to user input,
 * such as the keyboard and mouse<br/>
 * 5: Command slots provide an easy way to read from and write to a list of
 * arguments
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
	 * @throws UIInvalidException
	 *             - when this operation cannot be done. The reason will be
	 *             displayed when hovering over the disabled done button
	 */
	void addArgs(List<String> args) throws UIInvalidException;

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
	 * Draws anything which will need to be drawn over everything else, even the
	 * header and footer
	 * 
	 * @param x
	 * @param y
	 * @param mouseX
	 * @param mouseY
	 * @param partialTicks
	 */
	void drawForeground(int x, int y, int mouseX, int mouseY, float partialTicks);

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
	 * 
	 * @return false if the parent command slot should be able to react to the
	 *         key type, true if it should not
	 */
	boolean onKeyTyped(char typedChar, int keyCode);

	/**
	 * Called when a mouse button is pressed, when the command slot is in the
	 * view
	 * 
	 * @param mouseX
	 * @param mouseY
	 * @param mouseButton
	 * 
	 * @return false if the parent command slot should be able to react to the
	 *         mouse click, true if it should not
	 */
	boolean onMouseClicked(int mouseX, int mouseY, int mouseButton);

	/**
	 * Called when a mouse button is pressed, even outside the view
	 * 
	 * @param mouseX
	 * @param mouseY
	 * @param mouseButton
	 * @return false if the parent command slot should be able to react to this
	 *         event, true if it should not and to cancel all onMouseClicked
	 *         events
	 */
	boolean onMouseClickedForeground(int mouseX, int mouseY, int mouseButton);

	/**
	 * Called when a mouse button is released
	 * 
	 * @param mouseX
	 * @param mouseY
	 * @param mouseButton
	 * 
	 * @return false if the parent command slot should be able to react to the
	 *         mouse release, true if it should not
	 */
	boolean onMouseReleased(int mouseX, int mouseY, int mouseButton);

	/**
	 * Called when the mouse is clicked and dragged
	 * 
	 * @param mouseX
	 * @param mouseY
	 * @param clickedMouseButton
	 * @param timeSinceLastClick
	 * 
	 * @return false if the parent command slot should be able to react to the
	 *         mouse drag, true if it should not
	 */
	boolean onMouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick);

	/**
	 * Called when the mouse is scrolled. If it is scrolled up, scrolledUp is
	 * true. Otherwise, it is false
	 * 
	 * @param mouseX
	 * @param mouseY
	 * @param scrolledUp
	 * 
	 * @return false if the parent command slot should be able to react to the
	 *         scroll, true if it should not
	 */
	boolean onMouseScrolled(int mouseX, int mouseY, boolean scrolledUp);

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
	 * 
	 * @return The context of this command slot
	 */
	CommandSlotContext getContext();

	/**
	 * Sets the context of this command slot
	 * 
	 * @param context
	 */
	void setContext(CommandSlotContext context);

	/**
	 * Returns whether the command slot context is 'detached', i.e. independent
	 * from the parent
	 * 
	 * @return
	 */
	boolean isDetachedContext();

	/**
	 * Makes the command slot context 'detached', i.e. independent from the
	 * parent
	 */
	void detachContext();

	/**
	 * Draws a tooltip on top of everything else
	 * 
	 * @param x
	 * @param y
	 * @param lines
	 */
	void drawTooltip(int x, int y, List<String> lines);

}
