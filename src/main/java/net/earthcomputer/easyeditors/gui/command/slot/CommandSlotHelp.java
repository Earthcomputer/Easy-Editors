package net.earthcomputer.easyeditors.gui.command.slot;

import net.earthcomputer.easyeditors.gui.command.CommandHelpManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;

public class CommandSlotHelp extends CommandSlotButton {

	private String name;
	private String helpId;

	public CommandSlotHelp(String name, String helpId) {
		super(8, 8, new ResourceLocation("easyeditors:textures/gui/help_icon.png"), I18n.format("help.title"));
		setBackgroundDrawn(false);
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
