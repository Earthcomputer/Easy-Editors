package net.earthcomputer.easyeditors.gui.command;

import net.minecraft.item.Item;
import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;

/**
 * An event which is fired when {@link ItemDamageHandler#getHandlers(Item)}
 * tries to add a handler to the list of returned handlers. Cancel this event if
 * you wish for this damage handler not to be added to the list of returned
 * handlers.
 * 
 * This event is {@link Cancelable}.
 * 
 * This event is fired on the
 * {@link net.minecraftforge.common.MinecraftForge#EVENT_BUS
 * MinecraftForge.EVENT_BUS}
 * 
 * @author Earthcomputer
 *
 */
@Cancelable
public class ItemDamageHandlerEvent extends Event {

	/**
	 * The item the handler is being constructed for
	 */
	public final Item item;
	/**
	 * The class of the handler to be constructed
	 */
	public final Class<? extends ItemDamageHandler> handler;

	public ItemDamageHandlerEvent(Item item, Class<? extends ItemDamageHandler> handler) {
		this.item = item;
		this.handler = handler;
	}

}
