package net.earthcomputer.easyeditors.gui.command;

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
	int getEnchantment();

	/**
	 * Sets the currently selected enchantment by enchantment ID
	 * 
	 * @param id
	 */
	void setEnchantment(int id);

}
