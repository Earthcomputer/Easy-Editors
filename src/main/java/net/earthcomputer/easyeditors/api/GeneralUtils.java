package net.earthcomputer.easyeditors.api;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
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

}
