package net.earthcomputer.easyeditors.gui.command;

import java.io.IOException;
import java.util.List;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;

import net.earthcomputer.easyeditors.api.Colors;
import net.earthcomputer.easyeditors.gui.GuiTwoWayScroll;
import net.earthcomputer.easyeditors.gui.ISizeChangeListener;
import net.earthcomputer.easyeditors.gui.command.slot.CommandSlotCommand;
import net.earthcomputer.easyeditors.gui.command.slot.CommandSlotRectangle;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;

/**
 * The command editor GUI
 * 
 * @author Earthcomputer
 *
 */
public class GuiCommandEditor extends GuiTwoWayScroll implements ISizeChangeListener {

	private static final Joiner SPACE_JOINER = Joiner.on(' ');

	private GuiScreen previousGui;
	private ICommandEditorCallback callback;

	private GuiButton doneButton;
	private GuiButton cancelButton;

	private CommandSlotCommand commandSlotCommand;
	private CommandSlotRectangle commandSlotRectangle;

	public GuiCommandEditor(GuiScreen previousGui, ICommandEditorCallback callback) {
		super(30, 30, 4, 500);
		setLeftKey(Keyboard.KEY_LEFT);
		setRightKey(Keyboard.KEY_RIGHT);
		setUpKey(Keyboard.KEY_UP);
		setDownKey(Keyboard.KEY_DOWN);
		this.previousGui = previousGui;
		this.callback = callback;
		commandSlotCommand = new CommandSlotCommand();
		String str = callback.getCommand();
		if (str.startsWith("/"))
			str = str.substring(1);
		str = str.trim();
		commandSlotCommand.readFromArgs(str.split(" "), 0);
		commandSlotRectangle = new CommandSlotRectangle(commandSlotCommand, Colors.commandBox.color);
		commandSlotRectangle.addSizeChangeListener(this);
		setVirtualWidth(commandSlotRectangle.getWidth() + 4);
		setVirtualHeight(commandSlotRectangle.getHeight() + 4);
	}

	@Override
	public void initGui() {
		super.initGui();
		buttonList.add(doneButton = new GuiButton(0, width / 2 - 160, height - getFooterHeight() / 2 - 10, 150, 20,
				I18n.format("gui.done")));
		buttonList.add(cancelButton = new GuiButton(1, width / 2 + 5, height - getFooterHeight() / 2 - 10, 150, 20,
				I18n.format("gui.cancel")));
		Keyboard.enableRepeatEvents(true);
	}

	@Override
	public void onGuiClosed() {
		Keyboard.enableRepeatEvents(false);
	}

	@Override
	public void keyTyped(char typedChar, int keyCode) throws IOException {
		if (commandSlotRectangle.onKeyTyped(typedChar, keyCode))
			return;
		if (keyCode == Keyboard.KEY_ESCAPE) {
			actionPerformed(cancelButton);
		} else {
			super.keyTyped(typedChar, keyCode);
		}
	}

	@Override
	public void actionPerformed(GuiButton button) throws IOException {
		switch (button.id) {
		case 0:
			List<String> args = Lists.newArrayList();
			commandSlotRectangle.addArgs(args);
			callback.setCommand(SPACE_JOINER.join(args));
			mc.displayGuiScreen(previousGui);
			break;
		case 1:
			mc.displayGuiScreen(previousGui);
			break;
		default:
			super.actionPerformed(button);
		}
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		super.drawScreen(mouseX, mouseY, partialTicks);

		commandSlotRectangle.drawForeground(2 - getScrollX(), 2 - getScrollY() + getHeaderHeight(), mouseX, mouseY,
				partialTicks);
	}

	@Override
	protected void drawVirtualScreen(int mouseX, int mouseY, float partialTicks, int scrollX, int scrollY,
			int headerHeight) {
		commandSlotRectangle.draw(2 - scrollX, 2 - scrollY + headerHeight, mouseX, mouseY, partialTicks);
	}

	@Override
	protected void drawForeground(int mouseX, int mouseY, float partialTicks) {
		String str = I18n.format("gui.commandEditor.title");
		fontRendererObj.drawString(str, width / 2 - fontRendererObj.getStringWidth(str) / 2,
				getHeaderHeight() / 2 - fontRendererObj.FONT_HEIGHT / 2, 0xffffff);

		doneButton.enabled = commandSlotCommand.isValid();
	}

	@Override
	public void handleMouseInput() throws IOException {
		int amtScrolled = Mouse.getEventDWheel();
		if (amtScrolled != 0)
			if (commandSlotRectangle.onMouseScrolled(Mouse.getEventX() * width / mc.displayWidth,
					height - Mouse.getEventY() * height / mc.displayHeight - 1, amtScrolled > 0))
				setUsesMouseWheel(false);

		super.handleMouseInput();

		setUsesMouseWheel(true);
	}

	@Override
	public void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
		if (!commandSlotRectangle.onMouseClicked(mouseX, mouseY, mouseButton))
			super.mouseClicked(mouseX, mouseY, mouseButton);
	}

	@Override
	public void mouseReleased(int mouseX, int mouseY, int mouseButton) {
		if (!commandSlotRectangle.onMouseReleased(mouseX, mouseY, mouseButton))
			super.mouseReleased(mouseX, mouseY, mouseButton);
	}

	@Override
	public void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
		if (!commandSlotRectangle.onMouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick))
			super.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);
	}

	@Override
	public void onWidthChange(int oldWidth, int newWidth) {
		setVirtualWidth(newWidth + 4);
	}

	@Override
	public void onHeightChange(int oldHeight, int newHeight) {
		setVirtualHeight(newHeight + 4);
	}

}
