package net.earthcomputer.easyeditors.gui.command.slot;

import java.util.List;

import org.lwjgl.opengl.GL11;

import net.earthcomputer.easyeditors.gui.GuiColorPicker;
import net.earthcomputer.easyeditors.gui.IColorPickerCallback;
import net.earthcomputer.easyeditors.util.Translate;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraftforge.fml.client.config.HoverChecker;

/**
 * A command slot which can be used to pick a color. This command slot does not
 * read from and write from arguments, so if you require this functionality, you
 * must create a subclass
 * 
 * @author Earthcomputer
 *
 */
public class CommandSlotColor extends GuiCommandSlotImpl implements IColorPickerCallback {

	private int color;
	private boolean allowAlpha;

	private int x;
	private int y;
	private HoverChecker hoverChecker;

	public CommandSlotColor() {
		this(true);
	}

	public CommandSlotColor(boolean allowAlpha) {
		super(50, 20);
		this.color = allowAlpha ? 0xffffffff : 0xffffff;
		this.allowAlpha = allowAlpha;
	}

	@Override
	public int readFromArgs(String[] args, int index) {
		return 0;
	}

	@Override
	public void addArgs(List<String> args) {
	}

	@Override
	public void draw(int x, int y, int mouseX, int mouseY, float partialTicks) {
		this.x = x;
		this.y = y;

		int color = getColor();
		if ((color & 0xff000000) != 0xff000000) {
			GlStateManager.color(1, 1, 1, 1);
			Minecraft.getMinecraft().getTextureManager().bindTexture(GuiColorPicker.transparentBackground);
			Tessellator tessellator = Tessellator.getInstance();
			VertexBuffer buffer = tessellator.getBuffer();
			buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
			buffer.pos(x, y + getHeight(), 0).tex(0, (float) getHeight() / 16).endVertex();
			buffer.pos(x + getWidth(), y + getHeight(), 0).tex((float) getWidth() / 16, (float) getHeight() / 16)
					.endVertex();
			buffer.pos(x + getWidth(), y, 0).tex((float) getWidth() / 16, 0).endVertex();
			buffer.pos(x, y, 0).tex(0, 0).endVertex();
			tessellator.draw();
		}
		Gui.drawRect(x, y, x + getWidth(), y + getHeight(), color);
		drawHorizontalLine(x, x + getWidth(), y, 0xff000000);
		drawHorizontalLine(x, x + getWidth(), y + getHeight(), 0xff000000);
		drawVerticalLine(x, y, y + getHeight(), 0xff000000);
		drawVerticalLine(x + getWidth(), y, y + getHeight(), 0xff000000);

		if (hoverChecker == null)
			hoverChecker = new HoverChecker(y, y + getHeight(), x, x + getWidth(), 1000);
		else
			hoverChecker.updateBounds(y, y + getHeight(), x, x + getWidth());
		if (!getContext().isMouseInBounds(mouseX, mouseY))
			hoverChecker.resetHoverTimer();
		else if (hoverChecker.checkHover(mouseX, mouseY))
			drawTooltip(mouseX, mouseY, Translate.GUI_EASYEDITORSCONFIG_COLORTOOLTIP);
	}

	@Override
	public boolean onMouseClicked(int mouseX, int mouseY, int mouseButton) {
		if (mouseButton == 0) {
			if (getContext().isMouseInBounds(mouseX, mouseY) && mouseX >= x && mouseX < x + getWidth() && mouseY >= y
					&& mouseY < y + getHeight())
				Minecraft.getMinecraft()
						.displayGuiScreen(new GuiColorPicker(Minecraft.getMinecraft().currentScreen, this, allowAlpha));
		}
		return false;
	}

	@Override
	public int getColor() {
		return allowAlpha ? color : color | 0xff000000;
	}

	@Override
	public void setColor(int color) {
		this.color = allowAlpha ? color : color & 0x00ffffff;
	}

}
