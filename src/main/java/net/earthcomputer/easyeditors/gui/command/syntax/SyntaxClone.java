package net.earthcomputer.easyeditors.gui.command.syntax;

import java.util.List;

import com.google.common.collect.Lists;

import net.earthcomputer.easyeditors.api.util.Colors;
import net.earthcomputer.easyeditors.gui.command.UIInvalidException;
import net.earthcomputer.easyeditors.gui.command.slot.CommandSlotBlock;
import net.earthcomputer.easyeditors.gui.command.slot.CommandSlotLabel;
import net.earthcomputer.easyeditors.gui.command.slot.CommandSlotMenu;
import net.earthcomputer.easyeditors.gui.command.slot.CommandSlotModifiable;
import net.earthcomputer.easyeditors.gui.command.slot.CommandSlotOptional;
import net.earthcomputer.easyeditors.gui.command.slot.CommandSlotRectangle;
import net.earthcomputer.easyeditors.gui.command.slot.CommandSlotRelativeCoordinate;
import net.earthcomputer.easyeditors.gui.command.slot.IGuiCommandSlot;
import net.earthcomputer.easyeditors.util.Translate;

public class SyntaxClone extends CommandSyntax {

	private CommandSlotRelativeCoordinate fromFrom;
	private CommandSlotRelativeCoordinate fromTo;
	private CommandSlotRelativeCoordinate to;
	private CommandSlotMenu.Optional maskMode;
	private CommandSlotMenu.Optional cloneMode;
	private CommandSlotModifiable modifiableBlock;
	private IGuiCommandSlot argBlock;
	private CommandSlotBlock block;

	@Override
	public IGuiCommandSlot[] setupCommand() {
		fromFrom = new CommandSlotRelativeCoordinate();

		fromTo = new CommandSlotRelativeCoordinate();

		to = new CommandSlotRelativeCoordinate();

		maskMode = new CommandSlotMenu.WithDefault("replace",
				new String[] { Translate.GUI_COMMANDEDITOR_CLONE_MASKMODE_REPLACE,
						Translate.GUI_COMMANDEDITOR_CLONE_MASKMODE_MASKED,
						Translate.GUI_COMMANDEDITOR_CLONE_MASKMODE_FILTERED },
				"replace", "masked", "filtered") {
			@Override
			protected void onChanged(String to) {
				if ("filtered".equals(to)) {
					modifiableBlock.setChild(argBlock);
				} else {
					modifiableBlock.setChild(null);
				}
			}
		};

		cloneMode = new CommandSlotMenu.WithDefault("normal",
				new String[] { Translate.GUI_COMMANDEDITOR_CLONE_CLONEMODE_NORMAL,
						Translate.GUI_COMMANDEDITOR_CLONE_CLONEMODE_FORCE,
						Translate.GUI_COMMANDEDITOR_CLONE_CLONEMODE_MOVE },
				"normal", "force", "move");

		block = new CommandSlotBlock(true, 1, CommandSlotBlock.COMPONENT_BLOCK, CommandSlotBlock.COMPONENT_PROPERTIES);

		argBlock = CommandSlotLabel.createLabel(Translate.GUI_COMMANDEDITOR_CLONE_BLOCK,
				new CommandSlotRectangle(block, Colors.itemBox.color));

		modifiableBlock = new CommandSlotModifiable();

		List<CommandSlotOptional> optionalGroup = Lists.newArrayList();

		return new IGuiCommandSlot[] {
				CommandSlotLabel.createLabel(Translate.GUI_COMMANDEDITOR_CLONE_FROMFROM, fromFrom),
				CommandSlotLabel.createLabel(Translate.GUI_COMMANDEDITOR_CLONE_FROMTO, fromTo),
				CommandSlotLabel.createLabel(Translate.GUI_COMMANDEDITOR_CLONE_TO,
						Translate.GUI_COMMANDEDITOR_CLONE_TO_TOOLTIP, to),
				CommandSlotLabel.createLabel(Translate.GUI_COMMANDEDITOR_CLONE_MASKMODE,
						new CommandSlotOptional.Impl(maskMode, optionalGroup)),
				CommandSlotLabel.createLabel(Translate.GUI_COMMANDEDITOR_CLONE_CLONEMODE,
						new CommandSlotOptional.Impl(cloneMode, optionalGroup) {
							@Override
							public boolean isDefault() throws UIInvalidException {
								return super.isDefault() && maskMode.getCurrentIndex() != 2;
							}
						}),
				modifiableBlock };
	}

}
