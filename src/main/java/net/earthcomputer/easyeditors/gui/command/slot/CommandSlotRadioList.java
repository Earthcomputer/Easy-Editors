package net.earthcomputer.easyeditors.gui.command.slot;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import net.earthcomputer.easyeditors.api.util.Colors;
import net.earthcomputer.easyeditors.api.util.GeneralUtils;
import net.earthcomputer.easyeditors.gui.command.CommandSyntaxException;
import net.earthcomputer.easyeditors.gui.command.UIInvalidException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;

/**
 * A command slot representing a list of elements with radio buttons
 * 
 * @author Earthcomputer
 *
 */
public abstract class CommandSlotRadioList extends CommandSlotCollection {

	private static final ResourceLocation radioButtonLocation = new ResourceLocation(
			"easyeditors:textures/gui/radio_button.png");

	private int[] buttonTops;

	private int selectedIndex = 0;

	private int x;
	private int y;

	public CommandSlotRadioList(IGuiCommandSlot... children) {
		super(children);
		refreshButtonTops();
	}

	@Override
	protected void recalcWidth() {
		int max = 0;
		for (int i = 0; i < size(); i++) {
			IGuiCommandSlot child = getChildAt(i);
			if (child.getWidth() > max)
				max = child.getWidth();
		}
		setWidth(max + 22);
	}

	@Override
	protected void recalcHeight() {
		int total = size() == 0 ? 0 : size() * 3;
		for (int i = 0; i < size(); i++) {
			IGuiCommandSlot child = getChildAt(i);
			total += child.getHeight() > 16 ? child.getHeight() : 16;
		}
		setHeight(total);
		refreshButtonTops();
	}

	@Override
	protected int[] getXPosChildren() {
		int[] xs = new int[size()];
		Arrays.fill(xs, 20);
		return xs;
	}

	@Override
	protected int[] getYPosChildren() {
		int[] ys = new int[size()];
		int height = 1;
		for (int i = 0; i < ys.length; i++) {
			IGuiCommandSlot child = getChildAt(i);
			if (child.getHeight() > 16) {
				ys[i] = height;
				height += child.getHeight();
			} else {
				ys[i] = height + 8 - child.getHeight() / 2;
				height += 16;
			}
			height += 3;
		}
		return ys;
	}

	@Override
	public void clearChildren() {
		super.clearChildren();
		selectedIndex = 0;
		refreshButtonTops();
	}

	@Override
	public void addChild(IGuiCommandSlot child) {
		super.addChild(child);
		refreshButtonTops();
	}

	@Override
	public void addChild(int index, IGuiCommandSlot child) {
		super.addChild(index, child);
		if (index >= selectedIndex)
			selectedIndex++;
		refreshButtonTops();
	}

	@Override
	public void addChildren(Collection<? extends IGuiCommandSlot> children) {
		super.addChildren(children);
		refreshButtonTops();
	}

	@Override
	public void addChildren(int index, Collection<? extends IGuiCommandSlot> children) {
		super.addChildren(index, children);
		if (index >= selectedIndex)
			selectedIndex += children.size();
		refreshButtonTops();
	}

	@Override
	public void setChildAt(int index, IGuiCommandSlot child) {
		super.setChildAt(index, child);
		refreshButtonTops();
	}

	@Override
	public void removeChild(IGuiCommandSlot child) {
		int index = getChildren().indexOf(child);
		super.removeChild(child);
		if (index == selectedIndex)
			selectedIndex = 0;
		else if (index > selectedIndex)
			selectedIndex--;
		refreshButtonTops();
	}

	@Override
	public void removeChildAt(int index) {
		super.removeChildAt(index);
		if (index == selectedIndex)
			selectedIndex = 0;
		else if (index > selectedIndex)
			selectedIndex--;
		refreshButtonTops();
	}

	/**
	 * 
	 * @return The index of the currently selected element
	 */
	public int getSelectedIndex() {
		return selectedIndex;
	}

	/**
	 * Sets the index of the currently selected element
	 * 
	 * @param selectedIndex
	 */
	public void setSelectedIndex(int selectedIndex) {
		this.selectedIndex = selectedIndex;
	}

	@Override
	public int readFromArgs(String[] args, int index) throws CommandSyntaxException {
		if (index >= args.length)
			throw new CommandSyntaxException();
		selectedIndex = getSelectedIndexForString(args, index);
		if (size() != 0) {
			return getChildAt(selectedIndex).readFromArgs(args, index);
		}
		return 0;
	}

	@Override
	public void addArgs(List<String> args) throws UIInvalidException {
		if (size() != 0) {
			getChildAt(selectedIndex).addArgs(args);
		}
	}

	@Override
	public void draw(int x, int y, int mouseX, int mouseY, float partialTicks) {
		GlStateManager.disableLighting();
		GlStateManager.disableFog();
		this.x = x;
		this.y = y;
		for (int i = 0; i < size(); i++) {
			IGuiCommandSlot child = getChildAt(i);
			Minecraft.getMinecraft().getTextureManager().bindTexture(radioButtonLocation);
			GlStateManager.color(1, 1, 1, 1);
			drawModalRectWithCustomSizedTexture(x, y + buttonTops[i], i == selectedIndex ? 16 : 0,
					mouseX >= x && mouseY >= y + buttonTops[i] && mouseX < x + 16 && mouseY < y + buttonTops[i] + 16
							? 16 : 0,
					16, 16, 32, 32);
			if (i == selectedIndex) {
				drawHorizontalLine(x + 18, x + getWidth() - 1, y + getYOfChild(i) - 2, Colors.radioOutline.color);
				drawHorizontalLine(x + 18, x + getWidth() - 1, y + getYOfChild(i) + child.getHeight() + 1,
						Colors.radioOutline.color);
				drawVerticalLine(x + 18, y + getYOfChild(i) - 2, y + getYOfChild(i) + child.getHeight() + 1,
						Colors.radioOutline.color);
				drawVerticalLine(x + getWidth() - 1, y + getYOfChild(i) - 2, y + getYOfChild(i) + child.getHeight() + 1,
						Colors.radioOutline.color);
			}
		}
		super.draw(x, y, mouseX, mouseY, partialTicks);
	}

	/**
	 * 
	 * @param rawCommand
	 * @return What the selected index should be for the given part of the
	 *         command
	 * @throws CommandSyntaxException
	 */
	protected abstract int getSelectedIndexForString(String[] args, int index) throws CommandSyntaxException;

	@Override
	public boolean onKeyTyped(char typedChar, int keyCode) {
		if (size() != 0)
			return getChildAt(selectedIndex).onKeyTyped(typedChar, keyCode);
		else
			return false;
	}

	@Override
	public boolean onMouseClicked(int mouseX, int mouseY, int mouseButton) {
		if (mouseButton == 0) {
			if (mouseX >= x && mouseX < x + getWidth() && getContext().isMouseInBounds(mouseX, mouseY)) {
				for (int i = 0; i < buttonTops.length; i++) {
					int top = getYOfChild(i);
					if (top > buttonTops[i])
						top = buttonTops[i];
					int height = getChildAt(i).getHeight();
					if (height < 16)
						height = 16;
					if (mouseY >= top && mouseY < y + top + height) {
						if (selectedIndex != i) {
							selectedIndex = i;
							GeneralUtils.playButtonSound();
						}
						break;
					}
				}
			}
		}
		if (size() != 0) {
			return getChildAt(selectedIndex).onMouseClicked(mouseX, mouseY, mouseButton);
		}
		return false;
	}

	@Override
	public boolean onMouseClickedForeground(int mouseX, int mouseY, int mouseButton) {
		if (size() != 0)
			return getChildAt(selectedIndex).onMouseClickedForeground(mouseX, mouseY, mouseButton);
		return false;
	}

	@Override
	public boolean onMouseReleased(int mouseX, int mouseY, int mouseButton) {
		if (size() != 0)
			return getChildAt(selectedIndex).onMouseReleased(mouseX, mouseY, mouseButton);
		return false;
	}

	@Override
	public boolean onMouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
		if (size() != 0)
			return getChildAt(selectedIndex).onMouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);
		return false;
	}

	@Override
	public boolean onMouseScrolled(int mouseX, int mouseY, boolean scrolledUp) {
		if (size() != 0)
			return getChildAt(selectedIndex).onMouseScrolled(mouseX, mouseY, scrolledUp);
		return false;
	}

	private void refreshButtonTops() {
		if (buttonTops == null || size() != buttonTops.length) {
			buttonTops = new int[size()];
		}
		int height = 1;
		for (int i = 0; i < buttonTops.length; i++) {
			IGuiCommandSlot child = getChildAt(i);
			if (child.getHeight() > 16) {
				buttonTops[i] = height + child.getHeight() / 2 - 8;
			} else {
				buttonTops[i] = height;
			}
			height += child.getHeight() > 16 ? child.getHeight() : 16;
			height += 3;
		}
	}

}
