package net.earthcomputer.easyeditors.gui.command.slot;

import java.util.List;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import net.earthcomputer.easyeditors.gui.ISizeChangeListener;
import net.earthcomputer.easyeditors.gui.command.CommandSyntaxException;

/**
 * A command slot which contains multiple child command slots, arranged
 * horizontally
 * 
 * @author Earthcomputer
 *
 */
public class CommandSlotHorizontalArrangement extends GuiCommandSlotImpl implements ISizeChangeListener {

	/**
	 * An array of the child command slots. After modifying this array, all
	 * children in this array must have this command slot as one of their size
	 * change listeners (using
	 * {@link IGuiCommandSlot#addSizeChangeListener(ISizeChangeListener)}, and
	 * the children's parents must be modified using
	 * {@link IGuiCommandSlot#setParent(IGuiCommandSlot)}. Also, after modifying
	 * this array, {@link #recalcSize()} must be called
	 */
	protected IGuiCommandSlot[] children;

	public CommandSlotHorizontalArrangement(IGuiCommandSlot... children) {
		super(calcWidth(children), calcHeight(children));
		this.children = children;
		for (int i = 0; i < children.length; i++) {
			children[i].addSizeChangeListener(this);
			children[i].setParent(this);
		}
	}

	private static int calcWidth(IGuiCommandSlot[] children) {
		int width = children.length == 0 ? 0 : children.length * 2 - 2;
		for (int i = 0; i < children.length; i++)
			width += children[i].getWidth();
		return width;
	}

	private static int calcHeight(IGuiCommandSlot[] children) {
		int height = 0;
		for (int i = 0; i < children.length; i++) {
			int h = children[i].getHeight();
			if (h > height)
				height = h;
		}
		return height;
	}

	/**
	 * Recalculates the overall size of this command slot
	 */
	protected void recalcSize() {
		int size = children.length == 0 ? 0 : children.length * 2 - 2;
		int i;
		for (i = 0; i < children.length; i++)
			size += children[i].getWidth();
		setWidth(size);

		size = 0;
		for (i = 0; i < children.length; i++) {
			int h = children[i].getHeight();
			if (h > size)
				size = h;
		}
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
		int width = 0;
		for (IGuiCommandSlot child : children) {
			child.draw(x + width, y + getHeight() / 2 - child.getHeight() / 2, mouseX, mouseY, partialTicks);
			width += child.getWidth() + 2;
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
	public void onKeyTyped(char typedChar, int keyCode) {
		for (IGuiCommandSlot child : children) {
			child.onKeyTyped(typedChar, keyCode);
		}
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
