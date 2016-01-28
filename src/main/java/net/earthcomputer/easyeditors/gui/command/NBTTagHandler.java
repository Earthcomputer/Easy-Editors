package net.earthcomputer.easyeditors.gui.command;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.earthcomputer.easyeditors.api.Colors;
import net.earthcomputer.easyeditors.api.EasyEditorsApi;
import net.earthcomputer.easyeditors.api.GeneralUtils;
import net.earthcomputer.easyeditors.api.Instantiator;
import net.earthcomputer.easyeditors.gui.command.slot.CommandSlotColor;
import net.earthcomputer.easyeditors.gui.command.slot.CommandSlotLabel;
import net.earthcomputer.easyeditors.gui.command.slot.CommandSlotList;
import net.earthcomputer.easyeditors.gui.command.slot.CommandSlotTextField;
import net.earthcomputer.easyeditors.gui.command.slot.CommandSlotVerticalArrangement;
import net.earthcomputer.easyeditors.gui.command.slot.IGuiCommandSlot;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.Constants;

/**
 * A class which allows you to create user-friendly representations of NBT tags
 * 
 * @author Earthcomputer
 *
 */
public abstract class NBTTagHandler {

	private static final Map<String, Map<Predicate<?>, Class<? extends NBTTagHandler>>> handlers = Maps.newHashMap();

	static {
		Predicate<ItemStack> alwaysTrue = new Predicate<ItemStack>() {
			@Override
			public boolean apply(ItemStack stack) {
				return true;
			}
		};
		registerItemStackHandler(alwaysTrue, DisplayHandler.class);
		registerItemStackHandler(new Predicate<ItemStack>() {
			@Override
			public boolean apply(ItemStack stack) {
				if (stack == null)
					return false;
				Item item = stack.getItem();
				return (item instanceof ItemArmor)
						&& ((ItemArmor) item).getArmorMaterial() == ItemArmor.ArmorMaterial.LEATHER;
			}
		}, ArmorColorHandler.class);
	}

	/**
	 * Sets up the command slots that can be used to edit the handled part of
	 * the NBT tag. The returned array may be nested inside a
	 * {@link CommandSlotVerticalArrangement} if necessary
	 * 
	 * @return
	 */
	public abstract IGuiCommandSlot[] setupCommandSlot();

	/**
	 * Read from an {@link NBTTagCompound} and inject into the command slots
	 * 
	 * @param nbt
	 */
	public abstract void readFromNBT(NBTTagCompound nbt);

	/**
	 * Read from the command slots and inject into the NBT
	 * 
	 * @param nbt
	 */
	public abstract void writeToNBT(NBTTagCompound nbt);

	/**
	 * Registers an NBTTagHandler which deals with NBT tags specific to the
	 * given class of TileEntity. Modders should instead use
	 * {@link net.earthcomputer.easyeditors.api.EasyEditorsApi#registerTileEntityNBTHandler(Class, String)
	 * EasyEditorsApi.registerTileEntityNBTHandler(Class, String)}
	 * 
	 * @param tileEntity
	 * @param handler
	 */
	public static void registerTileEntityHandler(Class<? extends TileEntity> tileEntity,
			Class<? extends NBTTagHandler> handler) {
		registerHandler("tileEntity", Predicates.assignableFrom(tileEntity), handler);
	}

	/**
	 * Registers an NBTTagHandler which deals with NBT tags in item stacks, when
	 * your item contains custom information (like a book). Modders should
	 * instead use
	 * {@link net.earthcomputer.easyeditors.api.EasyEditorsApi#registerItemStackNBTHandler(Predicate, String)
	 * EasyEditorsApi.registerItemStackNBTHandler(Predicate, String)}
	 * 
	 * @param stackPredicate
	 * @param handler
	 */
	public static void registerItemStackHandler(Predicate<ItemStack> stackPredicate,
			Class<? extends NBTTagHandler> handler) {
		registerHandler("itemStack", stackPredicate, handler);
	}

	/**
	 * Registers an NBTTagHandler which deals with NBT tags specific to the
	 * given class of Entity. Modders should instead use
	 * {@link net.earthcomputer.easyeditors.api.EasyEditorsApi#registerEntityNBTHandler(Class, String)
	 * EasyEditorsApi.registerEntityNBTHandler(Class, String)}
	 * 
	 * @param entity
	 * @param handler
	 */
	public static void registerEntityHandler(Class<? extends Entity> entity, Class<? extends NBTTagHandler> handler) {
		registerHandler("entity", Predicates.assignableFrom(entity), handler);
	}

	/**
	 * Registers a custom NBTTagHandler which deals with NBT tags in custom
	 * scenarios, with the handlerType parameter representing the ID of the
	 * custom scenario. Modders should instead use
	 * {@link net.earthcomputer.easyeditors.api.EasyEditorsApi#registerNBTHandler(String, Predicate, String)
	 * EasyEditorsApi.registerNBTHandler(String, Predicate, String)}
	 * 
	 * @param handlerType
	 * @param predicate
	 * @param handler
	 */
	public static void registerHandler(String handlerType, Predicate<?> predicate,
			Class<? extends NBTTagHandler> handler) {
		if (!handlers.containsKey(handlerType))
			handlers.put(handlerType, new HashMap<Predicate<?>, Class<? extends NBTTagHandler>>());
		handlers.get(handlerType).put(predicate, handler);
	}

	/**
	 * Constructs a list of new NBTTagHandlers for the given class of TileEntity
	 * 
	 * @param tileEntity
	 * @return
	 */
	public static List<NBTTagHandler> constructTileEntityHandlers(Class<? extends TileEntity> tileEntity) {
		return constructHandlers("tileEntity", tileEntity);
	}

	/**
	 * Constructs a list of new NBTTagHandlers for the given ItemStack
	 * 
	 * @param itemStack
	 * @return
	 */
	public static List<NBTTagHandler> constructItemStackHandlers(ItemStack itemStack) {
		return constructHandlers("itemStack", itemStack);
	}

	/**
	 * Constructs a list of new NBTTagHandlers for the given class of Entity
	 * 
	 * @param entity
	 * @return
	 */
	public static List<NBTTagHandler> constructEntityHandlers(Class<? extends Entity> entity) {
		return constructHandlers("entity", entity);
	}

	/**
	 * Constructs a list of new NBTTagHandlers for the given custom scenario,
	 * with the handlerType parameter representing the ID of the custom
	 * scenario. If specifier is null, this method returns all the
	 * NBTTagHandlers assigned to this type of scenario
	 * 
	 * @param handlerType
	 * @param specifier
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static List<NBTTagHandler> constructHandlers(String handlerType, Object specifier) {
		List<NBTTagHandler> handlers = Lists.newArrayList();
		if (!NBTTagHandler.handlers.containsKey(handlerType))
			return handlers;
		if (specifier == null) {
			for (Class<? extends NBTTagHandler> handler : NBTTagHandler.handlers.get(handlerType).values()) {
				try {
					handlers.add(handler.getConstructor().newInstance());
				} catch (Exception e) {
					GeneralUtils.logStackTrace(EasyEditorsApi.logger, e);
				}
			}
		} else {
			for (Map.Entry<Predicate<?>, Class<? extends NBTTagHandler>> entry : NBTTagHandler.handlers.get(handlerType)
					.entrySet()) {
				if (((Predicate<Object>) entry.getKey()).apply(specifier)) {
					try {
						handlers.add(entry.getValue().getConstructor().newInstance());
					} catch (Exception e) {
						GeneralUtils.logStackTrace(EasyEditorsApi.logger, e);
					}
				}
			}
		}
		return handlers;
	}

	/**
	 * Returns an {@link IGuiCommandSlot} which will be the user interface to
	 * the NBT tags. The NBTTagHandlers must be preconstructed by using methods
	 * of this class
	 * 
	 * @param nbtHandlers
	 * @return
	 */
	public static IGuiCommandSlot setupCommandSlot(List<NBTTagHandler> nbtHandlers) {
		List<IGuiCommandSlot> slots = Lists.newArrayList();
		for (NBTTagHandler handler : nbtHandlers) {
			for (IGuiCommandSlot slot : handler.setupCommandSlot()) {
				slots.add(slot);
			}
		}
		return slots.isEmpty() ? null
				: (slots.size() == 1 ? slots.get(0)
						: new CommandSlotVerticalArrangement(slots.toArray(new IGuiCommandSlot[slots.size()])));
	}

	/**
	 * Reads from the given NBT tag to the given NBTTagHandlers to modify the
	 * user interface to reflect the changes in the NBT tag
	 * 
	 * @param nbt
	 * @param nbtHandlers
	 */
	public static void readFromNBT(NBTTagCompound nbt, List<NBTTagHandler> nbtHandlers) {
		for (NBTTagHandler handler : nbtHandlers) {
			handler.readFromNBT(nbt);
		}
	}

	/**
	 * Reads from the user interface via the given NBTTagHandlers to the given
	 * NBT tag to reflect the changes the user has made
	 * 
	 * @param nbt
	 * @param nbtHandlers
	 */
	public static void writeToNBT(NBTTagCompound nbt, List<NBTTagHandler> nbtHandlers) {
		for (NBTTagHandler handler : nbtHandlers) {
			handler.writeToNBT(nbt);
		}
	}

	public static class DisplayHandler extends NBTTagHandler {

		private CommandSlotTextField displayName;
		private CommandSlotList<CommandSlotTextField> lore;

		@Override
		public IGuiCommandSlot[] setupCommandSlot() {
			displayName = new CommandSlotTextField(100, 400);
			displayName.setMaxStringLength(Short.MAX_VALUE);
			lore = new CommandSlotList<CommandSlotTextField>(new Instantiator<CommandSlotTextField>() {
				@Override
				public CommandSlotTextField newInstance() {
					return new CommandSlotTextField(100, 400);
				}
			});
			return new IGuiCommandSlot[] {
					CommandSlotLabel.createLabel(I18n.format("gui.commandEditor.item.nbt.displayName"),
							Colors.itemLabel.color, displayName),
					CommandSlotLabel.createLabel(I18n.format("gui.commandEditor.item.nbt.lore"), Colors.itemLabel.color,
							lore) };
		}

		@Override
		public void readFromNBT(NBTTagCompound nbt) {
			String displayName = "";
			lore.clearEntries();
			if (nbt != null && nbt.hasKey("display", Constants.NBT.TAG_COMPOUND)) {
				NBTTagCompound display = nbt.getCompoundTag("display");
				if (display.hasKey("Name", Constants.NBT.TAG_STRING)) {
					displayName = display.getString("Name");
				}
				if (display.hasKey("Lore", Constants.NBT.TAG_LIST)) {
					try {
						NBTTagList lore = display.getTagList("Lore", Constants.NBT.TAG_STRING);
						for (int i = 0; i < lore.tagCount(); i++) {
							CommandSlotTextField textField = new CommandSlotTextField(100, 400);
							textField.setText(lore.getStringTagAt(i));
							this.lore.addEntry(textField);
						}
					} catch (Exception e) {
					}
				}
			}

			this.displayName.setText(displayName);
		}

		@Override
		public void writeToNBT(NBTTagCompound nbt) {
			NBTTagCompound display = new NBTTagCompound();

			if (!displayName.getText().isEmpty()) {
				display.setString("Name", displayName.getText());
			}

			NBTTagList lore = new NBTTagList();
			for (int i = 0; i < this.lore.entryCount(); i++) {
				lore.appendTag(new NBTTagString(this.lore.getEntry(i).getText()));
			}
			if (!lore.hasNoTags())
				display.setTag("Lore", lore);

			if (!display.hasNoTags()) {
				if (nbt.hasKey("display", Constants.NBT.TAG_COMPOUND))
					display.merge(nbt.getCompoundTag("display"));
				nbt.setTag("display", display);
			}
		}

	}

	public static class ArmorColorHandler extends NBTTagHandler {

		private CommandSlotColor color;

		@Override
		public IGuiCommandSlot[] setupCommandSlot() {
			return new IGuiCommandSlot[] {
					CommandSlotLabel.createLabel(I18n.format("gui.commandEditor.item.nbt.leatherArmor.color"),
							Colors.itemLabel.color, color = new CommandSlotColor(false)) };
		}

		@Override
		public void readFromNBT(NBTTagCompound nbt) {
			int color = 0xffffff;
			if (nbt != null && nbt.hasKey("display", Constants.NBT.TAG_COMPOUND)) {
				NBTTagCompound display = nbt.getCompoundTag("display");
				if (display.hasKey("color", Constants.NBT.TAG_INT))
					color = display.getInteger("color");
			}
			this.color.setColor(color);
		}

		@Override
		public void writeToNBT(NBTTagCompound nbt) {
			int color = this.color.getColor() & 0xffffff;
			if (color != 0xffffff) {
				NBTTagCompound display = new NBTTagCompound();
				display.setInteger("color", color);
				if (nbt.hasKey("display", Constants.NBT.TAG_COMPOUND))
					display.merge(nbt.getCompoundTag("display"));
				nbt.setTag("display", display);
			}
		}

	}

}
