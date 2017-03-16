package net.earthcomputer.easyeditors.gui.command;

import java.io.IOException;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import com.google.common.collect.Lists;

import net.earthcomputer.easyeditors.api.util.ChatBlocker;
import net.earthcomputer.easyeditors.api.util.ReturnedValueListener;
import net.earthcomputer.easyeditors.gui.GuiTwoWayScroll;
import net.earthcomputer.easyeditors.gui.ICallback;
import net.earthcomputer.easyeditors.util.Translate;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.I18n;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.Team;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.client.config.GuiCheckBox;

public class GuiSelectTeam extends GuiTwoWayScroll {

	private GuiScreen prevScreen;
	private ICallback<String> callback;

	private List<EditableTeam> editableTeams;
	private List<EditableTeam> removedTeams;
	private EditableTeam selectedTeam;
	private EnumState state = EnumState.WAITING;

	private GuiButton doneButton;
	private GuiButton cancelButton;

	private static final int VIRTUAL_WIDTH = 400;
	private static final int SLOT_HEIGHT = 50;

	public GuiSelectTeam(GuiScreen prevScreen, ICallback<String> callback) {
		super(30, 55, 400, 1);
		this.callback = callback;
		this.prevScreen = prevScreen;

		ChatBlocker.obtainTeamsList(new ReturnedValueListener<List<ScorePlayerTeam>>() {
			@Override
			public void returnValue(List<ScorePlayerTeam> value) {
				editableTeams = Lists.newArrayListWithExpectedSize(value.size());
				for (ScorePlayerTeam team : value) {
					EditableTeam editableTeam = new EditableTeam(team);
					if (team.getRegisteredName().equals(GuiSelectTeam.this.callback.getCallbackValue())) {
						selectedTeam = editableTeam;
					}
					editableTeams.add(editableTeam);
				}
				removedTeams = Lists.newArrayList();
				state = EnumState.SHOWING_TEAMS;
				recalcVirtualHeight();
				if (doneButton != null) {
					doneButton.enabled = true;
				}
			}

			@Override
			public void abortFindingValue(int reason) {
				if (reason == 0) {
					state = EnumState.TIMED_OUT;
				} else {
					state = EnumState.NO_PERMISSION;
				}
			}
		});
		setXScrollBarPolicy(SHOWN_NEVER);
	}

	private void recalcVirtualHeight() {
		setVirtualHeight(SLOT_HEIGHT * editableTeams.size());
	}

	private boolean areEditableTeamsValid() {
		for (EditableTeam team : editableTeams) {
			if (!team.isValid()) {
				return false;
			}
		}
		return true;
	}

	@Override
	public void initGui() {
		super.initGui();
		doneButton = addButton(new GuiButton(0, width / 2 - 160, height - 15 - 10, 150, 20, I18n.format("gui.done")));
		doneButton.enabled = state == EnumState.SHOWING_TEAMS;
		cancelButton = addButton(new GuiButton(1, width / 2 + 5, height - 15 - 10, 150, 20, I18n.format("gui.cancel")));

		addButton(new GuiButton(2, width / 2 - 160, height - 35 - 2 - 10, 150, 20,
				Translate.GUI_COMMANDEDITOR_SELECTTEAM_ADD));
		addButton(new GuiButton(3, width / 2 + 5, height - 35 - 2 - 10, 150, 20,
				Translate.GUI_COMMANDEDITOR_SELECTTEAM_REMOVE));
	}

	@Override
	protected void actionPerformed(GuiButton button) {
		switch (button.id) {
		case 0:
			for (EditableTeam team : editableTeams) {
				boolean hasCriticalInfoChanged = team.hasCriticalInfoChanged();
				if (hasCriticalInfoChanged) {
					team.sendRemoveCommand();
					team.sendAddCommand();
				}
				team.refreshChangedOptions(hasCriticalInfoChanged);
			}
			for (EditableTeam team : removedTeams) {
				team.sendRemoveCommand();
			}
			if (selectedTeam != null) {
				callback.setCallbackValue(selectedTeam.getTeamName());
			}
			// FALLTHROUGH
		case 1:
			mc.displayGuiScreen(prevScreen);
			break;
		case 2:
			EditableTeam newTeam = new EditableTeam();
			selectedTeam = newTeam;
			editableTeams.add(newTeam);
			recalcVirtualHeight();
			break;
		case 3:
			if (selectedTeam != null) {
				editableTeams.remove(selectedTeam);
				removedTeams.add(selectedTeam);
				selectedTeam = null;
				recalcVirtualHeight();
			}
			break;
		}
	}

	@Override
	protected void keyTyped(char typedChar, int keyCode) throws IOException {
		if (keyCode == Keyboard.KEY_ESCAPE) {
			actionPerformed(cancelButton);
			return;
		}
		super.keyTyped(typedChar, keyCode);
		if (state == EnumState.SHOWING_TEAMS) {
			for (EditableTeam team : editableTeams) {
				team.keyTyped(typedChar, keyCode);
			}
		}
	}

	@Override
	protected void mouseClickedVirtual(int mouseX, int mouseY, int mouseButton) {
		if (state == EnumState.SHOWING_TEAMS) {
			if (mouseButton == 0) {
				int clickedSlot = (mouseY + getScrollY() - getHeaderHeight()) / SLOT_HEIGHT;
				if (clickedSlot >= 0 && clickedSlot < editableTeams.size()) {
					selectedTeam = editableTeams.get(clickedSlot);
				} else {
					selectedTeam = null;
				}
			}
			for (EditableTeam team : editableTeams) {
				team.mouseClicked(mouseX, mouseY, mouseButton);
			}
		}
	}

	@Override
	protected void drawVirtualScreen(int mouseX, int mouseY, float partialTicks, int scrollX, int scrollY,
			int headerHeight) {
		switch (state) {
		case NO_PERMISSION:
			drawCenteredString(fontRendererObj, Translate.GUI_COMMANDEDITOR_SELECTTEAM_NOPERMISSION, width / 2,
					height / 2, 0xff0000);
			break;
		case SHOWING_TEAMS:
			drawTeamList(mouseX, mouseY, partialTicks, scrollX, scrollY, headerHeight);
			break;
		case TIMED_OUT:
			drawCenteredString(fontRendererObj, Translate.GUI_COMMANDEDITOR_SELECTTEAM_TIMEDOUT, width / 2, height / 2,
					0xff0000);
			break;
		case WAITING:
			drawCenteredString(fontRendererObj, Translate.GUI_COMMANDEDITOR_SELECTTEAM_WAITING, width / 2, height / 2,
					0xff0000);
			break;
		}
	}

	private void drawTeamList(int mouseX, int mouseY, float partialTicks, int scrollX, int scrollY, int headerHeight) {
		doneButton.enabled = areEditableTeamsValid();
		int top = scrollY / SLOT_HEIGHT;
		int bottom = MathHelper.ceil((float) (scrollY + getShownHeight()) / SLOT_HEIGHT);
		if (bottom >= editableTeams.size()) {
			bottom = editableTeams.size() - 1;
		}
		for (int i = top; i <= bottom; i++) {
			EditableTeam teamToDraw = editableTeams.get(i);
			int y = getHeaderHeight() + i * SLOT_HEIGHT - scrollY;
			if (selectedTeam == teamToDraw) {
				GlStateManager.color(1, 1, 1, 1);
				GlStateManager.disableTexture2D();
				Tessellator tessellator = Tessellator.getInstance();
				VertexBuffer buffer = tessellator.getBuffer();
				buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
				buffer.pos(7, y + SLOT_HEIGHT, 0).tex(0, 1).color(0x80, 0x80, 0x80, 255).endVertex();
				buffer.pos(getShownWidth() - 7, y + SLOT_HEIGHT, 0).tex(1, 1).color(0x80, 0x80, 0x80, 255).endVertex();
				buffer.pos(getShownWidth() - 7, y - 2, 0).tex(1, 0).color(0x80, 0x80, 0x80, 255).endVertex();
				buffer.pos(7, y - 2, 0).tex(0, 0).color(0x80, 0x80, 0x80, 255).endVertex();
				buffer.pos(8, y + SLOT_HEIGHT - 1, 0).tex(0, 1).color(0, 0, 0, 255).endVertex();
				buffer.pos(getShownWidth() - 8, y + SLOT_HEIGHT - 1, 0).tex(1, 1).color(0, 0, 0, 255).endVertex();
				buffer.pos(getShownWidth() - 8, y - 1, 0).tex(1, 0).color(0, 0, 0, 255).endVertex();
				buffer.pos(8, y - 1, 0).tex(0, 0).color(0, 0, 0, 255).endVertex();
				tessellator.draw();
				GlStateManager.enableTexture2D();
			}

			teamToDraw.draw(y, mouseX, mouseY, partialTicks);
		}
	}

	@Override
	protected void drawForeground(int mouseX, int mouseY, float partialTicks) {
		String str = Translate.GUI_COMMANDEDITOR_SELECTTEAM_TITLE;
		fontRendererObj.drawString(str, width / 2 - fontRendererObj.getStringWidth(str) / 2,
				15 - fontRendererObj.FONT_HEIGHT / 2, 0xffffff);
	}

	private static enum EnumState {
		WAITING, TIMED_OUT, NO_PERMISSION, SHOWING_TEAMS
	}

	private static class EditableTeam {
		private ScorePlayerTeam originalTeam;

		private GuiTextField nameTextField;
		private GuiTextField displayNameTextField;
		private TeamOptions options;
		private GuiButton optionsButton;

		public EditableTeam() {
			this(null);
		}

		public EditableTeam(ScorePlayerTeam originalTeam) {
			this.originalTeam = originalTeam;
			nameTextField = new GuiTextField(0, Minecraft.getMinecraft().fontRendererObj, 80, 0, VIRTUAL_WIDTH / 2 - 70,
					20);
			nameTextField.setMaxStringLength(16);
			if (originalTeam != null) {
				nameTextField.setText(originalTeam.getRegisteredName());
			}
			displayNameTextField = new GuiTextField(0, Minecraft.getMinecraft().fontRendererObj, 80, 0,
					VIRTUAL_WIDTH / 2 - 70, 20);
			displayNameTextField.setMaxStringLength(32);
			if (originalTeam != null) {
				displayNameTextField.setText(originalTeam.getTeamName());
			}
			if (originalTeam == null) {
				options = new TeamOptions();
			} else {
				options = new TeamOptions(originalTeam);
			}
			optionsButton = new GuiButton(0, VIRTUAL_WIDTH / 2 + 20, 0, VIRTUAL_WIDTH / 2 - 20, 20,
					Translate.GUI_COMMANDEDITOR_SELECTTEAM_OPTIONS);
		}

		public void keyTyped(char typedChar, int keyCode) {
			nameTextField.textboxKeyTyped(typedChar, keyCode);
			displayNameTextField.textboxKeyTyped(typedChar, keyCode);
		}

		public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
			nameTextField.mouseClicked(mouseX, mouseY, mouseButton);
			displayNameTextField.mouseClicked(mouseX, mouseY, mouseButton);
			if (optionsButton.mousePressed(Minecraft.getMinecraft(), mouseX, mouseY)) {
				Minecraft.getMinecraft().displayGuiScreen(new GuiTeamOptions(Minecraft.getMinecraft().currentScreen));
				optionsButton.playPressSound(Minecraft.getMinecraft().getSoundHandler());
			}
		}

		public void draw(int y, int mouseX, int mouseY, float partialTicks) {
			nameTextField.yPosition = y + 1;
			displayNameTextField.yPosition = y + SLOT_HEIGHT - 23;
			optionsButton.yPosition = y + SLOT_HEIGHT / 2 - 10;

			Minecraft.getMinecraft().fontRendererObj.drawString(Translate.GUI_COMMANDEDITOR_SELECTTEAM_NAME, 10, y + 7,
					0xffffff);
			nameTextField.drawTextBox();
			Minecraft.getMinecraft().fontRendererObj.drawString(Translate.GUI_COMMANDEDITOR_SELECTTEAM_DISPLAYNAME, 10,
					y + SLOT_HEIGHT - 16, 0xffffff);
			displayNameTextField.drawTextBox();
			optionsButton.drawButton(Minecraft.getMinecraft(), mouseX, mouseY);
		}

		public boolean isValid() {
			if (nameTextField.getText().isEmpty()) {
				return false;
			}
			if (displayNameTextField.getText().isEmpty()) {
				return false;
			}
			return true;
		}

		public boolean hasCriticalInfoChanged() {
			if (originalTeam == null) {
				return true;
			}
			if (!nameTextField.getText().equals(originalTeam.getRegisteredName())) {
				return true;
			}
			if (!displayNameTextField.getText().equals(originalTeam.getTeamName())) {
				return true;
			}
			if (options.color == null && originalTeam.getChatFormat() != null) {
				return true;
			}
			return false;
		}

		public void refreshChangedOptions(boolean hasCriticalInfoChanged) {
			String teamName = nameTextField.getText();
			boolean integratedServer = Minecraft.getMinecraft().isIntegratedServerRunning();
			ScorePlayerTeam team = null;
			if (integratedServer) {
				team = Minecraft.getMinecraft().getIntegratedServer().worldServerForDimension(0).getScoreboard()
						.getTeam(teamName);
			}
			if (hasCriticalInfoChanged ? options.color != null : options.color != originalTeam.getChatFormat()) {
				if (!integratedServer) {
					ChatBlocker.blockNextTranslation("commands.scoreboard.teams.option.success");
					ChatBlocker.executeCommand(String.format("/scoreboard teams option %s color %s", teamName,
							options.color.getFriendlyName()));
				} else {
					team.setChatFormat(options.color);
					team.setNamePrefix(options.color.toString());
					team.setNameSuffix(TextFormatting.RESET.toString());
				}
			}
			if (hasCriticalInfoChanged ? !options.friendlyFire
					: options.friendlyFire != originalTeam.getAllowFriendlyFire()) {
				if (!integratedServer) {
					ChatBlocker.blockNextTranslation("commands.scoreboard.teams.option.success");
					ChatBlocker.executeCommand(String.format("/scoreboard teams option %s friendlyfire %s", teamName,
							options.friendlyFire));
				} else {
					team.setAllowFriendlyFire(options.friendlyFire);
				}
			}
			if (hasCriticalInfoChanged ? !options.seeFriendlyInvisibles
					: options.seeFriendlyInvisibles != originalTeam.getSeeFriendlyInvisiblesEnabled()) {
				if (!integratedServer) {
					ChatBlocker.blockNextTranslation("commands.scoreboard.teams.option.success");
					ChatBlocker.executeCommand(String.format("/scoreboard teams option %s seeFriendlyInvisibles %s",
							teamName, options.seeFriendlyInvisibles));
				} else {
					team.setSeeFriendlyInvisiblesEnabled(options.seeFriendlyInvisibles);
				}
			}
			if (hasCriticalInfoChanged ? options.nameTagVisibility != Team.EnumVisible.ALWAYS
					: options.nameTagVisibility != originalTeam.getNameTagVisibility()) {
				if (!integratedServer) {
					ChatBlocker.blockNextTranslation("commands.scoreboard.teams.option.success");
					ChatBlocker.executeCommand(String.format("/scoreboard teams option %s nametagVisibility %s",
							teamName, options.nameTagVisibility.internalName));
				} else {
					team.setNameTagVisibility(options.nameTagVisibility);
				}
			}
			if (hasCriticalInfoChanged ? options.deathMessageVisibility != Team.EnumVisible.ALWAYS
					: options.deathMessageVisibility != originalTeam.getDeathMessageVisibility()) {
				if (!integratedServer) {
					ChatBlocker.blockNextTranslation("commands.scoreboard.teams.option.success");
					ChatBlocker.executeCommand(String.format("/scoreboard teams option %s deathMessageVisibility %s",
							teamName, options.deathMessageVisibility.internalName));
				} else {
					team.setDeathMessageVisibility(options.deathMessageVisibility);
				}
			}
			if (hasCriticalInfoChanged ? options.collisionRule != Team.CollisionRule.ALWAYS
					: options.collisionRule != originalTeam.getCollisionRule()) {
				if (!integratedServer) {
					ChatBlocker.blockNextTranslation("commands.scoreboard.teams.option.success");
					ChatBlocker.executeCommand(String.format("/scoreboard teams option %s collisionRule %s", teamName,
							options.collisionRule.name));
				} else {
					team.setCollisionRule(options.collisionRule);
				}
			}
		}

		public void sendRemoveCommand() {
			if (originalTeam == null) {
				return;
			}
			if (!Minecraft.getMinecraft().isIntegratedServerRunning()) {
				ChatBlocker.blockNextTranslation("commands.scoreboard.teams.remove.success");
				ChatBlocker
						.executeCommand(String.format("/scoreboard teams remove %s", originalTeam.getRegisteredName()));
			} else {
				Scoreboard scoreboard = Minecraft.getMinecraft().getIntegratedServer().worldServerForDimension(0)
						.getScoreboard();
				if (scoreboard.getTeam(originalTeam.getRegisteredName()) != null) {
					scoreboard.removeTeam(scoreboard.getTeam(originalTeam.getRegisteredName()));
				}
			}
		}

		public void sendAddCommand() {
			if (!Minecraft.getMinecraft().isIntegratedServerRunning()) {
				ChatBlocker.blockNextTranslation("commands.scoreboard.teams.add.success");
				ChatBlocker.executeCommand(String.format("/scoreboard teams add %s %s", nameTextField.getText(),
						displayNameTextField.getText()));
			} else {
				Scoreboard scoreboard = Minecraft.getMinecraft().getIntegratedServer().worldServerForDimension(0)
						.getScoreboard();
				if (scoreboard.getTeam(nameTextField.getText()) != null) {
					scoreboard.removeTeam(scoreboard.getTeam(nameTextField.getText()));
				}
				scoreboard.createTeam(nameTextField.getText()).setTeamName(displayNameTextField.getText());
			}
		}

		public String getTeamName() {
			return nameTextField.getText();
		}

		private class GuiTeamOptions extends GuiScreen {
			private GuiScreen prevScreen;
			private CycleButton<TextFormatting> color;
			private GuiCheckBox friendlyFire;
			private GuiCheckBox seeFriendlyInvisibles;
			private CycleButton<Team.EnumVisible> nameTagVisibility;
			private CycleButton<Team.EnumVisible> deathMessageVisibility;
			private CycleButton<Team.CollisionRule> collisionRule;

			public GuiTeamOptions(GuiScreen prevScreen) {
				this.prevScreen = prevScreen;
			}

			private GuiButton cancelButton;

			@Override
			public void initGui() {
				addButton(new GuiButton(0, width / 2 - 160, height - 15 - 10, 150, 20, I18n.format("gui.done")));
				cancelButton = addButton(
						new GuiButton(1, width / 2 + 5, height - 15 - 10, 150, 20, I18n.format("gui.cancel")));

				int halfWidth = width / 2;
				int halfHeight = height / 2;

				TextFormatting[] colors = new TextFormatting[17];
				String[] displayNames = new String[17];
				displayNames[0] = Translate.GUI_COMMANDEDITOR_SELECTTEAM_OPTIONS_COLOR_NONE;
				for (int i = 0; i < 16; i++) {
					colors[i + 1] = TextFormatting.fromColorIndex(i);
					displayNames[i + 1] = I18n.format("color." + colors[i + 1].getFriendlyName());
				}
				color = addButton(
						new CycleButton<TextFormatting>(halfWidth, halfHeight - 85, 100, 20, colors, displayNames));
				color.setCurrentValue(options.color);

				friendlyFire = addButton(new GuiCheckBox(-1, halfWidth, halfHeight - 52, "", options.friendlyFire));

				seeFriendlyInvisibles = addButton(
						new GuiCheckBox(-1, halfWidth, halfHeight - 22, "", options.seeFriendlyInvisibles));

				Team.EnumVisible[] visibilities = Team.EnumVisible.values();
				displayNames = new String[visibilities.length];
				for (int i = 0; i < visibilities.length; i++) {
					displayNames[i] = I18n
							.format("gui.commandEditor.selectTeam.options.visibility." + visibilities[i].internalName);
				}
				nameTagVisibility = addButton(new CycleButton<Team.EnumVisible>(halfWidth, halfHeight + 5, 100, 20,
						visibilities, displayNames));
				nameTagVisibility.setCurrentValue(options.nameTagVisibility);

				deathMessageVisibility = addButton(new CycleButton<Team.EnumVisible>(halfWidth, halfHeight + 35, 100,
						20, visibilities, displayNames));
				deathMessageVisibility.setCurrentValue(options.deathMessageVisibility);

				Team.CollisionRule[] collisionRules = Team.CollisionRule.values();
				displayNames = new String[visibilities.length];
				for (int i = 0; i < collisionRules.length; i++) {
					displayNames[i] = I18n
							.format("gui.commandEditor.selectTeam.options.collisionRule." + collisionRules[i].name);
				}
				collisionRule = addButton(new CycleButton<Team.CollisionRule>(halfWidth, halfHeight + 65, 100, 20,
						collisionRules, displayNames));
				collisionRule.setCurrentValue(options.collisionRule);
			}

			@Override
			public void drawScreen(int mouseX, int mouseY, float partialTicks) {
				drawBackground(0);
				super.drawScreen(mouseX, mouseY, partialTicks);
				String str = Translate.GUI_COMMANDEDITOR_SELECTTEAM_OPTIONS_TITLE;
				drawString(fontRendererObj, str, width / 2 - fontRendererObj.getStringWidth(str) / 2,
						15 - fontRendererObj.FONT_HEIGHT / 2, 0xffffff);
				int halfHeight = height / 2;
				drawString(fontRendererObj, Translate.GUI_COMMANDEDITOR_SELECTTEAM_OPTIONS_COLOR, 20, halfHeight - 80,
						0xffffff);
				drawString(fontRendererObj, Translate.GUI_COMMANDEDITOR_SELECTTEAM_OPTIONS_FRIENDLYFIRE, 20,
						halfHeight - 50, 0xffffff);
				drawString(fontRendererObj, Translate.GUI_COMMANDEDITOR_SELECTTEAM_OPTIONS_SEEFRIENDLYINVISIBLES, 20,
						halfHeight - 20, 0xffffff);
				drawString(fontRendererObj, Translate.GUI_COMMANDEDITOR_SELECTTEAM_OPTIONS_NAMETAGVISIBILITY, 20,
						halfHeight + 10, 0xffffff);
				drawString(fontRendererObj, Translate.GUI_COMMANDEDITOR_SELECTTEAM_OPTIONS_DEATHMESSAGEVISIBILITY, 20,
						halfHeight + 40, 0xffffff);
				drawString(fontRendererObj, Translate.GUI_COMMANDEDITOR_SELECTTEAM_OPTIONS_COLLISIONRULE, 20,
						halfHeight + 70, 0xffffff);
			}

			@Override
			protected void keyTyped(char typedChar, int keyCode) throws IOException {
				if (keyCode == Keyboard.KEY_ESCAPE) {
					actionPerformed(cancelButton);
				} else {
					super.keyTyped(typedChar, keyCode);
				}
			}

			@Override
			protected void actionPerformed(GuiButton button) throws IOException {
				switch (button.id) {
				case 0:
					options.color = color.getCurrentValue();
					options.friendlyFire = friendlyFire.isChecked();
					options.seeFriendlyInvisibles = seeFriendlyInvisibles.isChecked();
					options.nameTagVisibility = nameTagVisibility.getCurrentValue();
					options.deathMessageVisibility = deathMessageVisibility.getCurrentValue();
					options.collisionRule = collisionRule.getCurrentValue();
					// FALLTHROUGH
				case 1:
					mc.displayGuiScreen(prevScreen);
					break;
				}
			}
		}
	}

	private static class TeamOptions {
		private TextFormatting color;
		private boolean friendlyFire;
		private boolean seeFriendlyInvisibles;
		private Team.EnumVisible nameTagVisibility = Team.EnumVisible.ALWAYS;
		private Team.EnumVisible deathMessageVisibility = Team.EnumVisible.ALWAYS;
		private Team.CollisionRule collisionRule = Team.CollisionRule.ALWAYS;

		public TeamOptions() {
		}

		public TeamOptions(ScorePlayerTeam team) {
			this.color = team.getChatFormat();
			this.friendlyFire = team.getAllowFriendlyFire();
			this.seeFriendlyInvisibles = team.getSeeFriendlyInvisiblesEnabled();
			this.nameTagVisibility = team.getNameTagVisibility();
			this.deathMessageVisibility = team.getDeathMessageVisibility();
			this.collisionRule = team.getCollisionRule();
		}

	}

	private static class CycleButton<T> extends GuiButton {
		private String[] displayValues;
		private T[] actualValues;
		private int currentIndex;

		public CycleButton(int x, int y, int width, int height, T[] actualValues, String... displayValues) {
			super(-1, x, y, width, height, displayValues[0]);
			if (actualValues.length != displayValues.length) {
				throw new IllegalArgumentException("actualValues.length != displayValues.length");
			}
			this.actualValues = actualValues;
			this.displayValues = displayValues;
		}

		@Override
		public boolean mousePressed(Minecraft mc, int mouseX, int mouseY) {
			if (super.mousePressed(mc, mouseX, mouseY)) {
				if (GuiScreen.isShiftKeyDown()) {
					currentIndex--;
					if (currentIndex < 0) {
						currentIndex += actualValues.length;
					}
				} else {
					currentIndex++;
					if (currentIndex >= actualValues.length) {
						currentIndex -= actualValues.length;
					}
				}
				displayString = displayValues[currentIndex];
				return true;
			} else {
				return false;
			}
		}

		public void setCurrentValue(T value) {
			currentIndex = ArrayUtils.indexOf(actualValues, value);
			if (currentIndex == ArrayUtils.INDEX_NOT_FOUND) {
				currentIndex = 0;
			}
			displayString = displayValues[currentIndex];
		}

		public T getCurrentValue() {
			return actualValues[currentIndex];
		}
	}

}
