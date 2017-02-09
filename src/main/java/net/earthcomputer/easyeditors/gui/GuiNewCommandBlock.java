package net.earthcomputer.easyeditors.gui;

import java.io.IOException;
import java.lang.reflect.Field;

import com.google.common.base.Throwables;

import net.earthcomputer.easyeditors.gui.command.GuiCommandEditor;
import net.earthcomputer.easyeditors.gui.command.ICommandEditorCallback;
import net.earthcomputer.easyeditors.util.Translate;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiCommandBlock;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.tileentity.CommandBlockBaseLogic;
import net.minecraft.tileentity.TileEntityCommandBlock;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

/**
 * A replacement GUI for {@link GuiCommandBlock}
 * 
 * @author Earthcomputer
 *
 */
public class GuiNewCommandBlock extends GuiCommandBlock implements ICommandEditorCallback {

	private static final Field commandBlockField = ReflectionHelper.findField(GuiCommandBlock.class, "field_184078_g",
			"commandBlock");
	private static final Field commandTextFieldField = ReflectionHelper.findField(GuiCommandBlock.class,
			"field_146485_f", "commandTextField");
	private static final Field previousOutputTextFieldField = ReflectionHelper.findField(GuiCommandBlock.class,
			"field_146486_g", "previousOutputTextField");

	private TileEntityCommandBlock commandBlock;
	private GuiTextField commandTextField;
	private GuiTextField previousOutputTextField;
	private GuiButton outputBtn;

	private static final int COMMAND_EDITOR_BTN_ID = 8;

	public GuiNewCommandBlock(GuiCommandBlock old) throws Exception {
		super((TileEntityCommandBlock) commandBlockField.get(old));
		commandBlock = (TileEntityCommandBlock) commandBlockField.get(old);
	}

	@Override
	public void initGui() {
		String prevCommand = commandTextField == null ? null : commandTextField.getText();

		super.initGui();

		try {
			commandTextField = (GuiTextField) commandTextFieldField.get(this);
			previousOutputTextField = (GuiTextField) previousOutputTextFieldField.get(this);
		} catch (Exception e) {
			throw Throwables.propagate(e);
		}
		outputBtn = buttonList.get(2);

		if (prevCommand != null) {
			commandTextField.setText(prevCommand);
		}
		commandTextField.width = 196;
		addButton(new GuiButton(COMMAND_EDITOR_BTN_ID, width / 2 + 150 - 100, 50, 100, 20,
				Translate.GUI_COMMANDBLOCK_GOTOCOMMANDEDITOR));
		previousOutputTextField.width = 196;
		outputBtn.xPosition = width / 2 + 150 - 100;
		outputBtn.width = 100;
	}

	@Override
	protected void actionPerformed(GuiButton button) throws IOException {
		super.actionPerformed(button);
		if (button.enabled) {
			switch (button.id) {
			case 4:
				updateCmdOutput();
				break;
			case COMMAND_EDITOR_BTN_ID:
				mc.displayGuiScreen(new GuiCommandEditor(this, this, commandBlock.getCommandBlockLogic()));
				break;
			}
		}
	}

	@Override
	public void updateGui() {
		super.updateGui();
		updateCmdOutput();
	}

	private void updateCmdOutput() {
		CommandBlockBaseLogic logic = commandBlock.getCommandBlockLogic();

		if (logic.shouldTrackOutput()) {
			outputBtn.displayString = Translate.GUI_COMMANDBLOCK_TRACKINGOUTPUT;
			// Previous output text field text already set by superclass
		} else {
			outputBtn.displayString = Translate.GUI_COMMANDBLOCK_IGNORINGOUTPUT;
			previousOutputTextField.setText(Translate.GUI_COMMANDBLOCK_NOOUTPUT);
		}
	}

	@Override
	public void setCommand(String command) {
		commandTextField.setText(command);
	}

	@Override
	public String getCommand() {
		return commandTextField.getText();
	}

}
