package net.earthcomputer.easyeditors.gui.command;

import java.util.List;
import java.util.Map;

import com.google.common.base.Predicate;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.earthcomputer.easyeditors.gui.command.slot.CommandSlotVerticalArrangement;
import net.earthcomputer.easyeditors.gui.command.slot.IGuiCommandSlot;
import net.minecraft.item.Item;

/**
 * A class for creating a user-friendly interface for manipulating item damage
 * 
 * @author Earthcomputer
 *
 */
public abstract class ItemDamageHandler {

	private static final Map<Predicate<Item>, Class<? extends ItemDamageHandler>> handlers = Maps.newHashMap();

	/**
	 * Sets up the command slots in the user-friendly interface
	 * 
	 * @return
	 */
	public abstract IGuiCommandSlot[] setupCommandSlot();

	/**
	 * Sets up the pre-existing command slots so that they represent the given
	 * damage
	 * 
	 * @param damage
	 * @return The damage left to be dealt with by other handlers
	 */
	public abstract int setDamage(int damage);

	/**
	 * Reads the command slots and returns the appropriate damage value
	 * 
	 * @param initialDamage
	 *            - the damage of the item stack in CommandSlotItem
	 * @return
	 */
	public abstract int getDamage(int initialDamage);

	/**
	 * Registers an item damage handler which is active if
	 * <code>itemPredicate.apply</code> returns true
	 * 
	 * @param itemPredicate
	 * @param damageHandler
	 */
	public static void registerHandler(Predicate<Item> itemPredicate,
			Class<? extends ItemDamageHandler> damageHandler) {
		handlers.put(itemPredicate, damageHandler);
	}

	/**
	 * Returns a list of newly constructed item damage handlers for the given
	 * item
	 * 
	 * @param item
	 * @return
	 */
	public static List<ItemDamageHandler> getHandlers(Item item) {
		List<ItemDamageHandler> handlers = Lists.newArrayList();
		for (Map.Entry<Predicate<Item>, Class<? extends ItemDamageHandler>> entry : ItemDamageHandler.handlers
				.entrySet()) {
			if (entry.getKey().apply(item)) {
				try {
					handlers.add(entry.getValue().getConstructor().newInstance());
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		return handlers;
	}

	/**
	 * Sets up the command slots in the user interface, using the given handlers
	 * which have been constructed using {@link #getHandlers(Item)}
	 * 
	 * @param handlers
	 * @return
	 */
	public static IGuiCommandSlot setupCommandSlot(List<ItemDamageHandler> handlers) {
		List<IGuiCommandSlot> slots = Lists.newArrayList();
		for (ItemDamageHandler handler : handlers) {
			for (IGuiCommandSlot slot : handler.setupCommandSlot()) {
				slots.add(slot);
			}
		}
		return slots.isEmpty() ? null
				: (slots.size() == 1 ? slots.get(0)
						: new CommandSlotVerticalArrangement(slots.toArray(new IGuiCommandSlot[slots.size()])));
	}

	/**
	 * Uses the handlers to set up the command slots to represent the given item
	 * damage
	 * 
	 * @param handlers
	 * @param damage
	 * @return The damage left to be dealt with by the default handlers
	 */
	public static int setDamage(List<ItemDamageHandler> handlers, int damage) {
		for (ItemDamageHandler handler : handlers) {
			damage = handler.setDamage(damage);
		}
		return damage;
	}

	/**
	 * Uses the handlers to find the item damage from the command slots
	 * 
	 * @param handlers
	 * @param initialDamage
	 * @return
	 */
	public static int getDamage(List<ItemDamageHandler> handlers, int initialDamage) {
		for (ItemDamageHandler handler : handlers) {
			initialDamage = handler.getDamage(initialDamage);
		}
		return initialDamage;
	}

}
