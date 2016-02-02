package net.earthcomputer.easyeditors.gui.command.slot;

import com.google.common.base.Predicate;

import net.earthcomputer.easyeditors.api.util.Colors;
import net.earthcomputer.easyeditors.api.util.Patterns;
import net.earthcomputer.easyeditors.gui.command.CommandSyntaxException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;

/**
 * A command slot representing a player selector
 * 
 * @author Earthcomputer
 *
 */
public class CommandSlotPlayerSelector extends CommandSlotVerticalArrangement {

	private CommandSlotRadioList radioList;
	private CommandSlotTextField playerNameField;
	private CommandSlotTextField UUIDField;
	private CmdPlayerSelector playerSelector;

	public CommandSlotPlayerSelector() {
		addChild(new CommandSlotLabel(Minecraft.getMinecraft().fontRendererObj,
				I18n.format("gui.commandEditor.playerSelector.selectBy"), Colors.playerSelectorSelectBy.color));

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

		playerSelector = new CmdPlayerSelector();

		addChild(radioList = new CommandSlotRadioList(
				CommandSlotLabel.createLabel(I18n.format("gui.commandEditor.playerSelector.username"),
						Colors.playerSelectorLabel.color, playerNameField),
				CommandSlotLabel.createLabel(I18n.format("gui.commandEditor.playerSelector.uuid"),
						Colors.playerSelectorLabel.color, UUIDField),
				CommandSlotLabel.createLabel(I18n.format("gui.commandEditor.playerSelector.selector"),
						Colors.playerSelectorLabel.color, playerSelector)) {
			@Override
			protected int getSelectedIndexForString(String[] args, int index) throws CommandSyntaxException {
				String arg = args[index];
				if (Patterns.playerName.matcher(arg).matches())
					return 0;
				else if (Patterns.UUID.matcher(arg).matches())
					return 1;
				else if (Patterns.playerSelector.matcher(arg).matches())
					return 2;
				else
					throw new CommandSyntaxException();
			}
		});
	}

	/**
	 * 
	 * @return Whether this player selector is valid
	 */
	public boolean isValid() {
		switch (radioList.getSelectedIndex()) {
		case 0:
			return Patterns.playerName.matcher(playerNameField.getText()).matches();
		case 1:
			return Patterns.UUID.matcher(UUIDField.getText()).matches();
		case 2:
			return playerSelector.isValid();
		default:
			return false;
		}
	}

	private class CmdPlayerSelector extends CommandSlotVerticalArrangement {

		private CommandSlotMenu selectorType;
		private CommandSlotEntity targetEntity;

		public CmdPlayerSelector() {
			addChild(new CommandSlotHorizontalArrangement(
					selectorType = new CommandSlotMenu("Nearest", "Furthest", "All", "Random"),
					targetEntity = new CommandSlotEntity(true, false)));
		}

		public boolean isValid() {
			return targetEntity.isValid();
		}

	}

}
