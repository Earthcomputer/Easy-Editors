package net.earthcomputer.easyeditors.gui.command.syntax;

import net.earthcomputer.easyeditors.gui.command.slot.CommandSlotCheckbox;
import net.earthcomputer.easyeditors.gui.command.slot.CommandSlotNumberTextField;
import net.earthcomputer.easyeditors.gui.command.slot.CommandSlotPlayerSelector;
import net.earthcomputer.easyeditors.gui.command.slot.CommandSlotRadioList;
import net.earthcomputer.easyeditors.gui.command.slot.CommandSlotRelativeCoordinate;
import net.earthcomputer.easyeditors.gui.command.slot.IGuiCommandSlot;

public class SyntaxTP extends CommandSyntax {

	private CommandSlotPlayerSelector teleportingEntity;
	private CommandSlotRadioList target;
	private CommandSlotPlayerSelector targetEntity;
	private CommandSlotRelativeCoordinate targetCoordinate;
	private CommandSlotNumberTextField yaw;
	private CommandSlotNumberTextField pitch;
	private CommandSlotCheckbox yawRelative;
	private CommandSlotCheckbox pitchRelative;
	
	@Override
	public IGuiCommandSlot[] setupCommand() {
		// TODO Auto-generated method stub
		return null;
	}

}
