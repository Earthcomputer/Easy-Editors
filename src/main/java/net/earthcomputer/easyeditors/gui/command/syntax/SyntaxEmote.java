package net.earthcomputer.easyeditors.gui.command.syntax;

import net.earthcomputer.easyeditors.gui.command.slot.CommandSlotFormattedTextField;
import net.earthcomputer.easyeditors.gui.command.slot.CommandSlotLabel;
import net.earthcomputer.easyeditors.gui.command.slot.CommandSlotTextField;
import net.earthcomputer.easyeditors.gui.command.slot.IGuiCommandSlot;
import net.earthcomputer.easyeditors.gui.command.slot.ITextField;
import net.earthcomputer.easyeditors.util.Translate;

public class SyntaxEmote extends CommandSyntax {

	@Override
	public IGuiCommandSlot[] setupCommand() {
		ITextField<?> textField;
		if (getContext().canHoldFormatting()) {
			textField = new CommandSlotFormattedTextField(500);
		} else {
			textField = new CommandSlotTextField(500, 500);
		}
		textField.setMaxStringLength(Short.MAX_VALUE);
		return new IGuiCommandSlot[] {
				CommandSlotLabel.createLabel(Translate.GUI_COMMANDEDITOR_ME_MESSAGE, (IGuiCommandSlot) textField) };
	}

}
