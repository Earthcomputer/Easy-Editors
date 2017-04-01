package net.earthcomputer.easyeditors.gui.command.slot;

import net.earthcomputer.easyeditors.gui.GuiSelectFromList;
import net.earthcomputer.easyeditors.gui.ICallback;
import net.earthcomputer.easyeditors.gui.command.GuiSelectStat;
import net.earthcomputer.easyeditors.util.Translate;
import net.earthcomputer.easyeditors.util.TranslateKeys;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.stats.StatBase;
import net.minecraft.stats.StatList;

public class CommandSlotStat extends CommandSlotSelectFromList<StatBase> {

	private boolean includeAchievements;
	
	public CommandSlotStat(boolean includeAchievements) {
		super(Translate.GUI_COMMANDEDITOR_NOSTAT, TranslateKeys.GUI_COMMANDEDITOR_NOSTATSELECTED);
		this.includeAchievements = includeAchievements;
	}

	@Override
	protected GuiSelectFromList<StatBase> createGui(GuiScreen currentScreen, ICallback<StatBase> callback) {
		return new GuiSelectStat(currentScreen, callback, includeAchievements);
	}

	@Override
	protected String getDisplayName(StatBase val) {
		return val.getStatName().getFormattedText();
	}

	@Override
	protected StatBase readArg(String arg) {
		return StatList.getOneShotStat(arg);
	}

	@Override
	protected String writeArg(StatBase arg) {
		return arg.statId;
	}

}
