package net.earthcomputer.easyeditors.gui.command.syntax;

import java.util.Collections;
import java.util.Map;

import com.google.common.base.Joiner;
import com.google.common.collect.Maps;

import net.earthcomputer.easyeditors.gui.command.CommandSlotContext;
import net.earthcomputer.easyeditors.gui.command.UIInvalidException;
import net.earthcomputer.easyeditors.gui.command.slot.IGuiCommandSlot;
import net.minecraft.crash.CrashReport;
import net.minecraft.util.ReportedException;

/**
 * A class which defines the layout of the GUI of a single command
 * 
 * @author Earthcomputer
 *
 */
public abstract class CommandSyntax {

	private static Map<String, Class<? extends CommandSyntax>> commandSyntaxes = Maps.newHashMap();
	private static Map<String, String> aliases = Maps.newHashMap();

	protected static final Joiner spaceJoiner = Joiner.on(' ');

	private CommandSlotContext context;

	/**
	 * Builds the components of the command
	 * 
	 * @return The components of the command. They will be in the format of a
	 *         CommandSlotVerticalArrangement
	 */
	public abstract IGuiCommandSlot[] setupCommand();

	/**
	 * 
	 * @throws UIInvalidException
	 *             when the components of the command are invalid for a
	 *             miscellaneous reason (one not already thrown by the command
	 *             slots themselves)
	 */
	public void checkValid() throws UIInvalidException {
	}

	/**
	 * Returns true if the command sender can use this command
	 * 
	 * @param context
	 * @return
	 */
	public boolean canUseCommand(CommandSlotContext context) {
		return true;
	}

	private void setContext(CommandSlotContext context) {
		this.context = context;
	}

	/**
	 * Gets the context to build this syntax
	 * 
	 * @return
	 */
	public CommandSlotContext getContext() {
		return context;
	}

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
	public static void registerCommandSyntax(String commandName, Class<? extends CommandSyntax> syntaxClass) {
		commandSyntaxes.put(commandName, syntaxClass);
	}

	/**
	 * Registers an alias for a command. Modders should not use this method, and
	 * instead use
	 * {@link net.earthcomputer.easyeditors.api.EasyEditorsApi#registerCommandAlias(String, String)
	 * EasyEditorsApi.registerCommandAlias(String, String)} instead
	 * 
	 * @param mainCommandName
	 * @param aliasName
	 */
	public static void registerAlias(String mainCommandName, String aliasName) {
		aliases.put(aliasName, mainCommandName);
	}

	/**
	 * Creates an instance of ICommandSyntax from the given command name
	 * 
	 * @param commandName
	 * @return
	 */
	public static CommandSyntax forCommandName(String commandName, CommandSlotContext context) {
		if (aliases.containsKey(commandName)) {
			commandName = aliases.get(commandName);
		}
		Class<? extends CommandSyntax> syntax = commandSyntaxes.get(commandName);
		if (syntax == null)
			return null;
		try {
			CommandSyntax instance = syntax.getConstructor().newInstance();
			if (!instance.canUseCommand(context)) {
				return null;
			}
			instance.setContext(context);
			return instance;
		} catch (Exception e) {
			throw new ReportedException(CrashReport.makeCrashReport(e, "Doing reflection"));
		}
	}

	/**
	 * 
	 * @return A map of all registered command syntax types. The keys represent
	 *         the command names, and values represent types of command syntax
	 */
	public static Map<String, Class<? extends CommandSyntax>> getSyntaxList() {
		return Collections.unmodifiableMap(commandSyntaxes);
	}

	static {
		registerCommandSyntax("time", SyntaxTime.class);
		registerCommandSyntax("gamemode", SyntaxGamemode.class);
		registerCommandSyntax("difficulty", SyntaxDifficulty.class);
		registerCommandSyntax("defaultgamemode", SyntaxDefaultGamemode.class);
		registerCommandSyntax("kill", SyntaxKill.class);
		registerCommandSyntax("toggledownfall", SyntaxNoArguments.class);
		registerCommandSyntax("weather", SyntaxWeather.class);
		registerCommandSyntax("xp", SyntaxXP.class);
		registerCommandSyntax("tp", SyntaxTP.class);
		registerCommandSyntax("teleport", SyntaxTeleport.class);
		registerCommandSyntax("give", SyntaxGive.class);
		registerCommandSyntax("replaceitem", SyntaxReplaceItem.class);
		registerCommandSyntax("stats", SyntaxStats.class);
		registerCommandSyntax("effect", SyntaxEffect.class);
		registerCommandSyntax("enchant", SyntaxEnchant.class);
		registerCommandSyntax("particle", SyntaxParticle.class);
		registerCommandSyntax("me", SyntaxEmote.class);
		registerCommandSyntax("seed", SyntaxNoArguments.class);
		registerCommandSyntax("help", SyntaxNoArguments.class);
		registerAlias("help", "?");
		// TODO: should the debug command be included?
		registerCommandSyntax("tell", SyntaxTell.class);
		registerAlias("tell", "w");
		registerAlias("tell", "msg");
		registerCommandSyntax("say", SyntaxSay.class);
		registerCommandSyntax("spawnpoint", SyntaxSpawnpoint.class);
		registerCommandSyntax("setworldspawn", SyntaxSetWorldSpawn.class);
		registerCommandSyntax("gamerule", SyntaxGamerule.class);
		registerCommandSyntax("clear", SyntaxClear.class);
		registerCommandSyntax("testfor", SyntaxTestFor.class);
		registerCommandSyntax("spreadplayers", SyntaxSpreadPlayers.class);
		registerCommandSyntax("playsound", SyntaxPlaySound.class);
		registerCommandSyntax("scoreboard", SyntaxScoreboard.class);
	}

}
