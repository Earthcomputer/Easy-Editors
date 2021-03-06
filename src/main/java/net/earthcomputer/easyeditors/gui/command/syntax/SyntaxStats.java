package net.earthcomputer.easyeditors.gui.command.syntax;

import java.util.List;

import net.earthcomputer.easyeditors.api.util.Colors;
import net.earthcomputer.easyeditors.gui.command.CommandSyntaxException;
import net.earthcomputer.easyeditors.gui.command.UIInvalidException;
import net.earthcomputer.easyeditors.gui.command.slot.CommandSlotLabel;
import net.earthcomputer.easyeditors.gui.command.slot.CommandSlotMenu;
import net.earthcomputer.easyeditors.gui.command.slot.CommandSlotModifiable;
import net.earthcomputer.easyeditors.gui.command.slot.CommandSlotPlayerSelector;
import net.earthcomputer.easyeditors.gui.command.slot.CommandSlotRadioList;
import net.earthcomputer.easyeditors.gui.command.slot.CommandSlotRectangle;
import net.earthcomputer.easyeditors.gui.command.slot.CommandSlotRelativeCoordinate;
import net.earthcomputer.easyeditors.gui.command.slot.CommandSlotScore;
import net.earthcomputer.easyeditors.gui.command.slot.CommandSlotVerticalArrangement;
import net.earthcomputer.easyeditors.gui.command.slot.IGuiCommandSlot;
import net.earthcomputer.easyeditors.util.Translate;
import net.minecraft.client.resources.I18n;
import net.minecraft.command.CommandResultStats;

public class SyntaxStats extends CommandSyntax {

	private CommandSlotRadioList source;
	private CommandSlotRelativeCoordinate sourceBlock;
	private CommandSlotPlayerSelector sourceEntity;
	private CommandSlotMenu mode;
	private CommandSlotMenu commandResultType;
	private CommandSlotModifiable modeParam;
	private CommandSlotVerticalArrangement setModeParam;
	private CommandSlotVerticalArrangement clearModeParam;
	private CommandSlotPlayerSelector targetEntity;
	private CommandSlotScore targetObjective;

	@Override
	public IGuiCommandSlot[] setupCommand() {
		sourceBlock = new CommandSlotRelativeCoordinate(Colors.miscBigBoxLabel.color);
		sourceEntity = new CommandSlotPlayerSelector();
		source = new CommandSlotRadioList(
				CommandSlotLabel.createLabel(Translate.GUI_COMMANDEDITOR_REPLACEITEM_TARGET_BLOCK,
						Colors.miscBigBoxLabel.color, sourceBlock),
				CommandSlotLabel.createLabel(Translate.GUI_COMMANDEDITOR_REPLACEITEM_TARGET_ENTITY,
						Colors.miscBigBoxLabel.color,
						new CommandSlotRectangle(sourceEntity, Colors.playerSelectorBox.color))) {
			@Override
			protected int getSelectedIndexForString(String[] args, int index) throws CommandSyntaxException {
				// This was offset by one in the overridden version of
				// readFromArgs, subtract 1 to offset back
				String arg = args[index - 1];
				if ("block".equals(arg)) {
					return 0;
				} else if ("entity".equals(arg)) {
					return 1;
				} else {
					throw new CommandSyntaxException();
				}
			}

			@Override
			public int readFromArgs(String[] args, int index) throws CommandSyntaxException {
				if (args.length == index) {
					throw new CommandSyntaxException();
				}
				return super.readFromArgs(args, index + 1) + 1;
			}

			@Override
			public void addArgs(List<String> args) throws UIInvalidException {
				switch (getSelectedIndex()) {
				case 0:
					args.add("block");
					break;
				case 1:
					args.add("entity");
					break;
				default:
					throw new IllegalStateException();
				}
				super.addArgs(args);
			}
		};

		mode = new CommandSlotMenu(new String[] { Translate.GUI_COMMANDEDITOR_STATS_MODE_SET,
				Translate.GUI_COMMANDEDITOR_STATS_MODE_CLEAR }, "set", "clear") {
			@Override
			protected void onChanged(String to) {
				if ("set".equals(to)) {
					modeParam.setChild(setModeParam);
				} else if ("clear".equals(to)) {
					modeParam.setChild(clearModeParam);
				} else {
					throw new IllegalStateException();
				}
			}
		};

		CommandResultStats.Type[] resultTypes = CommandResultStats.Type.values();
		String[] resultTypeTranslations = new String[resultTypes.length];
		String[] resultTypeNames = new String[resultTypes.length];
		for (int i = 0; i < resultTypes.length; i++) {
			resultTypeTranslations[i] = I18n.format("gui.commandEditor.stats.type." + resultTypes[i].getTypeName());
			resultTypeNames[i] = resultTypes[i].getTypeName();
		}
		commandResultType = new CommandSlotMenu(resultTypeTranslations, resultTypeNames);

		targetEntity = new CommandSlotPlayerSelector(CommandSlotPlayerSelector.ONE_ONLY);

		targetObjective = new CommandSlotScore();

		setModeParam = new CommandSlotVerticalArrangement(
				CommandSlotLabel.createLabel(Translate.GUI_COMMANDEDITOR_STATS_TARGETENTITY,
						Translate.GUI_COMMANDEDITOR_STATS_TARGETENTITY_TOOLTIP,
						new CommandSlotRectangle(targetEntity, Colors.playerSelectorBox.color)),
				CommandSlotLabel.createLabel(Translate.GUI_COMMANDEDITOR_STATS_TARGETOBJECTIVE,
						Translate.GUI_COMMANDEDITOR_STATS_TARGETOBJECTIVE_TOOLTIP, targetObjective));

		clearModeParam = new CommandSlotVerticalArrangement();

		modeParam = new CommandSlotModifiable(setModeParam);

		return new IGuiCommandSlot[] {
				CommandSlotLabel.createLabel(Translate.GUI_COMMANDEDITOR_STATS_SOURCE,
						Translate.GUI_COMMANDEDITOR_STATS_SOURCE_TOOLTIP,
						new CommandSlotRectangle(source, Colors.miscBigBoxBox.color)),
				CommandSlotLabel.createLabel(Translate.GUI_COMMANDEDITOR_STATS_MODE,
						Translate.GUI_COMMANDEDITOR_STATS_MODE_TOOLTIP, mode),
				CommandSlotLabel.createLabel(Translate.GUI_COMMANDEDITOR_STATS_TYPE,
						Translate.GUI_COMMANDEDITOR_STATS_TYPE_TOOLTIP, commandResultType),
				modeParam };
	}

}
