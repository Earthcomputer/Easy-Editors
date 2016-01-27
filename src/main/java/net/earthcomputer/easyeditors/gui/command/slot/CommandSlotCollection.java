package net.earthcomputer.easyeditors.gui.command.slot;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import com.google.common.collect.Lists;

import net.earthcomputer.easyeditors.gui.ISizeChangeListener;
import net.earthcomputer.easyeditors.gui.command.CommandSyntaxException;

/**
 * A command slot which contains a number of child command slots
 * 
 * @author Earthcomputer
 *
 */
public abstract class CommandSlotCollection extends GuiCommandSlotImpl
		implements ISizeChangeListener, Iterable<IGuiCommandSlot> {

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
			xs = getXPosChildren(x);
		}
		if (recalcYPosChildren) {
			recalcYPosChildren = false;
			ys = getYPosChildren(y);
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
		for (IGuiCommandSlot child : children) {
			if (child.onKeyTyped(typedChar, keyCode))
				r = true;
		}
		return r;
	}

	@Override
	public boolean onMouseClicked(int mouseX, int mouseY, int mouseButton) {
		for (IGuiCommandSlot child : children) {
			if (child.onMouseClicked(mouseX, mouseY, mouseButton))
				return true;
		}
		return false;
	}

	@Override
	public boolean onMouseReleased(int mouseX, int mouseY, int mouseButton) {
		for (IGuiCommandSlot child : children) {
			if (child.onMouseReleased(mouseX, mouseY, mouseButton))
				return true;
		}
		return false;
	}

	@Override
	public boolean onMouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
		for (IGuiCommandSlot child : children) {
			if (child.onMouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick))
				return true;
		}
		return false;
	}

	@Override
	public boolean onMouseScrolled(int mouseX, int mouseY, boolean scrolledUp) {
		for (IGuiCommandSlot child : children) {
			if (child.onMouseScrolled(mouseX, mouseY, scrolledUp))
				return true;
		}
		return false;
	}

	public void clearChildren() {
		for (IGuiCommandSlot child : children) {
			child.removeSizeChangeListener(this);
		}
		children.clear();
	}

	public void addChild(IGuiCommandSlot child) {
		children.add(child);
		child.addSizeChangeListener(this);
		child.setParent(this);
		recalcSize();
		recalcPosChildren();
	}

	public void addChild(int index, IGuiCommandSlot child) {
		children.add(index, child);
		child.addSizeChangeListener(this);
		child.setParent(this);
		recalcSize();
		recalcPosChildren();
	}

	public void addChildren(IGuiCommandSlot... children) {
		addChildren(Arrays.asList(children));
	}
	
	public void addChildren(int index, IGuiCommandSlot... children) {
		addChildren(index, Arrays.asList(children));
	}

	public void addChildren(Collection<? extends IGuiCommandSlot> children) {
		this.children.addAll(children);
		for (IGuiCommandSlot child : children) {
			child.addSizeChangeListener(this);
			child.setParent(this);
		}
		recalcSize();
		recalcPosChildren();
	}
	
	public void addChildren(int index, Collection<? extends IGuiCommandSlot> children) {
		this.children.addAll(index, children);
		for (IGuiCommandSlot child : children) {
			child.addSizeChangeListener(this);
			child.setParent(this);
		}
		recalcSize();
		recalcPosChildren();
	}

	public void setChildAt(int index, IGuiCommandSlot child) {
		children.set(index, child);
		child.addSizeChangeListener(this);
		child.setParent(this);
		recalcSize();
		recalcPosChildren();
	}

	public IGuiCommandSlot getChildAt(int index) {
		return children.get(index);
	}

	public List<IGuiCommandSlot> getChildren() {
		return Collections.unmodifiableList(children);
	}

	public int size() {
		return children.size();
	}

	@Override
	public Iterator<IGuiCommandSlot> iterator() {
		return children.iterator();
	}

	protected void recalcPosChildren() {
		recalcXPosChildren();
		recalcYPosChildren();
	}

	protected void recalcXPosChildren() {
		recalcXPosChildren = true;
	}

	protected void recalcYPosChildren() {
		recalcYPosChildren = true;
	}

	protected void recalcSize() {
		recalcWidth();
		recalcHeight();
	}

	protected abstract void recalcWidth();

	protected abstract void recalcHeight();

	protected abstract int[] getXPosChildren(int x);

	protected abstract int[] getYPosChildren(int y);

	protected int getXOfChild(int x, int index) {
		if (recalcXPosChildren)
			xs = getXPosChildren(x);
		return xs[index];
	}

	protected int getYOfChild(int y, int index) {
		if (recalcYPosChildren)
			ys = getYPosChildren(y);
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
