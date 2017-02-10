package net.earthcomputer.easyeditors.gui.command.syntax;

import java.util.List;
import java.util.UUID;

import org.apache.commons.lang3.ArrayUtils;

import net.earthcomputer.easyeditors.api.util.Colors;
import net.earthcomputer.easyeditors.api.util.Patterns;
import net.earthcomputer.easyeditors.gui.command.CommandSyntaxException;
import net.earthcomputer.easyeditors.gui.command.UIInvalidException;
import net.earthcomputer.easyeditors.gui.command.slot.CommandSlotLabel;
import net.earthcomputer.easyeditors.gui.command.slot.CommandSlotMenu;
import net.earthcomputer.easyeditors.gui.command.slot.CommandSlotPlayerSelector;
import net.earthcomputer.easyeditors.gui.command.slot.CommandSlotRectangle;
import net.earthcomputer.easyeditors.gui.command.slot.IGuiCommandSlot;
import net.earthcomputer.easyeditors.util.Translate;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
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
		GameType[] gameModes = GameType.values();
		String[] displayNames = new String[gameModes.length - 1]; // -1 for
																	// NOT_SET
		String[] ids = new String[displayNames.length];

		int i = 0;
		for (GameType gameMode : gameModes) {
			if (gameMode == GameType.NOT_SET) {
				continue;
			}
			displayNames[i] = I18n.format("gameMode." + gameMode.getName());
			ids[i] = String.valueOf(gameMode.getID());
			i++;
		}

		return new CommandSlotMenu(displayNames, ids);
	}

	private CommandSlotPlayerSelector buildPlayerSlot() {
		return new CommandSlotPlayerSelector(CommandSlotPlayerSelector.PLAYERS_ONLY) {
			@Override
			public void addArgs(List<String> args) throws UIInvalidException {
				super.addArgs(args);
				if (getContext().getSender() instanceof EntityPlayer) {
					String lastArg = args.get(args.size() - 1);
					boolean redundant = false;
					if (lastArg.equals("@p")) {
						redundant = true;
					} else {
						EntityPlayer player = (EntityPlayer) getContext().getSender();
						if (lastArg.equals(player.getName())) {
							redundant = true;
						} else if (Patterns.UUID.matcher(lastArg).matches()
								&& UUID.fromString(lastArg).equals(player.getUniqueID())) {
							redundant = true;
						}
					}
					if (redundant) {
						args.remove(args.size() - 1);
					}
				}
			}

			@Override
			public int readFromArgs(String[] args, int index) throws CommandSyntaxException {
				if (args.length == index) {
					if (!(getContext().getSender() instanceof EntityPlayer)) {
						throw new CommandSyntaxException();
					}
					super.readFromArgs(ArrayUtils.add(args, "@p"), index);
					return 0;
				} else {
					return super.readFromArgs(args, index);
				}
			}
		};
	}

}
