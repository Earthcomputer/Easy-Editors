package net.earthcomputer.easyeditors.gui.command.syntax;

import net.earthcomputer.easyeditors.gui.command.CommandSyntaxException;
import net.earthcomputer.easyeditors.gui.command.slot.CommandSlotLabel;
import net.earthcomputer.easyeditors.gui.command.slot.CommandSlotMenu;
import net.earthcomputer.easyeditors.gui.command.slot.IGuiCommandSlot;
import net.earthcomputer.easyeditors.util.Translate;
import net.minecraft.client.resources.I18n;
import net.minecraft.world.EnumDifficulty;

public class SyntaxDifficulty extends CommandSyntax {

	@Override
	public IGuiCommandSlot[] setupCommand() {
		EnumDifficulty[] difficulties = EnumDifficulty.values();
		String[] names = new String[difficulties.length];
		String[] ids = new String[names.length];

		for (int i = 0; i < difficulties.length; i++) {
			names[i] = I18n.format(difficulties[i].getDifficultyResourceKey());
			ids[i] = String.valueOf(difficulties[i].getDifficultyId());
		}

		CommandSlotMenu difficultyMenu = new CommandSlotMenu(names, ids) {
			@Override
			public int readFromArgs(String[] args, int index) throws CommandSyntaxException {
				if (args.length == index) {
					throw new CommandSyntaxException();
				}
				String arg = args[index];
				EnumDifficulty difficulty;
				if ("peaceful".equals(arg) || "p".equals(arg)) {
					difficulty = EnumDifficulty.PEACEFUL;
				} else if ("easy".equals(arg) || "e".equals(arg)) {
					difficulty = EnumDifficulty.EASY;
				} else if ("normal".equals(arg) || "n".equals(arg)) {
					difficulty = EnumDifficulty.NORMAL;
				} else if ("hard".equals(arg) || "h".equals(arg)) {
					difficulty = EnumDifficulty.HARD;
				} else {
					int id;
					try {
						id = Integer.parseInt(arg);
					} catch (NumberFormatException e) {
						throw new CommandSyntaxException();
					}
					if (id < 0 || id >= EnumDifficulty.values().length) {
						throw new CommandSyntaxException();
					}
					difficulty = EnumDifficulty.getDifficultyEnum(id);
				}
				String difficultyId = String.valueOf(difficulty.getDifficultyId());
				boolean found = false;
				for (int i = 0; i < wordCount(); i++) {
					if (getValueAt(i).equals(difficultyId)) {
						setCurrentIndex(i);
						found = true;
						break;
					}
				}
				if (!found) {
					throw new CommandSyntaxException();
				}
				return 1;
			}
		};

		return new IGuiCommandSlot[] { CommandSlotLabel.createLabel(Translate.GUI_COMMANDEDITOR_DIFFICULTY_DIFFICULTY,
				Translate.GUI_COMMANDEDITOR_DIFFICULTY_DIFFICULTY_TOOLTIP, difficultyMenu) };
	}

}
