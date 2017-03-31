package net.earthcomputer.easyeditors.gui.command.slot;

import net.earthcomputer.easyeditors.api.util.GeneralUtils;
import net.earthcomputer.easyeditors.gui.GuiSelectFromList;
import net.earthcomputer.easyeditors.gui.ICallback;
import net.earthcomputer.easyeditors.gui.command.GuiSelectSound;
import net.earthcomputer.easyeditors.util.Translate;
import net.earthcomputer.easyeditors.util.TranslateKeys;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

public class CommandSlotSound extends CommandSlotSelectFromRegistry<SoundEvent> {

	public CommandSlotSound() {
		super(Translate.GUI_COMMANDEDITOR_NOSOUND, TranslateKeys.GUI_COMMANDEDITOR_NOSOUNDSELECTED,
				ForgeRegistries.SOUND_EVENTS);
	}

	@Override
	protected String getDisplayName(ResourceLocation val) {
		String soundId = GeneralUtils.resourceLocationToString(val);
		ITextComponent subtitleComponent = Minecraft.getMinecraft().getSoundHandler().getAccessor(val).getSubtitle();
		return subtitleComponent == null ? soundId : subtitleComponent.getFormattedText();
	}

	@Override
	protected String getDisplayNameForRegistryEntry(SoundEvent val) {
		throw new UnsupportedOperationException();
	}

	@Override
	protected GuiSelectFromList<ResourceLocation> createGui(GuiScreen currentScreen,
			ICallback<ResourceLocation> callback) {
		return new GuiSelectSound(currentScreen, callback);
	}

}
