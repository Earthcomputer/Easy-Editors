package net.earthcomputer.easyeditors.gui.command.syntax;

import com.google.common.collect.Lists;

import net.earthcomputer.easyeditors.gui.command.UIInvalidException;
import net.earthcomputer.easyeditors.gui.command.slot.CommandSlotOptional;
import net.earthcomputer.easyeditors.gui.command.slot.CommandSlotRelativeCoordinate;
import net.earthcomputer.easyeditors.gui.command.slot.IGuiCommandSlot;

public class SyntaxSetWorldSpawn extends CommandSyntax {

	@Override
	public IGuiCommandSlot[] setupCommand() {
		return new IGuiCommandSlot[] { new CommandSlotOptional.Impl(new CommandSlotRelativeCoordinate() {
			@Override
			public boolean isDefault() throws UIInvalidException {
				return super.isDefault() && getContext().getSenderClass() != null && getContext().isPlayer();
			}
		}, Lists.<CommandSlotOptional>newArrayList()) };
	}

}
