package net.earthcomputer.easyeditors.gui.command.syntax;

import net.earthcomputer.easyeditors.api.util.Colors;
import net.earthcomputer.easyeditors.gui.command.slot.CommandSlotItemStack;
import net.earthcomputer.easyeditors.gui.command.slot.CommandSlotLabel;
import net.earthcomputer.easyeditors.gui.command.slot.CommandSlotPlayerSelector;
import net.earthcomputer.easyeditors.gui.command.slot.CommandSlotRectangle;
import net.earthcomputer.easyeditors.gui.command.slot.IGuiCommandSlot;
import net.earthcomputer.easyeditors.util.Translate;

public class SyntaxGive extends CommandSyntax {

	private CommandSlotPlayerSelector playerSelector;
	private CommandSlotItemStack item;

	@Override
	public IGuiCommandSlot[] setupCommand() {
		item = new CommandSlotItemStack(1, CommandSlotItemStack.COMPONENT_ITEM,
				CommandSlotItemStack.COMPONENT_STACK_SIZE, CommandSlotItemStack.COMPONENT_DAMAGE,
				CommandSlotItemStack.COMPONENT_NBT);
		playerSelector = new CommandSlotPlayerSelector(CommandSlotPlayerSelector.PLAYERS_ONLY);

		return new IGuiCommandSlot[] {
				CommandSlotLabel.createLabel(Translate.GUI_COMMANDEDITOR_GIVE_PLAYER,
						Translate.GUI_COMMANDEDITOR_GIVE_PLAYER_TOOLTIP,
						new CommandSlotRectangle(playerSelector, Colors.playerSelectorBox.color)),
				CommandSlotLabel.createLabel(Translate.GUI_COMMANDEDITOR_GIVE_ITEM,
						Translate.GUI_COMMANDEDITOR_GIVE_ITEM_TOOLTIP,
						new CommandSlotRectangle(item, Colors.itemBox.color)) };
	}
}
