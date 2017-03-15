package net.earthcomputer.easyeditors.gui.command.slot;

import java.util.List;

import net.earthcomputer.easyeditors.gui.ICallback;
import net.earthcomputer.easyeditors.gui.command.CommandSyntaxException;
import net.earthcomputer.easyeditors.gui.command.GuiSelectScore;
import net.earthcomputer.easyeditors.gui.command.UIInvalidException;
import net.earthcomputer.easyeditors.util.Translate;
import net.earthcomputer.easyeditors.util.TranslateKeys;
import net.minecraft.client.Minecraft;

public class CommandSlotScore extends CommandSlotHorizontalArrangement implements ICallback<String> {

	private String selectedScore = null;
	private CommandSlotLabel scoreLabel;

	public CommandSlotScore() {
		addChild(scoreLabel = new CommandSlotLabel(Minecraft.getMinecraft().fontRendererObj,
				Translate.GUI_COMMANDEDITOR_NOSCORE, 0xff0000));

		addChild(new CommandSlotButton(20, 20, "...") {
			@Override
			public void onPress() {
				Minecraft.getMinecraft().displayGuiScreen(
						new GuiSelectScore(Minecraft.getMinecraft().currentScreen, CommandSlotScore.this));
			}
		});
	}

	public String getScore() {
		return selectedScore;
	}

	public void setScore(String score) {
		this.selectedScore = score;
		scoreLabel.setText(score);
		scoreLabel.setColor(0x000000);
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
		args.add(selectedScore);
	}

	public void checkValid() throws UIInvalidException {
		if (selectedScore == null) {
			throw new UIInvalidException(TranslateKeys.GUI_COMMANDEDITOR_NOSCORESELECTED);
		}
	}

}
