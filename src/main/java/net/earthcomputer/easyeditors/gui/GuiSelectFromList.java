package net.earthcomputer.easyeditors.gui;

import java.io.IOException;
import java.util.List;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import com.google.common.collect.Lists;

import net.earthcomputer.easyeditors.api.util.GeneralUtils;
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
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.client.config.HoverChecker;

public abstract class GuiSelectFromList<T> extends GuiTwoWayScroll {

	private GuiScreen prevScreen;
	private ICallback<T> callback;

	private List<T> allValues;
	private List<T> displayedValues;
	private T selectedValue = null;
	private T hoveredValue = null;

	private GuiTextField searchTextField;
	private GuiButton cancelButton;
	private HoverChecker hoverChecker;

	private String title;
	private int slotHeight;

	public GuiSelectFromList(GuiScreen prevScreen, ICallback<T> callback, List<T> allowedValues, String title) {
		this(prevScreen, callback, allowedValues, title, Minecraft.getMinecraft().fontRendererObj.FONT_HEIGHT * 2 + 8);
	}

	public GuiSelectFromList(GuiScreen prevScreen, ICallback<T> callback, List<T> allowedValues, String title,
			int slotHeight) {
		super(55, 30, 220, 1);
		this.prevScreen = prevScreen;
		this.callback = callback;
		this.title = title;
		this.slotHeight = slotHeight;
		this.allValues = allowedValues;
		this.displayedValues = Lists.newArrayList(allowedValues);
		this.selectedValue = callback.getCallbackValue();
		if (selectedValue == null) {
			selectedValue = displayedValues.get(0);
		}

		setXScrollBarPolicy(SHOWN_NEVER);
		setUpKey(Keyboard.KEY_UP);
		setDownKey(Keyboard.KEY_DOWN);
	}

	@Override
	public void initGui() {
		Keyboard.enableRepeatEvents(true);

		setFilterText("");

		super.initGui();

		addButton(new GuiButton(0, width / 2 - 160, height - 15 - 10, 150, 20, I18n.format("gui.done")));
		cancelButton = addButton(new GuiButton(1, width / 2 + 5, height - 15 - 10, 150, 20, I18n.format("gui.cancel")));
		int labelWidth = fontRendererObj.getStringWidth(Translate.GUI_COMMANDEDITOR_SEARCH);
		searchTextField = new GuiTextField(0, fontRendererObj, width / 2 - (205 + labelWidth) / 2 + labelWidth + 5, 25,
				200, 20);
	}

	@Override
	public void onGuiClosed() {
		Keyboard.enableRepeatEvents(false);
	}

	@Override
	public void actionPerformed(GuiButton button) throws IOException {
		switch (button.id) {
		case 0:
			callback.setCallbackValue(selectedValue);
			// FALLTHROUGH
		case 1:
			mc.displayGuiScreen(prevScreen);
			break;
		default:
			super.actionPerformed(button);
		}
	}

	@Override
	public void keyTyped(char typedChar, int keyCode) throws IOException {
		if (keyCode == Keyboard.KEY_ESCAPE) {
			actionPerformed(cancelButton);
		} else if (searchTextField.isFocused()) {
			searchTextField.textboxKeyTyped(typedChar, keyCode);
			setFilterText(searchTextField.getText());
		} else {
			super.keyTyped(typedChar, keyCode);
		}
	}

	@Override
	public void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
		super.mouseClicked(mouseX, mouseY, mouseButton);
		searchTextField.mouseClicked(mouseX, mouseY, mouseButton);
	}

	@Override
	public void mouseClickedVirtual(int mouseX, int mouseY, int mouseButton) {
		if (mouseButton == 0) {
			if (mouseX >= 7 && mouseX < getShownWidth() - 7) {
				int clickedSlot = (mouseY + getScrollY() - getHeaderHeight()) / slotHeight;
				if (clickedSlot >= displayedValues.size())
					clickedSlot = -1;
				if (clickedSlot >= 0) {
					selectedValue = displayedValues.get(clickedSlot);
				}
			}
		}
	}

	@Override
	public void drawForeground(int mouseX, int mouseY, float partialTicks) {
		drawString(fontRendererObj, title, width / 2 - fontRendererObj.getStringWidth(title) / 2,
				15 - fontRendererObj.FONT_HEIGHT / 2, 0xffffff);
		drawString(fontRendererObj, Translate.GUI_COMMANDEDITOR_SEARCH,
				width / 2 - (fontRendererObj.getStringWidth(Translate.GUI_COMMANDEDITOR_SEARCH) + 205) / 2, 30,
				0xffffff);
		searchTextField.drawTextBox();
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		super.drawScreen(mouseX, mouseY, partialTicks);

		if (mouseY < getHeaderHeight() || mouseY >= height - getFooterHeight()) {
			hoveredValue = null;
		} else {
			int hoveredSlot = (mouseY + getScrollY() - getHeaderHeight()) / slotHeight;
			if (hoveredSlot >= displayedValues.size())
				hoveredSlot = -1;
			if (hoveredSlot < 0) {
				hoveredValue = null;
			} else {
				T mouseOver = displayedValues.get(hoveredSlot);
				int top = hoveredSlot * slotHeight + getHeaderHeight() - getScrollY();
				int bottom = top + slotHeight;
				if (hoverChecker == null) {
					hoverChecker = new HoverChecker(top, bottom, 7, getShownWidth() - 7, 1000);
				} else {
					hoverChecker.updateBounds(top, bottom, 7, getShownWidth() - 7);
				}
				if (mouseOver != hoveredValue) {
					hoveredValue = mouseOver;
					hoverChecker.resetHoverTimer();
				}

				if (hoverChecker.checkHover(mouseX, mouseY)) {
					List<String> tooltip = getTooltip(mouseOver);
					if (!tooltip.isEmpty()) {
						GeneralUtils.drawTooltip(mouseX, mouseY, tooltip);
					}
				}
			}
		}
	}

	@Override
	public void drawVirtualScreen(int mouseX, int mouseY, float partialTicks, int scrollX, int scrollY,
			int headerHeight) {
		int top = scrollY / slotHeight;
		int bottom = MathHelper.ceil((float) (scrollY + getShownHeight()) / slotHeight);
		if (bottom >= displayedValues.size())
			bottom = displayedValues.size() - 1;
		for (int i = top; i <= bottom; i++) {
			T valueToDraw = displayedValues.get(i);
			int y = getHeaderHeight() + i * slotHeight - scrollY;
			if (areEqual(valueToDraw, selectedValue)) {
				GlStateManager.color(1, 1, 1, 1);
				GlStateManager.disableTexture2D();
				Tessellator tessellator = Tessellator.getInstance();
				VertexBuffer buffer = tessellator.getBuffer();
				buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
				buffer.pos(7, y + slotHeight, 0).tex(0, 1).color(0x80, 0x80, 0x80, 255).endVertex();
				buffer.pos(getShownWidth() - 7, y + slotHeight, 0).tex(1, 1).color(0x80, 0x80, 0x80, 255).endVertex();
				buffer.pos(getShownWidth() - 7, y - 2, 0).tex(1, 0).color(0x80, 0x80, 0x80, 255).endVertex();
				buffer.pos(7, y - 2, 0).tex(0, 0).color(0x80, 0x80, 0x80, 255).endVertex();
				buffer.pos(8, y + slotHeight - 1, 0).tex(0, 1).color(0, 0, 0, 255).endVertex();
				buffer.pos(getShownWidth() - 8, y + slotHeight - 1, 0).tex(1, 1).color(0, 0, 0, 255).endVertex();
				buffer.pos(getShownWidth() - 8, y - 1, 0).tex(1, 0).color(0, 0, 0, 255).endVertex();
				buffer.pos(8, y - 1, 0).tex(0, 0).color(0, 0, 0, 255).endVertex();
				tessellator.draw();
				GlStateManager.enableTexture2D();
			}

			drawSlot(y, valueToDraw);
		}
	}

	public void setFilterText(String filterText) {
		filterText = filterText.toLowerCase().trim();
		displayedValues.clear();
		for (T value : allValues) {
			if (doesSearchTextMatch(filterText, value)) {
				displayedValues.add(value);
			}
		}
		setVirtualHeight(displayedValues.size() * slotHeight);
		setScrollY(0);
	}

	protected boolean areEqual(T a, T b) {
		return a.equals(b);
	}

	protected abstract List<String> getTooltip(T value);

	protected abstract void drawSlot(int y, T value);

	protected abstract boolean doesSearchTextMatch(String searchText, T value);

}
