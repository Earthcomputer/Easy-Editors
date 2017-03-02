package net.earthcomputer.easyeditors.gui.command.syntax;

import net.earthcomputer.easyeditors.api.util.Colors;
import net.earthcomputer.easyeditors.gui.command.slot.CommandSlotLabel;
import net.earthcomputer.easyeditors.gui.command.slot.CommandSlotPlayerSelector;
import net.earthcomputer.easyeditors.gui.command.slot.CommandSlotRectangle;
import net.earthcomputer.easyeditors.gui.command.slot.IGuiCommandSlot;
import net.earthcomputer.easyeditors.gui.command.slot.ITextField;
import net.earthcomputer.easyeditors.util.Translate;

public class SyntaxTell extends CommandSyntax {

	@Override
	public IGuiCommandSlot[] setupCommand() {
		CommandSlotPlayerSelector target = new CommandSlotPlayerSelector(CommandSlotPlayerSelector.PLAYERS_ONLY);
		if (getContext().getSenderClass() == null || !getContext().isPlayer()) {
			SyntaxSay.Message message = new SyntaxSay.Message(getContext());
			return new IGuiCommandSlot[] { CommandSlotLabel.createLabel(Translate.GUI_COMMANDEDITOR_TELL_TARGET,
					new CommandSlotRectangle(target, Colors.playerSelectorBox.color)), message };
		} else {
			ITextField<?> message = SyntaxSay.Word.createTextField(getContext());
			return new IGuiCommandSlot[] {
					CommandSlotLabel.createLabel(Translate.GUI_COMMANDEDITOR_TELL_TARGET,
							new CommandSlotRectangle(target, Colors.playerSelectorBox.color)),
					(IGuiCommandSlot) message };
		}
	}

}
