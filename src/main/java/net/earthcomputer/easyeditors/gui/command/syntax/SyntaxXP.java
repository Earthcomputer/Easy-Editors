package net.earthcomputer.easyeditors.gui.command.syntax;

import java.util.List;

import net.earthcomputer.easyeditors.api.util.Colors;
import net.earthcomputer.easyeditors.gui.command.CommandSyntaxException;
import net.earthcomputer.easyeditors.gui.command.UIInvalidException;
import net.earthcomputer.easyeditors.gui.command.slot.CommandSlotIntTextField;
import net.earthcomputer.easyeditors.gui.command.slot.CommandSlotLabel;
import net.earthcomputer.easyeditors.gui.command.slot.CommandSlotPlayerSelector;
import net.earthcomputer.easyeditors.gui.command.slot.CommandSlotRadioList;
import net.earthcomputer.easyeditors.gui.command.slot.CommandSlotRectangle;
import net.earthcomputer.easyeditors.gui.command.slot.IGuiCommandSlot;
import net.earthcomputer.easyeditors.util.Translate;
import net.earthcomputer.easyeditors.util.TranslateKeys;

public class SyntaxXP extends CommandSyntax {

	@Override
	public IGuiCommandSlot[] setupCommand() {
		CommandSlotIntTextField xpPoints = new CommandSlotIntTextField(100, 100)
				.setNumberInvalidMessage(TranslateKeys.GUI_COMMANDEDITOR_XP_XP_POINTS_INVALID)
				.setOutOfBoundsMessage(TranslateKeys.GUI_COMMANDEDITOR_XP_XP_POINTS_OUTOFBOUNDS);
		CommandSlotIntTextField xpLevels = new CommandSlotIntTextField(100, 100) {
			@Override
			public int readFromArgs(String[] args, int index) throws CommandSyntaxException {
				if (args.length == index || args[index].isEmpty()) {
					throw new CommandSyntaxException();
				}
				return super.readFromArgs(new String[] { args[index].substring(0, args[index].length() - 1) }, 0);
			}

			@Override
			public void addArgs(List<String> args) throws UIInvalidException {
				super.addArgs(args);
				args.set(args.size() - 1, args.get(args.size() - 1) + "L");
			}
		};
		CommandSlotRadioList xp = new CommandSlotRadioList(
				CommandSlotLabel.createLabel(Translate.GUI_COMMANDEDITOR_XP_XP_POINTS,
						Translate.GUI_COMMANDEDITOR_XP_XP_POINTS_TOOLTIP, xpPoints),
				CommandSlotLabel.createLabel(Translate.GUI_COMMANDEDITOR_XP_XP_LEVELS,
						Translate.GUI_COMMANDEDITOR_XP_XP_LEVELS_TOOLTIP, xpLevels)) {
			@Override
			protected int getSelectedIndexForString(String[] args, int index) throws CommandSyntaxException {
				if (!args[index].isEmpty() && (args[index].endsWith("L") || args[index].endsWith("l"))) {
					return 1;
				} else {
					return 0;
				}
			}
		};
		CommandSlotPlayerSelector player = new CommandSlotPlayerSelector.WithDefault(
				CommandSlotPlayerSelector.PLAYERS_ONLY);
		return new IGuiCommandSlot[] {
				CommandSlotLabel.createLabel(Translate.GUI_COMMANDEDITOR_XP_XP,
						Translate.GUI_COMMANDEDITOR_XP_XP_TOOLTIP, xp),
				CommandSlotLabel.createLabel(Translate.GUI_COMMANDEDITOR_XP_PLAYER,
						Translate.GUI_COMMANDEDITOR_XP_PLAYER_TOOLTIP,
						new CommandSlotRectangle(player, Colors.playerSelectorBox.color)) };
	}

}
