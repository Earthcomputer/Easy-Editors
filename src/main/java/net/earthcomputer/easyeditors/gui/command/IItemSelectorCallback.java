package net.earthcomputer.easyeditors.gui.command;

import net.minecraft.item.ItemStack;

/**
 * An interface for use with the item selector. Once the user has selected an
 * item, setItem will be invoked
 * 
 * @author Earthcomputer
 *
 */
public interface IItemSelectorCallback {

	/**
	 * 
	 * @return The currently selected item
	 */
	ItemStack getItem();

	/**
	 * Sets the currently selected item
	 * 
	 * @param item
	 */
	void setItem(ItemStack item);

}
