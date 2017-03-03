package net.earthcomputer.easyeditors.gui;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.google.common.collect.Lists;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.IForgeRegistry;
import net.minecraftforge.fml.common.registry.IForgeRegistryEntry;

public abstract class GuiSelectFromRegistry<T extends IForgeRegistryEntry<T>>
		extends GuiSelectFromList<ResourceLocation> {

	private IForgeRegistry<T> registry;

	private static List<ResourceLocation> createAllowedValues(IForgeRegistry<?> registry) {
		List<ResourceLocation> values = Lists.newArrayList(registry.getKeys());
		Collections.sort(values, new Comparator<ResourceLocation>() {
			@Override
			public int compare(ResourceLocation first, ResourceLocation second) {
				return String.CASE_INSENSITIVE_ORDER.compare(first.toString(), second.toString());
			}
		});
		return values;
	}

	public GuiSelectFromRegistry(GuiScreen prevScreen, ICallback<ResourceLocation> callback, IForgeRegistry<T> registry,
			String title) {
		super(prevScreen, callback, createAllowedValues(registry), title);
		this.registry = registry;
	}

	public GuiSelectFromRegistry(GuiScreen prevScreen, ICallback<ResourceLocation> callback, IForgeRegistry<T> registry,
			String title, int slotHeight) {
		super(prevScreen, callback, createAllowedValues(registry), title, slotHeight);
		this.registry = registry;
	}

	@Override
	protected List<String> getTooltip(ResourceLocation value) {
		return Collections.emptyList();
	}

	@Override
	protected void drawSlot(int y, ResourceLocation value) {
		String str = getLocalizedName(registry.getValue(value));
		fontRendererObj.drawString(str, width / 2 - fontRendererObj.getStringWidth(str) / 2, y + 2, 0xffffff);
		str = value.toString();
		fontRendererObj.drawString(str, width / 2 - fontRendererObj.getStringWidth(str) / 2,
				y + 4 + fontRendererObj.FONT_HEIGHT, 0xc0c0c0);
	}

	@Override
	protected boolean doesSearchTextMatch(String searchText, ResourceLocation value) {
		String localizedName = getLocalizedName(registry.getValue(value));
		if (localizedName.toLowerCase().contains(searchText)) {
			return true;
		}
		if (String.valueOf(value).toLowerCase().contains(searchText)) {
			return true;
		}
		return false;
	}

	protected abstract String getLocalizedName(T value);

}
