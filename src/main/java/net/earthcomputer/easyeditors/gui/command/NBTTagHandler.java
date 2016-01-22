package net.earthcomputer.easyeditors.gui.command;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.earthcomputer.easyeditors.api.EasyEditorsApi;
import net.earthcomputer.easyeditors.api.GeneralUtils;
import net.earthcomputer.easyeditors.gui.command.slot.CommandSlotVerticalArrangement;
import net.earthcomputer.easyeditors.gui.command.slot.IGuiCommandSlot;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;

/**
 * A class which allows you to create user-friendly representations of NBT tags
 * 
 * @author Earthcomputer
 *
 */
public abstract class NBTTagHandler {

	private static final Map<String, Map<Predicate<?>, Class<? extends NBTTagHandler>>> handlers = Maps.newHashMap();

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

	public static void readFromNBT(NBTTagCompound nbt, List<NBTTagHandler> nbtHandlers) {
		for (NBTTagHandler handler : nbtHandlers) {
			handler.readFromNBT(nbt);
		}
	}

	public static void writeToNBT(NBTTagCompound nbt, List<NBTTagHandler> nbtHandlers) {
		for (NBTTagHandler handler : nbtHandlers) {
			handler.writeToNBT(nbt);
		}
	}

}
