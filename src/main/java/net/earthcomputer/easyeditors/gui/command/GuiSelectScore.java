package net.earthcomputer.easyeditors.gui.command;

import java.io.IOException;
import java.util.List;

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
import net.minecraft.scoreboard.IScoreCriteria;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.util.math.MathHelper;

public class GuiSelectScore extends GuiTwoWayScroll {

	private GuiScreen prevScreen;
	private ICallback<String> callback;

	private List<EditableScore> editableScores;
	private List<EditableScore> removedScores;
	private EditableScore selectedScore;
	private EnumState state = EnumState.WAITING;

	private GuiButton doneButton;
	private GuiButton cancelButton;

	private static final int VIRTUAL_WIDTH = 400;
	private static final int SCORE_HEIGHT = 50;

	public GuiSelectScore(GuiScreen prevScreen, ICallback<String> callback) {
		super(30, 55, VIRTUAL_WIDTH, 1);
		this.prevScreen = prevScreen;
		this.callback = callback;
		ChatBlocker.obtainObjectiveList(new ReturnedValueListener<List<ScoreObjective>>() {
			@Override
			public void returnValue(List<ScoreObjective> value) {
				editableScores = Lists.newArrayListWithExpectedSize(value.size());
				for (ScoreObjective objective : value) {
					EditableScore score = new EditableScore(objective);
					if (objective.getName().equals(GuiSelectScore.this.callback.getCallbackValue())) {
						selectedScore = score;
					}
					editableScores.add(score);
				}
				removedScores = Lists.newArrayList();
				state = EnumState.SHOWING_SCORES;
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
		setVirtualHeight(SCORE_HEIGHT * editableScores.size());
	}

	private boolean areEditableScoresValid() {
		for (EditableScore score : editableScores) {
			if (!score.isValid()) {
				return false;
			}
		}
		return true;
	}

	@Override
	public void initGui() {
		super.initGui();
		doneButton = addButton(new GuiButton(0, width / 2 - 160, height - 15 - 10, 150, 20, I18n.format("gui.done")));
		doneButton.enabled = state == EnumState.SHOWING_SCORES;
		cancelButton = addButton(new GuiButton(1, width / 2 + 5, height - 15 - 10, 150, 20, I18n.format("gui.cancel")));

		addButton(new GuiButton(2, width / 2 - 160, height - 35 - 2 - 10, 150, 20,
				Translate.GUI_COMMANDEDITOR_SELECTSCORE_ADD));
		addButton(new GuiButton(3, width / 2 + 5, height - 35 - 2 - 10, 150, 20,
				Translate.GUI_COMMANDEDITOR_SELECTSCORE_REMOVE));
	}

	@Override
	protected void actionPerformed(GuiButton button) throws IOException {
		switch (button.id) {
		case 0:
			for (EditableScore score : editableScores) {
				if (score.hasChanged()) {
					score.sendRemoveCommand();
					score.sendAddCommand();
				}
			}
			for (EditableScore score : removedScores) {
				score.sendRemoveCommand();
			}
			if (selectedScore != null) {
				callback.setCallbackValue(selectedScore.getScoreName());
			}
			// FALLTHROUGH
		case 1:
			mc.displayGuiScreen(prevScreen);
			break;
		case 2:
			EditableScore newScore = new EditableScore();
			selectedScore = newScore;
			editableScores.add(newScore);
			recalcVirtualHeight();
			break;
		case 3:
			if (selectedScore != null) {
				editableScores.remove(selectedScore);
				removedScores.add(selectedScore);
				selectedScore = null;
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
		if (state == EnumState.SHOWING_SCORES) {
			for (EditableScore score : editableScores) {
				score.keyTyped(typedChar, keyCode);
			}
		}
	}

	@Override
	protected void mouseClickedVirtual(int mouseX, int mouseY, int mouseButton) {
		if (state == EnumState.SHOWING_SCORES) {
			if (mouseButton == 0) {
				int clickedSlot = (mouseY + getScrollY() - getHeaderHeight()) / SCORE_HEIGHT;
				if (clickedSlot >= 0 && clickedSlot < editableScores.size()) {
					selectedScore = editableScores.get(clickedSlot);
				} else {
					selectedScore = null;
				}
			}
			for (EditableScore score : editableScores) {
				score.mouseClicked(mouseX, mouseY, mouseButton);
			}
		}
	}

	@Override
	protected void drawVirtualScreen(int mouseX, int mouseY, float partialTicks, int scrollX, int scrollY,
			int headerHeight) {
		switch (state) {
		case NO_PERMISSION:
			drawCenteredString(fontRendererObj, Translate.GUI_COMMANDEDITOR_SELECTSCORE_NOPERMISSION, width / 2,
					height / 2, 0xff0000);
			break;
		case SHOWING_SCORES:
			drawScoreList(mouseX, mouseY, partialTicks, scrollX, scrollY, headerHeight);
			break;
		case TIMED_OUT:
			drawCenteredString(fontRendererObj, Translate.GUI_COMMANDEDITOR_SELECTSCORE_TIMEDOUT, width / 2, height / 2,
					0xff0000);
			break;
		case WAITING:
			drawCenteredString(fontRendererObj, Translate.GUI_COMMANDEDITOR_SELECTSCORE_WAITING, width / 2, height / 2,
					0xff0000);
			break;
		}
	}

	private void drawScoreList(int mouseX, int mouseY, float partialTicks, int scrollX, int scrollY, int headerHeight) {
		doneButton.enabled = areEditableScoresValid();
		int top = scrollY / SCORE_HEIGHT;
		int bottom = MathHelper.ceil((float) (scrollY + getShownHeight()) / SCORE_HEIGHT);
		if (bottom >= editableScores.size()) {
			bottom = editableScores.size() - 1;
		}
		for (int i = top; i <= bottom; i++) {
			EditableScore scoreToDraw = editableScores.get(i);
			int y = getHeaderHeight() + i * SCORE_HEIGHT - scrollY;
			if (selectedScore == scoreToDraw) {
				GlStateManager.color(1, 1, 1, 1);
				GlStateManager.disableTexture2D();
				Tessellator tessellator = Tessellator.getInstance();
				VertexBuffer buffer = tessellator.getBuffer();
				buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
				buffer.pos(7, y + SCORE_HEIGHT, 0).tex(0, 1).color(0x80, 0x80, 0x80, 255).endVertex();
				buffer.pos(getShownWidth() - 7, y + SCORE_HEIGHT, 0).tex(1, 1).color(0x80, 0x80, 0x80, 255).endVertex();
				buffer.pos(getShownWidth() - 7, y - 2, 0).tex(1, 0).color(0x80, 0x80, 0x80, 255).endVertex();
				buffer.pos(7, y - 2, 0).tex(0, 0).color(0x80, 0x80, 0x80, 255).endVertex();
				buffer.pos(8, y + SCORE_HEIGHT - 1, 0).tex(0, 1).color(0, 0, 0, 255).endVertex();
				buffer.pos(getShownWidth() - 8, y + SCORE_HEIGHT - 1, 0).tex(1, 1).color(0, 0, 0, 255).endVertex();
				buffer.pos(getShownWidth() - 8, y - 1, 0).tex(1, 0).color(0, 0, 0, 255).endVertex();
				buffer.pos(8, y - 1, 0).tex(0, 0).color(0, 0, 0, 255).endVertex();
				tessellator.draw();
				GlStateManager.enableTexture2D();
			}

			scoreToDraw.draw(y, mouseX, mouseY, partialTicks);
		}
	}

	@Override
	protected void drawForeground(int mouseX, int mouseY, float partialTicks) {
		String str = Translate.GUI_COMMANDEDITOR_SELECTSCORE_TITLE;
		fontRendererObj.drawString(str, width / 2 - fontRendererObj.getStringWidth(str) / 2,
				15 - fontRendererObj.FONT_HEIGHT / 2, 0xffffff);
	}

	private static enum EnumState {
		WAITING, TIMED_OUT, NO_PERMISSION, SHOWING_SCORES;
	}

	private static class EditableScore implements ICallback<IScoreCriteria> {
		private ScoreObjective originalScore;

		private GuiTextField nameTextField;
		private GuiTextField displayNameTextField;
		private IScoreCriteria scoreCriteria;
		private GuiButton scoreCriteriaButton;

		public EditableScore() {
			this(null);
		}

		public EditableScore(ScoreObjective original) {
			this.originalScore = original;
			nameTextField = new GuiTextField(0, Minecraft.getMinecraft().fontRendererObj, 80, 0, VIRTUAL_WIDTH / 2 - 70,
					20);
			nameTextField.setMaxStringLength(16);
			if (original != null) {
				nameTextField.setText(original.getName());
			}
			displayNameTextField = new GuiTextField(0, Minecraft.getMinecraft().fontRendererObj, 80, 0,
					VIRTUAL_WIDTH / 2 - 70, 20);
			displayNameTextField.setMaxStringLength(32);
			if (original != null) {
				displayNameTextField.setText(original.getDisplayName());
			}
			if (original == null) {
				scoreCriteria = IScoreCriteria.DUMMY;
			} else {
				scoreCriteria = original.getCriteria();
			}
			scoreCriteriaButton = new GuiButton(0, VIRTUAL_WIDTH / 2 + 20, 0, VIRTUAL_WIDTH / 2 - 20, 20,
					Translate.GUI_COMMANDEDITOR_SELECTSCORE_CHANGECRITERIA);
		}

		public void keyTyped(char typedChar, int keyCode) {
			nameTextField.textboxKeyTyped(typedChar, keyCode);
			displayNameTextField.textboxKeyTyped(typedChar, keyCode);
		}

		public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
			nameTextField.mouseClicked(mouseX, mouseY, mouseButton);
			displayNameTextField.mouseClicked(mouseX, mouseY, mouseButton);
			if (scoreCriteriaButton.mousePressed(Minecraft.getMinecraft(), mouseX, mouseY)) {
				Minecraft.getMinecraft()
						.displayGuiScreen(new GuiSelectScoreCriteria(Minecraft.getMinecraft().currentScreen, this));
				scoreCriteriaButton.playPressSound(Minecraft.getMinecraft().getSoundHandler());
			}
		}

		public void draw(int y, int mouseX, int mouseY, float partialTicks) {
			nameTextField.yPosition = y + 1;
			displayNameTextField.yPosition = y + SCORE_HEIGHT - 23;
			scoreCriteriaButton.yPosition = y + SCORE_HEIGHT - 22;

			Minecraft.getMinecraft().fontRendererObj.drawString(Translate.GUI_COMMANDEDITOR_SELECTSCORE_NAME, 10, y + 7,
					0xffffff);
			nameTextField.drawTextBox();
			Minecraft.getMinecraft().fontRendererObj.drawString(Translate.GUI_COMMANDEDITOR_SELECTSCORE_DISPLAYNAME, 10,
					y + SCORE_HEIGHT - 16, 0xffffff);
			displayNameTextField.drawTextBox();
			Minecraft.getMinecraft().fontRendererObj.drawString(
					Translate.GUI_COMMANDEDITOR_SELECTSCORE_CRITERIA + " "
							+ GuiSelectScoreCriteria.getDisplayName(scoreCriteria.getName()),
					VIRTUAL_WIDTH / 2 + 20, y + 7, 0xffffff);
			scoreCriteriaButton.drawButton(Minecraft.getMinecraft(), mouseX, mouseY);
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

		public boolean hasChanged() {
			if (originalScore == null) {
				return true;
			}
			if (!nameTextField.getText().equals(originalScore.getName())) {
				return true;
			}
			if (!displayNameTextField.getText().equals(originalScore.getDisplayName())) {
				return true;
			}
			if (scoreCriteria != originalScore.getCriteria()) {
				return true;
			}
			return false;
		}

		public void sendRemoveCommand() {
			if (originalScore == null) {
				return;
			}
			if (!Minecraft.getMinecraft().isIntegratedServerRunning()) {
				ChatBlocker.blockNextTranslation("commands.scoreboard.objectives.remove.success");
				ChatBlocker.executeCommand(String.format("/scoreboard objectives remove %s", originalScore.getName()));
			} else {
				Scoreboard scoreboard = Minecraft.getMinecraft().getIntegratedServer().worldServerForDimension(0)
						.getScoreboard();
				if (scoreboard.getObjective(originalScore.getName()) != null) {
					scoreboard.removeObjective(scoreboard.getObjective(originalScore.getName()));
				}
			}
		}

		public void sendAddCommand() {
			if (!Minecraft.getMinecraft().isIntegratedServerRunning()) {
				ChatBlocker.blockNextTranslation("commands.scoreboard.objectives.add.success");
				ChatBlocker.executeCommand(String.format("/scoreboard objectives add %s %s %s", nameTextField.getText(),
						scoreCriteria.getName(), displayNameTextField.getText()));
			} else {
				Scoreboard scoreboard = Minecraft.getMinecraft().getIntegratedServer().worldServerForDimension(0)
						.getScoreboard();
				if (scoreboard.getObjective(nameTextField.getText()) != null) {
					scoreboard.removeObjective(scoreboard.getObjective(nameTextField.getText()));
				}
				scoreboard.addScoreObjective(nameTextField.getText(), scoreCriteria)
						.setDisplayName(displayNameTextField.getText());
			}
		}

		public String getScoreName() {
			return nameTextField.getText();
		}

		@Override
		public IScoreCriteria getCallbackValue() {
			return scoreCriteria;
		}

		@Override
		public void setCallbackValue(IScoreCriteria value) {
			this.scoreCriteria = value;
		}
	}

}
