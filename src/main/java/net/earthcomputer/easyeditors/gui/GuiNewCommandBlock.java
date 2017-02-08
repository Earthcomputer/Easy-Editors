package net.earthcomputer.easyeditors.gui;

import java.io.IOException;
import java.lang.reflect.Field;

import org.lwjgl.input.Keyboard;

import io.netty.buffer.Unpooled;
import net.earthcomputer.easyeditors.gui.command.GuiCommandEditor;
import net.earthcomputer.easyeditors.gui.command.ICommandEditorCallback;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiCommandBlock;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.resources.I18n;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.client.CPacketCustomPayload;
import net.minecraft.tileentity.TileEntityCommandBlock;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

/**
 * A replacement GUI for {@link GuiCommandBlock}
 * 
 * @author Earthcomputer
 *
 */
public class GuiNewCommandBlock extends GuiScreen implements ICommandEditorCallback {

	private static final Field commandBlockField = ReflectionHelper.findField(GuiCommandBlock.class, "field_184078_g",
			"commandBlock");

	private final TileEntityCommandBlock theCommandBlock;
	private GuiButton doneButton;
	private GuiButton cancelButton;
	private GuiButton ignoringButton;
	private GuiTextField commandText;
	private GuiTextField trackedOutput;
	private boolean shouldTrackOutput;

	/**
	 * Used to fix the vanilla bug where the command text field gets reset when
	 * the window is resized
	 */
	private boolean hadFirstInit = false;

	public GuiNewCommandBlock(GuiCommandBlock old) throws Exception {
		theCommandBlock = (TileEntityCommandBlock) commandBlockField.get(old);
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
		buttonList.add(new GuiButton(5, width / 2 + 150 - 100, 50, 100, 20,
				I18n.format("gui.commandBlock.goToCommandEditor")));
		String command = null;
		if (hadFirstInit)
			command = commandText.getText();
		commandText = new GuiTextField(2, fontRendererObj, width / 2 - 150, 50, 196, 20);
		commandText.setMaxStringLength(32767);
		commandText.setFocused(true);
		commandText.setText(hadFirstInit ? command : theCommandBlock.getCommandBlockLogic().getCommand());
		trackedOutput = new GuiTextField(3, fontRendererObj, width / 2 - 150, 150, 196, 20);
		trackedOutput.setMaxStringLength(32767);
		trackedOutput.setEnabled(false);
		trackedOutput.setText(I18n.format("gui.commandBlock.noOutput"));
		shouldTrackOutput = theCommandBlock.getCommandBlockLogic().shouldTrackOutput();
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
				theCommandBlock.getCommandBlockLogic().fillInInfo(buffer);
				buffer.writeString(commandText.getText());
				buffer.writeBoolean(theCommandBlock.getCommandBlockLogic().shouldTrackOutput());
				buffer.writeString("REDSTONE");
				buffer.writeBoolean(false);
				buffer.writeBoolean(false);
				mc.getConnection().sendPacket(new CPacketCustomPayload("MC|AutoCmd", buffer));

				if (!theCommandBlock.getCommandBlockLogic().shouldTrackOutput()) {
					theCommandBlock.getCommandBlockLogic().setLastOutput(null);
				}
				mc.displayGuiScreen(null);
				break;

			case 1:
				theCommandBlock.getCommandBlockLogic().setTrackOutput(shouldTrackOutput);
				mc.displayGuiScreen(null);
				break;

			case 4:
				theCommandBlock.getCommandBlockLogic()
						.setTrackOutput(!theCommandBlock.getCommandBlockLogic().shouldTrackOutput());
				updateTrackOutputButton();
				break;

			case 5:
				mc.displayGuiScreen(new GuiCommandEditor(this, this, theCommandBlock.getCommandBlockLogic()));
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
		if (theCommandBlock.getCommandBlockLogic().shouldTrackOutput()) {
			ignoringButton.displayString = I18n.format("gui.commandBlock.trackingOutput");

			if (theCommandBlock.getCommandBlockLogic().getLastOutput() != null) {
				trackedOutput.setText(theCommandBlock.getCommandBlockLogic().getLastOutput().getUnformattedText());
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
