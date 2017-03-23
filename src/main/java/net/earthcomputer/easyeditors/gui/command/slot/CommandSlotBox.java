package net.earthcomputer.easyeditors.gui.command.slot;

import java.util.List;

import net.earthcomputer.easyeditors.gui.ISizeChangeListener;
import net.earthcomputer.easyeditors.gui.command.CommandSlotContext;
import net.earthcomputer.easyeditors.gui.command.CommandSyntaxException;
import net.earthcomputer.easyeditors.gui.command.UIInvalidException;

/**
 * A command slot which contains another one
 * 
 * @author Earthcomputer
 */
public class CommandSlotBox extends GuiCommandSlotImpl implements ISizeChangeListener {

	private IGuiCommandSlot child;

	public CommandSlotBox(IGuiCommandSlot child) {
		super(0, 0);
		setChild(child);
	}

	/**
	 * 
	 * @return The child command slot
	 */
	public IGuiCommandSlot getChild() {
		return child;
	}

	/**
	 * Sets the child command slot. There is no need for
	 * {@link IGuiCommandSlot#addSizeChangeListener(ISizeChangeListener)} or
	 * {@link IGuiCommandSlot#setParent(IGuiCommandSlot)} to be called on the
	 * child: these are called automatically
	 * 
	 * @param child
	 */
	protected void setChild(IGuiCommandSlot child) {
		if (child != this.child) {
			if (this.child != null)
				this.child.removeSizeChangeListener(this);
			this.child = child;
			if (child == null) {
				setWidth(getPadding() * 2);
				setHeight(getPadding() * 2);
			} else {
				setWidth(child.getWidth() + getPadding() * 2);
				setHeight(child.getHeight() + getPadding() * 2);
				child.addSizeChangeListener(this);
				child.setParent(this);
				child.setContext(getContext());
			}
		}
	}

	@Override
	public int readFromArgs(String[] args, int index) throws CommandSyntaxException {
		return child == null ? 0 : child.readFromArgs(args, index);
	}

	@Override
	public void addArgs(List<String> args) throws UIInvalidException {
		if (child != null)
			child.addArgs(args);
	}

	@Override
	public void draw(int x, int y, int mouseX, int mouseY, float partialTicks) {
		if (child != null)
			child.draw(x + getPadding(), y + getPadding(), mouseX, mouseY, partialTicks);
	}

	@Override
	public void drawForeground(int x, int y, int mouseX, int mouseY, float partialTicks) {
		super.drawForeground(x, y, mouseX, mouseY, partialTicks);

		if (child != null)
			child.drawForeground(x + getPadding(), y + getPadding(), mouseX, mouseY, partialTicks);
	}

	@Override
	public boolean onKeyTyped(char typedChar, int keyCode) {
		return child == null ? false : child.onKeyTyped(typedChar, keyCode);
	}

	@Override
	public boolean onMouseClicked(int mouseX, int mouseY, int mouseButton) {
		if (child != null)
			return child.onMouseClicked(mouseX, mouseY, mouseButton);
		return false;
	}

	@Override
	public boolean onMouseClickedForeground(int mouseX, int mouseY, int mouseButton) {
		if (child != null)
			return child.onMouseClickedForeground(mouseX, mouseY, mouseButton);
		return false;
	}

	@Override
	public boolean onMouseReleased(int mouseX, int mouseY, int mouseButton) {
		if (child != null)
			return child.onMouseReleased(mouseX, mouseY, mouseButton);
		return false;
	}

	@Override
	public boolean onMouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
		if (child != null)
			return child.onMouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);
		return false;
	}

	@Override
	public boolean onMouseScrolled(int mouseX, int mouseY, boolean scrolledUp) {
		if (child != null)
			return child.onMouseScrolled(mouseX, mouseY, scrolledUp);
		return false;
	}

	@Override
	public void setContext(CommandSlotContext context) {
		super.setContext(context);
		if (child != null)
			child.setContext(context);
	}

	@Override
	public void onWidthChange(int oldWidth, int newWidth) {
		setWidth(newWidth + getPadding() * 2);
	}

	@Override
	public void onHeightChange(int oldHeight, int newHeight) {
		setHeight(newHeight + getPadding() * 2);
	}

	/**
	 * Returns the padding
	 * 
	 * @return
	 */
	protected int getPadding() {
		return 0;
	}

}
