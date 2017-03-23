package net.earthcomputer.easyeditors.gui.command.syntax;

import com.google.common.base.Supplier;

import net.earthcomputer.easyeditors.api.util.Colors;
import net.earthcomputer.easyeditors.gui.command.CommandSyntaxException;
import net.earthcomputer.easyeditors.gui.command.UIInvalidException;
import net.earthcomputer.easyeditors.gui.command.slot.CommandSlotCheckbox;
import net.earthcomputer.easyeditors.gui.command.slot.CommandSlotLabel;
import net.earthcomputer.easyeditors.gui.command.slot.CommandSlotList;
import net.earthcomputer.easyeditors.gui.command.slot.CommandSlotNumberTextField;
import net.earthcomputer.easyeditors.gui.command.slot.CommandSlotPlayerSelector;
import net.earthcomputer.easyeditors.gui.command.slot.CommandSlotRectangle;
import net.earthcomputer.easyeditors.gui.command.slot.CommandSlotRelativeCoordinate;
import net.earthcomputer.easyeditors.gui.command.slot.IGuiCommandSlot;
import net.earthcomputer.easyeditors.util.Translate;
import net.earthcomputer.easyeditors.util.TranslateKeys;

public class SyntaxSpreadPlayers extends CommandSyntax {

	@Override
	public IGuiCommandSlot[] setupCommand() {
		CommandSlotRelativeCoordinate.CoordinateArg x = new CommandSlotRelativeCoordinate.CoordinateArg();
		CommandSlotRelativeCoordinate.CoordinateArg z = new CommandSlotRelativeCoordinate.CoordinateArg();

		final CommandSlotNumberTextField spreadDistance = new CommandSlotNumberTextField(50, 50, 0);
		CommandSlotNumberTextField maxRange = new CommandSlotNumberTextField(50, 50) {
			@Override
			public void checkValid() throws UIInvalidException {
				super.checkValid();
				if (getDoubleValue() < spreadDistance.getDoubleValue() + 1) {
					throw new UIInvalidException(TranslateKeys.GUI_COMMANDEDITOR_NUMBEROUTOFBOUNDS,
							spreadDistance.getDoubleValue() + 1, Double.MAX_VALUE);
				}
			}
		};

		CommandSlotCheckbox respectTeams = new CommandSlotCheckbox(
				Translate.GUI_COMMANDEDITOR_SPREADPLAYERS_RESPECTTEAMS);

		CommandSlotList<IGuiCommandSlot> players = new CommandSlotList<IGuiCommandSlot>(
				new Supplier<IGuiCommandSlot>() {
					@Override
					public IGuiCommandSlot get() {
						return new CommandSlotRectangle(
								new CommandSlotPlayerSelector(CommandSlotPlayerSelector.DISALLOW_UUID),
								Colors.playerSelectorBox.color);
					}
				}) {
			@Override
			public int readFromArgs(String[] args, int index) throws CommandSyntaxException {
				clearEntries();
				for (int i = index; i < args.length; i++) {
					addEntry(newEntry());
				}
				return super.readFromArgs(args, index);
			}
		}.setAppendHoverText(Translate.GUI_COMMANDEDITOR_SPREADPLAYERS_PLAYERS_APPEND)
				.setInsertHoverText(Translate.GUI_COMMANDEDITOR_SPREADPLAYERS_PLAYERS_INSERT)
				.setRemoveHoverText(Translate.GUI_COMMANDEDITOR_SPREADPLAYERS_PLAYERS_REMOVE);

		return new IGuiCommandSlot[] { CommandSlotLabel.createLabel("X:", x), CommandSlotLabel.createLabel("Z:", z),
				CommandSlotLabel.createLabel(Translate.GUI_COMMANDEDITOR_SPREADPLAYERS_SPREADDISTANCE,
						Translate.GUI_COMMANDEDITOR_SPREADPLAYERS_SPREADDISTANCE_TOOLTIP, spreadDistance),
				CommandSlotLabel.createLabel(Translate.GUI_COMMANDEDITOR_SPREADPLAYERS_MAXRANGE,
						Translate.GUI_COMMANDEDITOR_SPREADPLAYERS_MAXRANGE_TOOLTIP, maxRange),
				respectTeams,
				CommandSlotLabel.createLabel(Translate.GUI_COMMANDEDITOR_SPREADPLAYERS_PLAYERS, players) };
	}

}
