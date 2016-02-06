package net.earthcomputer.easyeditors.gui.command.slot;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.google.common.collect.Lists;

import net.earthcomputer.easyeditors.gui.ISizeChangeListener;
import net.earthcomputer.easyeditors.gui.command.CommandSyntaxException;
import net.earthcomputer.easyeditors.gui.command.ICommandSlotContext;

/**
 * A command slot which contains a number of child command slots
 * 
 * @author Earthcomputer
 *
 */
public abstract class CommandSlotCollection extends GuiCommandSlotImpl implements ISizeChangeListener {

	private List<IGuiCommandSlot> children;
	private int[] xs;
	private int[] ys;
	private boolean recalcXPosChildren;
	private boolean recalcYPosChildren;

	public CommandSlotCollection(IGuiCommandSlot... children) {
		super(1, 1);
		this.children = Lists.newArrayList(children);
		for (IGuiCommandSlot child : children) {
			child.addSizeChangeListener(this);
			child.setParent(this);
		}
		recalcSize();
		recalcXPosChildren = true;
		recalcYPosChildren = true;
	}

	@Override
	public int readFromArgs(String[] args, int index) throws CommandSyntaxException {
		int len = 0;
		for (int i = 0; i < children.size(); i++) {
			len += children.get(i).readFromArgs(args, index + len);
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
		if (recalcXPosChildren) {
			recalcXPosChildren = false;
			xs = getXPosChildren();
		}
		if (recalcYPosChildren) {
			recalcYPosChildren = false;
			ys = getYPosChildren();
		}
		for (int i = 0; i < children.size(); i++) {
			children.get(i).draw(x + xs[i], y + ys[i], mouseX, mouseY, partialTicks);
		}
	}

	@Override
	public void drawForeground(int x, int y, int mouseX, int mouseY, float partialTicks) {
		super.drawForeground(x, y, mouseX, mouseY, partialTicks);

		for (int i = 0; i < children.size(); i++) {
			children.get(i).drawForeground(x + xs[i], y + ys[i], mouseX, mouseY, partialTicks);
		}
	}

	@Override
	public boolean onKeyTyped(char typedChar, int keyCode) {
		boolean r = false;
		for (int i = children.size() - 1; i >= 0; i--) {
			if (children.get(i).onKeyTyped(typedChar, keyCode))
				r = true;
		}
		return r;
	}

	@Override
	public boolean onMouseClicked(int mouseX, int mouseY, int mouseButton) {
		boolean r = false;
		for (int i = children.size() - 1; i >= 0; i--) {
			if (children.get(i).onMouseClicked(mouseX, mouseY, mouseButton))
				r = true;
		}
		return r;
	}

	@Override
	public boolean onMouseClickedForeground(int mouseX, int mouseY, int mouseButton) {
		boolean r = false;
		for (int i = children.size() - 1; i >= 0; i--) {
			if (children.get(i).onMouseClickedForeground(mouseX, mouseY, mouseButton))
				r = true;
		}
		return r;
	}

	@Override
	public boolean onMouseReleased(int mouseX, int mouseY, int mouseButton) {
		boolean r = false;
		for (int i = children.size() - 1; i >= 0; i--) {
			if (children.get(i).onMouseReleased(mouseX, mouseY, mouseButton))
				r = true;
		}
		return r;
	}

	@Override
	public boolean onMouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
		boolean r = false;
		for (int i = children.size() - 1; i >= 0; i--) {
			if (children.get(i).onMouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick))
				r = true;
		}
		return r;
	}

	@Override
	public boolean onMouseScrolled(int mouseX, int mouseY, boolean scrolledUp) {
		boolean r = false;
		for (int i = children.size() - 1; i >= 0; i--) {
			if (children.get(i).onMouseScrolled(mouseX, mouseY, scrolledUp))
				r = true;
		}
		return r;
	}

	@Override
	public void setContext(ICommandSlotContext context) {
		super.setContext(context);
		for (IGuiCommandSlot child : children) {
			child.setContext(context);
		}
	}

	/**
	 * Removes all children
	 */
	public void clearChildren() {
		for (IGuiCommandSlot child : children) {
			child.removeSizeChangeListener(this);
		}
		children.clear();
	}

	/**
	 * Adds a child to the end of this collection
	 * 
	 * @param child
	 */
	public void addChild(IGuiCommandSlot child) {
		children.add(child);
		child.addSizeChangeListener(this);
		child.setParent(this);
		child.setContext(getContext());
		recalcSize();
		recalcPosChildren();
	}

	/**
	 * Insets a child before the specified index
	 * 
	 * @param index
	 * @param child
	 */
	public void addChild(int index, IGuiCommandSlot child) {
		children.add(index, child);
		child.addSizeChangeListener(this);
		child.setParent(this);
		child.setContext(getContext());
		recalcSize();
		recalcPosChildren();
	}

	/**
	 * Adds all the specified children at the end of this collection
	 * 
	 * @param children
	 */
	public void addChildren(IGuiCommandSlot... children) {
		addChildren(Arrays.asList(children));
	}

	/**
	 * Inserts all the specified children before the specified index
	 * 
	 * @param index
	 * @param children
	 */
	public void addChildren(int index, IGuiCommandSlot... children) {
		addChildren(index, Arrays.asList(children));
	}

	/**
	 * Adds all the specified children at the end of this collection
	 * 
	 * @param children
	 */
	public void addChildren(Collection<? extends IGuiCommandSlot> children) {
		this.children.addAll(children);
		for (IGuiCommandSlot child : children) {
			child.addSizeChangeListener(this);
			child.setParent(this);
			child.setContext(getContext());
		}
		recalcSize();
		recalcPosChildren();
	}

	/**
	 * Inserts all the specified children before the specified index
	 * 
	 * @param index
	 * @param children
	 */
	public void addChildren(int index, Collection<? extends IGuiCommandSlot> children) {
		this.children.addAll(index, children);
		for (IGuiCommandSlot child : children) {
			child.addSizeChangeListener(this);
			child.setParent(this);
			child.setContext(getContext());
		}
		recalcSize();
		recalcPosChildren();
	}

	/**
	 * Sets the child at the specified index
	 * 
	 * @param index
	 * @param child
	 */
	public void setChildAt(int index, IGuiCommandSlot child) {
		children.set(index, child);
		child.addSizeChangeListener(this);
		child.setParent(this);
		child.setContext(getContext());
		recalcSize();
		recalcPosChildren();
	}

	/**
	 * Removes the specified child from this collection
	 * 
	 * @param child
	 */
	public void removeChild(IGuiCommandSlot child) {
		children.remove(child);
		child.removeSizeChangeListener(this);
		recalcSize();
		recalcPosChildren();
	}

	/**
	 * Removes the child at the given index from this collection
	 * 
	 * @param index
	 */
	public void removeChildAt(int index) {
		children.remove(index).removeSizeChangeListener(this);
		recalcSize();
		recalcPosChildren();
	}

	/**
	 * 
	 * @param index
	 * @return The child at the specified index
	 */
	public IGuiCommandSlot getChildAt(int index) {
		return children.get(index);
	}

	/**
	 * 
	 * @return The children as a list
	 */
	public List<IGuiCommandSlot> getChildren() {
		return Collections.unmodifiableList(children);
	}

	/**
	 * 
	 * @return The number of children
	 */
	public int size() {
		return children.size();
	}

	/**
	 * Recalculates the positions of all the children
	 */
	protected void recalcPosChildren() {
		recalcXPosChildren();
		recalcYPosChildren();
	}

	/**
	 * Recalculates the x-positions of all the children
	 */
	protected void recalcXPosChildren() {
		recalcXPosChildren = true;
	}

	/**
	 * Recalculates the y-positions of all the children
	 */
	protected void recalcYPosChildren() {
		recalcYPosChildren = true;
	}

	/**
	 * Recalculates the overall dimensions of this collection
	 */
	protected void recalcSize() {
		recalcWidth();
		recalcHeight();
	}

	/**
	 * Recalculates the overall width of this collection. It is expected that
	 * the width is set using {@link #setWidth(int)}
	 */
	protected abstract void recalcWidth();

	/**
	 * Recalculates the overall height of this collection. It is expected that
	 * the height is set using {@link #setHeight(int)}
	 */
	protected abstract void recalcHeight();

	/**
	 * Returns an array of all the x-coordinates of the children. These should
	 * be calculated on the fly
	 * 
	 * @return
	 */
	protected abstract int[] getXPosChildren();

	/**
	 * Returns an array of all the y-coordinates of the children. These should
	 * be calculated on the fly
	 * 
	 * @return
	 */
	protected abstract int[] getYPosChildren();

	/**
	 * 
	 * @param index
	 * @return The x-position of the child at the given index
	 */
	protected int getXOfChild(int index) {
		if (recalcXPosChildren)
			xs = getXPosChildren();
		return xs[index];
	}

	/**
	 * 
	 * @param index
	 * @return The y-position of the child at the given index
	 */
	protected int getYOfChild(int index) {
		if (recalcYPosChildren)
			ys = getYPosChildren();
		return ys[index];
	}

	@Override
	public void onWidthChange(int oldWidth, int newWidth) {
		recalcWidth();
		recalcXPosChildren = true;
	}

	@Override
	public void onHeightChange(int oldHeight, int newHeight) {
		recalcHeight();
		recalcYPosChildren = true;
	}

}
