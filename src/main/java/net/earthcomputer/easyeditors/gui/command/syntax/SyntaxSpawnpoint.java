package net.earthcomputer.easyeditors.gui.command.syntax;

import java.util.List;

import com.google.common.collect.Lists;

import net.earthcomputer.easyeditors.api.util.Colors;
import net.earthcomputer.easyeditors.gui.command.slot.CommandSlotLabel;
import net.earthcomputer.easyeditors.gui.command.slot.CommandSlotOptional;
import net.earthcomputer.easyeditors.gui.command.slot.CommandSlotPlayerSelector;
import net.earthcomputer.easyeditors.gui.command.slot.CommandSlotRectangle;
import net.earthcomputer.easyeditors.gui.command.slot.CommandSlotRelativeCoordinate;
import net.earthcomputer.easyeditors.gui.command.slot.IGuiCommandSlot;
import net.earthcomputer.easyeditors.util.Translate;

public class SyntaxSpawnpoint extends CommandSyntax {

	private CommandSlotPlayerSelector.Optional target;
	private CommandSlotRelativeCoordinate pos;

	@Override
	public IGuiCommandSlot[] setupCommand() {
		target = new CommandSlotPlayerSelector.Optional(CommandSlotPlayerSelector.PLAYERS_ONLY);
		pos = new CommandSlotRelativeCoordinate();
		List<CommandSlotOptional> optionalGroup = Lists.newArrayList();
		return new IGuiCommandSlot[] {
				CommandSlotLabel
						.createLabel(Translate.GUI_COMMANDEDITOR_SPAWNPOINT_TARGET,
								new CommandSlotRectangle(new CommandSlotOptional.Impl(target, optionalGroup),
										Colors.playerSelectorBox.color)),
				new CommandSlotOptional.Impl(pos, optionalGroup) };
	}

}
