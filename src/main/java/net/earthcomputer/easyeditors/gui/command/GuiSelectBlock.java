package net.earthcomputer.easyeditors.gui.command;

import java.util.Iterator;
import java.util.List;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import net.earthcomputer.easyeditors.api.BlockPropertyRegistry;
import net.earthcomputer.easyeditors.api.SmartTranslationRegistry;
import net.earthcomputer.easyeditors.api.util.AnimatedBlockRenderer;
import net.earthcomputer.easyeditors.gui.GuiSelectFromList;
import net.earthcomputer.easyeditors.gui.ICallback;
import net.earthcomputer.easyeditors.util.Translate;
import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

public class GuiSelectBlock extends GuiSelectFromList<IBlockState> {

	private static List<IBlockState> createAllowedValues(boolean allowSubBlocks,
			Predicate<IBlockState> allowBlockStatePredicate) {
		List<IBlockState> blocks = Lists.newArrayList();
		for (Block block : ForgeRegistries.BLOCKS) {
			if (allowSubBlocks) {
				List<IBlockState> variantStates = getAllVariantStates(block,
						BlockPropertyRegistry.getVariantProperties(block));
				for (IBlockState variantState : variantStates) {
					if (allowBlockStatePredicate.apply(variantState)) {
						blocks.add(variantState);
					}
				}
			} else {
				IBlockState defaultState = block.getDefaultState();
				if (allowBlockStatePredicate.apply(defaultState)) {
					blocks.add(defaultState);
				}
			}
		}
		return blocks;
	}

	private static List<IBlockState> getAllVariantStates(Block block, List<IProperty<?>> variantProperties) {
		IBlockState defaultState = block.getDefaultState();
		List<IBlockState> variantStates = Lists.newArrayList(defaultState);
		for (IProperty<?> variantProperty : variantProperties) {
			for (int i = 0, e = variantStates.size(); i < e; i++) {
				IBlockState state = variantStates.get(i);
				for (Comparable<?> allowedValue : variantProperty.getAllowedValues()) {
					if (!allowedValue.equals(defaultState.getValue(variantProperty))) {
						variantStates.add(stateWithProperty(state, variantProperty, allowedValue));
					}
				}
			}
		}
		return variantStates;
	}

	@SuppressWarnings("unchecked")
	private static <T extends Comparable<T>, V extends T> IBlockState stateWithProperty(IBlockState state,
			IProperty<?> prop, Comparable<?> value) {
		return state.withProperty((IProperty<T>) prop, (V) value);
	}

	public GuiSelectBlock(GuiScreen prevScreen, ICallback<IBlockState> callback, boolean allowSubBlocks) {
		this(prevScreen, callback, allowSubBlocks, Predicates.<IBlockState>alwaysTrue());
	}

	public GuiSelectBlock(GuiScreen prevScreen, ICallback<IBlockState> callback, boolean allowSubBlocks,
			Predicate<IBlockState> allowBlockStatePredicate) {
		super(prevScreen, callback, createAllowedValues(allowSubBlocks, allowBlockStatePredicate),
				Translate.GUI_COMMANDEDITOR_SELECTBLOCK_TITLE, 18);
	}

	@Override
	protected List<String> getTooltip(IBlockState value) {
		return getBlockStateTooltip(value);
	}

	@Override
	protected void drawSlot(int y, IBlockState value) {
		new AnimatedBlockRenderer(value).render(10, y);

		GlStateManager.disableLighting();
		GlStateManager.disableFog();
		String displayName = getDisplayName(value);
		drawString(fontRendererObj,
				displayName + TextFormatting.GRAY + " " + value.getBlock().delegate.name() + TextFormatting.RESET, 28,
				y + 4, 0xffffff);
	}

	@Override
	protected boolean doesSearchTextMatch(final String searchText, IBlockState value) {
		return Iterables.any(getTooltip(value), new Predicate<String>() {
			@Override
			public boolean apply(String line) {
				return TextFormatting.getTextWithoutFormattingCodes(line).toLowerCase().contains(searchText);
			}
		});
	}

	/**
	 * Returns a list of strings to display in a tooltip describing a block
	 * state
	 * 
	 * @param blockState
	 * @return
	 */
	public static List<String> getBlockStateTooltip(IBlockState blockState) {
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
		Iterator<IProperty<?>> iterator2 = BlockPropertyRegistry.getVariantProperties(blockState.getBlock()).iterator();
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

	/**
	 * Finds the display name for the given block state
	 * 
	 * @param block
	 * @return
	 */
	public static String getDisplayName(IBlockState block) {
		try {
			ItemStack item = block.getBlock().getPickBlock(block,
					new RayTraceResult(new Vec3d(0.5, 1, 0.5), EnumFacing.UP), Minecraft.getMinecraft().world,
					BlockPos.ORIGIN, Minecraft.getMinecraft().player);
			if (!item.isEmpty()) {
				String unlocalizedName = item.getUnlocalizedName() + ".name";
				if (SmartTranslationRegistry.getLanguageMapInstance().isKeyTranslated(unlocalizedName)) {
					return I18n.format(unlocalizedName);
				}
			}
		} catch (Throwable t) {
			// ignore
		}
		String unlocalizedName = block.getBlock().getUnlocalizedName() + ".name";
		if (SmartTranslationRegistry.getLanguageMapInstance().isKeyTranslated(unlocalizedName)) {
			return I18n.format(unlocalizedName);
		}
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
		blockNameBuilder.setCharAt(0, Character.toUpperCase(blockNameBuilder.charAt(0)));
		for (int i = 1; i < blockNameBuilder.length(); i++) {
			char lastChar = blockNameBuilder.charAt(i - 1);
			if (lastChar == ' ' || lastChar == '-' || lastChar == '\'' || lastChar == '"')
				blockNameBuilder.setCharAt(i, Character.toUpperCase(blockNameBuilder.charAt(i)));
		}

		return blockNameBuilder.toString();
	}

}
