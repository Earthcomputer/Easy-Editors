package net.earthcomputer.easyeditors.gui.command;

import net.minecraft.util.ResourceLocation;

/**
 * An interface for use with GuiSelectEnchantment. When the user has chosen an
 * enchantment, setEnchantment will be invoked
 * 
 * @author Earthcomputer
 *
 */
public interface IEnchantmentSelectorCallback {

	/**
	 * 
	 * @return The enchantment ID of the currently selected enchantment
	 */
	ResourceLocation getEnchantment();

	/**
	 * Sets the currently selected enchantment by enchantment ID
	 * 
	 * @param id
	 */
	void setEnchantment(ResourceLocation id);

}
