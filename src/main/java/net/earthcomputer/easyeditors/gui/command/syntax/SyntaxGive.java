package net.earthcomputer.easyeditors.gui.command.syntax;

import net.earthcomputer.easyeditors.api.Colors;
import net.earthcomputer.easyeditors.gui.command.slot.CommandSlotItemStack;
import net.earthcomputer.easyeditors.gui.command.slot.CommandSlotLabel;
import net.earthcomputer.easyeditors.gui.command.slot.CommandSlotPlayerSelector;
import net.earthcomputer.easyeditors.gui.command.slot.CommandSlotRectangle;
import net.earthcomputer.easyeditors.gui.command.slot.IGuiCommandSlot;
import net.minecraft.client.resources.I18n;

public class SyntaxGive extends ICommandSyntax {
	
	private CommandSlotPlayerSelector playerSelector;
	private CommandSlotItemStack item;

	@Override
	public IGuiCommandSlot[] setupCommand() {
		item = new CommandSlotItemStack(1, CommandSlotItemStack.COMPONENT_ITEM,
				CommandSlotItemStack.COMPONENT_STACK_SIZE, CommandSlotItemStack.COMPONENT_DAMAGE,
				CommandSlotItemStack.COMPONENT_NBT);
		playerSelector = new CommandSlotPlayerSelector();

		return new IGuiCommandSlot[] {
				CommandSlotLabel.createLabel(I18n.format("gui.commandEditor.give.player"),
						I18n.format("gui.commandEditor.give.player.tooltip"),
						new CommandSlotRectangle(playerSelector, Colors.playerSelectorBox.color)),
				CommandSlotLabel.createLabel(I18n.format("gui.commandEditor.give.item"),
						I18n.format("gui.commandEditor.give.item.tooltip"),
						new CommandSlotRectangle(item, Colors.itemBox.color)) };
	}

	@Override
	public boolean isValid() {
		return playerSelector.isValid() && item.isValid();
	}
}
