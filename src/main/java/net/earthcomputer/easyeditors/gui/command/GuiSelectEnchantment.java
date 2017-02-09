package net.earthcomputer.easyeditors.gui.command;

import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.lwjgl.input.Keyboard;

import com.google.common.collect.Lists;

import net.earthcomputer.easyeditors.util.Translate;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiSlot;
import net.minecraft.client.resources.I18n;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

/**
 * A GUI for selecting an enchantment
 * 
 * @author Earthcomputer
 *
 */
public class GuiSelectEnchantment extends GuiScreen {

	private GuiScreen previousScreen;
	private IEnchantmentSelectorCallback callback;

	private List<ResourceLocation> enchantments;
	private int selectedIndex = 0;

	private GuiButton cancelButton;
	private EnchantmentList list;

	public GuiSelectEnchantment(GuiScreen previousScreen, IEnchantmentSelectorCallback callback) {
		this.previousScreen = previousScreen;
		this.callback = callback;
		enchantments = Lists.newArrayList();
		for (ResourceLocation rl : Enchantment.REGISTRY.getKeys()) {
			enchantments.add(rl);
		}
		Collections.sort(enchantments, new Comparator<ResourceLocation>() {
			@Override
			public int compare(ResourceLocation first, ResourceLocation second) {
				return String.CASE_INSENSITIVE_ORDER.compare(first.toString(), second.toString());
			}
		});
		if (callback.getEnchantment() != null) {
			selectedIndex = enchantments.indexOf(callback.getEnchantment());
		}
	}

	@Override
	public void initGui() {
		Keyboard.enableRepeatEvents(true);
		buttonList.add(new GuiButton(0, width / 2 - 160, height - 15 - 10, 150, 20, I18n.format("gui.done")));
		buttonList.add(
				cancelButton = new GuiButton(1, width / 2 + 5, height - 15 - 10, 150, 20, I18n.format("gui.cancel")));
		list = new EnchantmentList();
	}

	@Override
	public void onGuiClosed() {
		Keyboard.enableRepeatEvents(false);
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		list.drawScreen(mouseX, mouseY, partialTicks);

		String str = Translate.GUI_COMMANDEDITOR_SELECTENCHANTMENT_TITLE;
		fontRendererObj.drawString(str, width / 2 - fontRendererObj.getStringWidth(str) / 2,
				15 - fontRendererObj.FONT_HEIGHT / 2, 0xffffff);

		super.drawScreen(mouseX, mouseY, partialTicks);
	}

	@Override
	public void actionPerformed(GuiButton button) {
		switch (button.id) {
		case 0:
			callback.setEnchantment(enchantments.get(selectedIndex));
			mc.displayGuiScreen(previousScreen);
			break;
		case 1:
			mc.displayGuiScreen(previousScreen);
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
		}
	}

	@Override
	public void handleMouseInput() throws IOException {
		super.handleMouseInput();
		list.handleMouseInput();
	}

	private class EnchantmentList extends GuiSlot {

		public EnchantmentList() {
			super(GuiSelectEnchantment.this.mc, GuiSelectEnchantment.this.width, GuiSelectEnchantment.this.height, 30,
					GuiSelectEnchantment.this.height - 30,
					GuiSelectEnchantment.this.fontRendererObj.FONT_HEIGHT * 2 + 8);
		}

		@Override
		protected int getSize() {
			return enchantments.size();
		}

		@Override
		protected void elementClicked(int slotIndex, boolean isDoubleClick, int mouseX, int mouseY) {
			selectedIndex = slotIndex;
		}

		@Override
		protected boolean isSelected(int slotIndex) {
			return slotIndex == selectedIndex;
		}

		@Override
		protected void drawBackground() {
			GuiSelectEnchantment.this.drawBackground(0);
		}

		@Override
		protected void drawSlot(int entryID, int x, int y, int height, int mouseX, int mouseY) {
			FontRenderer fontRenderer = GuiSelectEnchantment.this.fontRendererObj;
			ResourceLocation enchantmentName = enchantments.get(entryID);

			String str = I18n.format(ForgeRegistries.ENCHANTMENTS.getValue(enchantmentName).getName());
			fontRenderer.drawString(str, x + getListWidth() / 2 - fontRenderer.getStringWidth(str) / 2, y + 2,
					0xffffff);
			fontRenderer.drawString(enchantmentName.toString(),
					x + getListWidth() / 2 - fontRenderer.getStringWidth(enchantmentName.toString()) / 2,
					y + 4 + fontRenderer.FONT_HEIGHT, 0xc0c0c0);
		}

		@Override
		public int getListWidth() {
			return GuiSelectEnchantment.this.width - 12;
		}

		@Override
		public int getScrollBarX() {
			return GuiSelectEnchantment.this.width - 6;
		}

	}

}
