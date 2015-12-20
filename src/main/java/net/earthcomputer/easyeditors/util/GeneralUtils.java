package net.earthcomputer.easyeditors.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.util.ResourceLocation;

public class GeneralUtils {

	public static void playButtonSound() {
		Minecraft.getMinecraft().getSoundHandler()
				.playSound(PositionedSoundRecord.create(new ResourceLocation("gui.button.press"), 1));
	}

	public static int hexToInt(String hex) {
		long l = Long.parseLong(hex, 16);
		if (l > Integer.MAX_VALUE) {
			l -= Integer.MAX_VALUE + -((long) Integer.MIN_VALUE) + 1;
		}
		return (int) l;
	}

}
