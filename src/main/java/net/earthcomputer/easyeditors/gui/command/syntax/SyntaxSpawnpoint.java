package net.earthcomputer.easyeditors.gui.command.syntax;

import net.earthcomputer.easyeditors.api.util.Colors;
import net.earthcomputer.easyeditors.gui.command.UIInvalidException;
import net.earthcomputer.easyeditors.gui.command.slot.CommandSlotLabel;
import net.earthcomputer.easyeditors.gui.command.slot.CommandSlotPlayerSelector;
import net.earthcomputer.easyeditors.gui.command.slot.CommandSlotRectangle;
import net.earthcomputer.easyeditors.gui.command.slot.CommandSlotRelativeCoordinate;
import net.earthcomputer.easyeditors.gui.command.slot.IGuiCommandSlot;
import net.earthcomputer.easyeditors.util.Translate;

public class SyntaxSpawnpoint extends CommandSyntax {

	private CommandSlotPlayerSelector target;
	private CommandSlotRelativeCoordinate.WithDefault pos;

	@Override
	public IGuiCommandSlot[] setupCommand() {
		target = new CommandSlotPlayerSelector.WithDefault(CommandSlotPlayerSelector.PLAYERS_ONLY) {
			@Override
			public boolean isArgRedundant() throws UIInvalidException {
				return pos.isSetToDefault();
			}
		};
		pos = new CommandSlotRelativeCoordinate.WithDefault();
		return new IGuiCommandSlot[] { CommandSlotLabel.createLabel(Translate.GUI_COMMANDEDITOR_SPAWNPOINT_TARGET,
				new CommandSlotRectangle(target, Colors.playerSelectorBox.color)), pos };
	}

}
