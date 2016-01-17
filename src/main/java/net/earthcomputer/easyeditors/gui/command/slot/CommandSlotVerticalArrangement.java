package net.earthcomputer.easyeditors.gui.command.slot;

import java.util.List;

import net.earthcomputer.easyeditors.gui.ISizeChangeListener;
import net.earthcomputer.easyeditors.gui.command.CommandSyntaxException;

/**
 * A command slot which contains multiple child command slots, arranged
 * vertically
 * 
 * @author Earthcomputer
 *
 */
public class CommandSlotVerticalArrangement extends GuiCommandSlotImpl implements ISizeChangeListener {

	/**
	 * An array of the child command slots. After modifying this array, all
	 * children in this array must have this command slot as one of their size
	 * change listeners (using
	 * {@link IGuiCommandSlot#addSizeChangeListener(ISizeChangeListener)}, and
	 * the children's parent must be modified by using
	 * {@link IGuiCommandSlot#setParent(IGuiCommandSlot)}. Also, after modifying
	 * this array, {@link #recalcSize()} must be called
	 */
	protected IGuiCommandSlot[] children;

	public CommandSlotVerticalArrangement(IGuiCommandSlot... children) {
		super(calcWidth(children), calcHeight(children));
		this.children = children;
		for (int i = 0; i < children.length; i++) {
			children[i].addSizeChangeListener(this);
			children[i].setParent(this);
		}
	}

	private static int calcWidth(IGuiCommandSlot[] children) {
		int width = 0;
		for (int i = 0; i < children.length; i++) {
			int w = children[i].getWidth();
			if (w > width)
				width = w;
		}
		return width;
	}

	private static int calcHeight(IGuiCommandSlot[] children) {
		int height = children.length == 0 ? 0 : children.length * 2 - 2;
		for (int i = 0; i < children.length; i++) {
			height += children[i].getHeight();
		}
		return height;
	}

	/**
	 * Recalculates the overall size of this command slot
	 */
	protected void recalcSize() {
		int size = 0;
		int i;
		for (i = 0; i < children.length; i++) {
			int w = children[i].getWidth();
			if (w > size)
				size = w;
		}
		setWidth(size);

		size = children.length == 0 ? 0 : children.length * 2 - 2;
		for (i = 0; i < children.length; i++)
			size += children[i].getHeight();
		setHeight(size);
	}

	/**
	 * 
	 * @return The number of children
	 */
	public int size() {
		return children.length;
	}

	@Override
	public int readFromArgs(String[] args, int index) throws CommandSyntaxException {
		int len = 0;
		for (int i = 0; i < children.length; i++) {
			len += children[i].readFromArgs(args, index + len);
		}
		return len;
	}

	@Override
	public void addArgs(List<String> args) {
		for (IGuiCommandSlot child : children) {
			child.addArgs(args);
		}
	}

	@Override
	public void draw(int x, int y, int mouseX, int mouseY, float partialTicks) {
		int height = 0;
		for (IGuiCommandSlot child : children) {
			child.draw(x, y + height, mouseX, mouseY, partialTicks);
			height += child.getHeight() + 2;
		}

		super.draw(x, y, mouseX, mouseY, partialTicks);
	}

	@Override
	public void onWidthChange(int oldWidth, int newWidth) {
		recalcSize();
	}

	@Override
	public void onHeightChange(int oldHeight, int newHeight) {
		recalcSize();
	}

	@Override
	public boolean onKeyTyped(char typedChar, int keyCode) {
		boolean r = false;
		for (IGuiCommandSlot child : children) {
			if (child.onKeyTyped(typedChar, keyCode))
				r = true;
		}
		return r;
	}

	@Override
	public void onMouseClicked(int mouseX, int mouseY, int mouseButton) {
		for (IGuiCommandSlot child : children) {
			child.onMouseClicked(mouseX, mouseY, mouseButton);
		}
	}

	@Override
	public void onMouseReleased(int mouseX, int mouseY, int mouseButton) {
		for (IGuiCommandSlot child : children) {
			child.onMouseReleased(mouseX, mouseY, mouseButton);
		}
	}

	@Override
	public void onMouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
		for (IGuiCommandSlot child : children) {
			child.onMouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);
		}
	}

}
