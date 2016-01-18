package net.earthcomputer.easyeditors.gui.command;

import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.lwjgl.input.Keyboard;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.earthcomputer.easyeditors.api.GeneralUtils;
import net.earthcomputer.easyeditors.gui.GuiTwoWayScroll;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.MathHelper;
import net.minecraftforge.fml.client.config.HoverChecker;

/**
 * A GUI which displays a list of all the items in the game for the user to
 * select one. A search feature is included
 * 
 * @author Earthcomputer
 *
 */
public class GuiItemSelector extends GuiTwoWayScroll {

	private static Map<String, ItemStack> allItems = Maps.newLinkedHashMap();
	private static LinkedHashMultimap<Object, Object> allItemsAndSubitems = LinkedHashMultimap.create();
	private static boolean hasInitializedItems = false;

	private List<ItemStack> shownItems = Lists.newArrayList();

	private static void initializeItems() {
		for (Object obj : Item.itemRegistry.getKeys()) {
			String name = String.valueOf(obj);
			Item item = Item.getByNameOrId(name);
			allItems.put(name, new ItemStack(item));
			List<ItemStack> subItems = Lists.newArrayList();
			item.getSubItems(item, null, subItems);
			allItemsAndSubitems.putAll(name, subItems);
		}
	}

	private GuiScreen previous;
	private IItemSelectorCallback callback;
	private boolean allowSubItems;

	private GuiButton doneButton;
	private GuiButton cancelButton;
	private String searchLabel;
	private GuiTextField searchText;

	private ItemStack hoveredItem;
	private ItemStack selectedItem;

	private HoverChecker hoverChecker;

	/**
	 * Creates a GuiItemSelector with the given callback, with allowSubItems set
	 * to true
	 * 
	 * @param previous
	 * @param callback
	 */
	public GuiItemSelector(GuiScreen previous, IItemSelectorCallback callback) {
		this(previous, callback, true);
	}

	/**
	 * Creates a GuiItemSelector with the given callback, with allowSubItems
	 * specified
	 * 
	 * @param previous
	 * @param callback
	 * @param allowSubItems
	 */
	public GuiItemSelector(GuiScreen previous, IItemSelectorCallback callback, boolean allowSubItems) {
		super(55, 30, 220, 1);
		if (!hasInitializedItems) {
			initializeItems();
			hasInitializedItems = true;
		}
		this.previous = previous;
		this.callback = callback;
		this.allowSubItems = allowSubItems;
		this.selectedItem = callback.getItem();
		setXScrollBarPolicy(SHOWN_NEVER);
		setUpKey(Keyboard.KEY_UP);
		setDownKey(Keyboard.KEY_DOWN);
	}

	@Override
	public void initGui() {
		Keyboard.enableRepeatEvents(true);

		setFilterText("");
		super.initGui();

		buttonList.add(
				doneButton = new GuiButton(0, width / 2 - 160, height - 15 - 10, 150, 20, I18n.format("gui.done")));
		buttonList.add(
				cancelButton = new GuiButton(1, width / 2 + 5, height - 15 - 10, 150, 20, I18n.format("gui.cancel")));
		searchLabel = I18n.format("gui.commandEditor.selectItem.search");
		int labelWidth = fontRendererObj.getStringWidth(searchLabel);
		searchText = new GuiTextField(0, fontRendererObj, width / 2 - (205 + labelWidth) / 2 + labelWidth + 5, 25, 200,
				20);
	}

	@Override
	public void onGuiClosed() {
		Keyboard.enableRepeatEvents(false);
	}

	@Override
	public void actionPerformed(GuiButton button) throws IOException {
		switch (button.id) {
		case 0:
			callback.setItem(selectedItem);
			mc.displayGuiScreen(previous);
			break;
		case 1:
			mc.displayGuiScreen(previous);
			break;
		default:
			super.actionPerformed(button);
		}
	}

	@Override
	public void keyTyped(char typedChar, int keyCode) throws IOException {
		if (keyCode == Keyboard.KEY_ESCAPE) {
			actionPerformed(cancelButton);
		} else if (searchText.isFocused()) {
			searchText.textboxKeyTyped(typedChar, keyCode);
			setFilterText(searchText.getText());
		} else {
			super.keyTyped(typedChar, keyCode);
		}
	}

	@Override
	public void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
		super.mouseClicked(mouseX, mouseY, mouseButton);
		searchText.mouseClicked(mouseX, mouseY, mouseButton);
	}

	@Override
	public void mouseClickedVirtual(int mouseX, int mouseY, int mouseButton) {
		if (mouseButton == 0) {
			if (mouseX >= 7 && mouseX < getShownWidth() - 7) {
				int clickedSlot = (mouseY + getScrollY() - getHeaderHeight()) / 18;
				if (clickedSlot >= shownItems.size())
					clickedSlot = -1;
				if (clickedSlot >= 0) {
					selectedItem = shownItems.get(clickedSlot);
				}
			}
		}
		return;
	}

	@Override
	public void drawForeground(int mouseX, int mouseY, float partialTicks) {
		String str = I18n.format("gui.commandEditor.selectItem.title");
		drawString(fontRendererObj, str, width / 2 - fontRendererObj.getStringWidth(str) / 2,
				15 - fontRendererObj.FONT_HEIGHT / 2, 0xffffff);
		drawString(fontRendererObj, searchLabel, width / 2 - (fontRendererObj.getStringWidth(searchLabel) + 205) / 2,
				30, 0xffffff);
		searchText.drawTextBox();
	}
	
	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		super.drawScreen(mouseX, mouseY, partialTicks);
		
		if (mouseY < getHeaderHeight() || mouseY >= height - getFooterHeight()) {
			hoveredItem = null;
		} else {
			int hoveredSlot = (mouseY + getScrollY() - getHeaderHeight()) / 18;
			if (hoveredSlot >= shownItems.size())
				hoveredSlot = -1;
			if (hoveredSlot < 0) {
				hoveredItem = null;
			} else {
				ItemStack stack = shownItems.get(hoveredSlot);
				int top = hoveredSlot * 18 + getHeaderHeight() - getScrollY();
				int bottom = top + 18;
				if (hoverChecker == null) {
					hoverChecker = new HoverChecker(top, bottom, 7, getShownWidth() - 7, 1000);
				} else {
					hoverChecker.updateBounds(top, bottom, 7, getShownWidth() - 7);
				}
				if (stack != hoveredItem) {
					hoveredItem = stack;
					hoverChecker.resetHoverTimer();
				}

				if (hoverChecker.checkHover(mouseX, mouseY)) {
					GeneralUtils.drawTooltip(mouseX, mouseY, stack.getTooltip(Minecraft.getMinecraft().thePlayer,
							Minecraft.getMinecraft().gameSettings.advancedItemTooltips));
				}
			}
		}
	}

	@Override
	public void drawVirtualScreen(int mouseX, int mouseY, float partialTicks, int scrollX, int scrollY,
			int headerHeight) {
		int top = scrollY / 18;
		int bottom = MathHelper.ceiling_float_int((float) (scrollY + getShownHeight()) / 18);
		if (bottom >= shownItems.size())
			bottom = shownItems.size() - 1;
		for (int i = top; i <= bottom; i++) {
			ItemStack stack = shownItems.get(i);
			int y = getHeaderHeight() + i * 18 - scrollY;
			if (ItemStack.areItemStacksEqual(stack, selectedItem)) {
				GlStateManager.color(1, 1, 1, 1);
				GlStateManager.disableTexture2D();
				Tessellator tessellator = Tessellator.getInstance();
				WorldRenderer worldRenderer = tessellator.getWorldRenderer();
				worldRenderer.startDrawingQuads();
				worldRenderer.setColorOpaque_I(0x808080);
				worldRenderer.addVertexWithUV(7, y + 18, 0, 0, 1);
				worldRenderer.addVertexWithUV(getShownWidth() - 7, y + 18, 0, 1, 1);
				worldRenderer.addVertexWithUV(getShownWidth() - 7, y - 2, 0, 1, 0);
				worldRenderer.addVertexWithUV(7, y - 2, 0, 0, 0);
				worldRenderer.setColorOpaque_I(0);
				worldRenderer.addVertexWithUV(8, y + 17, 0, 0, 1);
				worldRenderer.addVertexWithUV(getShownWidth() - 8, y + 17, 0, 1, 1);
				worldRenderer.addVertexWithUV(getShownWidth() - 8, y - 1, 0, 1, 0);
				worldRenderer.addVertexWithUV(8, y - 1, 0, 0, 0);
				tessellator.draw();
				GlStateManager.enableTexture2D();
			}

			RenderHelper.disableStandardItemLighting();
			RenderHelper.enableGUIStandardItemLighting();
			GlStateManager.translate(0, 0, 32);
			zLevel = 200;
			itemRender.zLevel = 200;
			itemRender.renderItemAndEffectIntoGUI(stack, 10, y);
			zLevel = 0;
			itemRender.zLevel = 0;
			RenderHelper.enableStandardItemLighting();

			GlStateManager.disableLighting();
			GlStateManager.disableFog();
			String displayName = stack.getDisplayName();
			drawString(fontRendererObj,
					displayName + EnumChatFormatting.GRAY + " " + Item.itemRegistry.getNameForObject(stack.getItem())
							+ " @ " + stack.getItemDamage() + EnumChatFormatting.RESET,
					28, y + 4, 0xffffff);
		}
	}

	private void setFilterText(String filterText) {
		filterText = filterText.toLowerCase();
		shownItems.clear();
		if (allowSubItems) {
			for (Map.Entry entry : allItemsAndSubitems.asMap().entrySet()) {
				String internalName = (String) entry.getKey();
				for (ItemStack stack : (Set<ItemStack>) entry.getValue()) {
					if (doesFilterTextMatch(filterText, stack, internalName)) {
						shownItems.add(stack);
					}
				}
			}
		} else {
			for (Map.Entry<String, ItemStack> entry : allItems.entrySet()) {
				String internalName = entry.getKey();
				ItemStack stack = entry.getValue();
				if (doesFilterTextMatch(filterText, stack, internalName))
					shownItems.add(stack);
			}
		}
		Collections.sort(shownItems, new Comparator<ItemStack>() {
			@Override
			public int compare(ItemStack first, ItemStack second) {
				return Integer.compare(Item.getIdFromItem(first.getItem()), Item.getIdFromItem(second.getItem()));
			}
		});
		setVirtualHeight(shownItems.size() * 18);
		setScrollY(0);
	}

	private boolean doesFilterTextMatch(final String filterText, ItemStack stack, String internalName) {
		return Iterables.any(stack.getTooltip(mc.thePlayer, true), new Predicate() {
			public boolean apply(String line) {
				return EnumChatFormatting.getTextWithoutFormattingCodes(line).toLowerCase().contains(filterText);
			}

			@Override
			public boolean apply(Object line) {
				return apply((String) line);
			}
		});
	}

}
