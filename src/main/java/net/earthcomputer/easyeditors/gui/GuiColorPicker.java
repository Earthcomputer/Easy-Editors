package net.earthcomputer.easyeditors.gui;

import java.awt.Color;
import java.io.IOException;

import org.lwjgl.opengl.GL11;

import net.earthcomputer.easyeditors.util.GeneralUtils;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.MathHelper;

public class GuiColorPicker extends GuiScreen {

	private GuiScreen previousScreen;

	private GuiButton doneButton;
	private GuiButton cancelButton;

	private int hue;
	private int saturation = 100;
	private int value = 100;
	private int alpha = 255;

	/**
	 * -1 for nothing, 0 for wheel, 1 for hue, 2 for saturation, 3 for value, 4
	 * for red, 5 for green, 6 for blue
	 */
	private int clicked = -1;

	public GuiColorPicker(GuiScreen previousScreen) {
		this.previousScreen = previousScreen;
	}

	@Override
	public void initGui() {
		buttonList.add(doneButton = new GuiButton(0, width / 2 - 205, height - 30, 200, 20, I18n.format("gui.done")));
		buttonList.add(cancelButton = new GuiButton(1, width / 2 + 5, height - 30, 200, 20, I18n.format("gui.cancel")));
	}

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
		worldRenderer.setColorOpaque_I(GeneralUtils.hsvToRgb(hue, 0, value));
		worldRenderer.addVertexWithUV(250, y + 20, 0, 0, 1);
		worldRenderer.setColorOpaque_I(GeneralUtils.hsvToRgb(hue, 100, value));
		worldRenderer.addVertexWithUV(314, y + 20, 0, 1, 1);
		worldRenderer.addVertexWithUV(314, y, 0, 1, 0);
		worldRenderer.setColorOpaque_I(GeneralUtils.hsvToRgb(hue, 0, value));
		worldRenderer.addVertexWithUV(250, y, 0, 0, 0);
		tessellator.draw();

		y += 30;
		worldRenderer.startDrawingQuads();
		worldRenderer.setColorOpaque_I(0);
		worldRenderer.addVertexWithUV(250, y + 20, 0, 0, 1);
		worldRenderer.setColorOpaque_I(GeneralUtils.hsvToRgb(hue, saturation, 100));
		worldRenderer.addVertexWithUV(314, y + 20, 0, 1, 1);
		worldRenderer.addVertexWithUV(314, y, 0, 1, 0);
		worldRenderer.setColorOpaque_I(0);
		worldRenderer.addVertexWithUV(250, y, 0, 0, 0);
		tessellator.draw();

		y += 50;
		worldRenderer.startDrawingQuads();
		worldRenderer.setColorRGBA_I(GeneralUtils.hsvToRgb(hue, saturation, value), alpha);
		worldRenderer.addVertexWithUV(70, y + 20, 0, 0, 1);
		worldRenderer.addVertexWithUV(130, y + 20, 0, 1, 1);
		worldRenderer.addVertexWithUV(130, y, 0, 1, 0);
		worldRenderer.addVertexWithUV(70, y, 0, 0, 0);
		tessellator.draw();

		GlStateManager.enableTexture2D();

		y = height / 2 - 30;
		int dist = saturation / 2;
		GlStateManager.tryBlendFuncSeparate(GL11.GL_ONE_MINUS_DST_COLOR, GL11.GL_ONE_MINUS_SRC_COLOR, 1, 0);
		mc.getTextureManager().bindTexture(Gui.icons);
		this.drawTexturedModalRect(93 + (int) (Math.cos(Math.toRadians(hue)) * dist),
				y + (int) (Math.sin(Math.toRadians(hue)) * dist) - 7, 0, 0, 16, 16);

		super.drawScreen(mouseX, mouseY, partialTicks);
	}

	@Override
	public void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
		if (mouseButton == 0) {
			int y = height / 2 - 30;
			int dx = mouseX - 100;
			int dy = mouseY - y;
			if (dx * dx + dy * dy <= 50 * 50) {
				clicked = 0;
				updateWheelClick(dx, dy);
			}

			y -= 20;
			if (mouseX >= 250 && mouseX < 314 && mouseY >= y && mouseY < y + 20) {
				clicked = 1;
				updateSaturationClick(mouseX - 250);
			}
		}
		super.mouseClicked(mouseX, mouseY, mouseButton);
	}

	@Override
	public void mouseReleased(int mouseX, int mouseY, int mouseButton) {
		if (mouseButton == 0)
			clicked = -1;
	}

	@Override
	public void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
		switch (clicked) {
		case 0:
			updateWheelClick(mouseX - 100, mouseY - (height / 2 - 30));
			break;
		case 1:
			updateSaturationClick(mouseX - 250);
			break;
		}
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

	private void updateWheelClick(int dx, int dy) {
		hue = (int) Math.toDegrees(Math.atan2(dy, dx));
		if (hue < 0)
			hue += 360;

		int dist = (int) Math.sqrt(dx * dx + dy * dy);
		if (dist > 50)
			dist = 50;
		saturation = dist * 2;
	}

	private void updateSaturationClick(int dx) {
		saturation = MathHelper.clamp_int(dx * 100 / 64, 0, 100);
	}

}
