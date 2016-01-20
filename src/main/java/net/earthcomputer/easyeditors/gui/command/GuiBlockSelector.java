package net.earthcomputer.easyeditors.gui.command;

import java.io.IOException;
import java.util.Arrays;
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

import net.earthcomputer.easyeditors.api.AnimatedBlockRenderer;
import net.earthcomputer.easyeditors.api.BlockPropertyRegistry;
import net.earthcomputer.easyeditors.api.GeneralUtils;
import net.earthcomputer.easyeditors.gui.GuiTwoWayScroll;
import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
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
public class GuiBlockSelector extends GuiTwoWayScroll {

	private static Map<String, IBlockState> allBlocks = Maps.newLinkedHashMap();
	private static LinkedHashMultimap<String, IBlockState> allBlocksAndSubBlocks = LinkedHashMultimap.create();
	private static Map<IBlockState, ItemStack> blockStateToItemStack = Maps.newHashMap();
	private static boolean hasInitializedBlocks = false;

	private List<IBlockState> shownBlocks = Lists.newArrayList();

	private static void initializeBlocks() {
		for (Object obj : Block.blockRegistry) {
			String name = String.valueOf(obj);
			Block block = Block.getBlockFromName(name);
			allBlocks.put(name, block.getDefaultState());
			allBlocksAndSubBlocks.putAll(name, getVariantStates(block.getDefaultState(),
					Arrays.asList(BlockPropertyRegistry.getVariantProperties(block))));
		}
	}

	private static List<IBlockState> getVariantStates(IBlockState stateSoFar, List<IProperty> remainingProperties) {
		if (remainingProperties.isEmpty())
			return Arrays.asList(stateSoFar);
		List<IBlockState> r = Lists.newArrayList();
		IProperty variedProperty = remainingProperties.get(0);
		remainingProperties = remainingProperties.subList(1, remainingProperties.size());
		for (Object allowedValue : variedProperty.getAllowedValues()) {
			r.addAll(getVariantStates(stateSoFar.withProperty(variedProperty, (Comparable<?>) allowedValue),
					remainingProperties));
		}
		return r;
	}

	private GuiScreen previous;
	private IBlockSelectorCallback callback;
	private boolean allowSubBlocks;

	private GuiButton doneButton;
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

		buttonList.add(
				doneButton = new GuiButton(0, width / 2 - 160, height - 15 - 10, 150, 20, I18n.format("gui.done")));
		buttonList.add(
				cancelButton = new GuiButton(1, width / 2 + 5, height - 15 - 10, 150, 20, I18n.format("gui.cancel")));
		searchLabel = I18n.format("gui.commandEditor.selectBlock.search");
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
		String str = I18n.format("gui.commandEditor.selectBlock.title");
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
		int bottom = MathHelper.ceiling_float_int((float) (scrollY + getShownHeight()) / 18);
		if (bottom >= shownBlocks.size())
			bottom = shownBlocks.size() - 1;
		for (int i = top; i <= bottom; i++) {
			IBlockState block = shownBlocks.get(i);
			int y = getHeaderHeight() + i * 18 - scrollY;
			if (block == selectedBlock) {
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

			new AnimatedBlockRenderer(block).render(10, y);

			GlStateManager.disableLighting();
			GlStateManager.disableFog();
			String displayName = getDisplayName(block);
			drawString(
					fontRendererObj, displayName + EnumChatFormatting.GRAY + " "
							+ Block.blockRegistry.getNameForObject(block.getBlock()) + EnumChatFormatting.RESET,
					28, y + 4, 0xffffff);
		}
	}

	private void setFilterText(String filterText) {
		filterText = filterText.toLowerCase();
		shownBlocks.clear();
		if (allowSubBlocks) {
			for (Map.Entry entry : allBlocksAndSubBlocks.asMap().entrySet()) {
				String internalName = (String) entry.getKey();
				for (IBlockState block : (Set<IBlockState>) entry.getValue()) {
					if (doesFilterTextMatch(filterText, block, internalName)) {
						shownBlocks.add(block);
					}
				}
			}
		} else {
			for (Map.Entry<String, IBlockState> entry : allBlocks.entrySet()) {
				String internalName = entry.getKey();
				IBlockState block = entry.getValue();
				if (doesFilterTextMatch(filterText, block, internalName))
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

	public static String getDisplayName(IBlockState block) {
		return getDisplayName(String.valueOf(Block.blockRegistry.getNameForObject(block.getBlock())));
	}

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
		for (Object obj : blockState.getProperties().entrySet()) {
			Map.Entry<IProperty, Comparable<?>> entry = (Map.Entry<IProperty, Comparable<?>>) obj;
			currentLine = EnumChatFormatting.BLUE + entry.getKey().getName() + EnumChatFormatting.RESET + ": ";
			String value = entry.getValue().toString();
			if (entry.getValue() == Boolean.TRUE)
				value = EnumChatFormatting.GREEN + value;
			else if (entry.getValue() == Boolean.FALSE)
				value = EnumChatFormatting.RED + value;
			else
				value = EnumChatFormatting.YELLOW + value;
			currentLine += value;
			otherLines.add(currentLine);
		}

		if (advanced) {
			otherLines.add(EnumChatFormatting.DARK_GRAY
					+ String.valueOf(Block.blockRegistry.getNameForObject(blockState.getBlock())));
		}

		if (!otherLines.isEmpty()) {
			tooltip.add("");
			tooltip.addAll(otherLines);
		}
		return tooltip;
	}

	private boolean doesFilterTextMatch(final String filterText, IBlockState block, String internalName) {
		return Iterables.any(getTooltip(block), new Predicate() {
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
