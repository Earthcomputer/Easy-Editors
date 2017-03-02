package net.earthcomputer.easyeditors.gui.command.syntax;

import net.earthcomputer.easyeditors.gui.command.UIInvalidException;
import net.earthcomputer.easyeditors.gui.command.slot.CommandSlotRelativeCoordinate;
import net.earthcomputer.easyeditors.gui.command.slot.IGuiCommandSlot;

public class SyntaxSetWorldSpawn extends CommandSyntax {

	@Override
	public IGuiCommandSlot[] setupCommand() {
		return new IGuiCommandSlot[] { new CommandSlotRelativeCoordinate.WithDefault() {
			@Override
			protected boolean isArgAbsent(String[] args, int index) {
				return super.isArgAbsent(args, index) && getContext().isPlayer();
			}

			@Override
			protected boolean isArgRedundant() throws UIInvalidException {
				return getContext().getSenderClass() != null && getContext().isPlayer();
			}
		} };
	}

}
