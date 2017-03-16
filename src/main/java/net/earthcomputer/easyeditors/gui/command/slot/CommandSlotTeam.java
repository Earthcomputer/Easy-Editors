package net.earthcomputer.easyeditors.gui.command.slot;

import java.util.List;

import net.earthcomputer.easyeditors.gui.ICallback;
import net.earthcomputer.easyeditors.gui.command.CommandSyntaxException;
import net.earthcomputer.easyeditors.gui.command.GuiSelectTeam;
import net.earthcomputer.easyeditors.gui.command.UIInvalidException;
import net.earthcomputer.easyeditors.util.Translate;
import net.earthcomputer.easyeditors.util.TranslateKeys;
import net.minecraft.client.Minecraft;

public class CommandSlotTeam extends CommandSlotHorizontalArrangement implements ICallback<String> {

	private String selectedTeam = null;
	private CommandSlotLabel teamLabel;

	public CommandSlotTeam() {
		addChild(teamLabel = new CommandSlotLabel(Minecraft.getMinecraft().fontRendererObj,
				Translate.GUI_COMMANDEDITOR_NOTEAM, 0xff0000));

		addChild(new CommandSlotButton(20, 20, "...") {
			@Override
			public void onPress() {
				Minecraft.getMinecraft().displayGuiScreen(
						new GuiSelectTeam(Minecraft.getMinecraft().currentScreen, CommandSlotTeam.this));
			}
		});
	}

	public String getTeam() {
		return selectedTeam;
	}

	public void setTeam(String score) {
		this.selectedTeam = score;
		teamLabel.setText(score);
		teamLabel.setColor(0x000000);
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
		args.add(selectedTeam);
	}

	public void checkValid() throws UIInvalidException {
		if (selectedTeam == null) {
			throw new UIInvalidException(TranslateKeys.GUI_COMMANDEDITOR_NOSCORESELECTED);
		}
	}

}
