package net.earthcomputer.easyeditors.gui.command.slot;

import java.util.List;

import net.earthcomputer.easyeditors.gui.ISizeChangeListener;
import net.earthcomputer.easyeditors.gui.command.CommandSyntaxException;
import net.minecraft.client.renderer.GlStateManager;

/**
 * A command slot which encases its child in a colored rectangle
 * 
 * @author Earthcomputer
 *
 */
public class CommandSlotRectangle extends GuiCommandSlotImpl implements ISizeChangeListener {

	private IGuiCommandSlot child;
	private int rectColor;

	public CommandSlotRectangle(IGuiCommandSlot child, int rectColor) {
		super(child.getWidth() + 4, child.getHeight() + 4);
		this.child = child;
		child.addSizeChangeListener(this);
		child.setParent(this);
		this.rectColor = rectColor;
	}

	@Override
	public int readFromArgs(String[] args, int index) throws CommandSyntaxException {
		return child.readFromArgs(args, index);
	}

	@Override
	public void addArgs(List<String> args) {
		child.addArgs(args);
	}

	@Override
	public void draw(int x, int y, int mouseX, int mouseY, float partialTicks) {
		GlStateManager.disableLighting();
		GlStateManager.disableFog();
		drawRect(x, y, x + getWidth(), y + getHeight(), rectColor);
		drawHorizontalLine(x, x + getWidth(), y, 0xff000000);
		drawHorizontalLine(x, x + getWidth(), y + getHeight(), 0xff000000);
		drawVerticalLine(x, y, y + getHeight(), 0xff000000);
		drawVerticalLine(x + getWidth(), y, y + getHeight(), 0xff000000);
		child.draw(x + 2, y + 2, mouseX, mouseY, partialTicks);
	}

	@Override
	public void drawForeground(int x, int y, int mouseX, int mouseY, float partialTicks) {
		super.drawForeground(x, y, mouseX, mouseY, partialTicks);

		child.drawForeground(x + 2, y + 2, mouseX, mouseY, partialTicks);
	}

	@Override
	public boolean onKeyTyped(char typedChar, int keyCode) {
		return child.onKeyTyped(typedChar, keyCode);
	}

	@Override
	public void onMouseClicked(int mouseX, int mouseY, int mouseButton) {
		child.onMouseClicked(mouseX, mouseY, mouseButton);
	}

	@Override
	public void onMouseReleased(int mouseX, int mouseY, int mouseButton) {
		child.onMouseReleased(mouseX, mouseY, mouseButton);
	}

	@Override
	public void onMouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
		child.onMouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);
	}

	@Override
	public void onWidthChange(int oldWidth, int newWidth) {
		setWidth(newWidth + 4);
	}

	@Override
	public void onHeightChange(int oldHeight, int newHeight) {
		setHeight(newHeight + 4);
	}

}
