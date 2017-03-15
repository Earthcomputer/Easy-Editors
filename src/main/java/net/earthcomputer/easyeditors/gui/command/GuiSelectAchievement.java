package net.earthcomputer.easyeditors.gui.command;

import java.util.List;

import net.earthcomputer.easyeditors.gui.GuiSelectFromList;
import net.earthcomputer.easyeditors.gui.ICallback;
import net.earthcomputer.easyeditors.util.Translate;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.stats.Achievement;
import net.minecraft.stats.AchievementList;

public class GuiSelectAchievement extends GuiSelectFromList<Achievement> {

	/*
	 * This method is needed to fix a Forge bug which happens whenever
	 * StatList.reinit() is called, registerStat() is called again on all stats,
	 * including achievements, causing them to be added again to the achievement
	 * list.
	 */
	private static List<Achievement> deduplicateAchievementList(List<Achievement> original) {
		assert original.get(0) == AchievementList.OPEN_INVENTORY;
		return original.subList(0, original.subList(1, original.size()).indexOf(AchievementList.OPEN_INVENTORY) + 1);
	}

	public GuiSelectAchievement(GuiScreen prevScreen, ICallback<Achievement> callback) {
		super(prevScreen, callback, deduplicateAchievementList(AchievementList.ACHIEVEMENTS),
				Translate.GUI_COMMANDEDITOR_SELECTACHIEVEMENT_TITLE);
	}

	@Override
	protected List<String> getTooltip(Achievement value) {
		return fontRendererObj.listFormattedStringToWidth(value.getDescription(), width / 2);
	}

	@Override
	protected void drawSlot(int y, Achievement value) {
		String str = value.getStatName().getFormattedText();
		fontRendererObj.drawString(str, width / 2 - fontRendererObj.getStringWidth(str) / 2, y + 2, 0xffffff);
		str = value.statId;
		fontRendererObj.drawString(str, width / 2 - fontRendererObj.getStringWidth(str) / 2,
				y + 4 + fontRendererObj.FONT_HEIGHT, 0xc0c0c0);
	}

	@Override
	protected boolean doesSearchTextMatch(String searchText, Achievement value) {
		if (value.getStatName().getUnformattedText().toLowerCase().contains(searchText)) {
			return true;
		}
		if (value.statId.toLowerCase().contains(searchText)) {
			return true;
		}
		return false;
	}

}
