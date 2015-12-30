package net.earthcomputer.easyeditors.gui.command.slot;

import java.util.List;

import com.google.common.collect.Lists;

import net.earthcomputer.easyeditors.gui.ISizeChangeListener;
import net.minecraft.client.gui.Gui;

/**
 * An implementation of IGuiCommandSlot
 * 
 * @author Earthcomputer
 *
 */
public abstract class GuiCommandSlotImpl extends Gui implements IGuiCommandSlot {

	private int width;
	private int height;

	private List<ISizeChangeListener> sizeChangeListeners = Lists.newArrayList();

	public GuiCommandSlotImpl(int width, int height) {
		this.width = width;
		this.height = height;
	}

	@Override
	public int getWidth() {
		return width;
	}

	@Override
	public void setWidth(int width) {
		if (width != this.width) {
			this.width = width;
			for (ISizeChangeListener listener : sizeChangeListeners) {
				listener.onWidthChange(this.width, width);
			}
		}
	}

	@Override
	public int getHeight() {
		return height;
	}

	@Override
	public void setHeight(int height) {
		if (height != this.height) {
			this.height = height;
			for (ISizeChangeListener listener : sizeChangeListeners) {
				listener.onHeightChange(this.height, height);
			}
		}
	}

	@Override
	public void addSizeChangeListener(ISizeChangeListener listener) {
		sizeChangeListeners.add(listener);
	}

	@Override
	public void removeSizeChangeListener(ISizeChangeListener listener) {
		sizeChangeListeners.remove(listener);
	}

	@Override
	public void onKeyTyped(char typedChar, int keyCode) {
	}

	@Override
	public void onMouseClicked(int mouseX, int mouseY, int mouseButton) {
	}

	@Override
	public void onMouseReleased(int mouseX, int mouseY, int mouseButton) {
	}

	@Override
	public void onMouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
	}
}
