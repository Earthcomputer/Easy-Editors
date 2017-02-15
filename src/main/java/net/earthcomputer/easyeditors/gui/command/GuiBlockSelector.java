package net.earthcomputer.easyeditors.gui.command;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.earthcomputer.easyeditors.api.BlockPropertyRegistry;
import net.earthcomputer.easyeditors.api.util.AnimatedBlockRenderer;
import net.earthcomputer.easyeditors.api.util.GeneralUtils;
import net.earthcomputer.easyeditors.gui.GuiTwoWayScroll;
import net.earthcomputer.easyeditors.util.Translate;
import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.VertexBuffer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.client.config.HoverChecker;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

/**
 * A GUI which displays a list of all the blocks in the game for the user to
 * select one. A search feature is included
 * 
 * @author Earthcomputer
 *
 */
public class GuiBlockSelector extends GuiTwoWayScroll {

	private static Map<ResourceLocation, IBlockState> allBlocks = Maps.newLinkedHashMap();
	private static LinkedHashMultimap<ResourceLocation, IBlockState> allBlocksAndSubBlocks = LinkedHashMultimap
			.create();
	private static boolean hasInitializedBlocks = false;

	private List<IBlockState> shownBlocks = Lists.newArrayList();

	private static void initializeBlocks() {
		for (Block block : ForgeRegistries.BLOCKS) {
			ResourceLocation name = block.delegate.name();
			allBlocks.put(name, block.getDefaultState());
			allBlocksAndSubBlocks.putAll(name, getVariantStates(block.getDefaultState(),
					Arrays.asList(BlockPropertyRegistry.getVariantProperties(block))));
		}
	}

	private static List<IBlockState> getVariantStates(IBlockState stateSoFar,
			List<IProperty<? extends Comparable<?>>> remainingProperties) {
		if (remainingProperties.isEmpty())
			return Arrays.asList(stateSoFar);
		List<IBlockState> r = Lists.newArrayList();
		IProperty<? extends Comparable<?>> variedProperty = remainingProperties.get(0);
		remainingProperties = remainingProperties.subList(1, remainingProperties.size());
		for (Object allowedValue : variedProperty.getAllowedValues()) {
			r.addAll(getVariantStates(stateWithProperty(stateSoFar, variedProperty, (Comparable<?>) allowedValue),
					remainingProperties));
		}
		return r;
	}

	@SuppressWarnings("unchecked")
	private static <T extends Comparable<T>, V extends T> IBlockState stateWithProperty(IBlockState state,
			IProperty<? extends Comparable<?>> prop, Comparable<?> value) {
		return state.withProperty((IProperty<T>) prop, (V) value);
	}

	private GuiScreen previous;
	private IBlockSelectorCallback callback;
	private boolean allowSubBlocks;

	private GuiButton cancelButton;
	private String searchLabel;
	private GuiTextField searchText;

	private IBlockState hoveredItem;
	private IBlockState selectedBlock;

	private HoverChecker hoverChecker;

	/**
	 * Creates a GuiBlockSelector with the given callback, with allowSubBlocks
	 * set to true
	 * 
	 * @param previous
	 * @param callback
	 */
	public GuiBlockSelector(GuiScreen previous, IBlockSelectorCallback callback) {
		this(previous, callback, true);
	}

	/**
	 * Creates a GuiBlockSelector with the given callback, with allowSubBlocks
	 * specified
	 * 
	 * @param previous
	 * @param callback
	 * @param allowSubBlocks
	 */
	public GuiBlockSelector(GuiScreen previous, IBlockSelectorCallback callback, boolean allowSubBlocks) {
		super(55, 30, 220, 1);
		if (!hasInitializedBlocks) {
			initializeBlocks();
			hasInitializedBlocks = true;
		}
		this.previous = previous;
		this.callback = callback;
		this.allowSubBlocks = allowSubBlocks;
		this.selectedBlock = callback.getBlock();
		setXScrollBarPolicy(SHOWN_NEVER);
		setUpKey(Keyboard.KEY_UP);
		setDownKey(Keyboard.KEY_DOWN);
	}

	@Override
	public void initGui() {
		Keyboard.enableRepeatEvents(true);

		setFilterText("");
		super.initGui();

		buttonList.add(new GuiButton(0, width / 2 - 160, height - 15 - 10, 150, 20, I18n.format("gui.done")));
		buttonList.add(
				cancelButton = new GuiButton(1, width / 2 + 5, height - 15 - 10, 150, 20, I18n.format("gui.cancel")));
		searchLabel = Translate.GUI_COMMANDEDITOR_SELECTBLOCK_SEARCH;
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
			callback.setBlock(selectedBlock);
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
				if (clickedSlot >= shownBlocks.size())
					clickedSlot = -1;
				if (clickedSlot >= 0) {
					selectedBlock = shownBlocks.get(clickedSlot);
				}
			}
		}
		return;
	}

	@Override
	public void drawForeground(int mouseX, int mouseY, float partialTicks) {
		String str = Translate.GUI_COMMANDEDITOR_SELECTBLOCK_TITLE;
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
			if (hoveredSlot >= shownBlocks.size())
				hoveredSlot = -1;
			if (hoveredSlot < 0) {
				hoveredItem = null;
			} else {
				IBlockState block = shownBlocks.get(hoveredSlot);
				int top = hoveredSlot * 18 + getHeaderHeight() - getScrollY();
				int bottom = top + 18;
				if (hoverChecker == null) {
					hoverChecker = new HoverChecker(top, bottom, 7, getShownWidth() - 7, 1000);
				} else {
					hoverChecker.updateBounds(top, bottom, 7, getShownWidth() - 7);
				}
				if (block != hoveredItem) {
					hoveredItem = block;
					hoverChecker.resetHoverTimer();
				}

				if (hoverChecker.checkHover(mouseX, mouseY)) {
					GeneralUtils.drawTooltip(mouseX, mouseY, getTooltip(block));
				}
			}
		}
	}

	@Override
	public void drawVirtualScreen(int mouseX, int mouseY, float partialTicks, int scrollX, int scrollY,
			int headerHeight) {
		int top = scrollY / 18;
		int bottom = MathHelper.ceil((float) (scrollY + getShownHeight()) / 18);
		if (bottom >= shownBlocks.size())
			bottom = shownBlocks.size() - 1;
		for (int i = top; i <= bottom; i++) {
			IBlockState block = shownBlocks.get(i);
			int y = getHeaderHeight() + i * 18 - scrollY;
			if (block == selectedBlock) {
				GlStateManager.color(1, 1, 1, 1);
				GlStateManager.disableTexture2D();
				Tessellator tessellator = Tessellator.getInstance();
				VertexBuffer buffer = tessellator.getBuffer();
				buffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX_COLOR);
				buffer.pos(7, y + 18, 0).tex(0, 1).color(0x80, 0x80, 0x80, 255).endVertex();
				buffer.pos(getShownWidth() - 7, y + 18, 0).tex(1, 1).color(0x80, 0x80, 0x80, 255).endVertex();
				buffer.pos(getShownWidth() - 7, y - 2, 0).tex(1, 0).color(0x80, 0x80, 0x80, 255).endVertex();
				buffer.pos(7, y - 2, 0).tex(0, 0).color(0x80, 0x80, 0x80, 255).endVertex();
				buffer.pos(8, y + 17, 0).tex(0, 1).color(0, 0, 0, 255).endVertex();
				buffer.pos(getShownWidth() - 8, y + 17, 0).tex(1, 1).color(0, 0, 0, 255).endVertex();
				buffer.pos(getShownWidth() - 8, y - 1, 0).tex(1, 0).color(0, 0, 0, 255).endVertex();
				buffer.pos(8, y - 1, 0).tex(0, 0).color(0, 0, 0, 255).endVertex();
				tessellator.draw();
				GlStateManager.enableTexture2D();
			}

			new AnimatedBlockRenderer(block).render(10, y);

			GlStateManager.disableLighting();
			GlStateManager.disableFog();
			String displayName = getDisplayName(block);
			drawString(fontRendererObj,
					displayName + TextFormatting.GRAY + " " + block.getBlock().delegate.name() + TextFormatting.RESET,
					28, y + 4, 0xffffff);
		}
	}

	private void setFilterText(String filterText) {
		filterText = filterText.toLowerCase();
		shownBlocks.clear();
		if (allowSubBlocks) {
			for (Map.Entry<ResourceLocation, Collection<IBlockState>> entry : allBlocksAndSubBlocks.asMap()
					.entrySet()) {
				for (IBlockState block : entry.getValue()) {
					if (doesFilterTextMatch(filterText, block)) {
						shownBlocks.add(block);
					}
				}
			}
		} else {
			for (Map.Entry<ResourceLocation, IBlockState> entry : allBlocks.entrySet()) {
				IBlockState block = entry.getValue();
				if (doesFilterTextMatch(filterText, block))
					shownBlocks.add(block);
			}
		}
		Collections.sort(shownBlocks, new Comparator<IBlockState>() {
			@Override
			public int compare(IBlockState first, IBlockState second) {
				return Integer.compare(Block.getIdFromBlock(first.getBlock()), Block.getIdFromBlock(second.getBlock()));
			}
		});
		setVirtualHeight(shownBlocks.size() * 18);
		setScrollY(0);
	}

	/**
	 * Finds the display name for the given block state
	 * 
	 * @param block
	 * @return
	 */
	public static String getDisplayName(IBlockState block) {
		return getDisplayName(String.valueOf(block.getBlock().delegate.name()));
	}

	/**
	 * Finds the display name from the given block name (e.g.
	 * <code>"minecraft:stone"</code>)
	 * 
	 * @param blockName
	 * @return
	 */
	public static String getDisplayName(String blockName) {
		blockName = blockName.replace('_', ' ').trim().replaceAll("\\s+", " ");

		// Remove the namespace
		int colonIndex = blockName.indexOf(':');
		if (colonIndex != -1 && colonIndex != blockName.length() - 1)
			blockName = blockName.substring(colonIndex + 1);

		// Some mods prefix their block names with their modid then a dot
		int dotIndex = blockName.lastIndexOf('.');
		if (dotIndex != -1 && dotIndex != blockName.length() - 1)
			blockName = blockName.substring(dotIndex + 1);

		// Split camel case into separate words
		StringBuilder blockNameBuilder = new StringBuilder(blockName);
		for (int i = blockNameBuilder.length() - 1; i >= 1; i--) {
			if (Character.isLowerCase(blockNameBuilder.charAt(i - 1))
					&& Character.isUpperCase(blockNameBuilder.charAt(i)))
				blockNameBuilder.insert(i, ' ');
		}

		// Capitalize the first letter of each word
		for (int i = 1; i < blockNameBuilder.length(); i++) {
			char lastChar = blockNameBuilder.charAt(i - 1);
			if (lastChar == ' ' || lastChar == '-' || lastChar == '\'' || lastChar == '"')
				blockNameBuilder.setCharAt(i, Character.toUpperCase(blockNameBuilder.charAt(i)));
		}

		return blockNameBuilder.toString();
	}

	/**
	 * Returns a list of strings to display in a tooltip describing a block
	 * state
	 * 
	 * @param blockState
	 * @return
	 */
	public static List<String> getTooltip(IBlockState blockState) {
		List<String> tooltip = Lists.newArrayList();

		boolean advanced = Minecraft.getMinecraft().gameSettings.advancedItemTooltips;

		String currentLine = getDisplayName(blockState);
		if (advanced) {
			String closingBracket = "";
			if (currentLine.length() > 0) {
				currentLine += " (";
				closingBracket = ")";
			}
			int id = Block.getIdFromBlock(blockState.getBlock());
			if (!blockState.getProperties().isEmpty()) {
				currentLine += String.format("#%04d/%d%s", id, blockState.getBlock().getMetaFromState(blockState),
						closingBracket);
			} else {
				currentLine += String.format("#%04d%s", id, closingBracket);
			}
		}
		tooltip.add(currentLine);

		List<String> otherLines = Lists.newArrayList();
		Iterator<IProperty<?>> iterator1 = blockState.getProperties().keySet().iterator();
		Iterator<IProperty<? extends Comparable<?>>> iterator2 = Iterators
				.forArray(BlockPropertyRegistry.getVariantProperties(blockState.getBlock()));
		while (advanced ? iterator1.hasNext() : iterator2.hasNext()) {
			IProperty<?> prop;
			if (advanced)
				prop = iterator1.next();
			else
				prop = iterator2.next();
			currentLine = TextFormatting.BLUE + prop.getName() + TextFormatting.RESET + ": ";
			Comparable<?> value = blockState.getValue(prop);
			String valueStr = value.toString();
			if (value == Boolean.TRUE)
				valueStr = TextFormatting.GREEN + valueStr;
			else if (value == Boolean.FALSE)
				valueStr = TextFormatting.RED + valueStr;
			else
				valueStr = TextFormatting.YELLOW + valueStr;
			currentLine += valueStr;
			otherLines.add(currentLine);
		}

		if (advanced) {
			otherLines.add(TextFormatting.DARK_GRAY + String.valueOf(blockState.getBlock().delegate.name()));
		}

		if (!otherLines.isEmpty()) {
			tooltip.add("");
			tooltip.addAll(otherLines);
		}
		return tooltip;
	}

	private boolean doesFilterTextMatch(final String filterText, IBlockState block) {
		return Iterables.any(getTooltip(block), new Predicate<String>() {
			@Override
			public boolean apply(String line) {
				return TextFormatting.getTextWithoutFormattingCodes(line).toLowerCase().contains(filterText);
			}
		});
	}

}
