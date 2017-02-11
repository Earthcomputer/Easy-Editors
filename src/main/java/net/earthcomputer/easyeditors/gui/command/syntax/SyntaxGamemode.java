package net.earthcomputer.easyeditors.gui.command.syntax;

import net.earthcomputer.easyeditors.api.util.Colors;
import net.earthcomputer.easyeditors.gui.command.CommandSyntaxException;
import net.earthcomputer.easyeditors.gui.command.slot.CommandSlotLabel;
import net.earthcomputer.easyeditors.gui.command.slot.CommandSlotMenu;
import net.earthcomputer.easyeditors.gui.command.slot.CommandSlotPlayerSelector;
import net.earthcomputer.easyeditors.gui.command.slot.CommandSlotRectangle;
import net.earthcomputer.easyeditors.gui.command.slot.IGuiCommandSlot;
import net.earthcomputer.easyeditors.util.Translate;
import net.minecraft.client.resources.I18n;
import net.minecraft.world.GameType;

public class SyntaxGamemode extends CommandSyntax {

	private CommandSlotMenu gamemode;
	private CommandSlotPlayerSelector player;

	@Override
	public IGuiCommandSlot[] setupCommand() {
		gamemode = buildGamemodeSlot();
		player = buildPlayerSlot();
		return new IGuiCommandSlot[] {
				CommandSlotLabel.createLabel(Translate.GUI_COMMANDEDITOR_GAMEMODE_GAMEMODE,
						Translate.GUI_COMMANDEDITOR_GAMEMODE_GAMEMODE_TOOLTIP, gamemode),
				CommandSlotLabel.createLabel(Translate.GUI_COMMANDEDITOR_GAMEMODE_PLAYER,
						Translate.GUI_COMMANDEDITOR_GAMEMODE_PLAYER_TOOLTIP,
						new CommandSlotRectangle(player, Colors.playerSelectorBox.color)) };
	}

	private CommandSlotMenu buildGamemodeSlot() {
		return new GameModeMenu();
	}

	private CommandSlotPlayerSelector buildPlayerSlot() {
		return new CommandSlotPlayerSelector.WithDefault(CommandSlotPlayerSelector.PLAYERS_ONLY);
	}

	public static class GameModeMenu extends CommandSlotMenu {
		public GameModeMenu() {
			super(getGameModeNames(), getGameModeIds());
		}

		private static String[] getGameModeNames() {
			GameType[] gameTypes = GameType.values();
			// -1 for NOT_SET
			String[] names = new String[gameTypes.length - 1];
			int i = 0;
			for (GameType gameType : gameTypes) {
				if (gameType == GameType.NOT_SET) {
					continue;
				}
				names[i] = I18n.format("gameMode." + gameType.getName());
				i++;
			}
			return names;
		}

		private static String[] getGameModeIds() {
			GameType[] gameTypes = GameType.values();
			// -1 for NOT_SET
			String[] ids = new String[gameTypes.length - 1];
			int i = 0;
			for (GameType gameType : gameTypes) {
				if (gameType == GameType.NOT_SET) {
					continue;
				}
				ids[i] = String.valueOf(gameType.getID());
				i++;
			}
			return ids;
		}

		public int readFromArgs(String[] args, int index) throws CommandSyntaxException {
			if (index == args.length) {
				throw new CommandSyntaxException();
			}
			GameType gameMode;
			try {
				gameMode = GameType.parseGameTypeWithDefault(Integer.parseInt(args[index]), GameType.NOT_SET);
			} catch (NumberFormatException e) {
				gameMode = GameType.parseGameTypeWithDefault(args[index], GameType.NOT_SET);
			}
			if (gameMode == GameType.NOT_SET) {
				throw new CommandSyntaxException();
			}
			String id = String.valueOf(gameMode.getID());
			boolean found = false;
			for (int i = 0; i < wordCount(); i++) {
				if (getValueAt(i).equals(id)) {
					this.setCurrentIndex(i);
					found = true;
					break;
				}
			}
			if (!found) {
				throw new CommandSyntaxException();
			}
			return 1;
		}

	}

}
