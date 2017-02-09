package net.earthcomputer.easyeditors.gui.command;

import java.util.List;
import java.util.Map;

import com.google.common.base.Predicate;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.earthcomputer.easyeditors.api.EasyEditorsApi;
import net.earthcomputer.easyeditors.api.util.Colors;
import net.earthcomputer.easyeditors.api.util.GeneralUtils;
import net.earthcomputer.easyeditors.api.util.Predicates2;
import net.earthcomputer.easyeditors.gui.command.slot.CommandSlotIntTextField;
import net.earthcomputer.easyeditors.gui.command.slot.CommandSlotLabel;
import net.earthcomputer.easyeditors.gui.command.slot.CommandSlotVerticalArrangement;
import net.earthcomputer.easyeditors.gui.command.slot.IGuiCommandSlot;
import net.earthcomputer.easyeditors.util.Translate;
import net.earthcomputer.easyeditors.util.TranslateKeys;
import net.minecraft.client.Minecraft;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;

/**
 * A class for creating a user-friendly interface for manipulating item damage
 * 
 * @author Earthcomputer
 *
 */
public abstract class ItemDamageHandler {

	private static final Map<Predicate<ItemStack>, Class<? extends ItemDamageHandler>> handlers = Maps.newHashMap();

	/**
	 * Sets up the command slots in the user-friendly interface
	 * 
	 * @return
	 */
	public abstract IGuiCommandSlot[] setupCommandSlot(ItemStack item);

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
	 * 
	 * @throws UIInvalidException
	 *             - when the UI dealt with by this item damage handler cannot
	 *             be parsed into a damage value
	 */
	public abstract void checkValid() throws UIInvalidException;

	/**
	 * Registers an item damage handler which is active if
	 * <code>itemPredicate.apply</code> returns true
	 * 
	 * @param itemPredicate
	 * @param damageHandler
	 */
	public static void registerHandler(Predicate<ItemStack> itemPredicate,
			Class<? extends ItemDamageHandler> damageHandler) {
		if (handlers.containsKey(itemPredicate))
			itemPredicate = Predicates2.copyOf(itemPredicate);

		handlers.put(itemPredicate, damageHandler);
	}

	/**
	 * Returns a list of newly constructed item damage handlers for the given
	 * item. Mods can prevent specific handlers from being used by listening for
	 * {@link ItemDamageHandlerEvent}
	 * 
	 * @param item
	 * @return
	 */
	public static List<ItemDamageHandler> getHandlers(ItemStack item) {
		List<ItemDamageHandler> handlers = Lists.newArrayList();
		for (Map.Entry<Predicate<ItemStack>, Class<? extends ItemDamageHandler>> entry : ItemDamageHandler.handlers
				.entrySet()) {
			if (entry.getKey().apply(item)
					&& !MinecraftForge.EVENT_BUS.post(new ItemDamageHandlerEvent(item, entry.getValue()))) {
				try {
					handlers.add(entry.getValue().getConstructor().newInstance());
				} catch (Exception e) {
					GeneralUtils.logStackTrace(EasyEditorsApi.logger, e);
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
	public static IGuiCommandSlot setupCommandSlot(List<ItemDamageHandler> handlers, ItemStack item) {
		List<IGuiCommandSlot> slots = Lists.newArrayList();
		for (ItemDamageHandler handler : handlers) {
			for (IGuiCommandSlot slot : handler.setupCommandSlot(item)) {
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

	static {
		registerHandler(new Predicate<ItemStack>() {
			@Override
			public boolean apply(ItemStack input) {
				return !input.isEmpty() && input.getItem().isDamageable();
			}
		}, ToolHandler.class);
	}

	/**
	 * The handler for use with tool durabilities
	 * 
	 * @author Earthcomputer
	 *
	 */
	public static class ToolHandler extends ItemDamageHandler {

		private ItemStack item;
		private CommandSlotIntTextField durabilityField;

		@Override
		public IGuiCommandSlot[] setupCommandSlot(ItemStack item) {
			this.item = item;
			return new IGuiCommandSlot[] {
					CommandSlotLabel.createLabel(Translate.GUI_COMMANDEDITOR_ITEM_DAMAGE_TOOL_DURABILITY,
							Colors.itemLabel.color, Translate.GUI_COMMANDEDITOR_ITEM_DAMAGE_TOOL_DURABILITY_TOOLTIP,
							durabilityField = new CommandSlotIntTextField(32, 32, 1, item.getMaxDamage() + 1)
									.setNumberInvalidMessage(
											TranslateKeys.GUI_COMMANDEDITOR_ITEM_DAMAGE_TOOL_DURABILITY_INVALID)
									.setOutOfBoundsMessage(
											TranslateKeys.GUI_COMMANDEDITOR_ITEM_DAMAGE_TOOL_DURABILITY_OUTOFBOUNDS),
							new CommandSlotLabel(Minecraft.getMinecraft().fontRendererObj,
									"/ " + (item.getMaxDamage() + 1), Colors.itemLabel.color)) };
		}

		@Override
		public int setDamage(int damage) {
			durabilityField.setText(String.valueOf(item.getMaxDamage() - damage + 1));
			return 0;
		}

		@Override
		public int getDamage(int initialDamage) {
			return item.getMaxDamage() - durabilityField.getIntValue() + 1;
		}

		@Override
		public void checkValid() throws UIInvalidException {
			durabilityField.checkValid();
		}

	}

}
