package net.earthcomputer.easyeditors.gui.command.syntax;

import com.google.common.collect.Lists;

import net.earthcomputer.easyeditors.api.util.Colors;
import net.earthcomputer.easyeditors.gui.command.slot.CommandSlotLabel;
import net.earthcomputer.easyeditors.gui.command.slot.CommandSlotOptional;
import net.earthcomputer.easyeditors.gui.command.slot.CommandSlotPlayerSelector;
import net.earthcomputer.easyeditors.gui.command.slot.CommandSlotRectangle;
import net.earthcomputer.easyeditors.gui.command.slot.IGuiCommandSlot;
import net.earthcomputer.easyeditors.util.Translate;

public class SyntaxKill extends CommandSyntax {

	@Override
	public IGuiCommandSlot[] setupCommand() {
		return new IGuiCommandSlot[] {
				CommandSlotLabel
						.createLabel(Translate.GUI_COMMANDEDITOR_KILL_TARGET,
								Translate.GUI_COMMANDEDITOR_KILL_TARGET_TOOLTIP,
								new CommandSlotRectangle(
										new CommandSlotOptional.Impl(new CommandSlotPlayerSelector.Optional(),
												Lists.<CommandSlotOptional>newArrayList()),
										Colors.playerSelectorBox.color)) };
	}

}
