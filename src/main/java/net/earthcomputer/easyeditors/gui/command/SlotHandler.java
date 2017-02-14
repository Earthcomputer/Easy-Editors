package net.earthcomputer.easyeditors.gui.command;

import java.util.Collections;
import java.util.List;

import com.google.common.collect.Lists;

import net.earthcomputer.easyeditors.gui.GuiSelectEntity;
import net.earthcomputer.easyeditors.gui.command.slot.CommandSlotIntTextField;
import net.earthcomputer.easyeditors.gui.command.slot.CommandSlotLabel;
import net.earthcomputer.easyeditors.gui.command.slot.IGuiCommandSlot;
import net.earthcomputer.easyeditors.util.Translate;
import net.earthcomputer.easyeditors.util.TranslateKeys;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.entity.passive.AbstractHorse;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;

/**
 * A class for creating GUIs for inventory slot IDs. Used e.g. in the
 * <code>/replaceitem</code> command. One SlotHandler handles one name for an
 * inventory slot, e.g. one would handle <code>slot.hotbar.x</code>, where
 * <code>x</code> is between 1-9 and another <code>slot.armor.head</code>
 * 
 * @author Earthcomputer
 *
 */
public abstract class SlotHandler {

	/**
	 * The SlotHandler used for container inventories
	 */
	public static final SlotHandler CONTAINER_HANDLER;

	private static final List<SlotHandler> handlers = Lists.newArrayList();

	/**
	 * Register a slot handler
	 * 
	 * @param handler
	 */
	public static void registerHandler(SlotHandler handler) {
		handlers.add(handler);
	}

	/**
	 * Returns a list of all registered handlers
	 * 
	 * @return
	 */
	public static List<SlotHandler> getAllHandlers() {
		return Collections.unmodifiableList(handlers);
	}

	/**
	 * Returns a list of all registered handlers that can handle a specific
	 * entity
	 * 
	 * @param entity
	 * @return
	 */
	public static List<SlotHandler> getHandlersForEntity(ResourceLocation entity) {
		List<SlotHandler> r = Lists.newArrayList();
		for (SlotHandler handler : handlers) {
			if (handler.handlesEntity(entity)) {
				r.add(handler);
			}
		}
		return r;
	}

	/**
	 * Gets the unlocalized display name of this slot handler
	 * 
	 * @return
	 */
	public abstract String getUnlocalizedName();

	/**
	 * Returns whether this SlotHandler can handle the given type of entity.
	 * Note that if the type of entity is unknown, this method will not be
	 * called and this SlotHandler will be used anyway.
	 * 
	 * @param entityType
	 * @return
	 */
	public abstract boolean handlesEntity(ResourceLocation entityType);

	/**
	 * Returns whether this SlotHandler can handle a slot with the given name
	 * 
	 * @param slotName
	 * @return
	 */
	public abstract boolean handlesSlot(String slotName);

	/**
	 * Creates an array of <i>logical</i> command slots that the user will be
	 * able to edit and which will be parameters in the slot's logic. E.g. the
	 * <code>x</code> in <code>slot.inventory.x</code>
	 * 
	 * @return
	 */
	public abstract IGuiCommandSlot[] setupCommandSlots();

	/**
	 * Creates an array of command slots which will be what the user sees. These
	 * command slots should indirectly contain the logical command slots passed
	 * into this method which were created previously by
	 * {@link #setupCommandSlots()}
	 * 
	 * @param commandSlots
	 * @return
	 */
	public abstract IGuiCommandSlot[] layoutCommandSlots(IGuiCommandSlot[] commandSlots);

	/**
	 * Called mainly by {@link IGuiCommandSlot#readFromArgs(String[], int)}, to
	 * initialize the parameter command slots
	 * 
	 * @param slotName
	 * @param commandSlots
	 *            - the set of logical command slots to initialize
	 */
	public abstract void initializeCommandSlots(String slotName, IGuiCommandSlot[] commandSlots);

	/**
	 * Called mainly by {@link IGuiCommandSlot#addArgs(List)}, to get the raw
	 * slot name from the parameters
	 * 
	 * @param commandSlots
	 *            - the logical command slots to work with
	 * @return
	 */
	public abstract String getSlotName(IGuiCommandSlot[] commandSlots);

	private static abstract class Base extends SlotHandler {
		private String unlocalizedName;
		private Class<? extends Entity> entityType;

		public Base(String unlocalizedName, Class<? extends Entity> entityType) {
			this.unlocalizedName = unlocalizedName;
			this.entityType = entityType;
		}

		@Override
		public boolean handlesEntity(ResourceLocation entityType) {
			Class<?> entity;
			if (GuiSelectEntity.PLAYER.equals(entityType)) {
				entity = EntityPlayer.class;
			} else if (EntityList.LIGHTNING_BOLT.equals(entityType)) {
				entity = EntityLightningBolt.class;
			} else {
				entity = EntityList.getClass(entityType);
				if (entity == null) {
					throw new AssertionError();
				}
			}
			return this.entityType.isAssignableFrom(entity);
		}

		@Override
		public String getUnlocalizedName() {
			return unlocalizedName;
		}
	}

	/**
	 * A simple implementation of SlotHandler that only handles a slot with a
	 * specific name, e.g. <code>slot.armor.head</code>
	 * 
	 * @author Earthcomputer
	 *
	 */
	public static class Simple extends Base {
		private String slotName;

		public Simple(String unlocalizedName, String slotName, Class<? extends Entity> entityType) {
			super(unlocalizedName, entityType);
			this.slotName = slotName;
		}

		@Override
		public boolean handlesSlot(String slotName) {
			return this.slotName.equals(slotName);
		}

		@Override
		public IGuiCommandSlot[] setupCommandSlots() {
			return new IGuiCommandSlot[0];
		}

		@Override
		public IGuiCommandSlot[] layoutCommandSlots(IGuiCommandSlot[] commandSlots) {
			return commandSlots;
		}

		@Override
		public void initializeCommandSlots(String slotName, IGuiCommandSlot[] commandSlots) {
		}

		@Override
		public String getSlotName(IGuiCommandSlot[] commandSlots) {
			return slotName;
		}
	}

	/**
	 * An implementation of SlotHandler which handles slots in an inventory,
	 * e.g. <code>slot.inventory.x</code>, where <code>x</code> is a number
	 * between 0-26
	 * 
	 * @author Earthcomputer
	 *
	 */
	public static class Inventory extends Base {
		private String slotPrefix;
		private int minSlot;
		private int maxSlot;

		public Inventory(String unlocalizedName, String slotPrefix, int minSlot, int maxSlot,
				Class<? extends Entity> entityType) {
			super(unlocalizedName, entityType);
			this.slotPrefix = slotPrefix + ".";
			this.minSlot = minSlot;
			this.maxSlot = maxSlot;
		}

		@Override
		public boolean handlesSlot(String slotName) {
			if (!slotName.startsWith(slotPrefix)) {
				return false;
			}
			slotName = slotName.substring(slotPrefix.length());
			if ((slotName.startsWith("0") && !"0".equals(slotName)) || slotName.startsWith("+")) {
				return false;
			}
			int slotNumber;
			try {
				slotNumber = Integer.parseInt(slotName);
			} catch (NumberFormatException e) {
				return false;
			}
			return slotNumber >= minSlot && slotNumber <= maxSlot;
		}

		@Override
		public IGuiCommandSlot[] setupCommandSlots() {
			return new IGuiCommandSlot[] { new CommandSlotIntTextField(50, 100, minSlot, maxSlot) };
		}

		@Override
		public IGuiCommandSlot[] layoutCommandSlots(IGuiCommandSlot[] commandSlots) {
			return new IGuiCommandSlot[] {
					CommandSlotLabel.createLabel(Translate.GUI_COMMANDEDITOR_REPLACEITEM_SLOTNUMBER, commandSlots[0]) };
		}

		@Override
		public void initializeCommandSlots(String slotName, IGuiCommandSlot[] commandSlots) {
			slotName = slotName.substring(slotPrefix.length());
			((CommandSlotIntTextField) commandSlots[0]).setText(slotName);
		}

		@Override
		public String getSlotName(IGuiCommandSlot[] commandSlots) {
			return slotPrefix + ((CommandSlotIntTextField) commandSlots[0]).getIntValue();
		}
	}

	static {
		CONTAINER_HANDLER = new Inventory(TranslateKeys.GUI_COMMANDEDITOR_REPLACEITEM_SLOT_CONTAINER, "slot.container",
				0, 53, null) {
			@Override
			public boolean handlesEntity(ResourceLocation entity) {
				return false;
			}
		};
		registerHandler(CONTAINER_HANDLER);
		registerHandler(new Inventory(TranslateKeys.GUI_COMMANDEDITOR_REPLACEITEM_SLOT_HOTBAR, "slot.hotbar", 0, 8,
				EntityPlayer.class));
		registerHandler(new Inventory(TranslateKeys.GUI_COMMANDEDITOR_REPLACEITEM_SLOT_INVENTORY, "slot.inventory", 0,
				26, EntityPlayer.class));
		registerHandler(new Inventory(TranslateKeys.GUI_COMMANDEDITOR_REPLACEITEM_SLOT_ENDERCHEST, "slot.enderchest", 0,
				26, EntityPlayer.class));
		registerHandler(new Inventory(TranslateKeys.GUI_COMMANDEDITOR_REPLACEITEM_SLOT_VILLAGER, "slot.villager", 0, 7,
				EntityVillager.class));
		registerHandler(new Inventory(TranslateKeys.GUI_COMMANDEDITOR_REPLACEITEM_SLOT_HORSE, "slot.horse", 0, 14,
				AbstractHorse.class));
		registerHandler(new Simple(TranslateKeys.GUI_COMMANDEDITOR_REPLACEITEM_SLOT_WEAPON_MAINHAND, "slot.weapon",
				EntityLivingBase.class) {
			@Override
			public boolean handlesSlot(String slotName) {
				return super.handlesSlot(slotName) || "slot.weapon.mainhand".equals(slotName);
			}
		});
		registerHandler(new Simple(TranslateKeys.GUI_COMMANDEDITOR_REPLACEITEM_SLOT_WEAPON_OFFHAND,
				"slot.weapon.offhand", EntityLivingBase.class));
		registerHandler(new Simple(TranslateKeys.GUI_COMMANDEDITOR_REPLACEITEM_SLOT_ARMOR_HEAD, "slot.armor.head",
				EntityLivingBase.class));
		registerHandler(new Simple(TranslateKeys.GUI_COMMANDEDITOR_REPLACEITEM_SLOT_ARMOR_CHEST, "slot.armor.chest",
				EntityLivingBase.class));
		registerHandler(new Simple(TranslateKeys.GUI_COMMANDEDITOR_REPLACEITEM_SLOT_ARMOR_LEGS, "slot.armor.legs",
				EntityLivingBase.class));
		registerHandler(new Simple(TranslateKeys.GUI_COMMANDEDITOR_REPLACEITEM_SLOT_ARMOR_FEET, "slot.armor.feet",
				EntityLivingBase.class));
		registerHandler(new Simple(TranslateKeys.GUI_COMMANDEDITOR_REPLACEITEM_SLOT_HORSE_SADDLE, "slot.horse.saddle",
				AbstractHorse.class));
		registerHandler(new Simple(TranslateKeys.GUI_COMMANDEDITOR_REPLACEITEM_SLOT_HORSE_ARMOR, "slot.horse.armor",
				AbstractHorse.class));
		registerHandler(new Simple(TranslateKeys.GUI_COMMANDEDITOR_REPLACEITEM_SLOT_HORSE_CHEST, "slot.horse.chest",
				AbstractHorse.class));
	}

}
