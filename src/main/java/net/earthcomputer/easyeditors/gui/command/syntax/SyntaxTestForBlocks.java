package net.earthcomputer.easyeditors.gui.command.syntax;

import com.google.common.collect.Lists;

import net.earthcomputer.easyeditors.gui.command.slot.CommandSlotLabel;
import net.earthcomputer.easyeditors.gui.command.slot.CommandSlotMenu;
import net.earthcomputer.easyeditors.gui.command.slot.CommandSlotOptional;
import net.earthcomputer.easyeditors.gui.command.slot.CommandSlotRelativeCoordinate;
import net.earthcomputer.easyeditors.gui.command.slot.IGuiCommandSlot;
import net.earthcomputer.easyeditors.util.Translate;

public class SyntaxTestForBlocks extends CommandSyntax {

	private CommandSlotRelativeCoordinate fromFrom;
	private CommandSlotRelativeCoordinate fromTo;
	private CommandSlotRelativeCoordinate to;
	private CommandSlotMenu.Optional mask;

	@Override
	public IGuiCommandSlot[] setupCommand() {
		fromFrom = new CommandSlotRelativeCoordinate();

		fromTo = new CommandSlotRelativeCoordinate();

		to = new CommandSlotRelativeCoordinate();

		mask = new CommandSlotMenu.WithDefault("all", new String[] { Translate.GUI_COMMANDEDITOR_TESTFORBLOCKS_MASK_ALL,
				Translate.GUI_COMMANDEDITOR_TESTFORBLOCKS_MASK_MASKED }, "all", "masked");

		return new IGuiCommandSlot[] {
				CommandSlotLabel.createLabel(Translate.GUI_COMMANDEDITOR_TESTFORBLOCKS_FROMFROM, fromFrom),
				CommandSlotLabel.createLabel(Translate.GUI_COMMANDEDITOR_TESTFORBLOCKS_FROMTO, fromTo),
				CommandSlotLabel.createLabel(Translate.GUI_COMMANDEDITOR_TESTFORBLOCKS_TO,
						Translate.GUI_COMMANDEDITOR_TESTFORBLOCKS_TO_TOOLTIP, to),
				CommandSlotLabel.createLabel(Translate.GUI_COMMANDEDITOR_TESTFORBLOCKS_MASK,
						new CommandSlotOptional.Impl(mask, Lists.<CommandSlotOptional>newArrayList())) };
	}

}
