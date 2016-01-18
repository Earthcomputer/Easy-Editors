package net.earthcomputer.easyeditors.gui.command;

import net.minecraft.item.Item;
import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;

@Cancelable
public class ItemDamageHandlerEvent extends Event {

	public final Item item;
	public final Class<? extends ItemDamageHandler> handler;
	
	public ItemDamageHandlerEvent(Item item, Class<? extends ItemDamageHandler> handler) {
		this.item = item;
		this.handler = handler;
	}
	
}
