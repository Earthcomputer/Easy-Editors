package net.earthcomputer.easyeditors.gui.command;

import net.earthcomputer.easyeditors.gui.GuiSelectFromRegistry;
import net.earthcomputer.easyeditors.gui.ICallback;
import net.earthcomputer.easyeditors.util.Translate;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.potion.Potion;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

public class GuiSelectEffect extends GuiSelectFromRegistry<Potion> {

	public GuiSelectEffect(GuiScreen prevScreen, ICallback<ResourceLocation> callback) {
		super(prevScreen, callback, ForgeRegistries.POTIONS, Translate.GUI_COMMANDEDITOR_SELECTEFFECT_TITLE);
	}

	@Override
	protected String getLocalizedName(Potion value) {
		return I18n.format(value.getName());
	}

}
