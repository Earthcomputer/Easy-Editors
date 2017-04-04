package net.earthcomputer.easyeditors.gui.command.syntax;

import java.util.List;

import com.google.common.collect.Lists;

import net.earthcomputer.easyeditors.api.util.Colors;
import net.earthcomputer.easyeditors.gui.command.CommandSyntaxException;
import net.earthcomputer.easyeditors.gui.command.slot.CommandSlotLabel;
import net.earthcomputer.easyeditors.gui.command.slot.CommandSlotOptional;
import net.earthcomputer.easyeditors.gui.command.slot.CommandSlotPlayerSelector;
import net.earthcomputer.easyeditors.gui.command.slot.CommandSlotRadioList;
import net.earthcomputer.easyeditors.gui.command.slot.CommandSlotRectangle;
import net.earthcomputer.easyeditors.gui.command.slot.CommandSlotRelativeCoordinate;
import net.earthcomputer.easyeditors.gui.command.slot.CommandSlotVerticalArrangement;
import net.earthcomputer.easyeditors.gui.command.slot.IGuiCommandSlot;
import net.earthcomputer.easyeditors.util.Translate;

public class SyntaxTP extends CommandSyntax {

	private CommandSlotOptional optionalTeleportingEntity;
	private CommandSlotPlayerSelector.Optional teleportingEntity;
	private CommandSlotRadioList target;
	private CommandSlotPlayerSelector targetEntity;
	private CommandSlotRelativeCoordinate targetCoordinate;
	private CommandSlotRelativeCoordinate.CoordinateArg yaw;
	private CommandSlotRelativeCoordinate.CoordinateArg pitch;

	@Override
	public IGuiCommandSlot[] setupCommand() {
		teleportingEntity = new CommandSlotPlayerSelector.Optional();
		optionalTeleportingEntity = new CommandSlotOptional.Impl(teleportingEntity,
				Lists.<CommandSlotOptional>newArrayList()) {
			@Override
			public boolean isPresentInArgs(String[] args, int index) throws CommandSyntaxException {
				int len = args.length - index;
				return len == 2 || len == 4 || len == 6;
			}
		};
		targetEntity = new CommandSlotPlayerSelector(CommandSlotPlayerSelector.ONE_ONLY);
		targetCoordinate = new CommandSlotRelativeCoordinate(Colors.miscBigBoxLabel.color);
		yaw = new CommandSlotRelativeCoordinate.CoordinateArg();
		pitch = new CommandSlotRelativeCoordinate.CoordinateArg();
		List<CommandSlotOptional> optionalGroup = Lists.newArrayList();
		IGuiCommandSlot targetCoord = new CommandSlotVerticalArrangement(targetCoordinate, CommandSlotLabel.createLabel(
				Translate.GUI_COMMANDEDITOR_TP_ROTATION, Colors.miscBigBoxLabel.color,
				new CommandSlotVerticalArrangement(
						CommandSlotLabel.createLabel(Translate.GUI_COMMANDEDITOR_TP_ROTATION_YAW,
								Colors.miscBigBoxLabel.color, new CommandSlotOptional.Impl(yaw, optionalGroup)),
						CommandSlotLabel.createLabel(Translate.GUI_COMMANDEDITOR_TP_ROTATION_PITCH,
								Colors.miscBigBoxLabel.color, new CommandSlotOptional.Impl(pitch, optionalGroup)))));
		target = new CommandSlotRadioList(
				CommandSlotLabel.createLabel(Translate.GUI_COMMANDEDITOR_TP_TARGET_ENTITY, Colors.miscBigBoxLabel.color,
						new CommandSlotRectangle(targetEntity, Colors.playerSelectorBox.color)),
				CommandSlotLabel.createLabel(Translate.GUI_COMMANDEDITOR_TP_TARGET_COORDINATE,
						Colors.miscBigBoxLabel.color, targetCoord)) {
			@Override
			protected int getSelectedIndexForString(String[] args, int index) throws CommandSyntaxException {
				int len = args.length - index;
				return len == 1 || len == 2 ? 0 : 1;
			}
		};
		target.setSelectedIndex(1);
		return new IGuiCommandSlot[] {
				CommandSlotLabel.createLabel(Translate.GUI_COMMANDEDITOR_TP_TELEPORTINGENTITY,
						new CommandSlotRectangle(optionalTeleportingEntity, Colors.playerSelectorBox.color)),
				CommandSlotLabel.createLabel(Translate.GUI_COMMANDEDITOR_TP_TARGET,
						new CommandSlotRectangle(target, Colors.miscBigBoxBox.color)) };
	}

}
