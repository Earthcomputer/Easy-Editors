package net.earthcomputer.easyeditors.api;

import java.lang.reflect.Method;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;

import net.earthcomputer.easyeditors.EasyEditors;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;

/**
 * Static utilities involving the Easy Editors API.
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
	private static Class<?> cls_ICommandSyntax;
	private static Class<?> cls_NBTTagHandler;

	private static Method md_ICommandSyntax_registerCommandSyntax;
	private static Method md_NBTTagHandler_registerHandler;

	public static final Logger logger = LogManager.getLogger(EasyEditors.NAME);

	static {
		try {
			cls_EasyEditors = Class.forName("net.earthcomputer.easyeditors.EasyEditors");
			cls_ICommandSyntax = Class.forName("net.earthcomputer.easyeditors.gui.command.ICommandSyntax");
			cls_NBTTagHandler = Class.forName("net.earthcomputer.easyeditors.gui.command.NBTTagHandler");

			md_ICommandSyntax_registerCommandSyntax = cls_ICommandSyntax.getMethod("registerCommandSyntax",
					String.class, Class.class);
			md_NBTTagHandler_registerHandler = cls_NBTTagHandler.getMethod("registerHandler", String.class,
					Predicate.class, Class.class);
		} catch (Exception e) {
		}
	}

	/**
	 * A safe way of registering a command syntax without raising a
	 * {@link NoClassDefFoundError} if the Easy Editors mod is not present.
	 * Modders should ensure that the referred class in className is not
	 * referenced directly in code where one cannot be sure whether Easy Editors
	 * exists. Modders should also make sure that the referenced class is a
	 * subclass of
	 * {@link net.earthcomputer.easyeditors.gui.command.syntax.ICommandSyntax
	 * ICommandSyntax}
	 * 
	 * @param commandName
	 * @param className
	 * 
	 * @see net.earthcomputer.easyeditors.gui.command.syntax.ICommandSyntax#registerCommandSyntax(String,
	 *      Class) ICommandSyntax.registerCommandSyntax(String, Class)
	 */
	public static void registerCommandSyntax(String commandName, String className) {
		if (cls_EasyEditors == null)
			return;
		try {
			md_ICommandSyntax_registerCommandSyntax.invoke(null, commandName, Class.forName(className));
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

}
