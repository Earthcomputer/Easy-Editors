package net.earthcomputer.easyeditors.gui.command.slot;

import java.util.List;

import org.lwjgl.opengl.GL11;

import net.earthcomputer.easyeditors.gui.GuiColorPicker;
import net.earthcomputer.easyeditors.gui.IColorPickerCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.I18n;
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
			WorldRenderer worldRenderer = tessellator.getWorldRenderer();
			worldRenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);
			worldRenderer.pos(x, y + getHeight(), 0).tex(0, (float) getHeight() / 16).endVertex();
			worldRenderer.pos(x + getWidth(), y + getHeight(), 0).tex((float) getWidth() / 16, (float) getHeight() / 16)
					.endVertex();
			worldRenderer.pos(x + getWidth(), y, 0).tex((float) getWidth() / 16, 0).endVertex();
			worldRenderer.pos(x, y, 0).tex(0, 0).endVertex();
			tessellator.draw();
		}
		Gui.drawRect(x, y, x + getWidth(), y + getHeight(), color);
		drawHorizontalLine(x, x + getWidth(), y, 0);
		drawHorizontalLine(x, x + getWidth(), y + getHeight(), 0);
		drawVerticalLine(x, y, y + getHeight(), 0);
		drawVerticalLine(x + getWidth(), y, y + getHeight(), 0);

		if (hoverChecker == null)
			hoverChecker = new HoverChecker(y, y + getHeight(), x, x + getWidth(), 1000);
		else
			hoverChecker.updateBounds(y, y + getHeight(), x, x + getWidth());
		if (hoverChecker.checkHover(mouseX, mouseY))
			drawTooltip(mouseX, mouseY, I18n.format("gui.easyeditorsconfig.colortooltip"));
	}

	@Override
	public boolean onMouseClicked(int mouseX, int mouseY, int mouseButton) {
		if (mouseButton == 0) {
			if (mouseX >= x && mouseX < x + getWidth() && mouseY >= y && mouseY < y + getHeight())
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
