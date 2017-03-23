package net.earthcomputer.easyeditors.gui.command.syntax;

import java.util.List;

import com.google.common.collect.Lists;

import net.earthcomputer.easyeditors.api.util.Colors;
import net.earthcomputer.easyeditors.gui.command.CommandSyntaxException;
import net.earthcomputer.easyeditors.gui.command.UIInvalidException;
import net.earthcomputer.easyeditors.gui.command.slot.CommandSlotCheckbox;
import net.earthcomputer.easyeditors.gui.command.slot.CommandSlotIntTextField;
import net.earthcomputer.easyeditors.gui.command.slot.CommandSlotLabel;
import net.earthcomputer.easyeditors.gui.command.slot.CommandSlotMobEffect;
import net.earthcomputer.easyeditors.gui.command.slot.CommandSlotOptional;
import net.earthcomputer.easyeditors.gui.command.slot.CommandSlotPlayerSelector;
import net.earthcomputer.easyeditors.gui.command.slot.CommandSlotRadioList;
import net.earthcomputer.easyeditors.gui.command.slot.CommandSlotRectangle;
import net.earthcomputer.easyeditors.gui.command.slot.CommandSlotVerticalArrangement;
import net.earthcomputer.easyeditors.gui.command.slot.IGuiCommandSlot;
import net.earthcomputer.easyeditors.util.Translate;
import net.minecraft.client.Minecraft;

public class SyntaxEffect extends CommandSyntax {

	private CommandSlotPlayerSelector target;
	private CommandSlotRadioList action;
	private CommandSlotMobEffect effect;
	private CommandSlotRadioList actionOnEffect;
	private CommandSlotIntTextField.Optional duration;
	private CommandSlotIntTextField.Optional tier;
	private CommandSlotCheckbox.Optional hidden;

	@Override
	public IGuiCommandSlot[] setupCommand() {
		target = new CommandSlotPlayerSelector();

		CommandSlotLabel clearAllEffects = new CommandSlotLabel(Minecraft.getMinecraft().fontRendererObj,
				Translate.GUI_COMMANDEDITOR_EFFECT_ACTION_CLEARALL, Colors.miscBigBoxLabel.color,
				Translate.GUI_COMMANDEDITOR_EFFECT_ACTION_CLEARALL_TOOLTIP) {
			@Override
			public int readFromArgs(String[] args, int index) throws CommandSyntaxException {
				if (args.length == index) {
					throw new CommandSyntaxException();
				}
				return 1;
			}

			@Override
			public void addArgs(List<String> args) throws UIInvalidException {
				args.add("clear");
			}
		};

		effect = new CommandSlotMobEffect();

		CommandSlotLabel clearEffect = new CommandSlotLabel(Minecraft.getMinecraft().fontRendererObj,
				Translate.GUI_COMMANDEDITOR_EFFECT_ACTION_SPECIFIC_ACTION_REMOVE, Colors.miscBigBoxLabel.color,
				Translate.GUI_COMMANDEDITOR_EFFECT_ACTION_SPECIFIC_ACTION_REMOVE_TOOLTIP) {
			@Override
			public int readFromArgs(String[] args, int index) throws CommandSyntaxException {
				if (args.length == index) {
					// possible with /effect @p night_vision
					return 0;
				}
				return 1;
			}

			@Override
			public void addArgs(List<String> args) throws UIInvalidException {
				args.add("0");
			}
		};

		duration = new CommandSlotIntTextField.Optional(50, 100, 1, 1000000, 30);

		tier = new CommandSlotIntTextField.Optional(50, 100, 1, 256, 1) {
			@Override
			public int readFromArgs(String[] args, int index) throws CommandSyntaxException {
				int i;
				try {
					i = Integer.parseInt(args[index]);
				} catch (NumberFormatException e) {
					throw new CommandSyntaxException();
				}
				i++;
				setText(String.valueOf(i));
				return 1;
			}

			@Override
			public void addArgs(List<String> args) throws UIInvalidException {
				args.add(String.valueOf(getIntValue() + 1));
			}
		};

		hidden = new CommandSlotCheckbox.Optional(Translate.GUI_COMMANDEDITOR_EFFECT_ACTION_SPECIFIC_ACTION_ADD_HIDDEN,
				false);

		List<CommandSlotOptional> optionalGroup = Lists.newArrayList();

		IGuiCommandSlot addEffect = new CommandSlotVerticalArrangement(
				CommandSlotLabel.createLabel(Translate.GUI_COMMANDEDITOR_EFFECT_ACTION_SPECIFIC_ACTION_ADD_DURATION,
						Colors.miscBigBoxLabel.color,
						Translate.GUI_COMMANDEDITOR_EFFECT_ACTION_SPECIFIC_ACTION_ADD_DURATION_TOOLTIP,
						new CommandSlotOptional.Impl(duration, optionalGroup)),
				CommandSlotLabel.createLabel(Translate.GUI_COMMANDEDITOR_EFFECT_ACTION_SPECIFIC_ACTION_ADD_TIER,
						Colors.miscBigBoxLabel.color,
						Translate.GUI_COMMANDEDITOR_EFFECT_ACTION_SPECIFIC_ACTION_ADD_TIER_TOOLTIP,
						new CommandSlotOptional.Impl(tier, optionalGroup)),
				new CommandSlotOptional.Impl(hidden, optionalGroup));

		actionOnEffect = new CommandSlotRadioList(clearEffect,
				CommandSlotLabel.createLabel(Translate.GUI_COMMANDEDITOR_EFFECT_ACTION_SPECIFIC_ACTION_ADD,
						Colors.miscBigBoxLabel.color,
						Translate.GUI_COMMANDEDITOR_EFFECT_ACTION_SPECIFIC_ACTION_ADD_TOOLTIP, addEffect)) {
			@Override
			protected int getSelectedIndexForString(String[] args, int index) throws CommandSyntaxException {
				if (args.length == index) {
					// default should be 1
					return 1;
				}
				int i;
				try {
					i = Integer.parseInt(args[index]);
				} catch (NumberFormatException e) {
					throw new CommandSyntaxException();
				}
				if (i == 0) {
					return 0;
				} else {
					return 1;
				}
			}

			@Override
			public int readFromArgs(String[] args, int index) throws CommandSyntaxException {
				int amtRead = super.readFromArgs(args, index);
				return args.length == index ? 0 : amtRead;
			}

			@Override
			protected boolean shouldCheckIndexOutOfBounds() {
				return false;
			}
		};

		IGuiCommandSlot potionSpecific = new CommandSlotVerticalArrangement(
				CommandSlotLabel.createLabel(Translate.GUI_COMMANDEDITOR_EFFECT_ACTION_SPECIFIC_EFFECT,
						Colors.miscBigBoxLabel.color, Translate.GUI_COMMANDEDITOR_EFFECT_ACTION_SPECIFIC_EFFECT_TOOLTIP,
						effect),
				CommandSlotLabel.createLabel(Translate.GUI_COMMANDEDITOR_EFFECT_ACTION_SPECIFIC_ACTION,
						Colors.miscBigBoxLabel.color, actionOnEffect));

		action = new CommandSlotRadioList(clearAllEffects,
				CommandSlotLabel.createLabel(Translate.GUI_COMMANDEDITOR_EFFECT_ACTION_SPECIFIC,
						Colors.miscBigBoxLabel.color, Translate.GUI_COMMANDEDITOR_EFFECT_ACTION_SPECIFIC_TOOLTIP,
						potionSpecific)) {
			@Override
			protected int getSelectedIndexForString(String[] args, int index) throws CommandSyntaxException {
				if ("clear".equals(args[index])) {
					return 0;
				} else {
					return 1;
				}
			}
		};

		return new IGuiCommandSlot[] {
				CommandSlotLabel.createLabel(Translate.GUI_COMMANDEDITOR_EFFECT_TARGET,
						Translate.GUI_COMMANDEDITOR_EFFECT_TARGET_TOOLTIP,
						new CommandSlotRectangle(target, Colors.playerSelectorBox.color)),
				CommandSlotLabel.createLabel(Translate.GUI_COMMANDEDITOR_EFFECT_ACTION,
						new CommandSlotRectangle(action, Colors.miscBigBoxBox.color)) };
	}

}
