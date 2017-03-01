package net.earthcomputer.easyeditors.gui.command.syntax;

import net.earthcomputer.easyeditors.api.util.Colors;
import net.earthcomputer.easyeditors.api.util.Instantiator;
import net.earthcomputer.easyeditors.gui.command.CommandSyntaxException;
import net.earthcomputer.easyeditors.gui.command.slot.CommandSlotFormattedTextField;
import net.earthcomputer.easyeditors.gui.command.slot.CommandSlotLabel;
import net.earthcomputer.easyeditors.gui.command.slot.CommandSlotList;
import net.earthcomputer.easyeditors.gui.command.slot.CommandSlotPlayerSelector;
import net.earthcomputer.easyeditors.gui.command.slot.CommandSlotRadioList;
import net.earthcomputer.easyeditors.gui.command.slot.CommandSlotRectangle;
import net.earthcomputer.easyeditors.gui.command.slot.CommandSlotTextField;
import net.earthcomputer.easyeditors.gui.command.slot.IGuiCommandSlot;
import net.earthcomputer.easyeditors.gui.command.slot.ITextField;
import net.earthcomputer.easyeditors.util.Translate;
import net.minecraft.command.EntitySelector;

public class SyntaxTell extends CommandSyntax {

	@Override
	public IGuiCommandSlot[] setupCommand() {
		CommandSlotPlayerSelector target = new CommandSlotPlayerSelector(CommandSlotPlayerSelector.PLAYERS_ONLY);
		if (getContext().getSenderClass() == null || !getContext().isPlayer()) {
			CommandSlotList<Word> message = new CommandSlotList<Word>(new Instantiator<Word>() {
				@Override
				public Word newInstance() {
					return new Word();
				}
			}) {
				@Override
				public int readFromArgs(String[] args, int index) throws CommandSyntaxException {
					clearEntries();
					int amtRead = args.length - index;
					for (; index < args.length; index++) {
						Word word = new Word();
						if (EntitySelector.isSelector(args[index])) {
							word.getRadioList().setSelectedIndex(1);
							word.getSelector().readFromArgs(args, index);
						} else {
							word.getRadioList().setSelectedIndex(0);
							String text = args[index];
							while (index + 1 < args.length && !EntitySelector.isSelector(args[index + 1])) {
								index++;
								text = text + " " + args[index];
							}
							((ITextField<?>) word.getTextField()).setTextAsString(text);
						}
						addEntry(word);
					}
					return amtRead;
				}
				// No need to implement addArgs, is is already sufficiently
				// implemented
			}.setAppendHoverText(Translate.GUI_COMMANDEDITOR_TELL_WORD_APPEND)
					.setInsertHoverText(Translate.GUI_COMMANDEDITOR_TELL_WORD_INSERT)
					.setRemoveHoverText(Translate.GUI_COMMANDEDITOR_TELL_WORD_REMOVE);
			return new IGuiCommandSlot[] { CommandSlotLabel.createLabel(Translate.GUI_COMMANDEDITOR_TELL_TARGET,
					new CommandSlotRectangle(target, Colors.playerSelectorBox.color)), message };
		} else {
			ITextField<?> message = createTextField();
			return new IGuiCommandSlot[] {
					CommandSlotLabel.createLabel(Translate.GUI_COMMANDEDITOR_TELL_TARGET,
							new CommandSlotRectangle(target, Colors.playerSelectorBox.color)),
					(IGuiCommandSlot) message };
		}
	}

	private ITextField<?> createTextField() {
		if (getContext().canHoldFormatting()) {
			return new CommandSlotFormattedTextField(200);
		} else {
			return new CommandSlotTextField(200, 200);
		}
	}

	private CommandSlotPlayerSelector createSelector() {
		return new CommandSlotPlayerSelector(
				CommandSlotPlayerSelector.DISALLOW_USERNAME | CommandSlotPlayerSelector.DISALLOW_UUID);
	}

	private static ThreadLocal<CommandSlotRadioList> constructingRadioList = new ThreadLocal<CommandSlotRadioList>();

	private static CommandSlotRadioList createRadioList() {
		CommandSlotRadioList radioList = new CommandSlotRadioList() {
			@Override
			protected int getSelectedIndexForString(String[] args, int index) throws CommandSyntaxException {
				// This should never be called
				throw new UnsupportedOperationException();
			}
		};
		constructingRadioList.set(radioList);
		return radioList;
	}

	private class Word extends CommandSlotRectangle {
		private CommandSlotRadioList radioList;
		private IGuiCommandSlot textField;
		private CommandSlotPlayerSelector selector;

		public Word() {
			super(createRadioList(), Colors.miscBigBoxBox.color);
			radioList = constructingRadioList.get();
			constructingRadioList.set(null);
			textField = (IGuiCommandSlot) createTextField();
			radioList.addChild(CommandSlotLabel.createLabel(Translate.GUI_COMMANDEDITOR_TELL_WORDTYPE_WORD,
					Colors.miscBigBoxLabel.color, textField));
			selector = createSelector();
			radioList.addChild(CommandSlotLabel.createLabel(Translate.GUI_COMMANDEDITOR_TELL_WORDTYPE_SELECTOR,
					Colors.miscBigBoxLabel.color, new CommandSlotRectangle(selector, Colors.playerSelectorBox.color)));
		}

		public CommandSlotRadioList getRadioList() {
			return radioList;
		}

		public IGuiCommandSlot getTextField() {
			return textField;
		}

		public CommandSlotPlayerSelector getSelector() {
			return selector;
		}
	}

}
