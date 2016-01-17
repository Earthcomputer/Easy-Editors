package net.earthcomputer.easyeditors.gui.command.slot;

import java.util.List;

import com.google.common.collect.Lists;

import net.earthcomputer.easyeditors.api.Colors;
import net.earthcomputer.easyeditors.api.GeneralUtils;
import net.earthcomputer.easyeditors.gui.ISizeChangeListener;
import net.earthcomputer.easyeditors.gui.command.CommandSyntaxException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;

/**
 * A command slot representing a list of elements with radio buttons
 * 
 * @author Earthcomputer
 *
 */
public abstract class CommandSlotRadioList extends GuiCommandSlotImpl implements ISizeChangeListener {

	private static final ResourceLocation radioButtonLocation = new ResourceLocation(
			"easyeditors:textures/gui/radio_button.png");

	private List<IGuiCommandSlot> children;
	private int[] childTops;
	private int[] buttonTops;

	private int selectedIndex = 0;

	private int x;
	private int y;

	public CommandSlotRadioList(IGuiCommandSlot... children) {
		super(calcWidth(children), calcHeight(children));
		this.children = Lists.newArrayList(children);
		for (IGuiCommandSlot child : children) {
			child.addSizeChangeListener(this);
			child.setParent(this);
		}
		childTops = new int[children.length];
		buttonTops = new int[children.length];
		refreshHeights();
	}

	private static int calcWidth(IGuiCommandSlot[] children) {
		int max = 0;
		for (IGuiCommandSlot child : children) {
			if (child.getWidth() > max)
				max = child.getWidth();
		}
		return max + 22;
	}

	private static int calcHeight(IGuiCommandSlot[] children) {
		int total = children.length == 0 ? 0 : children.length * 3;
		for (IGuiCommandSlot child : children) {
			total += child.getHeight() > 16 ? child.getHeight() : 16;
		}
		return total;
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
		if (!children.isEmpty()) {
			return children.get(selectedIndex).readFromArgs(args, index);
		}
		return 0;
	}

	@Override
	public void addArgs(List<String> args) {
		if (!children.isEmpty()) {
			children.get(selectedIndex).addArgs(args);
		}
	}

	@Override
	public void draw(int x, int y, int mouseX, int mouseY, float partialTicks) {
		GlStateManager.disableLighting();
		GlStateManager.disableFog();
		this.x = x;
		this.y = y;
		for (int i = 0; i < children.size(); i++) {
			IGuiCommandSlot child = children.get(i);
			Minecraft.getMinecraft().getTextureManager().bindTexture(radioButtonLocation);
			GlStateManager.color(1, 1, 1, 1);
			drawModalRectWithCustomSizedTexture(x, y + buttonTops[i], i == selectedIndex ? 16 : 0,
					mouseX >= x && mouseY >= y + buttonTops[i] && mouseX < x + 16 && mouseY < y + buttonTops[i] + 16
							? 16 : 0,
					16, 16, 32, 32);
			if (i == selectedIndex) {
				drawHorizontalLine(x + 18, x + getWidth() - 1, y + childTops[i] - 2, Colors.radioOutline.color);
				drawHorizontalLine(x + 18, x + getWidth() - 1, y + childTops[i] + child.getHeight() + 1,
						Colors.radioOutline.color);
				drawVerticalLine(x + 18, y + childTops[i] - 2, y + childTops[i] + child.getHeight() + 1,
						Colors.radioOutline.color);
				drawVerticalLine(x + getWidth() - 1, y + childTops[i] - 2, y + childTops[i] + child.getHeight() + 1,
						Colors.radioOutline.color);
			}
			child.draw(x + 20, y + childTops[i], mouseX, mouseY, partialTicks);
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
	public void onKeyTyped(char typedChar, int keyCode) {
		if (!children.isEmpty())
			children.get(selectedIndex).onKeyTyped(typedChar, keyCode);
	}

	@Override
	public void onMouseClicked(int mouseX, int mouseY, int mouseButton) {
		if (mouseButton == 0) {
			if (mouseX >= x && mouseX < x + getWidth()) {
				for (int i = 0; i < buttonTops.length; i++) {
					if (mouseY >= y + buttonTops[i] && mouseY < y + buttonTops[i] + 16) {
						if (selectedIndex != i) {
							selectedIndex = i;
							GeneralUtils.playButtonSound();
						}
						break;
					}
				}
			}
		}
		if (!children.isEmpty()) {
			children.get(selectedIndex).onMouseClicked(mouseX, mouseY, mouseButton);
		}
	}

	@Override
	public void onMouseReleased(int mouseX, int mouseY, int mouseButton) {
		if (!children.isEmpty())
			children.get(selectedIndex).onMouseReleased(mouseX, mouseY, mouseButton);
	}

	@Override
	public void onMouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
		if (!children.isEmpty())
			children.get(selectedIndex).onMouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);
	}

	@Override
	public void onWidthChange(int oldWidth, int newWidth) {
		int max = 0;
		for (IGuiCommandSlot child : children) {
			if (child.getWidth() > max)
				max = child.getWidth();
		}
		setWidth(max + 22);
	}

	@Override
	public void onHeightChange(int oldHeight, int newHeight) {
		int total = children.size() == 0 ? 0 : children.size() * 3;
		for (IGuiCommandSlot child : children) {
			total += child.getHeight() > 16 ? child.getHeight() : 16;
		}
		setHeight(total);
		refreshHeights();
	}

	private void refreshHeights() {
		if (children.size() != childTops.length) {
			childTops = new int[children.size()];
			buttonTops = new int[childTops.length];
		}
		int height = 1;
		for (int i = 0; i < childTops.length; i++) {
			IGuiCommandSlot child = children.get(i);
			if (child.getHeight() > 16) {
				childTops[i] = height;
				buttonTops[i] = height + child.getHeight() / 2 - 8;
			} else {
				childTops[i] = height + 8 - child.getHeight() / 2;
				buttonTops[i] = height;
			}
			height += child.getHeight() > 16 ? child.getHeight() : 16;
			height += 3;
		}
	}

}
