package net.earthcomputer.easyeditors.gui.command.syntax;

import java.util.Collections;
import java.util.Map;

import com.google.common.base.Joiner;
import com.google.common.collect.Maps;

import net.earthcomputer.easyeditors.gui.command.slot.IGuiCommandSlot;
import net.minecraft.crash.CrashReport;
import net.minecraft.util.ReportedException;

/**
 * A class which defines the layout of the GUI of a single command
 * 
 * @author Earthcomputer
 *
 */
public abstract class ICommandSyntax {

	private static Map<String, Class<? extends ICommandSyntax>> commandSyntaxes = Maps.newHashMap();

	protected static final Joiner spaceJoiner = Joiner.on(' ');

	/**
	 * Builds the components of the command
	 * 
	 * @return The components of the command. They will be in the format of a
	 *         CommandSlotVerticalArrangement
	 */
	public abstract IGuiCommandSlot[] setupCommand();

	/**
	 * 
	 * @return Whether using the components from {@link #setupCommand()} would
	 *         produce a valid command
	 */
	public abstract boolean isValid();

	/**
	 * Returns a String, containing the elements in args from index n to
	 * args.length - 1, joined with spaces
	 * 
	 * @param args
	 * @param n
	 * @return
	 */
	public static String getStringFromNthArg(String[] args, int n) {
		String[] stringsToJoin = new String[args.length - n];
		System.arraycopy(args, n, stringsToJoin, 0, stringsToJoin.length);
		return spaceJoiner.join(stringsToJoin);
	}

	/**
	 * Registers a type of command syntax. Modders should not use this method,
	 * and instead use
	 * {@link net.earthcomputer.easyeditors.api.EasyEditorsApi#registerCommandSyntax(String, String)
	 * EasyEditorsApi.registerCommandSyntax(String, String)} instead
	 * 
	 * @param commandName
	 * @param syntaxClass
	 */
	public static void registerCommandSyntax(String commandName, Class<? extends ICommandSyntax> syntaxClass) {
		commandSyntaxes.put(commandName, syntaxClass);
	}

	/**
	 * Creates an instance of ICommandSyntax from the given command name
	 * 
	 * @param commandName
	 * @return
	 */
	public static ICommandSyntax forCommandName(String commandName) {
		Class<? extends ICommandSyntax> syntax = commandSyntaxes.get(commandName);
		if (syntax == null)
			return null;
		try {
			return syntax.getConstructor().newInstance();
		} catch (Exception e) {
			throw new ReportedException(CrashReport.makeCrashReport(e, "Doing reflection"));
		}
	}

	/**
	 * 
	 * @return A map of all registered command syntax types. The keys represent
	 *         the command names, and values represent types of command syntax
	 */
	public static Map<String, Class<? extends ICommandSyntax>> getSyntaxList() {
		return Collections.unmodifiableMap(commandSyntaxes);
	}

	static {
		registerCommandSyntax("give", SyntaxGive.class);
		registerCommandSyntax("scoreboard", SyntaxScoreboard.class);
	}

}
