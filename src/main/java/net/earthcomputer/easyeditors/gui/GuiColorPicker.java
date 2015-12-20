package net.earthcomputer.easyeditors.gui;

import java.awt.Color;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.resources.I18n;

public class GuiColorPicker extends GuiScreen {

	private GuiScreen previousScreen;

	private GuiButton doneButton;
	private GuiButton cancelButton;

	private int hue;
	private int saturation = 255;
	private int brightness = 127;
	private int alpha = 255;

	public GuiColorPicker(GuiScreen previousScreen) {
		this.previousScreen = previousScreen;
	}

	@Override
	public void initGui() {
		buttonList.add(doneButton = new GuiButton(0, width / 2 - 205, height - 30, 200, 20, I18n.format("gui.done")));
		buttonList.add(cancelButton = new GuiButton(1, width / 2 + 5, height - 30, 200, 20, I18n.format("gui.cancel")));
	}

	private static final int COL = 7425;

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		drawDefaultBackground();

		GlStateManager.disableLighting();
		GlStateManager.disableFog();
		GlStateManager.enableBlend();
		GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);

		String str = I18n.format("colorPicker.title");
		drawString(fontRendererObj, str, width / 2 - fontRendererObj.getStringWidth(str) / 2, 12, 0xffffff);

		GlStateManager.disableTexture2D();
		GlStateManager.shadeModel(GL11.GL_SMOOTH);

		Tessellator tessellator = Tessellator.getInstance();
		WorldRenderer worldRenderer = tessellator.getWorldRenderer();
		int y = height / 2 - 30;
		for (float f = 0; f < 360; f += 0.25) {
			worldRenderer.startDrawing(GL11.GL_LINES);
			float fRads = (float) Math.toRadians(f);
			worldRenderer.setColorOpaque_I(0xffffff);
			worldRenderer.addVertex(100, y, 0);
			worldRenderer.setColorOpaque_I(Color.HSBtoRGB(f / 360, 1, 1));
			worldRenderer.addVertex(100 + Math.cos(fRads) * 50, y + Math.sin(fRads) * 50, 0);
			tessellator.draw();
		}

		y -= 20;
		worldRenderer.startDrawingQuads();
		worldRenderer.setColorOpaque_I(Color.HSBtoRGB((float) hue / 360, 0, (float) brightness / 255));
		worldRenderer.addVertexWithUV(250, y + 20, 0, 0, 1);
		worldRenderer.setColorOpaque_I(Color.HSBtoRGB((float) hue / 360, 1, (float) brightness / 255));
		worldRenderer.addVertexWithUV(314, y + 20, 0, 1, 1);
		worldRenderer.addVertexWithUV(314, y, 0, 1, 0);
		worldRenderer.setColorOpaque_I(Color.HSBtoRGB((float) hue / 360, 0, (float) brightness / 255));
		worldRenderer.addVertexWithUV(250, y, 0, 0, 0);
		tessellator.draw();

		y += 30;
		worldRenderer.startDrawingQuads();
		worldRenderer.setColorOpaque_I(0);
		worldRenderer.addVertexWithUV(250, y + 20, 0, 0, 1);
		worldRenderer.setColorOpaque_I(Color.HSBtoRGB((float) hue / 360, (float) saturation / 255, 0.5f));
		worldRenderer.addVertexWithUV(314, y + 20, 0, 1, 1);
		worldRenderer.addVertexWithUV(314, y, 0, 1, 0);
		worldRenderer.setColorOpaque_I(0);
		worldRenderer.addVertexWithUV(250, y, 0, 0, 0);
		tessellator.draw();

		y += 50;
		worldRenderer.startDrawingQuads();
		worldRenderer.setColorRGBA_I(
				Color.HSBtoRGB((float) hue / 360, (float) saturation / 255, (float) brightness / 255), alpha);
		worldRenderer.addVertexWithUV(70, y + 20, 0, 0, 1);
		worldRenderer.addVertexWithUV(130, y + 20, 0, 1, 1);
		worldRenderer.addVertexWithUV(130, y, 0, 1, 0);
		worldRenderer.addVertexWithUV(70, y, 0, 0, 0);
		tessellator.draw();

		GlStateManager.enableTexture2D();

		y = height / 2 - 30;
		int dist = brightness >= 128 ? (brightness - 128) * 50 / 128 : 50;
		GlStateManager.tryBlendFuncSeparate(GL11.GL_ONE_MINUS_DST_COLOR, GL11.GL_ONE_MINUS_SRC_COLOR, 1, 0);
		mc.getTextureManager().bindTexture(Gui.icons);
		this.drawTexturedModalRect(92 + (int) Math.cos(Math.toRadians(hue)) * dist,
				y + (int) Math.sin(Math.toRadians(hue)) * dist - 8, 0, 0, 16, 16);

		super.drawScreen(mouseX, mouseY, partialTicks);
	}

	@Override
	public void actionPerformed(GuiButton button) {
		switch (button.id) {
		case 0:
			mc.displayGuiScreen(previousScreen);
			break;
		case 1:
			mc.displayGuiScreen(previousScreen);
			break;
		}
	}

}
