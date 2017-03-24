package net.earthcomputer.easyeditors.gui.command.slot;

import java.util.List;

import com.google.common.base.Predicate;

import net.earthcomputer.easyeditors.gui.ICallback;
import net.earthcomputer.easyeditors.gui.command.CommandSyntaxException;
import net.earthcomputer.easyeditors.gui.command.GuiSelectTeam;
import net.earthcomputer.easyeditors.gui.command.UIInvalidException;
import net.earthcomputer.easyeditors.util.TranslateKeys;
import net.minecraft.client.Minecraft;

public class CommandSlotTeam extends CommandSlotHorizontalArrangement implements ICallback<String> {

	private CommandSlotTextField teamTextField;

	public CommandSlotTeam() {
		addChild(teamTextField = new CommandSlotTextField(100, 100));
		teamTextField.setContentFilter(new Predicate<String>() {
			@Override
			public boolean apply(String input) {
				return !input.contains(" ");
			}
		});

		addChild(new CommandSlotButton(20, 20, "...") {
			@Override
			public void onPress() {
				Minecraft.getMinecraft().displayGuiScreen(
						new GuiSelectTeam(Minecraft.getMinecraft().currentScreen, CommandSlotTeam.this));
			}
		});
	}

	public String getTeam() {
		return teamTextField.getText();
	}

	public void setTeam(String score) {
		teamTextField.setText(score);
	}

	@Override
	public String getCallbackValue() {
		return getTeam();
	}

	@Override
	public void setCallbackValue(String value) {
		setTeam(value);
	}

	@Override
	public int readFromArgs(String[] args, int index) throws CommandSyntaxException {
		if (args.length == index) {
			throw new CommandSyntaxException();
		}
		setTeam(args[index]);
		return 1;
	}

	@Override
	public void addArgs(List<String> args) throws UIInvalidException {
		checkValid();
		args.add(teamTextField.getText());
	}

	public void checkValid() throws UIInvalidException {
		if (teamTextField.getText().isEmpty()) {
			throw new UIInvalidException(TranslateKeys.GUI_COMMANDEDITOR_NOTEAMSELECTED);
		}
	}

}
