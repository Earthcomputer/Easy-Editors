package net.earthcomputer.easyeditors.gui.command;

import net.earthcomputer.easyeditors.gui.GuiSelectFromRegistry;
import net.earthcomputer.easyeditors.gui.ICallback;
import net.earthcomputer.easyeditors.util.Translate;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

/**
 * A GUI for selecting an enchantment
 * 
 * @author Earthcomputer
 *
 */
public class GuiSelectEnchantment extends GuiSelectFromRegistry<Enchantment> {

	public GuiSelectEnchantment(GuiScreen prevScreen, ICallback<ResourceLocation> callback) {
		super(prevScreen, callback, ForgeRegistries.ENCHANTMENTS, Translate.GUI_COMMANDEDITOR_SELECTENCHANTMENT_TITLE);
	}

	@Override
	protected String getLocalizedName(Enchantment value) {
		return I18n.format(value.getName());
	}

}
