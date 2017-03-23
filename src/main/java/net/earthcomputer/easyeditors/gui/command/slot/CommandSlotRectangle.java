package net.earthcomputer.easyeditors.gui.command.slot;

import net.minecraft.client.renderer.GlStateManager;

/**
 * A command slot which encases its child in a colored rectangle
 * 
 * @author Earthcomputer
 *
 */
public class CommandSlotRectangle extends CommandSlotBox {

	private int rectColor;

	public CommandSlotRectangle(IGuiCommandSlot child, int rectColor) {
		super(child);
		this.rectColor = rectColor;
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
		super.draw(x, y, mouseX, mouseY, partialTicks);
	}
	
	@Override
	protected int getPadding() {
		return 2;
	}

}
