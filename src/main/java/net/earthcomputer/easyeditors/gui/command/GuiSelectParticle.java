package net.earthcomputer.easyeditors.gui.command;

import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.lwjgl.input.Keyboard;

import com.google.common.collect.Lists;

import net.earthcomputer.easyeditors.util.Translate;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiSlot;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.EnumParticleTypes;

public class GuiSelectParticle extends GuiScreen {

	private GuiScreen prevScreen;
	private IParticleSelectorCallback callback;

	private List<EnumParticleTypes> allParticles;
	private List<EnumParticleTypes> displayedParticles;
	private EnumParticleTypes selectedParticle;

	private GuiTextField searchTextField;
	private ParticleList list;
	private GuiButton cancelButton;

	public GuiSelectParticle(GuiScreen prevScreen, IParticleSelectorCallback callback) {
		this.prevScreen = prevScreen;
		this.callback = callback;

		allParticles = Lists.newArrayList(EnumParticleTypes.values());
		Collections.sort(allParticles, new Comparator<EnumParticleTypes>() {
			@Override
			public int compare(EnumParticleTypes first, EnumParticleTypes second) {
				return String.CASE_INSENSITIVE_ORDER.compare(first.getParticleName(), second.getParticleName());
			}
		});
		displayedParticles = Lists.newArrayList(allParticles);

		selectedParticle = callback.getParticle();
		if (selectedParticle == null) {
			selectedParticle = displayedParticles.get(0);
		}
	}

	@Override
	public void initGui() {
		Keyboard.enableRepeatEvents(true);
		addButton(new GuiButton(0, width / 2 - 160, height - 15 - 10, 150, 20, I18n.format("gui.done")));
		cancelButton = addButton(new GuiButton(1, width / 2 + 5, height - 15 - 10, 150, 20, I18n.format("gui.cancel")));

		list = new ParticleList();

		String searchLabel = Translate.GUI_COMMANDEDITOR_SEARCH;
		int labelWidth = fontRendererObj.getStringWidth(searchLabel);
		searchTextField = new GuiTextField(0, fontRendererObj, width / 2 - (205 + labelWidth) / 2 + labelWidth + 5, 25,
				200, 20);
	}

	@Override
	public void onGuiClosed() {
		Keyboard.enableRepeatEvents(false);
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		list.drawScreen(mouseX, mouseY, partialTicks);

		String str = Translate.GUI_COMMANDEDITOR_SELECTEFFECT_TITLE;
		fontRendererObj.drawString(str, width / 2 - fontRendererObj.getStringWidth(str) / 2,
				15 - fontRendererObj.FONT_HEIGHT / 2, 0xffffff);

		str = Translate.GUI_COMMANDEDITOR_SEARCH;
		drawString(fontRendererObj, str, width / 2 - (fontRendererObj.getStringWidth(str) + 205) / 2, 30, 0xffffff);
		searchTextField.drawTextBox();

		super.drawScreen(mouseX, mouseY, partialTicks);
	}

	@Override
	protected void actionPerformed(GuiButton button) throws IOException {
		switch (button.id) {
		case 0:
			callback.setParticle(selectedParticle);
			// FALLTHROUGH
		case 1:
			mc.displayGuiScreen(prevScreen);
			break;
		default:
			list.actionPerformed(button);
		}
	}

	@Override
	public void keyTyped(char typedChar, int keyCode) throws IOException {
		if (keyCode == Keyboard.KEY_ESCAPE) {
			actionPerformed(cancelButton);
		} else {
			super.keyTyped(typedChar, keyCode);
			if (searchTextField.textboxKeyTyped(typedChar, keyCode)) {
				filterSearch();
			}
		}
	}

	@Override
	public void handleMouseInput() throws IOException {
		super.handleMouseInput();
		list.handleMouseInput();
	}

	@Override
	protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
		super.mouseClicked(mouseX, mouseY, mouseButton);
		searchTextField.mouseClicked(mouseX, mouseY, mouseButton);
	}

	private void filterSearch() {
		displayedParticles.clear();
		displayedParticles.addAll(allParticles);

		String searchText = searchTextField.getText();
		searchText = searchText.trim().toLowerCase();

		Iterator<EnumParticleTypes> particleItr = displayedParticles.iterator();
		while (particleItr.hasNext()) {
			EnumParticleTypes particle = particleItr.next();

			String particleName = particle.getParticleName();
			if (particleName.toLowerCase().contains(searchText)) {
				continue;
			}
			if (I18n.format("gui.commandEditor.particle." + particleName + ".name").toLowerCase()
					.contains(searchText)) {
				continue;
			}

			particleItr.remove();
		}
	}

	private class ParticleList extends GuiSlot {

		public ParticleList() {
			super(GuiSelectParticle.this.mc, GuiSelectParticle.this.width, GuiSelectParticle.this.height, 55,
					GuiSelectParticle.this.height - 30, GuiSelectParticle.this.fontRendererObj.FONT_HEIGHT * 2 + 8);
		}

		@Override
		protected int getSize() {
			return displayedParticles.size();
		}

		@Override
		protected void elementClicked(int slotIndex, boolean isDoubleClick, int mouseX, int mouseY) {
			selectedParticle = displayedParticles.get(slotIndex);
		}

		@Override
		protected boolean isSelected(int slotIndex) {
			return displayedParticles.get(slotIndex) == selectedParticle;
		}

		@Override
		protected void drawBackground() {
			GuiSelectParticle.this.drawBackground(0);
		}

		@Override
		protected void drawSlot(int entryID, int x, int y, int height, int mouseX, int mouseY) {
			FontRenderer fontRenderer = GuiSelectParticle.this.fontRendererObj;
			EnumParticleTypes particle = displayedParticles.get(entryID);

			String particleName = particle.getParticleName();

			String str = I18n.format("gui.commandEditor.particle." + particleName + ".name");
			fontRenderer.drawString(str, x + getListWidth() / 2 - fontRenderer.getStringWidth(str) / 2, y + 2,
					0xffffff);
			fontRenderer.drawString(particleName,
					x + getListWidth() / 2 - fontRenderer.getStringWidth(particleName) / 2,
					y + 4 + fontRenderer.FONT_HEIGHT, 0xc0c0c0);
		}

		@Override
		public int getListWidth() {
			return GuiSelectParticle.this.width - 12;
		}

		@Override
		public int getScrollBarX() {
			return GuiSelectParticle.this.width - 6;
		}
	}

}
