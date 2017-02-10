package net.earthcomputer.easyeditors.gui.command.syntax;

import net.earthcomputer.easyeditors.gui.command.slot.CommandSlotLabel;
import net.earthcomputer.easyeditors.gui.command.slot.IGuiCommandSlot;
import net.earthcomputer.easyeditors.util.Translate;

public class SyntaxDefaultGamemode extends CommandSyntax {

	@Override
	public IGuiCommandSlot[] setupCommand() {
		return new IGuiCommandSlot[] { CommandSlotLabel.createLabel(
				Translate.GUI_COMMANDEDITOR_DEFAULTGAMEMODE_GAMEMODE,
				Translate.GUI_COMMANDEDITOR_DEFAULTGAMEMODE_GAMEMODE_TOOLTIP, new SyntaxGamemode.GameModeMenu()) };
	}

}
