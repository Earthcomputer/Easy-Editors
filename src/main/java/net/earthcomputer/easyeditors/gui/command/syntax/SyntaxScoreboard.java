package net.earthcomputer.easyeditors.gui.command.syntax;

import net.earthcomputer.easyeditors.gui.command.slot.CommandSlotLabel;
import net.earthcomputer.easyeditors.gui.command.slot.CommandSlotMenu;
import net.earthcomputer.easyeditors.gui.command.slot.IGuiCommandSlot;
import net.minecraft.client.resources.I18n;

public class SyntaxScoreboard extends ICommandSyntax {

	@Override
	public IGuiCommandSlot[] setupCommand() {
		return new IGuiCommandSlot[] {
				CommandSlotLabel.createLabel(I18n.format("gui.commandEditor.scoreboard.subcommand"),
						new CommandSlotMenu("objectives", "players", "teams")) };
	}

}
