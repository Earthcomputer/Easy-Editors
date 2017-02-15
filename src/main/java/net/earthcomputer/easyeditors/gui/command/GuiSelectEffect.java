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
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

public class GuiSelectEffect extends GuiScreen {

	private GuiScreen prevScreen;
	private IEffectSelectorCallback callback;

	private List<ResourceLocation> allEffects;
	private List<ResourceLocation> displayedEffects;
	private ResourceLocation selectedEffect;

	private GuiTextField searchTextField;
	private EffectList list;
	private GuiButton cancelButton;

	public GuiSelectEffect(GuiScreen prevScreen, IEffectSelectorCallback callback) {
		this.prevScreen = prevScreen;
		this.callback = callback;

		allEffects = Lists.newArrayList(ForgeRegistries.POTIONS.getKeys());
		Collections.sort(allEffects, new Comparator<ResourceLocation>() {
			@Override
			public int compare(ResourceLocation first, ResourceLocation second) {
				return String.CASE_INSENSITIVE_ORDER.compare(String.valueOf(first), String.valueOf(second));
			}
		});
		displayedEffects = Lists.newArrayList(allEffects);

		selectedEffect = callback.getEffect();
		if (selectedEffect == null) {
			selectedEffect = displayedEffects.get(0);
		}
	}

	@Override
	public void initGui() {
		Keyboard.enableRepeatEvents(true);
		addButton(new GuiButton(0, width / 2 - 160, height - 15 - 10, 150, 20, I18n.format("gui.done")));
		cancelButton = addButton(new GuiButton(1, width / 2 + 5, height - 15 - 10, 150, 20, I18n.format("gui.cancel")));

		list = new EffectList();

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
			callback.setEffect(selectedEffect);
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
		displayedEffects.clear();
		displayedEffects.addAll(allEffects);

		String searchText = searchTextField.getText();
		searchText = searchText.trim().toLowerCase();

		Iterator<ResourceLocation> effectItr = displayedEffects.iterator();
		while (effectItr.hasNext()) {
			ResourceLocation effect = effectItr.next();

			String localizedName = I18n.format(ForgeRegistries.POTIONS.getValue(effect).getName());
			if (localizedName.toLowerCase().contains(searchText)) {
				continue;
			}
			if (String.valueOf(effect).toLowerCase().contains(searchText)) {
				continue;
			}

			effectItr.remove();
		}
	}

	private class EffectList extends GuiSlot {

		public EffectList() {
			super(GuiSelectEffect.this.mc, GuiSelectEffect.this.width, GuiSelectEffect.this.height, 55,
					GuiSelectEffect.this.height - 30, GuiSelectEffect.this.fontRendererObj.FONT_HEIGHT * 2 + 8);
		}

		@Override
		protected int getSize() {
			return displayedEffects.size();
		}

		@Override
		protected void elementClicked(int slotIndex, boolean isDoubleClick, int mouseX, int mouseY) {
			selectedEffect = displayedEffects.get(slotIndex);
		}

		@Override
		protected boolean isSelected(int slotIndex) {
			return displayedEffects.get(slotIndex).equals(selectedEffect);
		}

		@Override
		protected void drawBackground() {
			GuiSelectEffect.this.drawBackground(0);
		}

		@Override
		protected void drawSlot(int entryID, int x, int y, int height, int mouseX, int mouseY) {
			FontRenderer fontRenderer = GuiSelectEffect.this.fontRendererObj;
			ResourceLocation effectName = displayedEffects.get(entryID);

			String str = I18n.format(ForgeRegistries.POTIONS.getValue(effectName).getName());
			fontRenderer.drawString(str, x + getListWidth() / 2 - fontRenderer.getStringWidth(str) / 2, y + 2,
					0xffffff);
			fontRenderer.drawString(effectName.toString(),
					x + getListWidth() / 2 - fontRenderer.getStringWidth(effectName.toString()) / 2,
					y + 4 + fontRenderer.FONT_HEIGHT, 0xc0c0c0);
		}

		@Override
		public int getListWidth() {
			return GuiSelectEffect.this.width - 12;
		}

		@Override
		public int getScrollBarX() {
			return GuiSelectEffect.this.width - 6;
		}

	}

}
