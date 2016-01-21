package net.earthcomputer.easyeditors.api;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.List;

import org.apache.logging.log4j.Logger;
import org.lwjgl.opengl.GL11;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;

/**
 * General utilities used by Easy Editors. These will work even if Easy Editors
 * is not loaded.
 * 
 * <b>This class is a member of the Easy Editors API</b>
 * 
 * @author Earthcomputer
 *
 */
public class GeneralUtils {

	/**
	 * Plays the button sound, as if a button had been pressed in a GUI
	 */
	public static void playButtonSound() {
		Minecraft.getMinecraft().getSoundHandler()
				.playSound(PositionedSoundRecord.create(new ResourceLocation("gui.button.press"), 1));
	}

	/**
	 * Converts an unsigned hexadecimal number to a Java signed integer
	 * 
	 * @param hex
	 * @return
	 */
	public static int hexToInt(String hex) {
		long l = Long.parseLong(hex, 16);
		if (l > Integer.MAX_VALUE) {
			l -= Integer.MAX_VALUE + -((long) Integer.MIN_VALUE) + 1;
		}
		return (int) l;
	}

	/**
	 * Converts the hue, saturation and value color model to the red, green and
	 * blue color model
	 * 
	 * @param hue
	 *            - A positive integer, which, modulo 360, will represent the
	 *            hue of the color
	 * @param saturation
	 *            - An integer between 0 and 100
	 * @param value
	 *            - An integer between 0 and 100
	 * @return
	 */
	public static int hsvToRgb(int hue, int saturation, int value) {
		// Source: en.wikipedia.org/wiki/HSL_and_HSV#Converting_to_RGB#From_HSV
		hue %= 360;
		float s = (float) saturation / 100;
		float v = (float) value / 100;
		float c = v * s;
		float h = (float) hue / 60;
		float x = c * (1 - Math.abs(h % 2 - 1));
		float r, g, b;
		switch (hue / 60) {
		case 0:
			r = c;
			g = x;
			b = 0;
			break;
		case 1:
			r = x;
			g = c;
			b = 0;
			break;
		case 2:
			r = 0;
			g = c;
			b = x;
			break;
		case 3:
			r = 0;
			g = x;
			b = c;
			break;
		case 4:
			r = x;
			g = 0;
			b = c;
			break;
		case 5:
			r = c;
			g = 0;
			b = x;
			break;
		default:
			return 0;
		}
		float m = v - c;
		return ((int) ((r + m) * 255) << 16) | ((int) ((g + m) * 255) << 8) | ((int) ((b + m) * 255));
	}

	/**
	 * Converts the red, green and blue color model to the hue, saturation and
	 * value color model
	 * 
	 * @param rgb
	 * @return A 3-length array containing hue, saturation and value, in that
	 *         order. Hue is an integer between 0 and 359, or -1 if it is
	 *         undefined. Saturation and value are both integers between 0 and
	 *         100
	 */
	public static int[] rgbToHsv(int rgb) {
		// Source: en.wikipedia.org/wiki/HSV_and_HSL#Formal_derivation
		float r = (float) ((rgb & 0xff0000) >> 16) / 255;
		float g = (float) ((rgb & 0x00ff00) >> 8) / 255;
		float b = (float) (rgb & 0x0000ff) / 255;
		float M = r > g ? (r > b ? r : b) : (g > b ? g : b);
		float m = r < g ? (r < b ? r : b) : (g < b ? g : b);
		float c = M - m;
		float h;
		if (M == r) {
			h = ((g - b) / c);
			while (h < 0)
				h = 6 - h;
			h %= 6;
		} else if (M == g) {
			h = ((b - r) / c) + 2;
		} else {
			h = ((r - g) / c) + 4;
		}
		h *= 60;
		float s = c / M;
		return new int[] { c == 0 ? -1 : (int) h, (int) (s * 100), (int) (M * 100) };
	}

	/**
	 * A static alternative to
	 * {@link net.minecraft.client.gui.Gui#drawGradientRect(int, int, int, int, int, int)
	 * Gui.drawGradientRect(int, int, int, int, int, int)}
	 * 
	 * @param left
	 * @param right
	 * @param top
	 * @param bottom
	 * @param startColor
	 * @param endColor
	 * @param zLevel
	 */
	public static void drawGradientRect(int left, int right, int top, int bottom, int startColor, int endColor,
			int zLevel) {
		float startAlpha = (float) (startColor >> 24 & 255) / 255;
		float startRed = (float) (startColor >> 16 & 255) / 255;
		float startGreen = (float) (startColor >> 8 & 255) / 255;
		float startBlue = (float) (startColor & 255) / 255;
		float endAlpha = (float) (endColor >> 24 & 255) / 255;
		float endRed = (float) (endColor >> 16 & 255) / 255;
		float endGreen = (float) (endColor >> 8 & 255) / 255;
		float endBlue = (float) (endColor & 255) / 255;
		GlStateManager.disableTexture2D();
		GlStateManager.enableBlend();
		GlStateManager.disableAlpha();
		GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);
		GlStateManager.shadeModel(GL11.GL_SMOOTH);
		Tessellator tessellator = Tessellator.getInstance();
		WorldRenderer worldRenderer = tessellator.getWorldRenderer();
		// worldRenderer.startDrawingQuads();
		// worldRenderer.setColorRGBA_F(startRed, startGreen, startBlue,
		// startAlpha);
		// worldRenderer.addVertex(right, top, zLevel);
		// worldRenderer.addVertex(left, top, zLevel);
		// worldRenderer.setColorRGBA_F(endRed, endGreen, endBlue, endAlpha);
		// worldRenderer.addVertex(left, bottom, zLevel);
		// worldRenderer.addVertex(right, bottom, zLevel);
		worldRenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);
		worldRenderer.pos(right, top, zLevel).color(startRed, startGreen, startBlue, startAlpha).endVertex();
		worldRenderer.pos(left, top, zLevel).color(startRed, startGreen, startBlue, startAlpha).endVertex();
		worldRenderer.pos(left, bottom, zLevel).color(endRed, endGreen, endBlue, endAlpha).endVertex();
		worldRenderer.pos(right, bottom, zLevel).color(endRed, endGreen, endBlue, endAlpha).endVertex();
		tessellator.draw();
		GlStateManager.shadeModel(GL11.GL_FLAT);
		GlStateManager.disableBlend();
		GlStateManager.enableAlpha();
		GlStateManager.enableTexture2D();
	}

	/**
	 * Draws a tooltip, automatically splitting the string so it fits in the
	 * specified width
	 * 
	 * @param x
	 * @param y
	 * @param text
	 * @param maxWidth
	 */
	public static void drawTooltip(int x, int y, String text, int maxWidth) {
		drawTooltip(x, y, Minecraft.getMinecraft().fontRendererObj.listFormattedStringToWidth(text, maxWidth));
	}

	/**
	 * A static alternative to
	 * {@link net.minecraft.client.gui.GuiScreen#drawHoveringText(List, int, int)
	 * GuiScreen.drawHoveringText(List, int, int)}
	 * 
	 * @param x
	 * @param y
	 * @param lines
	 */
	public static void drawTooltip(int x, int y, String... lines) {
		drawTooltip(x, y, Arrays.asList(lines));
	}

	/**
	 * A static alternative to
	 * {@link net.minecraft.client.gui.GuiScreen#drawHoveringText(List, int, int)
	 * GuiScreen.drawHoveringText(List, int, int)}
	 * 
	 * @param x
	 * @param y
	 * @param lines
	 */
	public static void drawTooltip(int x, int y, List<String> lines) {
		Minecraft mc = Minecraft.getMinecraft();
		FontRenderer font = mc.fontRendererObj;
		ScaledResolution res = new ScaledResolution(mc);
		int width = res.getScaledWidth();
		int height = res.getScaledHeight();

		if (!lines.isEmpty()) {
			GlStateManager.disableRescaleNormal();
			RenderHelper.disableStandardItemLighting();
			GlStateManager.disableLighting();
			GlStateManager.disableDepth();
			int maxLineWidth = 0;
			for (String line : lines) {
				int lineWidth = font.getStringWidth(line);

				if (lineWidth > maxLineWidth) {
					maxLineWidth = lineWidth;
				}
			}

			int textX = x + 12;
			int textY = y - 12;
			int h = 8;

			if (lines.size() > 1) {
				h += 2 + (lines.size() - 1) * 10;
			}

			if (textX + maxLineWidth > width) {
				textX -= 28 + maxLineWidth;
			}

			if (textY + h + 6 > height) {
				textY = height - h - 6;
			}

			mc.getRenderItem().zLevel = 300;
			int col1 = 0xf0100010;
			drawGradientRect(textX - 3, textX + maxLineWidth + 3, textY - 4, textY - 3, col1, col1, 300);
			drawGradientRect(textX - 3, textX + maxLineWidth + 3, textY + h + 3, textY + h + 4, col1, col1, 300);
			drawGradientRect(textX - 3, textX + maxLineWidth + 3, textY - 3, textY + h + 3, col1, col1, 300);
			drawGradientRect(textX - 4, textX - 3, textY - 3, textY + h + 3, col1, col1, 300);
			drawGradientRect(textX + maxLineWidth + 3, textX + maxLineWidth + 4, textY - 3, textY + h + 3, col1, col1,
					300);
			int col2 = 0x505000ff;
			int col3 = (col2 & 0xfefefe) >> 1 | col2 & 0xff000000;
			drawGradientRect(textX - 3, textX - 3 + 1, textY - 3 + 1, textY + h + 3 - 1, col2, col3, 300);
			drawGradientRect(textX + maxLineWidth + 2, textX + maxLineWidth + 3, textY - 3 + 1, textY + h + 3 - 1, col2,
					col3, 300);
			drawGradientRect(textX - 3, textX + maxLineWidth + 3, textY - 3, textY - 3 + 1, col2, col2, 300);
			drawGradientRect(textX - 3, textX + maxLineWidth + 3, textY + h + 2, textY + h + 3, col3, col3, 300);

			for (int i = 0; i < lines.size(); i++) {
				String line = lines.get(i);
				font.drawStringWithShadow(line, textX, textY, -1);

				if (i == 0) {
					textY += 2;
				}

				textY += 10;
			}

			mc.getRenderItem().zLevel = 0;
			GlStateManager.enableLighting();
			GlStateManager.enableDepth();
			RenderHelper.enableStandardItemLighting();
			GlStateManager.enableRescaleNormal();
		}
	}

	public static void logStackTrace(Logger logger, Throwable throwable) {
		StringWriter sw = new StringWriter();
		throwable.printStackTrace(new PrintWriter(sw));
		for (String line : sw.toString().split("\n")) {
			logger.error(line);
		}
	}

}
