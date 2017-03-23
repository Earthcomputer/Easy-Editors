package net.earthcomputer.easyeditors.gui.command.syntax;

import java.util.List;

import com.google.common.collect.Lists;

import net.earthcomputer.easyeditors.api.util.Colors;
import net.earthcomputer.easyeditors.gui.command.slot.CommandSlotLabel;
import net.earthcomputer.easyeditors.gui.command.slot.CommandSlotOptional;
import net.earthcomputer.easyeditors.gui.command.slot.CommandSlotPlayerSelector;
import net.earthcomputer.easyeditors.gui.command.slot.CommandSlotRectangle;
import net.earthcomputer.easyeditors.gui.command.slot.CommandSlotRelativeCoordinate;
import net.earthcomputer.easyeditors.gui.command.slot.CommandSlotVerticalArrangement;
import net.earthcomputer.easyeditors.gui.command.slot.IGuiCommandSlot;
import net.earthcomputer.easyeditors.util.Translate;

public class SyntaxTeleport extends CommandSyntax {

	private CommandSlotPlayerSelector teleportingEntity;
	private CommandSlotRelativeCoordinate targetCoordinates;
	private CommandSlotRelativeCoordinate.CoordinateArg yaw;
	private CommandSlotRelativeCoordinate.CoordinateArg pitch;

	@Override
	public IGuiCommandSlot[] setupCommand() {
		teleportingEntity = new CommandSlotPlayerSelector();
		targetCoordinates = new CommandSlotRelativeCoordinate(Colors.miscBigBoxLabel.color);
		yaw = new CommandSlotRelativeCoordinate.CoordinateArg();
		pitch = new CommandSlotRelativeCoordinate.CoordinateArg();
		List<CommandSlotOptional> optionalGroup = Lists.newArrayList();
		IGuiCommandSlot target = new CommandSlotVerticalArrangement(targetCoordinates, CommandSlotLabel.createLabel(
				Translate.GUI_COMMANDEDITOR_TP_ROTATION, Colors.miscBigBoxLabel.color,
				new CommandSlotVerticalArrangement(
						CommandSlotLabel.createLabel(Translate.GUI_COMMANDEDITOR_TP_ROTATION_YAW,
								Colors.miscBigBoxLabel.color, new CommandSlotOptional.Impl(yaw, optionalGroup)),
						CommandSlotLabel.createLabel(Translate.GUI_COMMANDEDITOR_TP_ROTATION_PITCH,
								Colors.miscBigBoxLabel.color, new CommandSlotOptional.Impl(pitch, optionalGroup)))));
		return new IGuiCommandSlot[] {
				CommandSlotLabel.createLabel(Translate.GUI_COMMANDEDITOR_TP_TELEPORTINGENTITY,
						new CommandSlotRectangle(teleportingEntity, Colors.playerSelectorBox.color)),
				CommandSlotLabel.createLabel(Translate.GUI_COMMANDEDITOR_TELEPORT_TARGETCOORDINATE,
						new CommandSlotRectangle(target, Colors.miscBigBoxBox.color)) };
	}

}
