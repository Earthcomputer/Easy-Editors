package net.earthcomputer.easyeditors.gui.command.syntax;

import java.util.List;

import com.google.common.collect.Lists;

import net.earthcomputer.easyeditors.api.util.Colors;
import net.earthcomputer.easyeditors.gui.command.CommandSyntaxException;
import net.earthcomputer.easyeditors.gui.command.UIInvalidException;
import net.earthcomputer.easyeditors.gui.command.slot.CommandSlotLabel;
import net.earthcomputer.easyeditors.gui.command.slot.CommandSlotMenu;
import net.earthcomputer.easyeditors.gui.command.slot.CommandSlotOptional;
import net.earthcomputer.easyeditors.gui.command.slot.CommandSlotPlayerSelector;
import net.earthcomputer.easyeditors.gui.command.slot.CommandSlotRadioList;
import net.earthcomputer.easyeditors.gui.command.slot.CommandSlotRectangle;
import net.earthcomputer.easyeditors.gui.command.slot.CommandSlotStat;
import net.earthcomputer.easyeditors.gui.command.slot.IGuiCommandSlot;
import net.earthcomputer.easyeditors.util.Translate;
import net.minecraft.client.Minecraft;

public class SyntaxAchievement extends CommandSyntax {

	private CommandSlotMenu mode;
	private CommandSlotRadioList achievement;
	private CommandSlotStat stat;
	private CommandSlotPlayerSelector.Optional player;

	@Override
	public IGuiCommandSlot[] setupCommand() {
		mode = new CommandSlotMenu(new String[] { Translate.GUI_COMMANDEDITOR_ACHIEVEMENT_MODE_GIVE,
				Translate.GUI_COMMANDEDITOR_ACHIEVEMENT_MODE_TAKE }, "give", "take");

		stat = new CommandSlotStat(true);
		achievement = new CommandSlotRadioList(new CommandSlotLabel(Minecraft.getMinecraft().fontRendererObj,
				Translate.GUI_COMMANDEDITOR_ACHIEVEMENT_ACHIEVEMENT_ALL) {
			@Override
			public int readFromArgs(String[] args, int index) throws CommandSyntaxException {
				return 1;
			}

			@Override
			public void addArgs(List<String> args) throws UIInvalidException {
				args.add("*");
			}
		}, stat) {
			@Override
			protected int getSelectedIndexForString(String[] args, int index) throws CommandSyntaxException {
				return "*".equals(args[index]) ? 0 : 1;
			}
		};

		player = new CommandSlotPlayerSelector.Optional(CommandSlotPlayerSelector.PLAYERS_ONLY);

		return new IGuiCommandSlot[] { CommandSlotLabel.createLabel(Translate.GUI_COMMANDEDITOR_ACHIEVEMENT_MODE, mode),
				CommandSlotLabel.createLabel(Translate.GUI_COMMANDEDITOR_ACHIEVEMENT_ACHIEVEMENT, achievement),
				CommandSlotLabel.createLabel(Translate.GUI_COMMANDEDITOR_ACHIEVEMENT_PLAYER,
						new CommandSlotRectangle(
								new CommandSlotOptional.Impl(player, Lists.<CommandSlotOptional>newArrayList()),
								Colors.playerSelectorBox.color)) };
	}

}
