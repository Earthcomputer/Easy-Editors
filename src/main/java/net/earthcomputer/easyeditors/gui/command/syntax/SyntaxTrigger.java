package net.earthcomputer.easyeditors.gui.command.syntax;

import net.earthcomputer.easyeditors.gui.command.CommandSlotContext;
import net.earthcomputer.easyeditors.gui.command.slot.CommandSlotIntTextField;
import net.earthcomputer.easyeditors.gui.command.slot.CommandSlotLabel;
import net.earthcomputer.easyeditors.gui.command.slot.CommandSlotMenu;
import net.earthcomputer.easyeditors.gui.command.slot.CommandSlotScore;
import net.earthcomputer.easyeditors.gui.command.slot.IGuiCommandSlot;
import net.earthcomputer.easyeditors.util.Translate;

public class SyntaxTrigger extends CommandSyntax {

	private CommandSlotScore objective;
	private CommandSlotMenu operation;
	private CommandSlotIntTextField value;

	@Override
	public IGuiCommandSlot[] setupCommand() {
		objective = new CommandSlotScore();

		operation = new CommandSlotMenu(new String[] { Translate.GUI_COMMANDEDITOR_TRIGGER_OPERATION_SET,
				Translate.GUI_COMMANDEDITOR_TRIGGER_OPERATION_ADD }, "set", "add");

		value = new CommandSlotIntTextField(50, 50);

		return new IGuiCommandSlot[] {
				CommandSlotLabel.createLabel(Translate.GUI_COMMANDEDITOR_TRIGGER_OBJECTIVE, objective),
				CommandSlotLabel.createLabel(Translate.GUI_COMMANDEDITOR_TRIGGER_OPERATION, operation),
				CommandSlotLabel.createLabel(Translate.GUI_COMMANDEDITOR_TRIGGER_VALUE, value) };
	}

	@Override
	public boolean canUseCommand(CommandSlotContext context) {
		return context.getSenderClass() == null || context.isPlayer();
	}

}
