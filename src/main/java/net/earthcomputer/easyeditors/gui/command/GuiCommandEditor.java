package net.earthcomputer.easyeditors.gui.command;

import java.io.IOException;
import java.util.List;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;

import net.earthcomputer.easyeditors.api.util.Colors;
import net.earthcomputer.easyeditors.api.util.GeneralUtils;
import net.earthcomputer.easyeditors.gui.GuiTwoWayScroll;
import net.earthcomputer.easyeditors.gui.ISizeChangeListener;
import net.earthcomputer.easyeditors.gui.command.slot.CommandSlotCommand;
import net.earthcomputer.easyeditors.gui.command.slot.CommandSlotRectangle;
import net.earthcomputer.easyeditors.util.Translate;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiYesNo;
import net.minecraft.client.gui.GuiYesNoCallback;
import net.minecraft.client.resources.I18n;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.client.config.HoverChecker;

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
	private ICommandSender sender;

	private GuiButton doneButton;
	private GuiButton cancelButton;

	private CommandSlotCommand commandSlotCommand;
	private CommandSlotRectangle commandSlotRectangle;

	private String invalidText = null;
	private HoverChecker doneHoverChecker;

	public GuiCommandEditor(GuiScreen previousGui, ICommandEditorCallback callback, ICommandSender sender) {
		super(30, 30, 4, 500);
		setLeftKey(Keyboard.KEY_LEFT);
		setRightKey(Keyboard.KEY_RIGHT);
		setUpKey(Keyboard.KEY_UP);
		setDownKey(Keyboard.KEY_DOWN);
		this.previousGui = previousGui;
		this.callback = callback;
		this.sender = sender;
	}

	private boolean hasInitGui = false;

	@Override
	public void initGui() {
		if (!hasInitGui) {
			hasInitGui = true;
			commandSlotCommand = new CommandSlotCommand();
			commandSlotRectangle = new CommandSlotRectangle(commandSlotCommand, Colors.commandBox.color);
			commandSlotRectangle.addSizeChangeListener(this);
			commandSlotRectangle.setContext(new Cxt());
			String str = callback.getCommand();
			if (str.startsWith("/"))
				str = str.substring(1);
			str = str.trim();
			commandSlotCommand.readFromArgs(str.split(" "), 0);
			setVirtualWidth(commandSlotRectangle.getWidth() + 4);
			setVirtualHeight(commandSlotRectangle.getHeight() + 4);
		}
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
			try {
				commandSlotRectangle.addArgs(args);
			} catch (UIInvalidException e) {
				// This should never happen, the done button will be enabled
				e.printStackTrace();
			}
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

		if (invalidText != null) {
			if (doneHoverChecker == null)
				doneHoverChecker = new HoverChecker(doneButton, 1000);

			if (doneHoverChecker.checkHover(mouseX, mouseY))
				GeneralUtils.drawTooltip(mouseX, mouseY, TextFormatting.RED + invalidText, width / 2);
		}

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
		String str = Translate.GUI_COMMANDEDITOR_TITLE;
		fontRendererObj.drawString(str, width / 2 - fontRendererObj.getStringWidth(str) / 2,
				getHeaderHeight() / 2 - fontRendererObj.FONT_HEIGHT / 2, 0xffffff);

		try {
			commandSlotRectangle.addArgs(Lists.<String>newArrayList());
			doneButton.enabled = true;
			invalidText = null;
		} catch (UIInvalidException e) {
			doneButton.enabled = false;
			invalidText = e.getMessage();
		}
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
		if (!commandSlotRectangle.onMouseClickedForeground(mouseX, mouseY, mouseButton))
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
		int newVirtualWidth = newWidth + 4;
		setVirtualWidth(newVirtualWidth);
		if (getScrollX() + getShownWidth() > newVirtualWidth)
			setScrollX(newVirtualWidth - getShownWidth());
	}

	@Override
	public void onHeightChange(int oldHeight, int newHeight) {
		int newVirtualHeight = newHeight + 4;
		setVirtualHeight(newHeight);
		if (getScrollY() + getShownHeight() > newVirtualHeight)
			setScrollY(newVirtualHeight - getShownHeight());
	}

	private class Cxt extends CommandSlotContext implements GuiYesNoCallback {

		@Override
		public World getWorld() {
			return sender.getEntityWorld();
		}

		@Override
		public BlockPos getPos() {
			return sender.getPosition();
		}

		@Override
		public ICommandSender getSender() {
			return sender;
		}

		@Override
		public boolean isMouseInBounds(int mouseX, int mouseY) {
			return mouseX < getShownWidth() && mouseY >= getHeaderHeight()
					&& mouseY < getHeaderHeight() + getShownHeight();
		}

		@Override
		public void ensureXInView(int x) {
			if (x < 0)
				addScrollX(x);
			else if (x > getShownWidth())
				addScrollX(x - getShownWidth());
		}

		@Override
		public void ensureYInView(int y) {
			if (y < getHeaderHeight())
				addScrollY(y - getHeaderHeight());
			else if (y > getHeaderHeight() + getShownHeight())
				addScrollY(y - (getHeaderHeight() + getShownHeight()));
		}

		@Override
		public void commandSyntaxError() {
			Minecraft.getMinecraft()
					.displayGuiScreen(new GuiYesNo(this, Translate.GUI_COMMANDEDITOR_INVALIDCOMMAND_LINE1,
							Translate.GUI_COMMANDEDITOR_INVALIDCOMMAND_LINE2, 0));
		}

		@Override
		public void confirmClicked(boolean result, int id) {
			if (id == 0) {
				if (result) {
					Minecraft.getMinecraft().displayGuiScreen(GuiCommandEditor.this);
				} else {
					Minecraft.getMinecraft().displayGuiScreen(previousGui);
				}
			}
		}

	}

}
