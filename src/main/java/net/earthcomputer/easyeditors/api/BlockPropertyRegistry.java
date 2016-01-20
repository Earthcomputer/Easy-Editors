package net.earthcomputer.easyeditors.api;

import java.util.List;
import java.util.Map;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.minecraft.block.Block;
import net.minecraft.block.BlockAnvil;
import net.minecraft.block.BlockBanner.BlockBannerStanding;
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

public class BlockPropertyRegistry {

	static {
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
		registerVariantProperty(Blocks.yellow_flower.getTypeProperty());
		registerVariantProperty(Blocks.red_flower.getTypeProperty());
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

	private static Map<Predicate<Block>, IProperty> variants = Maps.newHashMap();

	public static void registerVariantProperty(IProperty property) {
		registerVariantProperty((Predicate<Block>) (Predicate<?>) Predicates.alwaysTrue(), property);
	}

	public static void registerVariantProperty(Block block, IProperty property) {
		registerVariantProperty(Predicates.equalTo(block), property);
	}

	public static void registerVariantProperty(Predicate<Block> blockPredicate, IProperty property) {
		variants.put(blockPredicate, property);
	}

	public static boolean isVariantProperty(Block block, IProperty property) {
		for (Map.Entry<Predicate<Block>, IProperty> entry : variants.entrySet()) {
			if (entry.getKey().apply(block)) {
				if (entry.getValue().equals(property)) {
					return true;
				}
			}
		}
		return false;
	}

	public static IProperty[] getVariantProperties(Block block) {
		List<IProperty> variantProps = Lists.newArrayList();
		for (Object obj : block.getDefaultState().getProperties().entrySet()) {
			IProperty prop = (IProperty) obj;
			if (isVariantProperty(block, prop))
				variantProps.add(prop);
		}
		return variantProps.toArray(new IProperty[variantProps.size()]);
	}

}
