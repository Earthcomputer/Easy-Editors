package net.earthcomputer.easyeditors.gui.command;

import java.io.IOException;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.lwjgl.input.Keyboard;

import com.google.common.collect.Lists;

import net.earthcomputer.easyeditors.gui.command.syntax.CommandSyntax;
import net.earthcomputer.easyeditors.util.Translate;
import net.earthcomputer.easyeditors.util.TranslateKeys;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiSlot;
import net.minecraft.client.resources.I18n;

/**
 * A GuiScreen which the user will use to select a command out of a list of
 * available commands in the command editor
 * 
 * @author Earthcomputer
 *
 */
public class GuiCommandSelector extends GuiScreen {

	private GuiScreen previousScreen;
	private ICommandEditorCallback callback;

	private List<String> commands;
	private int selectedIndex = 0;

	private GuiButton cancelButton;
	private CommandList list;

	/**
	 * Creates a command selector with the given callback
	 * 
	 * @param previousScreen
	 * @param callback
	 */
	public GuiCommandSelector(GuiScreen previousScreen, ICommandEditorCallback callback, CommandSlotContext context) {
		this.previousScreen = previousScreen;
		this.callback = callback;

		commands = Lists.newArrayList(CommandSyntax.getSyntaxList().keySet());
		Iterator<String> commandsItr = commands.iterator();
		while (commandsItr.hasNext()) {
			if (CommandSyntax.forCommandName(commandsItr.next(), context) == null) {
				commandsItr.remove();
			}
		}
		Collections.sort(commands, String.CASE_INSENSITIVE_ORDER);

		String selectedSyntax = callback.getCommand();
		if (commands.contains(selectedSyntax))
			selectedIndex = commands.indexOf(selectedSyntax);
	}

	@Override
	public void initGui() {
		Keyboard.enableRepeatEvents(true);
		buttonList.add(new GuiButton(0, width / 2 - 160, height - 15 - 10, 150, 20, I18n.format("gui.done")));
		buttonList.add(
				cancelButton = new GuiButton(1, width / 2 + 5, height - 15 - 10, 150, 20, I18n.format("gui.cancel")));
		list = new CommandList();
	}

	@Override
	public void onGuiClosed() {
		Keyboard.enableRepeatEvents(false);
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		list.drawScreen(mouseX, mouseY, partialTicks);

		String str = Translate.GUI_COMMANDEDITOR_SELECTCOMMAND_TITLE;
		fontRendererObj.drawString(str, width / 2 - fontRendererObj.getStringWidth(str) / 2,
				15 - fontRendererObj.FONT_HEIGHT / 2, 0xffffff);

		super.drawScreen(mouseX, mouseY, partialTicks);
	}

	@Override
	public void actionPerformed(GuiButton button) {
		switch (button.id) {
		case 0:
			callback.setCommand(commands.get(selectedIndex));
			mc.displayGuiScreen(previousScreen);
			break;
		case 1:
			mc.displayGuiScreen(previousScreen);
			break;
		default:
			list.actionPerformed(button);
		}
	}

	@Override
	public void keyTyped(char typedChar, int keyCode) throws IOException {
		if (keyCode == Keyboard.KEY_ESCAPE) {
			actionPerformed(cancelButton);
		} else {
			super.keyTyped(typedChar, keyCode);
		}
	}

	@Override
	public void handleMouseInput() throws IOException {
		super.handleMouseInput();
		list.handleMouseInput();
	}

	private class CommandList extends GuiSlot {
		public CommandList() {
			super(GuiCommandSelector.this.mc, GuiCommandSelector.this.width, GuiCommandSelector.this.height, 30,
					GuiCommandSelector.this.height - 30, GuiCommandSelector.this.fontRendererObj.FONT_HEIGHT * 4 + 6);
		}

		@Override
		protected int getSize() {
			return commands.size();
		}

		@Override
		protected void elementClicked(int slotIndex, boolean isDoubleClick, int mouseX, int mouseY) {
			selectedIndex = slotIndex;
		}

		@Override
		protected boolean isSelected(int slotIndex) {
			return slotIndex == selectedIndex;
		}

		@Override
		protected void drawBackground() {
			GuiCommandSelector.this.drawBackground(0);
		}

		@Override
		protected void drawSlot(int entryID, int x, int y, int height, int mouseX, int mouseY) {
			FontRenderer fontRenderer = GuiCommandSelector.this.fontRendererObj;
			String commandName = commands.get(entryID);
			fontRenderer.drawString(commandName, x + getListWidth() / 2 - fontRenderer.getStringWidth(commandName) / 2,
					y + 2, 0xffffff);
			String str = I18n.format("gui.commandEditor.selectCommand." + commandName + ".desc");
			fontRenderer.drawString(str, x + getListWidth() / 2 - fontRenderer.getStringWidth(str) / 2,
					y + 4 + fontRenderer.FONT_HEIGHT, 0xc0c0c0);
			str = I18n.format(TranslateKeys.GUI_COMMANDEDITOR_SELECTCOMMAND_EXAMPLE,
					I18n.format("gui.commandEditor.selectCommand." + commandName + ".example"));
			fontRenderer.drawString(str, x + getListWidth() / 2 - fontRenderer.getStringWidth(str) / 2,
					y + 6 + fontRenderer.FONT_HEIGHT * 2, 0xc0c0c0);
		}

		@Override
		public int getListWidth() {
			return GuiCommandSelector.this.width - 12;
		}

		@Override
		public int getScrollBarX() {
			return GuiCommandSelector.this.width - 6;
		}
	}

}
