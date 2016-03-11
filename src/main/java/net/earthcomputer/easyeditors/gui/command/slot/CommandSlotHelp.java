package net.earthcomputer.easyeditors.gui.command.slot;

import net.earthcomputer.easyeditors.gui.command.CommandHelpManager;

public class CommandSlotHelp extends CommandSlotButton {

	private String name;
	private String helpId;

	public CommandSlotHelp(String name, String helpId) {
		super(20, 20, "?");
		this.name = name;
		this.helpId = helpId;
	}

	public CommandSlotHelp(String name, String modid, String helpId) {
		this(name, modid + ":" + helpId);
	}

	@Override
	public void onPress() {
		CommandHelpManager.getInstance().displayHelpScreen(name, helpId);
	}

}
