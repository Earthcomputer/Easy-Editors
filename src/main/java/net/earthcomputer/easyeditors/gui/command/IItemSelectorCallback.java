package net.earthcomputer.easyeditors.gui.command;

import net.minecraft.item.ItemStack;

public interface IItemSelectorCallback {

	ItemStack getItem();
	
	void setItem(ItemStack item);
	
}
