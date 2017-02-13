package net.earthcomputer.easyeditors.gui.command.syntax;

import java.util.List;

import net.earthcomputer.easyeditors.api.util.Colors;
import net.earthcomputer.easyeditors.gui.command.CommandSyntaxException;
import net.earthcomputer.easyeditors.gui.command.UIInvalidException;
import net.earthcomputer.easyeditors.gui.command.slot.CommandSlotCheckbox;
import net.earthcomputer.easyeditors.gui.command.slot.CommandSlotLabel;
import net.earthcomputer.easyeditors.gui.command.slot.CommandSlotNumberTextField;
import net.earthcomputer.easyeditors.gui.command.slot.CommandSlotPlayerSelector;
import net.earthcomputer.easyeditors.gui.command.slot.CommandSlotRadioList;
import net.earthcomputer.easyeditors.gui.command.slot.CommandSlotRectangle;
import net.earthcomputer.easyeditors.gui.command.slot.CommandSlotRelativeCoordinate;
import net.earthcomputer.easyeditors.gui.command.slot.CommandSlotVerticalArrangement;
import net.earthcomputer.easyeditors.gui.command.slot.IGuiCommandSlot;
import net.earthcomputer.easyeditors.util.Translate;

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
		teleportingEntity = new CommandSlotPlayerSelector.WithDefault() {
			@Override
			protected boolean isArgAbsent(String[] args, int index) {
				int len = args.length - index;
				return len != 2 && len != 4 && len != 6;
			}
		};
		targetEntity = new CommandSlotPlayerSelector(CommandSlotPlayerSelector.ONE_ONLY);
		targetCoordinate = new CommandSlotRelativeCoordinate(Colors.tpTargetLabel.color);
		yaw = new CommandSlotNumberTextField(50, 100, -30000000, 30000000) {
			@Override
			public int readFromArgs(String[] args, int index) throws CommandSyntaxException {
				if (args.length == index) {
					setText("0");
					yawRelative.setChecked(true);
					return 0;
				}
				String arg = args[index];
				boolean relative = arg.startsWith("~");
				if (relative) {
					arg = arg.substring(1);
					if (arg.isEmpty()) {
						arg = "0";
					}
				}
				yawRelative.setChecked(relative);
				return super.readFromArgs(new String[] { arg }, 0);
			}

			@Override
			public void addArgs(List<String> args) throws UIInvalidException {
				super.addArgs(args);
				if (yawRelative.isChecked()) {
					String arg;
					if (getDoubleValue() == 0) {
						if (pitch.getDoubleValue() == 0 && pitchRelative.isChecked()) {
							args.remove(args.size() - 1);
							return;
						}
						arg = "~";
					} else {
						arg = args.get(args.size() - 1);
					}
					args.set(args.size() - 1, arg);
				}
			}
		};
		yaw.setText("0");
		pitch = new CommandSlotNumberTextField(50, 100, -30000000, 30000000) {
			@Override
			public int readFromArgs(String[] args, int index) throws CommandSyntaxException {
				if (args.length == index) {
					setText("0");
					pitchRelative.setChecked(true);
					return 0;
				}
				String arg = args[index];
				boolean relative = arg.startsWith("~");
				if (relative) {
					arg = arg.substring(1);
					if (arg.isEmpty()) {
						arg = "0";
					}
				}
				pitchRelative.setChecked(relative);
				return super.readFromArgs(new String[] { arg }, 0);
			}

			@Override
			public void addArgs(List<String> args) throws UIInvalidException {
				super.addArgs(args);
				if (pitchRelative.isChecked()) {
					if (getDoubleValue() == 0) {
						args.remove(args.size() - 1);
						return;
					}
					args.set(args.size() - 1, "~" + args.get(args.size() - 1));
				}
			}
		};
		pitch.setText("0");
		yawRelative = new CommandSlotCheckbox(Translate.GUI_COMMANDEDITOR_RELATIVECOORDINATE) {
			@Override
			public int readFromArgs(String[] args, int index) throws CommandSyntaxException {
				return 0;
			}

			@Override
			public void addArgs(List<String> args) {
			}
		};
		yawRelative.setChecked(true);
		pitchRelative = new CommandSlotCheckbox(Translate.GUI_COMMANDEDITOR_RELATIVECOORDINATE) {
			@Override
			public int readFromArgs(String[] args, int index) throws CommandSyntaxException {
				return 0;
			}

			@Override
			public void addArgs(List<String> args) {
			}
		};
		pitchRelative.setChecked(true);
		target = new CommandSlotRadioList(
				CommandSlotLabel.createLabel(Translate.GUI_COMMANDEDITOR_TP_TARGET_ENTITY, Colors.tpTargetLabel.color,
						new CommandSlotRectangle(targetEntity, Colors.playerSelectorBox.color)),
				CommandSlotLabel.createLabel(Translate.GUI_COMMANDEDITOR_TP_TARGET_COORDINATE,
						Colors.tpTargetLabel.color,
						new CommandSlotVerticalArrangement(targetCoordinate, CommandSlotLabel.createLabel(
								Translate.GUI_COMMANDEDITOR_TP_ROTATION, Colors.tpTargetLabel.color,
								new CommandSlotVerticalArrangement(
										CommandSlotLabel.createLabel(Translate.GUI_COMMANDEDITOR_TP_ROTATION_YAW,
												Colors.tpTargetLabel.color, yaw, yawRelative),
										CommandSlotLabel.createLabel(Translate.GUI_COMMANDEDITOR_TP_ROTATION_PITCH,
												Colors.tpTargetLabel.color, pitch, pitchRelative)))))) {
			@Override
			protected int getSelectedIndexForString(String[] args, int index) throws CommandSyntaxException {
				int len = args.length - index;
				return len == 1 || len == 2 ? 0 : 1;
			}
		};
		target.setSelectedIndex(1);
		return new IGuiCommandSlot[] {
				CommandSlotLabel.createLabel(Translate.GUI_COMMANDEDITOR_TP_TELEPORTINGENTITY,
						new CommandSlotRectangle(teleportingEntity, Colors.playerSelectorBox.color)),
				CommandSlotLabel.createLabel(Translate.GUI_COMMANDEDITOR_TP_TARGET,
						new CommandSlotRectangle(target, Colors.tpTargetBox.color)) };
	}

}
