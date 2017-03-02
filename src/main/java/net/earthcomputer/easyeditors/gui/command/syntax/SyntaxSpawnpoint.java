package net.earthcomputer.easyeditors.gui.command.syntax;

import java.util.List;

import net.earthcomputer.easyeditors.api.util.Colors;
import net.earthcomputer.easyeditors.gui.command.CommandSyntaxException;
import net.earthcomputer.easyeditors.gui.command.UIInvalidException;
import net.earthcomputer.easyeditors.gui.command.slot.CommandSlotLabel;
import net.earthcomputer.easyeditors.gui.command.slot.CommandSlotPlayerSelector;
import net.earthcomputer.easyeditors.gui.command.slot.CommandSlotRectangle;
import net.earthcomputer.easyeditors.gui.command.slot.CommandSlotRelativeCoordinate;
import net.earthcomputer.easyeditors.gui.command.slot.IGuiCommandSlot;
import net.earthcomputer.easyeditors.util.Translate;

public class SyntaxSpawnpoint extends CommandSyntax {

	private CommandSlotPlayerSelector target;
	private RelativeCoord pos;

	@Override
	public IGuiCommandSlot[] setupCommand() {
		target = new CommandSlotPlayerSelector.WithDefault(CommandSlotPlayerSelector.PLAYERS_ONLY) {
			@Override
			public boolean isArgRedundant() throws UIInvalidException {
				return pos.isRedundant();
			}
		};
		pos = new RelativeCoord();
		return new IGuiCommandSlot[] { CommandSlotLabel.createLabel(Translate.GUI_COMMANDEDITOR_SPAWNPOINT_TARGET,
				new CommandSlotRectangle(target, Colors.playerSelectorBox.color)), pos };
	}

	private static class RelativeCoord extends CommandSlotRelativeCoordinate {
		public boolean isRedundant() throws UIInvalidException {
			getXArg().checkValid();
			getYArg().checkValid();
			getZArg().checkValid();
			return getXArg().getTextField().getDoubleValue() == 0 && getXArg().getRelative().isChecked()
					&& getYArg().getTextField().getDoubleValue() == 0 && getYArg().getRelative().isChecked()
					&& getZArg().getTextField().getDoubleValue() == 0 && getZArg().getRelative().isChecked();
		}

		@Override
		public int readFromArgs(String[] args, int index) throws CommandSyntaxException {
			if (args.length == index) {
				getXArg().getTextField().setText("0");
				getXArg().getRelative().setChecked(true);
				getYArg().getTextField().setText("0");
				getYArg().getRelative().setChecked(true);
				getZArg().getTextField().setText("0");
				getZArg().getRelative().setChecked(true);
				return 0;
			}
			return super.readFromArgs(args, index);
		}

		@Override
		public void addArgs(List<String> args) throws UIInvalidException {
			if (!isRedundant()) {
				super.addArgs(args);
			}
		}
	}

}
