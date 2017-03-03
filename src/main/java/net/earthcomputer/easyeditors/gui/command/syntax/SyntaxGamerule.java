package net.earthcomputer.easyeditors.gui.command.syntax;

import java.util.List;

import net.earthcomputer.easyeditors.api.SmartTranslationRegistry;
import net.earthcomputer.easyeditors.api.util.ChatBlocker;
import net.earthcomputer.easyeditors.api.util.Colors;
import net.earthcomputer.easyeditors.api.util.ReturnedValueListener;
import net.earthcomputer.easyeditors.gui.command.CommandSyntaxException;
import net.earthcomputer.easyeditors.gui.command.UIInvalidException;
import net.earthcomputer.easyeditors.gui.command.slot.CommandSlotCheckbox;
import net.earthcomputer.easyeditors.gui.command.slot.CommandSlotFormattedTextField;
import net.earthcomputer.easyeditors.gui.command.slot.CommandSlotLabel;
import net.earthcomputer.easyeditors.gui.command.slot.CommandSlotMenu;
import net.earthcomputer.easyeditors.gui.command.slot.CommandSlotModifiable;
import net.earthcomputer.easyeditors.gui.command.slot.CommandSlotRadioList;
import net.earthcomputer.easyeditors.gui.command.slot.CommandSlotRectangle;
import net.earthcomputer.easyeditors.gui.command.slot.CommandSlotTextField;
import net.earthcomputer.easyeditors.gui.command.slot.CommandSlotVerticalArrangement;
import net.earthcomputer.easyeditors.gui.command.slot.IGuiCommandSlot;
import net.earthcomputer.easyeditors.gui.command.slot.ITextField;
import net.earthcomputer.easyeditors.util.Translate;
import net.earthcomputer.easyeditors.util.TranslateKeys;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.world.GameRules;

public class SyntaxGamerule extends CommandSyntax {

	private CommandSlotRadioList action;
	private CommandSlotModifiable<IGuiCommandSlot> rule;
	private CommandSlotMenu ruleMenu;
	private String waitingGameRule;
	private CommandSlotModifiable<IGuiCommandSlot> value;
	private CommandSlotCheckbox valueCheckbox;
	private ITextField<?> valueTextField;
	private CommandSlotModifiable<IGuiCommandSlot> arg;
	private IGuiCommandSlot argList;
	private IGuiCommandSlot argQuery;
	private IGuiCommandSlot argSet;

	@Override
	public IGuiCommandSlot[] setupCommand() {
		rule = new CommandSlotModifiable<IGuiCommandSlot>(
				CommandSlotLabel.createLabel(Translate.GUI_COMMANDEDITOR_GAMERULE_WAITING, 0xff0000)) {
			@Override
			public void addArgs(List<String> args) throws UIInvalidException {
				if (ruleMenu == null) {
					throw new UIInvalidException(TranslateKeys.GUI_COMMANDEDITOR_GAMERULE_RULESNOTOBTAINED);
				}
				super.addArgs(args);
			}

			@Override
			public int readFromArgs(String[] args, int index) throws CommandSyntaxException {
				if (args.length == index) {
					throw new CommandSyntaxException();
				}
				if (ruleMenu == null) {
					waitingGameRule = args[index];
				} else {
					super.readFromArgs(args, index);
				}
				return 1;
			}
		};

		valueCheckbox = new CommandSlotCheckbox(Translate.GUI_COMMANDEDITOR_GAMERULE_VALUE_TRUE);

		if (getContext().canHoldFormatting()) {
			valueTextField = new CommandSlotFormattedTextField(200) {
				@Override
				public void addArgs(List<String> args) throws UIInvalidException {
					if (getText().isEmpty()) {
						throw new UIInvalidException(TranslateKeys.GUI_COMMANDEDITOR_GAMERULE_VALUE_NOVALUE);
					}
					super.addArgs(args);
				}
			};
		} else {
			valueTextField = new CommandSlotTextField(200, 200) {
				@Override
				public void addArgs(List<String> args) throws UIInvalidException {
					if (getText().isEmpty()) {
						throw new UIInvalidException(TranslateKeys.GUI_COMMANDEDITOR_GAMERULE_VALUE_NOVALUE);
					}
					super.addArgs(args);
				}
			};
		}

		value = new CommandSlotModifiable<IGuiCommandSlot>((IGuiCommandSlot) valueTextField);

		ChatBlocker.getGameRuleNames(new ReturnedValueListener<List<String>>() {
			@Override
			public void returnValue(final List<String> value) {
				String[] values = value.toArray(new String[value.size()]);
				String[] displayedValues = values.clone();
				for (int i = 0; i < displayedValues.length; i++) {
					String translationKey = "gui.commandEditor.gamerule.rule." + displayedValues[i];
					if (SmartTranslationRegistry.getLanguageMapInstance().isKeyTranslated(translationKey)) {
						displayedValues[i] = I18n.format(translationKey);
					}
				}
				ruleMenu = new CommandSlotMenu(displayedValues, values) {
					{
						onChanged(getValueAt(0));
					}

					@Override
					protected void onChanged(String to) {
						GameRules gameRules = Minecraft.getMinecraft().world.getGameRules();
						boolean isBooleanRule = false;
						if (gameRules.hasRule(to)) {
							if (gameRules.areSameType(to, GameRules.ValueType.BOOLEAN_VALUE)) {
								isBooleanRule = true;
							}
						}
						if (isBooleanRule) {
							SyntaxGamerule.this.value.setChild(valueCheckbox);
						} else {
							SyntaxGamerule.this.value.setChild((IGuiCommandSlot) valueTextField);
						}
					}
				};
				if (waitingGameRule != null) {
					int index = value.indexOf(waitingGameRule);
					if (index == -1) {
						index = 0;
					}
					ruleMenu.setCurrentIndex(index);
					waitingGameRule = null;
				}
				rule.setChild(ruleMenu);
			}

			@Override
			public void abortFindingValue(int reason) {
				String message = reason == 0 ? Translate.GUI_COMMANDEDITOR_GAMERULE_TIMEDOUT
						: Translate.GUI_COMMANDEDITOR_GAMERULE_NOPERMISSION;
				rule.setChild(CommandSlotLabel.createLabel(message, 0xff0000));
			}
		});

		arg = new CommandSlotModifiable<IGuiCommandSlot>(null);

		argList = new CommandSlotVerticalArrangement();

		argQuery = CommandSlotLabel.createLabel(Translate.GUI_COMMANDEDITOR_GAMERULE_RULE, rule);

		argSet = new CommandSlotVerticalArrangement(
				CommandSlotLabel.createLabel(Translate.GUI_COMMANDEDITOR_GAMERULE_RULE, rule),
				CommandSlotLabel.createLabel(Translate.GUI_COMMANDEDITOR_GAMERULE_VALUE, value));

		action = new CommandSlotRadioList(
				CommandSlotLabel.createLabel(Translate.GUI_COMMANDEDITOR_GAMERULE_ACTION_LIST,
						Colors.miscBigBoxLabel.color),
				CommandSlotLabel.createLabel(Translate.GUI_COMMANDEDITOR_GAMERULE_ACTION_QUERY,
						Colors.miscBigBoxLabel.color),
				CommandSlotLabel.createLabel(Translate.GUI_COMMANDEDITOR_GAMERULE_ACTION_SET,
						Colors.miscBigBoxLabel.color)) {
			@Override
			protected int getSelectedIndexForString(String[] args, int index) throws CommandSyntaxException {
				int remainingArgs = args.length - index;
				if (remainingArgs == 0) {
					return 0;
				} else if (remainingArgs == 1) {
					return 1;
				} else {
					return 2;
				}
			}

			@Override
			protected boolean shouldCheckIndexOutOfBounds() {
				return false;
			}

			@Override
			protected void onValueChanged() {
				switch (getSelectedIndex()) {
				case 0:
					arg.setChild(argList);
					break;
				case 1:
					arg.setChild(argQuery);
					break;
				case 2:
					arg.setChild(argSet);
					break;
				}
			}
		};
		action.setSelectedIndex(2);

		return new IGuiCommandSlot[] { CommandSlotLabel.createLabel(Translate.GUI_COMMANDEDITOR_GAMERULE_ACTION,
				new CommandSlotRectangle(action, Colors.miscBigBoxBox.color)), arg };
	}

}
