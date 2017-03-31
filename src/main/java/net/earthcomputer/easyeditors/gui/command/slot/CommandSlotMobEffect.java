package net.earthcomputer.easyeditors.gui.command.slot;

import net.earthcomputer.easyeditors.gui.GuiSelectFromList;
import net.earthcomputer.easyeditors.gui.ICallback;
import net.earthcomputer.easyeditors.gui.command.GuiSelectEffect;
import net.earthcomputer.easyeditors.util.Translate;
import net.earthcomputer.easyeditors.util.TranslateKeys;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.potion.Potion;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

public class CommandSlotMobEffect extends CommandSlotSelectFromRegistry<Potion> {

	public CommandSlotMobEffect() {
		super(Translate.GUI_COMMANDEDITOR_NOEFFECT, TranslateKeys.GUI_COMMANDEDITOR_NOEFFECTSELECTED,
				ForgeRegistries.POTIONS);
	}

	@Override
	protected String getDisplayNameForRegistryEntry(Potion val) {
		return I18n.format(val.getName());
	}

	@Override
	protected ResourceLocation readArg(String arg) {
		try {
			Potion potion = Potion.getPotionById(Integer.parseInt(arg));
			if (potion != null) {
				return super.readArg(potion.delegate.name().toString());
			}
		} catch (NumberFormatException e) {
		}
		return super.readArg(arg);
	}

	@Override
	protected GuiSelectFromList<ResourceLocation> createGui(GuiScreen currentScreen,
			ICallback<ResourceLocation> callback) {
		return new GuiSelectEffect(currentScreen, callback);
	}

}
