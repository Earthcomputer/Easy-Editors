package net.earthcomputer.easyeditors.gui;

import java.io.IOException;
import java.lang.reflect.Field;

import org.lwjgl.input.Keyboard;

import io.netty.buffer.Unpooled;
import net.earthcomputer.easyeditors.gui.command.GuiCommandEditor;
import net.earthcomputer.easyeditors.gui.command.ICommandEditorCallback;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiCommandBlock;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.resources.I18n;
import net.minecraft.command.server.CommandBlockLogic;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.client.C17PacketCustomPayload;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

public class GuiNewCommandBlock extends GuiScreen implements ICommandEditorCallback {

	private static final Field localCommandBlockField = ReflectionHelper.findField(GuiCommandBlock.class,
			"field_146489_h", "localCommandBlock");

	private final CommandBlockLogic theCommandBlock;
	private GuiButton doneButton;
	private GuiButton cancelButton;
	private GuiButton ignoringButton;
	private GuiButton linkToCommandEditor;
	private GuiTextField commandText;
	private GuiTextField trackedOutput;
	private boolean shouldTrackOutput;

	private boolean hadFirstInit = false;

	public GuiNewCommandBlock(GuiCommandBlock old) throws Exception {
		theCommandBlock = (CommandBlockLogic) localCommandBlockField.get(old);
	}

	@Override
	public void initGui() {
		Keyboard.enableRepeatEvents(true);
		buttonList.clear();
		buttonList.add(doneButton = new GuiButton(0, width / 2 - 4 - 150, height / 4 + 120 + 12, 150, 20,
				I18n.format("gui.done")));
		buttonList.add(cancelButton = new GuiButton(1, width / 2 + 4, height / 4 + 120 + 12, 150, 20,
				I18n.format("gui.cancel")));
		buttonList.add(ignoringButton = new GuiButton(4, width / 2 + 150 - 100, 150, 100, 20,
				I18n.format("gui.commandBlock.trackingOutput")));
		buttonList.add(linkToCommandEditor = new GuiButton(5, width / 2 + 150 - 100, 50, 100, 20,
				I18n.format("gui.commandBlock.goToCommandEditor")));
		String command = null;
		if (hadFirstInit)
			command = commandText.getText();
		commandText = new GuiTextField(2, fontRendererObj, width / 2 - 150, 50, 196, 20);
		commandText.setMaxStringLength(32767);
		commandText.setFocused(true);
		commandText.setText(hadFirstInit ? command : theCommandBlock.getCustomName());
		trackedOutput = new GuiTextField(3, fontRendererObj, width / 2 - 150, 150, 196, 20);
		trackedOutput.setMaxStringLength(32767);
		trackedOutput.setEnabled(false);
		trackedOutput.setText(I18n.format("gui.commandBlock.noOutput"));
		shouldTrackOutput = theCommandBlock.shouldTrackOutput();
		updateTrackOutputButton();
		doneButton.enabled = commandText.getText().trim().length() > 0;

		hadFirstInit = true;
	}

	@Override
	public void onGuiClosed() {
		Keyboard.enableRepeatEvents(false);
	}

	@Override
	protected void actionPerformed(GuiButton button) throws IOException {
		if (button.enabled) {
			switch (button.id) {
			case 0:
				PacketBuffer buffer = new PacketBuffer(Unpooled.buffer());
				buffer.writeByte(theCommandBlock.func_145751_f());
				theCommandBlock.func_145757_a(buffer);
				buffer.writeString(commandText.getText());
				buffer.writeBoolean(theCommandBlock.shouldTrackOutput());
				mc.getNetHandler().addToSendQueue(new C17PacketCustomPayload("MC|AdvCdm", buffer));

				if (!theCommandBlock.shouldTrackOutput()) {
					theCommandBlock.setLastOutput(null);
				}
				mc.displayGuiScreen(null);
				break;

			case 1:
				theCommandBlock.setTrackOutput(shouldTrackOutput);
				mc.displayGuiScreen(null);
				break;

			case 4:
				theCommandBlock.setTrackOutput(!theCommandBlock.shouldTrackOutput());
				updateTrackOutputButton();
				break;

			case 5:
				mc.displayGuiScreen(new GuiCommandEditor(this, this));
				break;
			}
		}
	}

	@Override
	protected void keyTyped(char typedChar, int keyCode) throws IOException {
		commandText.textboxKeyTyped(typedChar, keyCode);
		trackedOutput.textboxKeyTyped(typedChar, keyCode);
		doneButton.enabled = commandText.getText().trim().length() > 0;

		if (keyCode != Keyboard.KEY_RETURN && keyCode != Keyboard.KEY_NUMPADENTER) {
			if (keyCode == Keyboard.KEY_ESCAPE)
				actionPerformed(cancelButton);
		} else {
			actionPerformed(doneButton);
		}
	}

	@Override
	protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
		super.mouseClicked(mouseX, mouseY, mouseButton);
		commandText.mouseClicked(mouseX, mouseY, mouseButton);
		trackedOutput.mouseClicked(mouseX, mouseY, mouseButton);
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		drawDefaultBackground();
		drawCenteredString(fontRendererObj, I18n.format("advMode.setCommand"), width / 2, 20, 0xffffff);
		drawString(fontRendererObj, I18n.format("advMode.command"), width / 2 - 150, 37, 0xa0a0a0);
		commandText.drawTextBox();
		byte hintsTop = 75;
		int hintsLeft = width / 2 - 150;
		int hintNumber = 1;
		drawString(fontRendererObj, I18n.format("advMode.nearestPlayer"), hintsLeft, hintsTop, 0xa0a0a0);
		drawString(fontRendererObj, I18n.format("advMode.randomPlayer"), hintsLeft,
				hintsTop + hintNumber++ * fontRendererObj.FONT_HEIGHT, 0xa0a0a0);
		drawString(fontRendererObj, I18n.format("advMode.allPlayers"), hintsLeft,
				hintsTop + hintNumber++ * fontRendererObj.FONT_HEIGHT, 0xa0a0a0);
		drawString(fontRendererObj, I18n.format("advMode.allEntities"), hintsLeft,
				hintsTop + hintNumber++ * fontRendererObj.FONT_HEIGHT, 0xa0a0a0);
		drawString(fontRendererObj, "", hintsLeft, hintsTop + hintNumber++ * fontRendererObj.FONT_HEIGHT, 0xa0a0a0);

		if (trackedOutput.getText().length() > 0) {
			int y = hintsTop + hintNumber * fontRendererObj.FONT_HEIGHT + 16;
			drawString(fontRendererObj, I18n.format("advMode.previousOutput"), width / 2 - 150, y, 0xa0a0a0);
			trackedOutput.drawTextBox();
		}

		super.drawScreen(mouseX, mouseY, partialTicks);
	}

	private void updateTrackOutputButton() {
		if (theCommandBlock.shouldTrackOutput()) {
			ignoringButton.displayString = I18n.format("gui.commandBlock.trackingOutput");

			if (theCommandBlock.getLastOutput() != null) {
				trackedOutput.setText(theCommandBlock.getLastOutput().getUnformattedText());
			}
		} else {
			ignoringButton.displayString = I18n.format("gui.commandBlock.ignoringOutput");
			trackedOutput.setText(I18n.format("gui.commandBlock.noOutput"));
		}
	}

	@Override
	public void setCommand(String command) {
		commandText.setText(command);
	}

	@Override
	public String getCommand() {
		return commandText.getText();
	}

}
