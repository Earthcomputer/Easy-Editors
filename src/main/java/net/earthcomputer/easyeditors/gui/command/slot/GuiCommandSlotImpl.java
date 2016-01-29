package net.earthcomputer.easyeditors.gui.command.slot;

import java.util.Arrays;
import java.util.List;

import com.google.common.collect.Lists;

import net.earthcomputer.easyeditors.api.GeneralUtils;
import net.earthcomputer.easyeditors.gui.ISizeChangeListener;
import net.minecraft.client.Minecraft;
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

	private IGuiCommandSlot parent;
	private List<ISizeChangeListener> sizeChangeListeners = Lists.newArrayList();

	private List<Tooltip> tooltips = Lists.newArrayList();

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
	public boolean onKeyTyped(char typedChar, int keyCode) {
		return false;
	}

	@Override
	public boolean onMouseClicked(int mouseX, int mouseY, int mouseButton) {
		return false;
	}

	@Override
	public boolean onMouseClickedForeground(int mouseX, int mouseY, int mouseButton) {
		return false;
	}

	@Override
	public boolean onMouseReleased(int mouseX, int mouseY, int mouseButton) {
		return false;
	}

	@Override
	public boolean onMouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
		return false;
	}

	@Override
	public boolean onMouseScrolled(int mouseX, int mouseY, boolean scrolledUp) {
		return false;
	}

	@Override
	public IGuiCommandSlot getParent() {
		return parent;
	}

	@Override
	public void setParent(IGuiCommandSlot parent) {
		this.parent = parent;
	}

	@Override
	public void drawForeground(int x, int y, int mouseX, int mouseY, float partialTicks) {
		for (Tooltip tooltip : tooltips) {
			GeneralUtils.drawTooltip(tooltip.x, tooltip.y, tooltip.lines);
		}
		tooltips.clear();
	}

	/**
	 * A convenience method which draws a tooltip, splitting the text into lines
	 * so that it fits into maxWidth
	 * 
	 * @param x
	 * @param y
	 * @param text
	 * @param maxWidth
	 */
	public void drawTooltip(int x, int y, String text, int maxWidth) {
		drawTooltip(x, y, Minecraft.getMinecraft().fontRendererObj.listFormattedStringToWidth(text, maxWidth));
	}

	/**
	 * A convenience method which draws a tooltip with lines in an array instead
	 * of a list
	 * 
	 * @param x
	 * @param y
	 * @param lines
	 */
	public void drawTooltip(int x, int y, String... lines) {
		drawTooltip(x, y, Arrays.asList(lines));
	}

	@Override
	public void drawTooltip(int x, int y, List<String> lines) {
		tooltips.add(new Tooltip(x, y, lines));
	}

	private static class Tooltip {
		public Tooltip(int x, int y, List<String> lines) {
			this.x = x;
			this.y = y;
			this.lines = lines;
		}

		public int x;
		public int y;
		public List<String> lines;
	}
}
