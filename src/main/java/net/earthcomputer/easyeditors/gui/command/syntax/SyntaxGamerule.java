package net.earthcomputer.easyeditors.gui.command.syntax;

import java.util.Collections;
import java.util.List;

import net.earthcomputer.easyeditors.api.SmartTranslationRegistry;
import net.earthcomputer.easyeditors.api.util.ChatBlocker;
import net.earthcomputer.easyeditors.api.util.Colors;
import net.earthcomputer.easyeditors.api.util.ReturnedValueListener;
import net.earthcomputer.easyeditors.gui.GuiSelectFromList;
import net.earthcomputer.easyeditors.gui.ICallback;
import net.earthcomputer.easyeditors.gui.command.CommandSyntaxException;
import net.earthcomputer.easyeditors.gui.command.UIInvalidException;
import net.earthcomputer.easyeditors.gui.command.slot.CommandSlotButton;
import net.earthcomputer.easyeditors.gui.command.slot.CommandSlotCheckbox;
import net.earthcomputer.easyeditors.gui.command.slot.CommandSlotFormattedTextField;
import net.earthcomputer.easyeditors.gui.command.slot.CommandSlotHorizontalArrangement;
import net.earthcomputer.easyeditors.gui.command.slot.CommandSlotLabel;
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
	private CommandSlotModifiable rule;
	private CommandSlotGameRule ruleMenu;
	private String waitingGameRule;
	private CommandSlotModifiable value;
	private CommandSlotCheckbox valueCheckbox;
	private ITextField<?> valueTextField;
	private CommandSlotModifiable arg;
	private IGuiCommandSlot argList;
	private IGuiCommandSlot argQuery;
	private IGuiCommandSlot argSet;

	@Override
	public IGuiCommandSlot[] setupCommand() {
		rule = new CommandSlotModifiable(
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

		value = new CommandSlotModifiable((IGuiCommandSlot) valueTextField);

		ChatBlocker.getGameRuleNames(new ReturnedValueListener<List<String>>() {
			@Override
			public void returnValue(List<String> value) {
				ruleMenu = new CommandSlotGameRule(value);
				if (waitingGameRule != null) {
					ruleMenu.setSelectedRule(waitingGameRule);
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

		arg = new CommandSlotModifiable();

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

	private class CommandSlotGameRule extends CommandSlotHorizontalArrangement implements ICallback<String> {
		private List<String> rules;
		private String selectedRule = null;
		private CommandSlotLabel label;

		public CommandSlotGameRule(List<String> rules) {
			this.rules = rules;
			this.label = new CommandSlotLabel(Minecraft.getMinecraft().fontRendererObj,
					Translate.GUI_COMMANDEDITOR_NOGAMERULE, Colors.invalidItemName.color);
			addChild(label);
			addChild(new CommandSlotButton(20, 20, "...") {
				@Override
				public void onPress() {
					Minecraft.getMinecraft()
							.displayGuiScreen(new GuiSelectFromList<String>(Minecraft.getMinecraft().currentScreen,
									CommandSlotGameRule.this, CommandSlotGameRule.this.rules,
									Translate.GUI_COMMANDEDITOR_SELECTGAMERULE_TITLE) {
								@Override
								protected List<String> getTooltip(String value) {
									return Collections.emptyList();
								}

								@Override
								protected void drawSlot(int y, String value) {
									String str;
									String unlocalizedName = "gui.commandEditor.gamerule.rule." + value;
									if (SmartTranslationRegistry.getLanguageMapInstance()
											.isKeyTranslated(unlocalizedName)) {
										str = I18n.format(unlocalizedName);
									} else {
										str = value;
									}
									fontRendererObj.drawString(str, width / 2 - fontRendererObj.getStringWidth(str) / 2,
											y + 2, 0xffffff);
									str = value;
									fontRendererObj.drawString(str, width / 2 - fontRendererObj.getStringWidth(str) / 2,
											y + 4 + fontRendererObj.FONT_HEIGHT, 0xc0c0c0);
								}

								@Override
								protected boolean doesSearchTextMatch(String searchText, String value) {
									if (value.toLowerCase().contains(searchText)) {
										return true;
									}
									String unlocalizedName = "gui.commandEditor.gamerule.rule." + value;
									if (SmartTranslationRegistry.getLanguageMapInstance()
											.isKeyTranslated(unlocalizedName)) {
										if (I18n.format(unlocalizedName).toLowerCase().contains(searchText)) {
											return true;
										}
									}
									return false;
								}
							});
				}
			});
		}

		public String getSelectedRule() {
			return selectedRule;
		}

		public void setSelectedRule(String rule) {
			if (!rules.contains(rule)) {
				rule = rules.get(0);
			}
			this.selectedRule = rule;
			String unlocalizedName = "gui.commandEditor.gamerule.rule." + rule;
			if (SmartTranslationRegistry.getLanguageMapInstance().isKeyTranslated(unlocalizedName)) {
				this.label.setText(I18n.format(unlocalizedName));
			} else {
				this.label.setText(rule);
			}
			this.label.setColor(Colors.itemName.color);

			GameRules gameRules = Minecraft.getMinecraft().world.getGameRules();
			boolean isBooleanRule = false;
			if (gameRules.hasRule(rule)) {
				if (gameRules.areSameType(rule, GameRules.ValueType.BOOLEAN_VALUE)) {
					isBooleanRule = true;
				}
			}
			if (isBooleanRule) {
				SyntaxGamerule.this.value.setChild(valueCheckbox);
			} else {
				SyntaxGamerule.this.value.setChild((IGuiCommandSlot) valueTextField);
			}
		}

		@Override
		public String getCallbackValue() {
			return getSelectedRule();
		}

		@Override
		public void setCallbackValue(String value) {
			setSelectedRule(value);
		}

		@Override
		public int readFromArgs(String[] args, int index) throws CommandSyntaxException {
			if (args.length == index) {
				throw new CommandSyntaxException();
			}
			if (!rules.contains(args[index])) {
				throw new CommandSyntaxException();
			}
			setSelectedRule(args[index]);
			return 1;
		}

		@Override
		public void addArgs(List<String> args) throws UIInvalidException {
			if (selectedRule == null) {
				throw new UIInvalidException(TranslateKeys.GUI_COMMANDEDITOR_NOGAMERULESELECTED);
			}
			args.add(selectedRule);
		}
	}

}
