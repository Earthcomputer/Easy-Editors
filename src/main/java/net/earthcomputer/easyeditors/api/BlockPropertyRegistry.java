package net.earthcomputer.easyeditors.api;

import java.util.List;
import java.util.Map;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.minecraft.block.Block;
import net.minecraft.block.BlockAnvil;
import net.minecraft.block.BlockCarpet;
import net.minecraft.block.BlockColored;
import net.minecraft.block.BlockDirt;
import net.minecraft.block.BlockDoublePlant;
import net.minecraft.block.BlockNewLeaf;
import net.minecraft.block.BlockNewLog;
import net.minecraft.block.BlockOldLeaf;
import net.minecraft.block.BlockOldLog;
import net.minecraft.block.BlockPistonExtension;
import net.minecraft.block.BlockPistonMoving;
import net.minecraft.block.BlockPlanks;
import net.minecraft.block.BlockPrismarine;
import net.minecraft.block.BlockQuartz;
import net.minecraft.block.BlockRedSandstone;
import net.minecraft.block.BlockRedstoneComparator;
import net.minecraft.block.BlockSand;
import net.minecraft.block.BlockSandStone;
import net.minecraft.block.BlockSapling;
import net.minecraft.block.BlockStainedGlass;
import net.minecraft.block.BlockStainedGlassPane;
import net.minecraft.block.BlockStone;
import net.minecraft.block.BlockStoneSlab;
import net.minecraft.block.BlockStoneSlabNew;
import net.minecraft.block.BlockTallGrass;
import net.minecraft.block.BlockWoodSlab;
import net.minecraft.block.properties.IProperty;
import net.minecraft.init.Blocks;

/**
 * Use this class to register stuff about the possible block states of your
 * custom block.
 * 
 * <b>This class is a member of the Easy Editors API</b>
 * 
 * @author Earthcomputer
 *
 */
public class BlockPropertyRegistry {

	private static Map<Predicate<Block>, IProperty<? extends Comparable<?>>> variants = Maps.newHashMap();

	static {
		registerVanillaVariantProps();
	}

	private static void registerVanillaVariantProps() {
		registerVariantProperty(BlockStone.VARIANT);
		registerVariantProperty(BlockPlanks.VARIANT);
		registerVariantProperty(BlockSapling.TYPE);
		registerVariantProperty(BlockDirt.VARIANT);
		registerVariantProperty(BlockSand.VARIANT);
		registerVariantProperty(BlockOldLog.VARIANT);
		registerVariantProperty(BlockNewLog.VARIANT);
		registerVariantProperty(BlockOldLeaf.VARIANT);
		registerVariantProperty(BlockNewLeaf.VARIANT);
		registerVariantProperty(BlockSandStone.TYPE);
		registerVariantProperty(BlockTallGrass.TYPE);
		registerVariantProperty(BlockPistonExtension.TYPE);
		registerVariantProperty(BlockColored.COLOR);
		registerVariantProperty(BlockPistonMoving.TYPE);
		registerVariantProperty(Blocks.YELLOW_FLOWER.getTypeProperty());
		registerVariantProperty(Blocks.RED_FLOWER.getTypeProperty());
		registerVariantProperty(BlockStoneSlab.VARIANT);
		registerVariantProperty(BlockWoodSlab.VARIANT);
		registerVariantProperty(BlockAnvil.DAMAGE);
		registerVariantProperty(BlockRedstoneComparator.POWERED);
		registerVariantProperty(BlockQuartz.VARIANT);
		registerVariantProperty(BlockCarpet.COLOR);
		registerVariantProperty(BlockDoublePlant.VARIANT);
		registerVariantProperty(BlockStainedGlass.COLOR);
		registerVariantProperty(BlockStainedGlassPane.COLOR);
		registerVariantProperty(BlockPrismarine.VARIANT);
		registerVariantProperty(BlockRedSandstone.TYPE);
		registerVariantProperty(BlockStoneSlabNew.VARIANT);
	}

	/**
	 * Registers an IProperty to be counted as a variant property. More about
	 * variant properties {@link #registerVariantProperty(Predicate, IProperty)
	 * here}
	 * 
	 * @param property
	 */
	@SuppressWarnings("unchecked")
	public static <T extends Comparable<T>> void registerVariantProperty(IProperty<T> property) {
		registerVariantProperty((Predicate<Block>) (Predicate<?>) Predicates.alwaysTrue(), property);
	}

	/**
	 * Registers an IProperty to be counted as a variant property, but only when
	 * the block state is of the specified block. More about variant properties
	 * {@link #registerVariantProperty(Predicate, IProperty) here}
	 * 
	 * @param block
	 * @param property
	 */
	public static <T extends Comparable<T>> void registerVariantProperty(Block block, IProperty<T> property) {
		registerVariantProperty(Predicates.equalTo(block), property);
	}

	/**
	 * Registers an IProperty to be counted as a variant property, but only when
	 * the block is allowed by the predicate. When a variant property has
	 * different values, they will effectively produce separate blocks from an
	 * inexperienced user's point of view. They will be listed as separate
	 * blocks in the select-blocks screen and the property will not be included
	 * in other ways of editing block states. These will also be the only
	 * properties listed in the tooltip of a block state when advanced item
	 * tooltips is off. See {@link #registerVanillaVariantProps() this method}
	 * for a list of all the vanilla variant properties
	 * 
	 * @param blockPredicate
	 * @param property
	 */
	public static <T extends Comparable<T>> void registerVariantProperty(Predicate<Block> blockPredicate,
			IProperty<T> property) {
		variants.put(blockPredicate, property);
	}

	/**
	 * Returns whether property is a variant property when in a block state of
	 * the given block
	 * 
	 * @param block
	 * @param property
	 * @return
	 */
	public static <T extends Comparable<T>> boolean isVariantProperty(Block block, IProperty<T> property) {
		for (Map.Entry<Predicate<Block>, IProperty<? extends Comparable<?>>> entry : variants.entrySet()) {
			if (entry.getKey().apply(block)) {
				if (entry.getValue().equals(property)) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Returns all the variant properties in a block state of the given block
	 * 
	 * @param block
	 * @return
	 */
	public static List<IProperty<? extends Comparable<?>>> getVariantProperties(Block block) {
		List<IProperty<? extends Comparable<?>>> variantProps = Lists.newArrayList();
		for (IProperty<? extends Comparable<?>> prop : block.getDefaultState().getProperties().keySet()) {
			if (isVariantProperty(block, prop))
				variantProps.add(prop);
		}
		return variantProps;
	}

}
