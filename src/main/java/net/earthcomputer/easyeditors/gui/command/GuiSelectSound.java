package net.earthcomputer.easyeditors.gui.command;

import net.earthcomputer.easyeditors.api.util.GeneralUtils;
import net.earthcomputer.easyeditors.gui.GuiSelectFromRegistry;
import net.earthcomputer.easyeditors.gui.ICallback;
import net.earthcomputer.easyeditors.util.Translate;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

public class GuiSelectSound extends GuiSelectFromRegistry<SoundEvent> {

	public GuiSelectSound(GuiScreen prevScreen, ICallback<ResourceLocation> callback) {
		super(prevScreen, callback, ForgeRegistries.SOUND_EVENTS, Translate.GUI_COMMANDEDITOR_SELECTSOUND_TITLE);
	}

	@Override
	protected String getLocalizedName(SoundEvent value) {
		// Subtitles in vanilla are stored in the sounds.json file, and are
		// obtained from
		// ISound.createAccessor(SoundHandler).getSubtitle().getFormattedText(),
		// but for sounds played via the /playsound command send a
		// SPacketCustomSound to clients which creates and plays a
		// PositionedSoundRecord, which implements createAccessor(SoundHandler)
		// by calling SoundHandler.getAccessor(ResourceLocation).
		ITextComponent subtitle = Minecraft.getMinecraft().getSoundHandler().getAccessor(value.getSoundName())
				.getSubtitle();
		return subtitle == null ? GeneralUtils.resourceLocationToString(value.getSoundName())
				: subtitle.getFormattedText();
	}

}
