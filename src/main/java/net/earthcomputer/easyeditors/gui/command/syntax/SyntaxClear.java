package net.earthcomputer.easyeditors.gui.command.syntax;

import java.util.List;

import com.google.common.collect.Lists;

import net.earthcomputer.easyeditors.api.util.Colors;
import net.earthcomputer.easyeditors.gui.command.CommandSyntaxException;
import net.earthcomputer.easyeditors.gui.command.UIInvalidException;
import net.earthcomputer.easyeditors.gui.command.slot.CommandSlotItemStack;
import net.earthcomputer.easyeditors.gui.command.slot.CommandSlotLabel;
import net.earthcomputer.easyeditors.gui.command.slot.CommandSlotOptional;
import net.earthcomputer.easyeditors.gui.command.slot.CommandSlotPlayerSelector;
import net.earthcomputer.easyeditors.gui.command.slot.CommandSlotRadioList;
import net.earthcomputer.easyeditors.gui.command.slot.CommandSlotRectangle;
import net.earthcomputer.easyeditors.gui.command.slot.IGuiCommandSlot;
import net.earthcomputer.easyeditors.util.Translate;

public class SyntaxClear extends CommandSyntax {

	private CommandSlotPlayerSelector.Optional target;
	private CommandSlotOptional optionalTarget;
	private CommandSlotItemStack item;
	private CommandSlotOptional optionalItem;

	@Override
	public IGuiCommandSlot[] setupCommand() {
		List<CommandSlotOptional> optionalGroup = Lists.newArrayList();

		target = new CommandSlotPlayerSelector.Optional(CommandSlotPlayerSelector.PLAYERS_ONLY);

		optionalTarget = new CommandSlotOptional.Impl(target, optionalGroup);

		item = new CommandSlotItemStack(true, 1, CommandSlotItemStack.COMPONENT_ITEM,
				CommandSlotItemStack.COMPONENT_DAMAGE, CommandSlotItemStack.COMPONENT_STACK_SIZE,
				CommandSlotItemStack.COMPONENT_NBT);

		optionalItem = new CommandSlotOptional(new CommandSlotRadioList(
				CommandSlotLabel.createLabel(Translate.GUI_COMMANDEDITOR_CLEAR_ALLITEMS, Colors.miscBigBoxLabel.color),
				CommandSlotLabel.createLabel(Translate.GUI_COMMANDEDITOR_CLEAR_SPECIFICITEM,
						Colors.miscBigBoxLabel.color, new CommandSlotRectangle(item, Colors.itemBox.color))) {
			@Override
			protected int getSelectedIndexForString(String[] args, int index) throws CommandSyntaxException {
				return 1;
			}
		}, optionalGroup) {
			@Override
			protected boolean isDefault() throws UIInvalidException {
				return ((CommandSlotRadioList) getChild()).getSelectedIndex() == 0;
			}

			@Override
			protected void setToDefault() {
				((CommandSlotRadioList) getChild()).setSelectedIndex(0);
			}
		};

		return new IGuiCommandSlot[] {
				CommandSlotLabel.createLabel(Translate.GUI_COMMANDEDITOR_CLEAR_TARGET,
						new CommandSlotRectangle(optionalTarget, Colors.playerSelectorBox.color)),
				new CommandSlotRectangle(optionalItem, Colors.miscBigBoxBox.color) };
	}

}
