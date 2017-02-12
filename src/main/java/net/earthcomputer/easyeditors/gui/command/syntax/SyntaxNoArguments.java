package net.earthcomputer.easyeditors.gui.command.syntax;

import net.earthcomputer.easyeditors.gui.command.slot.CommandSlotConsumeRemainingArgs;
import net.earthcomputer.easyeditors.gui.command.slot.IGuiCommandSlot;

public class SyntaxNoArguments extends CommandSyntax {

	@Override
	public IGuiCommandSlot[] setupCommand() {
		return new IGuiCommandSlot[] { new CommandSlotConsumeRemainingArgs() };
	}

}
