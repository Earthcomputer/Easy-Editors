package net.earthcomputer.easyeditors.gui.command.syntax;

import net.earthcomputer.easyeditors.api.util.Colors;
import net.earthcomputer.easyeditors.gui.command.CommandSyntaxException;
import net.earthcomputer.easyeditors.gui.command.UIInvalidException;
import net.earthcomputer.easyeditors.gui.command.slot.CommandSlotItemStack;
import net.earthcomputer.easyeditors.gui.command.slot.CommandSlotLabel;
import net.earthcomputer.easyeditors.gui.command.slot.CommandSlotPlayerSelector;
import net.earthcomputer.easyeditors.gui.command.slot.CommandSlotRadioList;
import net.earthcomputer.easyeditors.gui.command.slot.CommandSlotRectangle;
import net.earthcomputer.easyeditors.gui.command.slot.IGuiCommandSlot;
import net.earthcomputer.easyeditors.util.Translate;

public class SyntaxClear extends CommandSyntax {

	private CommandSlotPlayerSelector target;
	private CommandSlotRadioList optionalItem;
	private CommandSlotItemStack item;

	@Override
	public IGuiCommandSlot[] setupCommand() {
		target = new CommandSlotPlayerSelector.WithDefault(CommandSlotPlayerSelector.PLAYERS_ONLY) {
			@Override
			protected boolean isArgRedundant() throws UIInvalidException {
				return optionalItem.getSelectedIndex() == 0;
			}
		};

		item = new CommandSlotItemStack(true, 1, CommandSlotItemStack.COMPONENT_ITEM,
				CommandSlotItemStack.COMPONENT_DAMAGE, CommandSlotItemStack.COMPONENT_STACK_SIZE,
				CommandSlotItemStack.COMPONENT_NBT);

		optionalItem = new CommandSlotRadioList(
				CommandSlotLabel.createLabel(Translate.GUI_COMMANDEDITOR_CLEAR_ALLITEMS, Colors.miscBigBoxLabel.color),
				CommandSlotLabel.createLabel(Translate.GUI_COMMANDEDITOR_CLEAR_SPECIFICITEM,
						Colors.miscBigBoxLabel.color, new CommandSlotRectangle(item, Colors.itemBox.color))) {
			@Override
			protected int getSelectedIndexForString(String[] args, int index) throws CommandSyntaxException {
				if (args.length == index) {
					return 0;
				} else {
					return 1;
				}
			}

			@Override
			protected boolean shouldCheckIndexOutOfBounds() {
				return false;
			}
		};

		return new IGuiCommandSlot[] {
				CommandSlotLabel.createLabel(Translate.GUI_COMMANDEDITOR_CLEAR_TARGET,
						new CommandSlotRectangle(target, Colors.playerSelectorBox.color)),
				new CommandSlotRectangle(optionalItem, Colors.miscBigBoxBox.color) };
	}

}
