package net.earthcomputer.easyeditors.gui.command.slot;

import java.util.List;

import com.google.common.base.Predicate;

import net.earthcomputer.easyeditors.gui.ICallback;
import net.earthcomputer.easyeditors.gui.command.CommandSyntaxException;
import net.earthcomputer.easyeditors.gui.command.GuiSelectScore;
import net.earthcomputer.easyeditors.gui.command.UIInvalidException;
import net.earthcomputer.easyeditors.util.TranslateKeys;
import net.minecraft.client.Minecraft;

public class CommandSlotScore extends CommandSlotHorizontalArrangement implements ICallback<String> {

	private CommandSlotTextField scoreTextField;

	public CommandSlotScore() {
		addChild(scoreTextField = new CommandSlotTextField(100, 100));
		scoreTextField.setContentFilter(new Predicate<String>() {
			@Override
			public boolean apply(String input) {
				return !input.contains(" ");
			}
		});

		addChild(new CommandSlotButton(20, 20, "...") {
			@Override
			public void onPress() {
				Minecraft.getMinecraft().displayGuiScreen(
						new GuiSelectScore(Minecraft.getMinecraft().currentScreen, CommandSlotScore.this));
			}
		});
	}

	public String getScore() {
		return scoreTextField.getText();
	}

	public void setScore(String score) {
		scoreTextField.setText(score);
	}

	@Override
	public String getCallbackValue() {
		return getScore();
	}

	@Override
	public void setCallbackValue(String value) {
		setScore(value);
	}

	@Override
	public int readFromArgs(String[] args, int index) throws CommandSyntaxException {
		if (args.length == index) {
			throw new CommandSyntaxException();
		}
		setScore(args[index]);
		return 1;
	}

	@Override
	public void addArgs(List<String> args) throws UIInvalidException {
		checkValid();
		args.add(scoreTextField.getText());
	}

	public void checkValid() throws UIInvalidException {
		if (scoreTextField.getText().isEmpty()) {
			throw new UIInvalidException(TranslateKeys.GUI_COMMANDEDITOR_NOSCORESELECTED);
		}
	}

}
