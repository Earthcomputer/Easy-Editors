package net.earthcomputer.easyeditors.gui.command.syntax;

import net.earthcomputer.easyeditors.gui.command.slot.CommandSlotFormattedTextField;
import net.earthcomputer.easyeditors.gui.command.slot.CommandSlotLabel;
import net.earthcomputer.easyeditors.gui.command.slot.CommandSlotTextField;
import net.earthcomputer.easyeditors.gui.command.slot.IGuiCommandSlot;
import net.earthcomputer.easyeditors.util.Translate;

public class SyntaxEmote extends CommandSyntax {

	@Override
	public IGuiCommandSlot[] setupCommand() {
		return new IGuiCommandSlot[] {
				CommandSlotLabel.createLabel(Translate.GUI_COMMANDEDITOR_ME_MESSAGE, getContext().canHoldFormatting()
						? new CommandSlotFormattedTextField(500) : new CommandSlotTextField(500, 500)) };
	}

}
