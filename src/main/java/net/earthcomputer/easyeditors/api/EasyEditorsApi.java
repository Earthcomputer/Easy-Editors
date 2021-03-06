package net.earthcomputer.easyeditors.api;

import java.lang.reflect.Method;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;

import net.earthcomputer.easyeditors.EasyEditors;
import net.earthcomputer.easyeditors.api.util.GeneralUtils;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;

/**
 * Static utilities involving the Easy Editors API. Don't forget if you are
 * using the API, you should call the {@link #init()} method from the
 * pre-initialization phase of the mod.
 * 
 * <b>This class is a member of the Easy Editors API</b>
 * 
 * @author Earthcomputer
 *
 */
public class EasyEditorsApi {

	/**
	 * Whether Easy Editors is active, as set in the configuration. One should
	 * not expect this field to have its expected value until after Easy Editors
	 * has initialized
	 */
	public static boolean isEasyEditorsActive = true;

	private static Class<?> cls_EasyEditors;
	private static Class<?> cls_CommandSyntax;
	private static Class<?> cls_NBTTagHandler;
	private static Class<?> cls_SlotHandler;

	private static Method md_CommandSyntax_registerCommandSyntax;
	private static Method md_CommandSyntax_registerAlias;
	private static Method md_NBTTagHandler_registerHandler;
	private static Method md_SlotHandler_registerHandler;

	/**
	 * The logger, mainly for internal use
	 */
	public static final Logger logger = LogManager.getLogger(EasyEditors.NAME);

	static {
		try {
			cls_EasyEditors = Class.forName("net.earthcomputer.easyeditors.EasyEditors");
			cls_CommandSyntax = Class.forName("net.earthcomputer.easyeditors.gui.command.syntax.CommandSyntax");
			cls_NBTTagHandler = Class.forName("net.earthcomputer.easyeditors.gui.command.NBTTagHandler");
			cls_SlotHandler = Class.forName("net.earthcomputer.easyeditors.gui.command.SlotHandler");

			md_CommandSyntax_registerCommandSyntax = cls_CommandSyntax.getMethod("registerCommandSyntax", String.class,
					Class.class);
			md_CommandSyntax_registerAlias = cls_CommandSyntax.getMethod("registerAlias", String.class, String.class);
			md_NBTTagHandler_registerHandler = cls_NBTTagHandler.getMethod("registerHandler", String.class,
					Predicate.class, Class.class);
			md_SlotHandler_registerHandler = cls_SlotHandler.getMethod("registerHandler", cls_SlotHandler);
		} catch (Exception e) {
		}
	}

	private static boolean hasInitialized = false;

	/**
	 * Call this if you are going to use the API!
	 */
	public static void init() {
		if (!hasInitialized) {
			hasInitialized = true;
			BlockPropertyRegistry.init();
		}
	}

	/**
	 * A safe way of registering a command syntax without raising a
	 * {@link NoClassDefFoundError} if the Easy Editors mod is not present.
	 * Modders should ensure that the referred class in className is not
	 * referenced directly in code where one cannot be sure whether Easy Editors
	 * exists. Modders should also make sure that the referenced class is a
	 * subclass of
	 * {@link net.earthcomputer.easyeditors.gui.command.syntax.CommandSyntax
	 * ICommandSyntax}
	 * 
	 * @param commandName
	 * @param className
	 * 
	 * @see net.earthcomputer.easyeditors.gui.command.syntax.CommandSyntax#registerCommandSyntax(String,
	 *      Class) ICommandSyntax.registerCommandSyntax(String, Class)
	 */
	public static void registerCommandSyntax(String commandName, String className) {
		if (cls_EasyEditors == null)
			return;
		try {
			md_CommandSyntax_registerCommandSyntax.invoke(null, commandName, Class.forName(className));
		} catch (Exception e) {
			GeneralUtils.logStackTrace(logger, e);
		}
	}

	/**
	 * A safe way of registering a command syntax alias without raising a
	 * {@link NoClassDefFoundError} if the Easy Editors mod is not present.
	 * 
	 * @param commandName
	 * @param className
	 * 
	 * @see net.earthcomputer.easyeditors.gui.command.syntax.CommandSyntax#registerCommandSyntax(String,
	 *      Class) ICommandSyntax.registerCommandSyntax(String, Class)
	 */
	public static void registerCommandAlias(String mainCommandName, String aliasName) {
		if (cls_EasyEditors == null)
			return;
		try {
			md_CommandSyntax_registerAlias.invoke(null, mainCommandName, aliasName);
		} catch (Exception e) {
			GeneralUtils.logStackTrace(logger, e);
		}
	}

	/**
	 * A safe way of registering an NBTTagHandler for TileEntities without
	 * raising a {@link NoClassDefFoundError} if the Easy Editors mod is not
	 * present. Modders should ensure that the referred class in
	 * handlerClassName is not referenced directly in code where one cannot be
	 * sure whether Easy Editors exists. Modders should also make sure that the
	 * referenced class is a subclass of
	 * {@link net.earthcomputer.easyeditors.gui.command.NBTTagHandler
	 * NBTTagHandler}
	 * 
	 * @param tileEntity
	 * @param handlerClassName
	 * 
	 * @see net.earthcomputer.easyeditors.gui.command.NBTTagHandler#registerTileEntityHandler(Class,
	 *      Class) NBTTagHandler.registerTileEntityHandler(Class, Class)
	 */
	public static void registerTileEntityNBTHandler(Class<? extends TileEntity> tileEntity, String handlerClassName) {
		registerNBTHandler("tileEntity", Predicates.assignableFrom(tileEntity), handlerClassName);
	}

	/**
	 * A safe way of registering an NBTTagHandler for ItemStacks without raising
	 * a {@link NoClassDefFoundError} if the Easy Editors mod is not present.
	 * Modders should ensure that the referred class in handlerClassName is not
	 * referenced directly in code where one cannot be sure whether Easy Editors
	 * exists. Modders should also make sure that the referenced class is a
	 * subclass of
	 * {@link net.earthcomputer.easyeditors.gui.command.NBTTagHandler
	 * NBTTagHandler}
	 * 
	 * @param itemStackPredicate
	 * @param handlerClassName
	 * 
	 * @see net.earthcomputer.easyeditors.gui.command.NBTTagHandler#registerItemStackHandler(Predicate,
	 *      Class) NBTTagHandler.registerItemStackHandler(Predicate, Class)
	 */
	public static void registerItemStackNBTHandler(Predicate<ItemStack> itemStackPredicate, String handlerClassName) {
		registerNBTHandler("itemStack", itemStackPredicate, handlerClassName);
	}

	/**
	 * A safe way of registering an NBTTagHandler for Entities without raising a
	 * {@link NoClassDefFoundError} if the Easy Editors mod is not present.
	 * Modders should ensure that the referred class in handlerClassName is not
	 * referenced directly in code where one cannot be sure whether Easy Editors
	 * exists. Modders should also make sure that the referenced class is a
	 * subclass of
	 * {@link net.earthcomputer.easyeditors.gui.command.NBTTagHandler
	 * NBTTagHandler}
	 * 
	 * @param entity
	 * @param handlerClassName
	 * 
	 * @see net.earthcomputer.easyeditors.gui.command.NBTTagHandler#registerEntityHandler(Class,
	 *      Class) NBTTagHandler.registerEntityHandler(Class, Class)
	 */
	public static void registerEntityNBTHandler(Class<? extends Entity> entity, String handlerClassName) {
		registerNBTHandler("entity", Predicates.assignableFrom(entity), handlerClassName);
	}

	/**
	 * A safe way of registering an NBTTagHandler for custom purposes without
	 * raising a {@link NoClassDefFoundError} if the Easy Editors mod is not
	 * present. Modders should ensure that the referred class in
	 * handlerClassName is not referenced directly in code where one cannot be
	 * sure whether Easy Editors exists. Modders should also make sure that the
	 * referenced class is a subclass of
	 * {@link net.earthcomputer.easyeditors.gui.command.NBTTagHandler
	 * NBTTagHandler}
	 * 
	 * @param handlerType
	 * @param predicate
	 * @param handlerClassName
	 * 
	 * @see net.earthcomputer.easyeditors.gui.command.NBTTagHandler#registerHandler(String,
	 *      Predicate, Class) NBTTagHandler.registerTileEntityHandler(String,
	 *      Predicate, Class)
	 */
	public static void registerNBTHandler(String handlerType, Predicate<?> predicate, String handlerClassName) {
		if (cls_EasyEditors == null)
			return;
		try {
			md_NBTTagHandler_registerHandler.invoke(null, handlerType, predicate, Class.forName(handlerClassName));
		} catch (Exception e) {
			GeneralUtils.logStackTrace(logger, e);
		}
	}

	/**
	 * A safe way of registering a SlotHandler without raising a
	 * {@link NoClassDefFoundError} if the Easy Editors mod is not present.
	 * Modders should ensure that the referred class in slotHandlerClassName is
	 * not referenced directly in code where one cannot be sure whether Easy
	 * Editors exists. Modders should also make sure that the referenced class
	 * is a subclass of
	 * {@link net.earthcomputer.easyeditors.gui.command.SlotHandler SlotHandler}
	 * 
	 * @param slotHandlerClassName
	 */
	public static void registerSlotHandler(String slotHandlerClassName) {
		if (cls_EasyEditors == null) {
			return;
		}
		try {
			Class<?> slotHandlerClass = Class.forName(slotHandlerClassName);
			if (!cls_SlotHandler.isAssignableFrom(slotHandlerClass)) {
				throw new IllegalArgumentException("Not a SlotHandler");
			}
			md_SlotHandler_registerHandler.invoke(null, slotHandlerClass.newInstance());
		} catch (Exception e) {
			GeneralUtils.logStackTrace(logger, e);
		}
	}

}
