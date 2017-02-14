package net.earthcomputer.easyeditors.gui.command.syntax;

import java.util.List;

import net.earthcomputer.easyeditors.api.util.Colors;
import net.earthcomputer.easyeditors.gui.command.CommandSyntaxException;
import net.earthcomputer.easyeditors.gui.command.UIInvalidException;
import net.earthcomputer.easyeditors.gui.command.slot.CommandSlotLabel;
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
		yaw = new CommandSlotRelativeCoordinate.CoordinateArg() {
			@Override
			public int readFromArgs(String[] args, int index) throws CommandSyntaxException {
				if (args.length == index) {
					getTextField().setText("0");
					getRelative().setChecked(true);
					return 0;
				}
				return super.readFromArgs(args, index);
			}

			@Override
			public void addArgs(List<String> args) throws UIInvalidException {
				checkValid();
				pitch.checkValid();
				if (getTextField().getDoubleValue() != 0 || !getRelative().isChecked()
						|| pitch.getTextField().getDoubleValue() != 0 || !pitch.getRelative().isChecked()) {
					super.addArgs(args);
				}
			}
		};
		pitch = new CommandSlotRelativeCoordinate.CoordinateArg() {
			@Override
			public int readFromArgs(String[] args, int index) throws CommandSyntaxException {
				if (args.length == index) {
					getTextField().setText("0");
					getRelative().setChecked(true);
					return 0;
				}
				return super.readFromArgs(args, index);
			}

			@Override
			public void addArgs(List<String> args) throws UIInvalidException {
				// Already checked validity by yaw
				if (getTextField().getDoubleValue() != 0 || !getRelative().isChecked()) {
					super.addArgs(args);
				}
			}
		};
		IGuiCommandSlot target = new CommandSlotVerticalArrangement(targetCoordinates,
				CommandSlotLabel.createLabel(Translate.GUI_COMMANDEDITOR_TP_ROTATION, Colors.miscBigBoxLabel.color,
						new CommandSlotVerticalArrangement(
								CommandSlotLabel.createLabel(Translate.GUI_COMMANDEDITOR_TP_ROTATION_YAW,
										Colors.miscBigBoxLabel.color, yaw),
								CommandSlotLabel.createLabel(Translate.GUI_COMMANDEDITOR_TP_ROTATION_PITCH,
										Colors.miscBigBoxLabel.color, pitch))));
		return new IGuiCommandSlot[] {
				CommandSlotLabel.createLabel(Translate.GUI_COMMANDEDITOR_TP_TELEPORTINGENTITY,
						new CommandSlotRectangle(teleportingEntity, Colors.playerSelectorBox.color)),
				CommandSlotLabel.createLabel(Translate.GUI_COMMANDEDITOR_TELEPORT_TARGETCOORDINATE,
						new CommandSlotRectangle(target, Colors.miscBigBoxBox.color)) };
	}

}
