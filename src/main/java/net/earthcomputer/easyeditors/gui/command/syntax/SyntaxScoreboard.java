package net.earthcomputer.easyeditors.gui.command.syntax;

import net.earthcomputer.easyeditors.gui.command.slot.CommandSlotLabel;
import net.earthcomputer.easyeditors.gui.command.slot.CommandSlotMenu;
import net.earthcomputer.easyeditors.gui.command.slot.IGuiCommandSlot;
import net.earthcomputer.easyeditors.util.Translate;

public class SyntaxScoreboard extends ICommandSyntax {

	@Override
	public IGuiCommandSlot[] setupCommand() {
		return new IGuiCommandSlot[] { CommandSlotLabel.createLabel(Translate.GUI_COMMANDEDITOR_SCOREBOARD_SUBCOMMAND,
				new CommandSlotMenu("objectives", "players", "teams")) };
	}

}
