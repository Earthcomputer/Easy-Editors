package net.earthcomputer.easyeditors.gui;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

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
	private static final Field commandBlockModeField = ReflectionHelper.findField(GuiCommandBlock.class,
			"field_184082_w", "commandBlockMode");
	private static final Field conditionalField = ReflectionHelper.findField(GuiCommandBlock.class, "field_184084_y",
			"conditional");
	private static final Field automaticField = ReflectionHelper.findField(GuiCommandBlock.class, "field_184085_z",
			"automatic");

	private static final Method updateModeMethod = ReflectionHelper.findMethod(GuiCommandBlock.class, null,
			new String[] { "func_184073_g", "updateMode" });
	private static final Method updateConditionalMethod = ReflectionHelper.findMethod(GuiCommandBlock.class, null,
			new String[] { "func_184077_i", "updateConditional" });
	private static final Method updateAutoExecMethod = ReflectionHelper.findMethod(GuiCommandBlock.class, null,
			new String[] { "func_184076_j", "updateAutoExec" });

	private TileEntityCommandBlock commandBlock;
	private GuiTextField commandTextField;
	private GuiTextField previousOutputTextField;
	private GuiButton outputBtn;
	private GuiButton gotoCommandEditorBtn;

	private static final int COMMAND_EDITOR_BTN_ID = 8;

	private boolean resetGuiOnLayout = true;

	public GuiNewCommandBlock(GuiCommandBlock old) throws Exception {
		super((TileEntityCommandBlock) commandBlockField.get(old));
		commandBlock = (TileEntityCommandBlock) commandBlockField.get(old);
	}

	@Override
	public void initGui() {
		String prevCommand = commandTextField == null ? null : commandTextField.getText();
		TileEntityCommandBlock.Mode prevMode;
		boolean prevConditional;
		boolean prevAutomatic;
		try {
			prevMode = (TileEntityCommandBlock.Mode) commandBlockModeField.get(this);
			prevConditional = conditionalField.getBoolean(this);
			prevAutomatic = automaticField.getBoolean(this);
		} catch (Exception e) {
			throw Throwables.propagate(e);
		}

		super.initGui();

		try {
			commandTextField = (GuiTextField) commandTextFieldField.get(this);
			previousOutputTextField = (GuiTextField) previousOutputTextFieldField.get(this);
		} catch (Exception e) {
			throw Throwables.propagate(e);
		}
		outputBtn = buttonList.get(2);
		outputBtn.displayString = Translate.GUI_COMMANDBLOCK_TRACKINGOUTPUT;

		if (!resetGuiOnLayout) {
			updateGui();
			commandTextField.setText(prevCommand);
			try {
				commandBlockModeField.set(this, prevMode);
				updateModeMethod.invoke(this);
				conditionalField.set(this, prevConditional);
				updateConditionalMethod.invoke(this);
				automaticField.set(this, prevAutomatic);
				updateAutoExecMethod.invoke(this);
			} catch (Exception e) {
				throw Throwables.propagate(e);
			}
		}

		commandTextField.width = 196;
		gotoCommandEditorBtn = addButton(new GuiButton(COMMAND_EDITOR_BTN_ID, width / 2 + 150 - 100, 50, 100, 20,
				Translate.GUI_COMMANDBLOCK_GOTOCOMMANDEDITOR));
		gotoCommandEditorBtn.enabled = !resetGuiOnLayout;
		commandTextField.setEnabled(!resetGuiOnLayout);
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
		gotoCommandEditorBtn.enabled = true;
		commandTextField.setEnabled(true);
		resetGuiOnLayout = false;
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
