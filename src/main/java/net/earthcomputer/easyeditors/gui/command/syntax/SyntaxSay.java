package net.earthcomputer.easyeditors.gui.command.syntax;

import net.earthcomputer.easyeditors.api.util.Colors;
import net.earthcomputer.easyeditors.api.util.Instantiator;
import net.earthcomputer.easyeditors.gui.command.CommandSlotContext;
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

public class SyntaxSay extends CommandSyntax {

	@Override
	public IGuiCommandSlot[] setupCommand() {
		if (getContext().getSenderClass() == null || !getContext().isPlayer()) {
			return new IGuiCommandSlot[] { new Message(getContext()) };
		} else {
			return new IGuiCommandSlot[] { (IGuiCommandSlot) Word.createTextField(getContext()) };
		}
	}

	public static class Message extends CommandSlotList<Word> {
		public Message(final CommandSlotContext context) {
			super(new Instantiator<Word>() {
				@Override
				public Word newInstance() {
					return new Word(context);
				}
			});
			setAppendHoverText(Translate.GUI_COMMANDEDITOR_SAY_WORD_APPEND);
			setInsertHoverText(Translate.GUI_COMMANDEDITOR_SAY_WORD_INSERT);
			setRemoveHoverText(Translate.GUI_COMMANDEDITOR_SAY_WORD_REMOVE);
		}

		@Override
		public int readFromArgs(String[] args, int index) throws CommandSyntaxException {
			clearEntries();
			int amtRead = args.length - index;
			for (; index < args.length; index++) {
				Word word = new Word(getContext());
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
	}

	public static class Word extends CommandSlotRectangle {
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

		private CommandSlotRadioList radioList;
		private IGuiCommandSlot textField;
		private CommandSlotPlayerSelector selector;

		public Word(CommandSlotContext context) {
			super(createRadioList(), Colors.miscBigBoxBox.color);
			radioList = constructingRadioList.get();
			constructingRadioList.set(null);
			textField = (IGuiCommandSlot) createTextField(context);
			radioList.addChild(CommandSlotLabel.createLabel(Translate.GUI_COMMANDEDITOR_SAY_WORDTYPE_WORD,
					Colors.miscBigBoxLabel.color, textField));
			selector = createSelector();
			radioList.addChild(CommandSlotLabel.createLabel(Translate.GUI_COMMANDEDITOR_SAY_WORDTYPE_SELECTOR,
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

		public static ITextField<?> createTextField(CommandSlotContext context) {
			if (context.canHoldFormatting()) {
				return new CommandSlotFormattedTextField(200);
			} else {
				return new CommandSlotTextField(200, 200);
			}
		}

		public static CommandSlotPlayerSelector createSelector() {
			return new CommandSlotPlayerSelector(
					CommandSlotPlayerSelector.DISALLOW_USERNAME | CommandSlotPlayerSelector.DISALLOW_UUID);
		}
	}

}
