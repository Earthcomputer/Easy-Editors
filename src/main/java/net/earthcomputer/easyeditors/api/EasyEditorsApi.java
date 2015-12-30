package net.earthcomputer.easyeditors.api;

import java.lang.reflect.Method;

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

	private static Object easyEditorsInstance;

	private static Class<?> cls_EasyEditors;
	private static Class<?> cls_ICommandSyntax;

	private static Method md_ICommandSyntax_registerCommandSyntax;

	static {
		try {
			cls_EasyEditors = Class.forName("net.earthcomputer.easyeditors.EasyEditors");
			cls_ICommandSyntax = Class.forName("net.earthcomputer.easyeditors.gui.command.ICommandSyntax");

			md_ICommandSyntax_registerCommandSyntax = cls_ICommandSyntax.getMethod("registerCommandSyntax",
					String.class, Class.class);
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
	 * {@link net.earthcomputer.easyeditors.gui.command.ICommandSyntax
	 * ICommandSyntax}
	 * 
	 * @param commandName
	 * @param className
	 */
	public static void registerCommandSyntax(String commandName, String className) {
		if (cls_EasyEditors == null)
			return;
		try {
			md_ICommandSyntax_registerCommandSyntax.invoke(null, commandName, Class.forName(className));
		} catch (Exception e) {
		}
	}

}
