package net.earthcomputer.easyeditors.gui.command;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.lwjgl.input.Keyboard;

import com.google.common.collect.Lists;

import net.earthcomputer.easyeditors.gui.ICallback;
import net.earthcomputer.easyeditors.util.Translate;
import net.earthcomputer.easyeditors.util.TranslateKeys;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.scoreboard.IScoreCriteria;
import net.minecraft.scoreboard.ScoreCriteriaStat;
import net.minecraft.stats.Achievement;
import net.minecraft.stats.StatBase;
import net.minecraft.stats.StatList;
import net.minecraft.util.text.TextFormatting;

public class GuiSelectScoreCriteria extends GuiScreen {

	private final List<String> topLevelTypes = Lists.newArrayList();
	private final List<StatBase> stats = Lists.newArrayList();
	private final List<Achievement> achievements = Lists.newArrayList();
	{
		for (Map.Entry<String, IScoreCriteria> criteria : IScoreCriteria.INSTANCES.entrySet()) {
			if (!(criteria.getValue() instanceof ScoreCriteriaStat)) {
				String criteriaName = criteria.getValue().getName();
				if (!criteriaName.startsWith("teamkill.") && !criteriaName.startsWith("killedByTeam.")) {
					topLevelTypes.add(criteria.getKey());
				}
			} else {
				// stupidly named, should be getStatById
				StatBase stat = StatList.getOneShotStat(criteria.getKey());
				if (stat instanceof Achievement) {
					achievements.add((Achievement) stat);
				} else {
					stats.add(stat);
				}
			}
		}
		topLevelTypes.add("teamkill");
		topLevelTypes.add("killedByTeam");
		topLevelTypes.add("achievement");
		topLevelTypes.add("stat");
	}

	private GuiScreen prevScreen;
	private ICallback<IScoreCriteria> callback;

	private int currentTopLevelType = 0;
	private GuiButton topLevelButton;
	private StatBase stat = stats.get(0);
	private GuiButton selectStatButton;
	private TextFormatting color = TextFormatting.WHITE;
	private GuiButton[] colorButtons = new GuiButton[16];

	private GuiButton cancelButton;

	public GuiSelectScoreCriteria(GuiScreen prevScreen, ICallback<IScoreCriteria> callback) {
		this.prevScreen = prevScreen;
		this.callback = callback;

		IScoreCriteria callbackValue = callback.getCallbackValue();
		if (callbackValue != null) {
			String topLevelType = null;
			if (callbackValue instanceof ScoreCriteriaStat) {
				StatBase stat = StatList.getOneShotStat(callbackValue.getName());
				if (stat != null) {
					if (stat instanceof Achievement) {
						topLevelType = "achievement";
					} else {
						topLevelType = "stat";
					}
					this.stat = stat;
				}
			} else {
				String criteriaName = callbackValue.getName();
				if (criteriaName.startsWith("teamkill.") || criteriaName.startsWith("killedByTeam.")) {
					topLevelType = criteriaName.startsWith("teamkill.") ? "teamkill" : "killedByTeam";
					String colorName = criteriaName.substring(criteriaName.startsWith("teamkill.") ? 9 : 13);
					for (int i = 0; i < 16; i++) {
						TextFormatting color = TextFormatting.fromColorIndex(i);
						if (color.getFriendlyName().equals(colorName)) {
							this.color = color;
							break;
						}
					}
				} else {
					topLevelType = criteriaName;
				}
			}
			if (topLevelType != null) {
				this.currentTopLevelType = topLevelTypes.indexOf(topLevelType);
			}
		}
	}

	@Override
	public void initGui() {
		addButton(new GuiButton(0, width / 2 - 160, height - 15 - 10, 150, 20, I18n.format("gui.done")));
		cancelButton = addButton(new GuiButton(1, width / 2 + 5, height - 15 - 10, 150, 20, I18n.format("gui.cancel")));
		topLevelButton = addButton(new GuiButton(2, 20, height / 2 - 10, 100, 20,
				I18n.format("gui.commandEditor.selectScore.criteria." + topLevelTypes.get(currentTopLevelType))));
		selectStatButton = addButton(new GuiButton(3, width - 40, height / 2 - 10, 20, 20, "..."));
		selectStatButton.visible = isStatOrAchievement();

		boolean isColored = isColored();
		for (int x = 0; x < 4; x++) {
			for (int y = 0; y < 4; y++) {
				int index = y * 4 + x;
				colorButtons[index] = addButton(new ColoredButton(16 + index, 150 + 20 * x, height / 2 - 38 + 20 * y,
						TextFormatting.fromColorIndex(index)));
				colorButtons[index].visible = isColored;
			}
		}
	}

	private boolean isStatOrAchievement() {
		return isStat() || isAchievement();
	}

	private boolean isStat() {
		return "stat".equals(topLevelTypes.get(currentTopLevelType));
	}

	private boolean isAchievement() {
		return "achievement".equals(topLevelTypes.get(currentTopLevelType));
	}

	private boolean isColored() {
		return "teamkill".equals(topLevelTypes.get(currentTopLevelType))
				|| "killedByTeam".equals(topLevelTypes.get(currentTopLevelType));
	}

	@Override
	protected void actionPerformed(GuiButton button) throws IOException {
		if (button.id >= 16 && button.id < 32) {
			color = ((ColoredButton) button).getColor();
			return;
		}
		switch (button.id) {
		case 0:
			callback.setCallbackValue(IScoreCriteria.INSTANCES.get(getSelectedCriteriaId()));
			// FALLTHROUGH
		case 1:
			mc.displayGuiScreen(prevScreen);
			break;
		case 2:
			if (GuiScreen.isShiftKeyDown()) {
				currentTopLevelType--;
				if (currentTopLevelType == -1) {
					currentTopLevelType = topLevelTypes.size() - 1;
				}
			} else {
				currentTopLevelType++;
				if (currentTopLevelType == topLevelTypes.size()) {
					currentTopLevelType = 0;
				}
			}
			topLevelButton.displayString = I18n
					.format("gui.commandEditor.selectScore.criteria." + topLevelTypes.get(currentTopLevelType));
			selectStatButton.visible = isStatOrAchievement();
			boolean isColored = isColored();
			if (isColored != colorButtons[0].visible) {
				for (GuiButton colorButton : colorButtons) {
					colorButton.visible = isColored;
				}
			}
			break;
		case 3:
			if (isStat()) {
				mc.displayGuiScreen(new GuiSelectStat(this, new ICallback<StatBase>() {
					@Override
					public StatBase getCallbackValue() {
						return stat;
					}

					@Override
					public void setCallbackValue(StatBase value) {
						stat = value;
					}
				}, false));
			} else {
				mc.displayGuiScreen(new GuiSelectAchievement(this, new ICallback<Achievement>() {
					@Override
					public Achievement getCallbackValue() {
						return stat instanceof Achievement ? (Achievement) stat : null;
					}

					@Override
					public void setCallbackValue(Achievement value) {
						stat = value;
					}
				}));
			}
			break;
		}
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		drawBackground(0);
		super.drawScreen(mouseX, mouseY, partialTicks);

		String str = Translate.GUI_COMMANDEDITOR_SELECTSCORECRITERIA_TITLE;
		fontRendererObj.drawString(str, width / 2 - fontRendererObj.getStringWidth(str) / 2,
				15 - fontRendererObj.FONT_HEIGHT / 2, 0xffffff);

		if (isStatOrAchievement()) {
			str = getDisplayName(stat.statId);
			fontRendererObj.drawString(str, 130, height / 2 - fontRendererObj.FONT_HEIGHT / 2, 0xffffff);
		} else if (isColored()) {
			str = I18n.format("color." + color.getFriendlyName());
			fontRendererObj.drawString(str, 230, height / 2 - fontRendererObj.FONT_HEIGHT / 2, 0xffffff);
		}
	}

	@Override
	protected void keyTyped(char typedChar, int keyCode) throws IOException {
		if (keyCode == Keyboard.KEY_ESCAPE) {
			actionPerformed(cancelButton);
		} else {
			super.keyTyped(typedChar, keyCode);
		}
	}

	private String getSelectedCriteriaId() {
		if (isStatOrAchievement()) {
			return stat.statId;
		} else if (isColored()) {
			return topLevelTypes.get(currentTopLevelType) + "." + color.getFriendlyName();
		} else {
			return topLevelTypes.get(currentTopLevelType);
		}
	}

	public static String getDisplayName(String criteriaId) {
		if (criteriaId.startsWith("teamkill.")) {
			return I18n.format(TranslateKeys.GUI_COMMANDEDITOR_SELECTSCORE_CRITERIA_TEAMKILL_SPECIFIC,
					I18n.format("color." + criteriaId.substring(9)));
		} else if (criteriaId.startsWith("killedByTeam.")) {
			return I18n.format(TranslateKeys.GUI_COMMANDEDITOR_SELECTSCORE_CRITERIA_KILLEDBYTEAM_SPECIFIC,
					I18n.format("color." + criteriaId.substring(13)));
		} else if (criteriaId.startsWith("achievement.")) {
			return I18n.format(TranslateKeys.GUI_COMMANDEDITOR_SELECTSCORE_CRITERIA_ACHIEVEMENT_SPECIFIC,
					StatList.getOneShotStat(criteriaId).getStatName().getFormattedText());
		} else if (criteriaId.startsWith("stat.")) {
			StatBase stat = StatList.getOneShotStat(criteriaId);
			String statName;
			// Fix format errors in vanilla with entity stats
			if (criteriaId.startsWith("stat.killEntity.")) {
				statName = I18n.format(TranslateKeys.GUI_COMMANDEDITOR_SELECTSCORE_CRITERIA_STAT_KILLENTITY,
						I18n.format("entity." + criteriaId.substring(16) + ".name"));
			} else if (criteriaId.startsWith("stat.entityKilledBy.")) {
				statName = I18n.format(TranslateKeys.GUI_COMMANDEDITOR_SELECTSCORE_CRITERIA_STAT_ENTITYKILLEDBY,
						I18n.format("entity." + criteriaId.substring(20) + ".name"));
			} else {
				statName = stat.getStatName().getFormattedText();
			}
			return I18n.format(TranslateKeys.GUI_COMMANDEDITOR_SELECTSCORE_CRITERIA_STAT_SPECIFIC, statName);
		} else {
			return I18n.format("gui.commandEditor.selectScore.criteria." + criteriaId);
		}
	}

	private static class ColoredButton extends GuiButton {
		private TextFormatting color;

		public ColoredButton(int id, int x, int y, TextFormatting color) {
			super(id, x, y, 16, 16, "");
			this.color = color;
		}

		@Override
		public void drawButton(Minecraft mc, int mouseX, int mouseY) {
			if (visible) {
				int color = Minecraft.getMinecraft().fontRendererObj.getColorCode(this.color.toString().charAt(1));
				color |= 0xff000000;
				drawRect(xPosition, yPosition, xPosition + 15, yPosition + 15, color);
			}
		}

		public TextFormatting getColor() {
			return color;
		}
	}

}
