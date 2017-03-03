package net.earthcomputer.easyeditors.gui.command;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import com.google.common.collect.Lists;

import net.earthcomputer.easyeditors.gui.GuiSelectFromList;
import net.earthcomputer.easyeditors.gui.ICallback;
import net.earthcomputer.easyeditors.gui.command.syntax.CommandSyntax;
import net.earthcomputer.easyeditors.util.Translate;
import net.earthcomputer.easyeditors.util.TranslateKeys;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;

public class GuiSelectCommand extends GuiSelectFromList<String> {

	private static List<String> createAllowedValues(CommandSlotContext context) {
		List<String> commands = Lists.newArrayList(CommandSyntax.getSyntaxList().keySet());
		Iterator<String> commandsItr = commands.iterator();
		while (commandsItr.hasNext()) {
			if (CommandSyntax.forCommandName(commandsItr.next(), context) == null) {
				commandsItr.remove();
			}
		}
		Collections.sort(commands, String.CASE_INSENSITIVE_ORDER);
		return commands;
	}

	public GuiSelectCommand(GuiScreen prevScreen, ICallback<String> callback, CommandSlotContext context) {
		super(prevScreen, callback, createAllowedValues(context), Translate.GUI_COMMANDEDITOR_SELECTCOMMAND_TITLE,
				Minecraft.getMinecraft().fontRendererObj.FONT_HEIGHT * 4 + 6);
	}

	@Override
	protected List<String> getTooltip(String value) {
		return Collections.emptyList();
	}

	@Override
	protected void drawSlot(int y, String value) {
		fontRendererObj.drawString(value, width / 2 - fontRendererObj.getStringWidth(value) / 2, y + 2, 0xffffff);
		String str = I18n.format("gui.commandEditor.selectCommand." + value + ".desc");
		fontRendererObj.drawString(str, width / 2 - fontRendererObj.getStringWidth(str) / 2,
				y + 4 + fontRendererObj.FONT_HEIGHT, 0xc0c0c0);
		str = I18n.format(TranslateKeys.GUI_COMMANDEDITOR_SELECTCOMMAND_EXAMPLE,
				I18n.format("gui.commandEditor.selectCommand." + value + ".example"));
		fontRendererObj.drawString(str, width / 2 - fontRendererObj.getStringWidth(str) / 2,
				y + 6 + fontRendererObj.FONT_HEIGHT * 2, 0xc0c0c0);
	}

	@Override
	protected boolean doesSearchTextMatch(String searchText, String value) {
		return value.toLowerCase().contains(searchText);
	}

}
