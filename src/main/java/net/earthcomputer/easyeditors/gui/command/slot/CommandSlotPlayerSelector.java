package net.earthcomputer.easyeditors.gui.command.slot;

import com.google.common.base.Predicate;

import net.earthcomputer.easyeditors.gui.command.CommandSyntaxException;
import net.earthcomputer.easyeditors.util.Colors;
import net.earthcomputer.easyeditors.util.Patterns;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;

public class CommandSlotPlayerSelector extends CommandSlotVerticalArrangement {

	private CommandSlotRadioList radioList;
	private CommandSlotTextField playerNameField;
	private CommandSlotTextField UUIDField;

	public CommandSlotPlayerSelector() {
		children = new IGuiCommandSlot[2];

		children[0] = new CommandSlotLabel(Minecraft.getMinecraft().fontRendererObj,
				I18n.format("gui.commandEditor.playerSelector.selectBy"), Colors.playerSelectorSelectBy.color);

		playerNameField = new CommandSlotTextField(200, 200);
		playerNameField.setContentFilter(new Predicate<String>() {
			@Override
			public boolean apply(String input) {
				return Patterns.partialPlayerName.matcher(input).matches();
			}
		});

		UUIDField = new CommandSlotTextField(225, 225);
		UUIDField.setText("----");
		UUIDField.setMaxStringLength(48);
		UUIDField.setContentFilter(new Predicate<String>() {
			@Override
			public boolean apply(String input) {
				return Patterns.partialUUID.matcher(input).matches();
			}
		});

		children[1] = radioList = new CommandSlotRadioList(
				CommandSlotLabel.createLabel(I18n.format("gui.commandEditor.playerSelector.username"),
						Colors.playerSelectorLabel.color, playerNameField),
				CommandSlotLabel.createLabel(I18n.format("gui.commandEditor.playerSelector.uuid"),
						Colors.playerSelectorLabel.color, UUIDField)) {
			@Override
			protected int getSelectedIndexForString(String rawCommand) throws CommandSyntaxException {
				if (Patterns.playerName.matcher(rawCommand).matches())
					return 0;
				else if (Patterns.UUID.matcher(rawCommand).matches())
					return 1;
				else if (Patterns.playerSelector.matcher(rawCommand).matches())
					return 2;
				else
					throw new CommandSyntaxException();
			}
		};

		for (IGuiCommandSlot child : children)
			child.addSizeChangeListener(this);

		recalcSize();
	}

	public boolean isValid() {
		switch (radioList.getSelectedIndex()) {
		case 0:
			return Patterns.playerName.matcher(playerNameField.getText()).matches();
		case 1:
			return Patterns.UUID.matcher(UUIDField.getText()).matches();
		default:
			return false;
		}
	}

}
