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

	private String newCommand = null;

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
		commandText = new GuiTextField(2, fontRendererObj, width / 2 - 150, 50, 196, 20);
		commandText.setMaxStringLength(32767);
		commandText.setFocused(true);
		commandText.setText(newCommand == null ? theCommandBlock.getCustomName() : newCommand);
		newCommand = null;
		trackedOutput = new GuiTextField(3, fontRendererObj, width / 2 - 150, 150, 196, 20);
		trackedOutput.setMaxStringLength(32767);
		trackedOutput.setEnabled(false);
		trackedOutput.setText(I18n.format("gui.commandBlock.noOutput"));
		shouldTrackOutput = theCommandBlock.shouldTrackOutput();
		updateTrackOutputButton();
		doneButton.enabled = commandText.getText().trim().length() > 0;
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
		drawCenteredString(fontRendererObj, I18n.format("advMode.setCommand"), width / 2, 20, 16777215);
		drawString(fontRendererObj, I18n.format("advMode.command"), width / 2 - 150, 37, 10526880);
		commandText.drawTextBox();
		byte b0 = 75;
		byte b1 = 0;
		FontRenderer fontrenderer = fontRendererObj;
		String s = I18n.format("advMode.nearestPlayer");
		int i1 = width / 2 - 150;
		int l = b1 + 1;
		drawString(fontrenderer, s, i1, b0 + b1 * fontRendererObj.FONT_HEIGHT, 10526880);
		drawString(fontRendererObj, I18n.format("advMode.randomPlayer"), width / 2 - 150,
				b0 + l++ * fontRendererObj.FONT_HEIGHT, 10526880);
		drawString(fontRendererObj, I18n.format("advMode.allPlayers"), width / 2 - 150,
				b0 + l++ * fontRendererObj.FONT_HEIGHT, 10526880);
		drawString(fontRendererObj, I18n.format("advMode.allEntities"), width / 2 - 150,
				b0 + l++ * fontRendererObj.FONT_HEIGHT, 10526880);
		drawString(fontRendererObj, "", width / 2 - 150, b0 + l++ * fontRendererObj.FONT_HEIGHT, 10526880);

		if (trackedOutput.getText().length() > 0) {
			int k = b0 + l * fontRendererObj.FONT_HEIGHT + 16;
			drawString(fontRendererObj, I18n.format("advMode.previousOutput"), width / 2 - 150, k, 10526880);
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
		newCommand = command;
	}

	@Override
	public String getCommand() {
		return commandText.getText();
	}

}
