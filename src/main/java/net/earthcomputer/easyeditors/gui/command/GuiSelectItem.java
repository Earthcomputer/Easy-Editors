package net.earthcomputer.easyeditors.gui.command;

import java.util.List;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import net.earthcomputer.easyeditors.gui.GuiSelectFromList;
import net.earthcomputer.easyeditors.gui.ICallback;
import net.earthcomputer.easyeditors.util.Translate;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

public class GuiSelectItem extends GuiSelectFromList<ItemStack> {

	private static List<ItemStack> createAllowedValues(boolean allowSubItems,
			Predicate<ItemStack> allowItemStackPredicate) {
		List<ItemStack> values = Lists.newArrayList();
		for (Item item : ForgeRegistries.ITEMS) {
			if (allowSubItems) {
				NonNullList<ItemStack> subItems = NonNullList.create();
				item.getSubItems(item, null, subItems);
				for (ItemStack subItem : subItems) {
					if (allowItemStackPredicate.apply(subItem)) {
						values.add(subItem);
					}
				}
			} else {
				ItemStack stackToAdd = new ItemStack(item);
				if (allowItemStackPredicate.apply(stackToAdd)) {
					values.add(stackToAdd);
				}
			}
		}
		return values;
	}

	public GuiSelectItem(GuiScreen prevScreen, ICallback<ItemStack> callback) {
		this(prevScreen, callback, true);
	}

	public GuiSelectItem(GuiScreen prevScreen, ICallback<ItemStack> callback, boolean allowSubItems) {
		this(prevScreen, callback, allowSubItems, Predicates.<ItemStack>alwaysTrue());
	}

	public GuiSelectItem(GuiScreen prevScreen, ICallback<ItemStack> callback, boolean allowSubItems,
			Predicate<ItemStack> allowItemStackPredicate) {
		super(prevScreen, callback, createAllowedValues(allowSubItems, allowItemStackPredicate),
				Translate.GUI_COMMANDEDITOR_SELECTITEM_TITLE, 18);
	}

	@Override
	protected boolean areEqual(ItemStack a, ItemStack b) {
		return ItemStack.areItemStacksEqual(a, b);
	}

	@Override
	protected List<String> getTooltip(ItemStack value) {
		return value.getTooltip(Minecraft.getMinecraft().player,
				Minecraft.getMinecraft().gameSettings.advancedItemTooltips);
	}

	@Override
	protected void drawSlot(int y, ItemStack value) {
		RenderHelper.disableStandardItemLighting();
		RenderHelper.enableGUIStandardItemLighting();
		GlStateManager.translate(0, 0, 32);
		zLevel = 200;
		itemRender.zLevel = 200;
		itemRender.renderItemAndEffectIntoGUI(value, 10, y);
		zLevel = 0;
		itemRender.zLevel = 0;
		RenderHelper.enableStandardItemLighting();

		GlStateManager.disableLighting();
		GlStateManager.disableFog();
		String displayName = value.getDisplayName();
		drawString(fontRendererObj, displayName + TextFormatting.GRAY + " " + value.getItem().delegate.name() + " @ "
				+ value.getItemDamage() + TextFormatting.RESET, 28, y + 4, 0xffffff);
	}

	@Override
	protected boolean doesSearchTextMatch(final String searchText, ItemStack value) {
		return Iterables.any(value.getTooltip(mc.player, true), new Predicate<String>() {
			@Override
			public boolean apply(String line) {
				return TextFormatting.getTextWithoutFormattingCodes(line).toLowerCase().contains(searchText);
			}
		});
	}

}
