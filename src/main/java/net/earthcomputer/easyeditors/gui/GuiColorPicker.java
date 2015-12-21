package net.earthcomputer.easyeditors.gui;

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
	private int rgb = 0xff0000;
	private int alpha = 255;

	/**
	 * -1 for nothing, 0 for wheel, 1 for hue, 2 for saturation, 3 for value, 4
	 * for red, 5 for green, 6 for blue, 7 for alpha
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
		int x = 100;
		int y = height / 2 - 30;
		for (float f = 0; f < 360; f += 0.25) {
			worldRenderer.startDrawing(GL11.GL_LINES);
			float fRads = (float) Math.toRadians(f);
			worldRenderer.setColorOpaque_I(0xffffff);
			worldRenderer.addVertex(x, y, 0);
			worldRenderer.setColorOpaque_I(GeneralUtils.hsvToRgb((int) f, 100, 100));
			worldRenderer.addVertex(x + Math.cos(fRads) * 50, y + Math.sin(fRads) * 50, 0);
			tessellator.draw();
		}

		x = width / 2;

		y -= 10;
		for (int f = 0; f < 64; f += 8) {
			worldRenderer.startDrawingQuads();
			worldRenderer.setColorOpaque_I(GeneralUtils.hsvToRgb(f * 360 / 64, 100, 100));
			worldRenderer.addVertexWithUV(x + f, y + 20, 0, 0, 1);
			worldRenderer.setColorOpaque_I(GeneralUtils.hsvToRgb((f + 8) * 360 / 64, 100, 100));
			worldRenderer.addVertexWithUV(x + f + 8, y + 20, 0, 1, 1);
			worldRenderer.addVertexWithUV(x + f + 8, y, 0, 1, 1);
			worldRenderer.setColorOpaque_I(GeneralUtils.hsvToRgb(f * 360 / 64, 100, 100));
			worldRenderer.addVertexWithUV(x + f, y, 0, 0, 0);
			tessellator.draw();
		}

		y += 30;
		worldRenderer.startDrawingQuads();
		worldRenderer.setColorOpaque_I(GeneralUtils.hsvToRgb(hue, 0, value));
		worldRenderer.addVertexWithUV(x, y + 20, 0, 0, 1);
		worldRenderer.setColorOpaque_I(GeneralUtils.hsvToRgb(hue, 100, value));
		worldRenderer.addVertexWithUV(x + 64, y + 20, 0, 1, 1);
		worldRenderer.addVertexWithUV(x + 64, y, 0, 1, 0);
		worldRenderer.setColorOpaque_I(GeneralUtils.hsvToRgb(hue, 0, value));
		worldRenderer.addVertexWithUV(x, y, 0, 0, 0);
		tessellator.draw();

		y += 30;
		worldRenderer.startDrawingQuads();
		worldRenderer.setColorOpaque_I(0);
		worldRenderer.addVertexWithUV(x, y + 20, 0, 0, 1);
		worldRenderer.setColorOpaque_I(GeneralUtils.hsvToRgb(hue, saturation, 100));
		worldRenderer.addVertexWithUV(x + 64, y + 20, 0, 1, 1);
		worldRenderer.addVertexWithUV(x + 64, y, 0, 1, 0);
		worldRenderer.setColorOpaque_I(0);
		worldRenderer.addVertexWithUV(x, y, 0, 0, 0);
		tessellator.draw();
		
		x += 100;
		y = height / 2 - 40;
		worldRenderer.startDrawingQuads();
		worldRenderer.setColorOpaque_I(rgb & 0x00ffff);
		worldRenderer.addVertexWithUV(x, y + 20, 0, 0, 1);
		worldRenderer.setColorOpaque_I(rgb | 0xff0000);
		worldRenderer.addVertexWithUV(x + 64, y + 20, 0, 1, 1);
		worldRenderer.addVertexWithUV(x + 64, y, 0, 1, 0);
		worldRenderer.setColorOpaque_I(rgb & 0x00ffff);
		worldRenderer.addVertexWithUV(x, y, 0, 0, 0);
		tessellator.draw();

		y += 30;
		worldRenderer.startDrawingQuads();
		worldRenderer.setColorOpaque_I(rgb & 0xff00ff);
		worldRenderer.addVertexWithUV(x, y + 20, 0, 0, 1);
		worldRenderer.setColorOpaque_I(rgb | 0x00ff00);
		worldRenderer.addVertexWithUV(x + 64, y + 20, 0, 1, 1);
		worldRenderer.addVertexWithUV(x + 64, y, 0, 1, 0);
		worldRenderer.setColorOpaque_I(rgb & 0xff00ff);
		worldRenderer.addVertexWithUV(x, y, 0, 0, 0);
		tessellator.draw();
		
		y += 30;
		worldRenderer.startDrawingQuads();
		worldRenderer.setColorOpaque_I(rgb & 0xffff00);
		worldRenderer.addVertexWithUV(x, y + 20, 0, 0, 1);
		worldRenderer.setColorOpaque_I(rgb | 0x0000ff);
		worldRenderer.addVertexWithUV(x + 64, y + 20, 0, 1, 1);
		worldRenderer.addVertexWithUV(x + 64, y, 0, 1, 0);
		worldRenderer.setColorOpaque_I(rgb & 0xffff00);
		worldRenderer.addVertexWithUV(x, y, 0, 0, 0);
		tessellator.draw();
		
		x = 100;
		y = height / 2 + 30;
		worldRenderer.startDrawingQuads();
		worldRenderer.setColorRGBA_I(rgb, alpha);
		worldRenderer.addVertexWithUV(x - 30, y + 20, 0, 0, 1);
		worldRenderer.addVertexWithUV(x + 30, y + 20, 0, 1, 1);
		worldRenderer.addVertexWithUV(x + 30, y, 0, 1, 0);
		worldRenderer.addVertexWithUV(x - 30, y, 0, 0, 0);
		tessellator.draw();

		x = width / 2;
		GlStateManager.tryBlendFuncSeparate(GL11.GL_ONE_MINUS_DST_COLOR, GL11.GL_ONE_MINUS_SRC_COLOR, 1, 0);
		y = height / 2 - 40;
		worldRenderer.startDrawing(GL11.GL_LINES);
		worldRenderer.addVertex(x + hue * 64 / 360, y, 0);
		worldRenderer.addVertex(x + hue * 64 / 360, y + 20, 0);
		tessellator.draw();
		
		y += 30;
		worldRenderer.startDrawing(GL11.GL_LINES);
		worldRenderer.addVertex(x + saturation * 64 / 100, y, 0);
		worldRenderer.addVertex(x + saturation * 64 / 100, y + 20, 0);
		tessellator.draw();
		
		y += 30;
		worldRenderer.startDrawing(GL11.GL_LINES);
		worldRenderer.addVertex(x + value * 64 / 100, y, 0);
		worldRenderer.addVertex(x + value * 64 / 100, y + 20, 0);
		tessellator.draw();
		
		x += 100;
		y = height / 2 - 40;
		worldRenderer.startDrawing(GL11.GL_LINES);
		worldRenderer.addVertex(x + ((rgb & 0xff0000) >> 16) * 64 / 255, y, 0);
		worldRenderer.addVertex(x + ((rgb & 0xff0000) >> 16) * 64 / 255, y + 20, 0);
		tessellator.draw();
		
		y += 30;
		worldRenderer.startDrawing(GL11.GL_LINES);
		worldRenderer.addVertex(x + ((rgb & 0x00ff00) >> 8) * 64 / 255, y, 0);
		worldRenderer.addVertex(x + ((rgb & 0x00ff00) >> 8) * 64 / 255, y + 20, 0);
		tessellator.draw();
		
		y += 30;
		worldRenderer.startDrawing(GL11.GL_LINES);
		worldRenderer.addVertex(x + (rgb & 0x0000ff) * 64 / 255, y, 0);
		worldRenderer.addVertex(x + (rgb & 0x0000ff) * 64 / 255, y + 20, 0);
		tessellator.draw();

		GlStateManager.enableTexture2D();

		x = 100;
		y = height / 2 - 30;
		int dist = saturation / 2;
		mc.getTextureManager().bindTexture(Gui.icons);
		this.drawTexturedModalRect(x + (int) (Math.cos(Math.toRadians(hue)) * dist - 7),
				y + (int) (Math.sin(Math.toRadians(hue)) * dist) - 7, 0, 0, 16, 16);
		
		GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);
		x = width / 2;
		y -= 10;
		drawString(fontRendererObj, "H:", x - 10, y + 5, 0xffffff);
		drawString(fontRendererObj, "R:", x + 90, y + 5, 0xffffff);
		y += 30;
		drawString(fontRendererObj, "S:", x - 10, y + 5, 0xffffff);
		drawString(fontRendererObj, "G:", x + 90, y + 5, 0xffffff);
		y += 30;
		drawString(fontRendererObj, "V:", x - 10, y + 5, 0xffffff);
		drawString(fontRendererObj, "B:", x + 90, y + 5, 0xffffff);

		super.drawScreen(mouseX, mouseY, partialTicks);
	}

	@Override
	public void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
		if (mouseButton == 0) {
			int x = 100;
			int y = height / 2 - 30;
			int dx = mouseX - x;
			int dy = mouseY - y;
			if (dx * dx + dy * dy <= 50 * 50) {
				clicked = 0;
				updateWheelClick(dx, dy);
			}

			x = width / 2;
			y -= 10;
			if (mouseX >= x && mouseX < x + 64 && mouseY >= y && mouseY < y + 20) {
				clicked = 1;
				updateHueClick(mouseX - x);
			}

			y += 30;
			if (mouseX >= x && mouseX < x + 64 && mouseY >= y && mouseY < y + 20) {
				clicked = 2;
				updateSaturationClick(mouseX - x);
			}
			y += 30;
			if (mouseX >= x && mouseX < x + 64 && mouseY >= y && mouseY < y + 20) {
				clicked = 3;
				updateValueClick(mouseX - x);
			}
			x += 100;
			y = height / 2 - 40;
			if (mouseX >= x && mouseX < x + 64 && mouseY >= y && mouseY < y + 20) {
				clicked = 4;
				updateRedClick(mouseX - x);
			}
			y += 30;
			if (mouseX >= x && mouseY < x + 64 && mouseY >= y && mouseY < y + 20) {
				clicked = 5;
				updateGreenClick(mouseX - x);
			}
			y += 30;
			if (mouseX >= x && mouseY < x + 64 && mouseY >= y && mouseY < y + 20) {
				clicked = 6;
				updateBlueClick(mouseX - x);
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
		int hsvBoxesLeft = width / 2;
		int rgbBoxesLeft = hsvBoxesLeft + 100;
		switch (clicked) {
		case 0:
			updateWheelClick(mouseX - 100, mouseY - (height / 2 - 30));
			break;
		case 1:
			updateHueClick(mouseX - hsvBoxesLeft);
			break;
		case 2:
			updateSaturationClick(mouseX - hsvBoxesLeft);
			break;
		case 3:
			updateValueClick(mouseX - hsvBoxesLeft);
			break;
		case 4:
			updateRedClick(mouseX - rgbBoxesLeft);
			break;
		case 5:
			updateGreenClick(mouseX - rgbBoxesLeft);
			break;
		case 6:
			updateBlueClick(mouseX - rgbBoxesLeft);
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
		
		rgb = GeneralUtils.hsvToRgb(hue, saturation, value);
	}

	private void updateHueClick(int dx) {
		hue = MathHelper.clamp_int(dx * 360 / 64, 0, 360);
		
		rgb = GeneralUtils.hsvToRgb(hue, saturation, value);
	}

	private void updateSaturationClick(int dx) {
		saturation = MathHelper.clamp_int(dx * 100 / 64, 0, 100);
		
		rgb = GeneralUtils.hsvToRgb(hue, saturation, value);
	}

	private void updateValueClick(int dx) {
		value = MathHelper.clamp_int(dx * 100 / 64, 0, 100);
		
		rgb = GeneralUtils.hsvToRgb(hue, saturation, value);
	}
	
	private void updateRedClick(int dx) {
		dx = MathHelper.clamp_int(dx, 0, 64);
		rgb &= 0x00ffff;
		rgb |= (dx * 255 / 64) << 16;
		int[] hsv = GeneralUtils.rgbToHsv(rgb);
		if (hsv[0] != -1)
			hue = hsv[0];
		saturation = hsv[1];
		value = hsv[2];
	}
	
	private void updateGreenClick(int dx) {
		dx = MathHelper.clamp_int(dx, 0, 64);
		rgb &= 0xff00ff;
		rgb |= (dx * 255 / 64) << 8;
		int[] hsv = GeneralUtils.rgbToHsv(rgb);
		if (hsv[0] != -1)
			hue = hsv[0];
		saturation = hsv[1];
		value = hsv[2];
	}
	
	private void updateBlueClick(int dx) {
		dx = MathHelper.clamp_int(dx, 0, 64);
		rgb &= 0xffff00;
		rgb |= dx * 255 / 64;
		int[] hsv = GeneralUtils.rgbToHsv(rgb);
		if (hsv[0] != -1)
			hue = hsv[0];
		saturation = hsv[1];
		value = hsv[2];
	}

}
