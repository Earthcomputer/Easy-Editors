package net.earthcomputer.easyeditors.gui.command;

import java.util.Collections;
import java.util.Map;

import com.google.common.base.Joiner;
import com.google.common.collect.Maps;

import net.earthcomputer.easyeditors.gui.command.slot.CommandSlotExpand;
import net.earthcomputer.easyeditors.gui.command.slot.CommandSlotIntTextField;
import net.earthcomputer.easyeditors.gui.command.slot.CommandSlotItem;
import net.earthcomputer.easyeditors.gui.command.slot.CommandSlotLabel;
import net.earthcomputer.easyeditors.gui.command.slot.CommandSlotPlayerSelector;
import net.earthcomputer.easyeditors.gui.command.slot.CommandSlotRectangle;
import net.earthcomputer.easyeditors.gui.command.slot.IGuiCommandSlot;
import net.earthcomputer.easyeditors.util.Colors;
import net.minecraft.client.resources.I18n;
import net.minecraft.crash.CrashReport;
import net.minecraft.util.ReportedException;

public abstract class ICommandSyntax {

	private static Map<String, Class<? extends ICommandSyntax>> commandSyntaxes = Maps.newHashMap();

	public static final Joiner spaceJoiner = Joiner.on(' ');

	public abstract IGuiCommandSlot[] setupCommand();

	public abstract boolean isValid();

	protected String getStringFromNthArg(String[] args, int n) {
		String[] stringsToJoin = new String[args.length - n];
		System.arraycopy(args, n, stringsToJoin, 0, stringsToJoin.length);
		return spaceJoiner.join(stringsToJoin);
	}

	public static void registerCommandSyntax(String commandName, Class<? extends ICommandSyntax> syntaxClass) {
		commandSyntaxes.put(commandName, syntaxClass);
	}

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

	public static Map<String, Class<? extends ICommandSyntax>> getSyntaxList() {
		return Collections.unmodifiableMap(commandSyntaxes);
	}

	static {
		registerCommandSyntax("give", SyntaxGive.class);
	}

	public static class SyntaxGive extends ICommandSyntax {

		private CommandSlotPlayerSelector playerSelector;
		private CommandSlotItem item;
		private CommandSlotExpand expand1;
		private CommandSlotIntTextField damage;

		@Override
		public IGuiCommandSlot[] setupCommand() {
			item = new CommandSlotItem();
			playerSelector = new CommandSlotPlayerSelector();

			return new IGuiCommandSlot[] {
					CommandSlotLabel.createLabel(I18n.format("gui.commandEditor.give.player"),
							new CommandSlotRectangle(playerSelector, Colors.playerSelectorBox.color)),
					CommandSlotLabel.createLabel(I18n.format("gui.commandEditor.give.item"), item.withButton()),
					expand1 = new CommandSlotExpand(
							CommandSlotLabel.createLabel(I18n.format("gui.commandEditor.give.damage"),
									damage = new CommandSlotIntTextField(50, 50, 0, Short.MAX_VALUE))) };
		}

		@Override
		public boolean isValid() {
			return playerSelector.isValid() && item.getItem() != null;
		}

	}

}
