package net.earthcomputer.easyeditors.gui.command.syntax;

import java.util.List;

import net.earthcomputer.easyeditors.gui.command.CommandSyntaxException;
import net.earthcomputer.easyeditors.gui.command.UIInvalidException;
import net.earthcomputer.easyeditors.gui.command.slot.CommandSlotButton;
import net.earthcomputer.easyeditors.gui.command.slot.CommandSlotIntTextField;
import net.earthcomputer.easyeditors.gui.command.slot.CommandSlotLabel;
import net.earthcomputer.easyeditors.gui.command.slot.CommandSlotMenu;
import net.earthcomputer.easyeditors.gui.command.slot.CommandSlotModifiable;
import net.earthcomputer.easyeditors.gui.command.slot.IGuiCommandSlot;
import net.earthcomputer.easyeditors.util.Translate;
import net.earthcomputer.easyeditors.util.TranslateKeys;

public class SyntaxTime extends CommandSyntax {

	private CommandSlotMenu subcommand;
	private CommandSlotModifiable<IGuiCommandSlot> subcommandArgs;
	private IGuiCommandSlot subcommandSet;
	private IGuiCommandSlot subcommandAdd;
	private IGuiCommandSlot subcommandQuery;
	private CommandSlotIntTextField time;
	private CommandSlotMenu timeToQuery;

	@Override
	public IGuiCommandSlot[] setupCommand() {
		subcommand = setupSubcommandSlot();
		time = setupTimeSlot();
		timeToQuery = setupTimeToQuerySlot();
		subcommandSet = setupSubcommandSetSlot();
		subcommandAdd = setupSubcommandAddSlot();
		subcommandQuery = setupSubcommandQuerySlot();
		subcommandArgs = new CommandSlotModifiable<IGuiCommandSlot>(subcommandSet);
		return new IGuiCommandSlot[] {
				CommandSlotLabel.createLabel(Translate.GUI_COMMANDEDITOR_TIME_SUBCOMMAND, subcommand), subcommandArgs };
	}

	private CommandSlotMenu setupSubcommandSlot() {
		return new CommandSlotMenu(new String[] { Translate.GUI_COMMANDEDITOR_TIME_SET,
				Translate.GUI_COMMANDEDITOR_TIME_ADD, Translate.GUI_COMMANDEDITOR_TIME_QUERY }, "set", "add", "query") {
			@Override
			protected void onChanged(String to) {
				if ("set".equals(to)) {
					subcommandArgs.setChild(subcommandSet);
				} else if ("add".equals(to)) {
					subcommandArgs.setChild(subcommandAdd);
				} else if ("query".equals(to)) {
					subcommandArgs.setChild(subcommandQuery);
				} else {
					throw new IllegalStateException("Illegal state of subcommand menu");
				}
			}
		};
	}

	private IGuiCommandSlot setupSubcommandSetSlot() {
		return CommandSlotLabel.createLabel(Translate.GUI_COMMANDEDITOR_TIME_SET_SUBCOMMAND, time,
				new CommandSlotButton(100, 20, Translate.GUI_COMMANDEDITOR_TIME_SET_DAY) {
					@Override
					public void onPress() {
						time.setText("1000");
					}
				}, new CommandSlotButton(100, 20, Translate.GUI_COMMANDEDITOR_TIME_SET_NIGHT) {
					@Override
					public void onPress() {
						time.setText("13000");
					}
				}, new CommandSlotButton(100, 20, Translate.GUI_COMMANDEDITOR_TIME_SET_MIDDAY) {
					@Override
					public void onPress() {
						time.setText("6000");
					}
				}, new CommandSlotButton(100, 20, Translate.GUI_COMMANDEDITOR_TIME_SET_MIDNIGHT) {
					@Override
					public void onPress() {
						time.setText("18000");
					}
				});
	}

	private IGuiCommandSlot setupSubcommandAddSlot() {
		return CommandSlotLabel.createLabel(Translate.GUI_COMMANDEDITOR_TIME_ADD_SUBCOMMAND, time);
	}

	private IGuiCommandSlot setupSubcommandQuerySlot() {
		return CommandSlotLabel.createLabel(Translate.GUI_COMMANDEDITOR_TIME_QUERY_SUBCOMMAND, timeToQuery);
	}

	private CommandSlotIntTextField setupTimeSlot() {
		return new CommandSlotIntTextField(100, 100, 0) {
			@Override
			public int readFromArgs(String[] args, int index) throws CommandSyntaxException {
				if (!"set".equals(subcommand.getCurrentValue())) {
					return super.readFromArgs(args, index);
				}
				if ("day".equals(args[index])) {
					setText("1000");
					return 1;
				} else if ("night".equals(args[index])) {
					setText("13000");
					return 1;
				} else {
					return super.readFromArgs(args, index);
				}
			}

			@Override
			public void addArgs(List<String> args) throws UIInvalidException {
				if (!"set".equals(subcommand.getCurrentValue())) {
					super.addArgs(args);
					return;
				}
				if (getIntValue() == 1000) {
					args.add("day");
				} else if (getIntValue() == 13000) {
					args.add("night");
				} else {
					super.addArgs(args);
				}
			}
		}.setNumberInvalidMessage(TranslateKeys.GUI_COMMANDEDITOR_TIME_INVALID)
				.setOutOfBoundsMessage(TranslateKeys.GUI_COMMANDEDITOR_TIME_OUTOFBOUNDS);
	}

	private CommandSlotMenu setupTimeToQuerySlot() {
		return new CommandSlotMenu(
				new String[] { Translate.GUI_COMMANDEDITOR_TIME_QUERY_DAYTIME,
						Translate.GUI_COMMANDEDITOR_TIME_QUERY_DAY, Translate.GUI_COMMANDEDITOR_TIME_QUERY_GAMETIME },
				"daytime", "day", "gametime");
	}

}
