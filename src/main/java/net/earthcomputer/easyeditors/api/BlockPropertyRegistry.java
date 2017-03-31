package net.earthcomputer.easyeditors.api;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.tuple.Pair;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.minecraft.block.Block;
import net.minecraft.block.BlockAnvil;
import net.minecraft.block.BlockBanner;
import net.minecraft.block.BlockBed;
import net.minecraft.block.BlockBeetroot;
import net.minecraft.block.BlockBrewingStand;
import net.minecraft.block.BlockButton;
import net.minecraft.block.BlockCactus;
import net.minecraft.block.BlockCake;
import net.minecraft.block.BlockCarpet;
import net.minecraft.block.BlockCauldron;
import net.minecraft.block.BlockChorusFlower;
import net.minecraft.block.BlockChorusPlant;
import net.minecraft.block.BlockCocoa;
import net.minecraft.block.BlockColored;
import net.minecraft.block.BlockCommandBlock;
import net.minecraft.block.BlockCrops;
import net.minecraft.block.BlockDaylightDetector;
import net.minecraft.block.BlockDirectional;
import net.minecraft.block.BlockDirt;
import net.minecraft.block.BlockDispenser;
import net.minecraft.block.BlockDoor;
import net.minecraft.block.BlockDoublePlant;
import net.minecraft.block.BlockEndPortalFrame;
import net.minecraft.block.BlockFarmland;
import net.minecraft.block.BlockFence;
import net.minecraft.block.BlockFenceGate;
import net.minecraft.block.BlockFire;
import net.minecraft.block.BlockFlowerPot;
import net.minecraft.block.BlockFrostedIce;
import net.minecraft.block.BlockGrass;
import net.minecraft.block.BlockHopper;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.BlockHugeMushroom;
import net.minecraft.block.BlockJukebox;
import net.minecraft.block.BlockLeaves;
import net.minecraft.block.BlockLever;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.BlockLog;
import net.minecraft.block.BlockMycelium;
import net.minecraft.block.BlockNetherWart;
import net.minecraft.block.BlockNewLeaf;
import net.minecraft.block.BlockNewLog;
import net.minecraft.block.BlockObserver;
import net.minecraft.block.BlockOldLeaf;
import net.minecraft.block.BlockOldLog;
import net.minecraft.block.BlockPane;
import net.minecraft.block.BlockPistonBase;
import net.minecraft.block.BlockPistonExtension;
import net.minecraft.block.BlockPistonMoving;
import net.minecraft.block.BlockPlanks;
import net.minecraft.block.BlockPortal;
import net.minecraft.block.BlockPressurePlate;
import net.minecraft.block.BlockPressurePlateWeighted;
import net.minecraft.block.BlockPrismarine;
import net.minecraft.block.BlockPurpurSlab;
import net.minecraft.block.BlockQuartz;
import net.minecraft.block.BlockRail;
import net.minecraft.block.BlockRailDetector;
import net.minecraft.block.BlockRailPowered;
import net.minecraft.block.BlockRedSandstone;
import net.minecraft.block.BlockRedstoneComparator;
import net.minecraft.block.BlockRedstoneRepeater;
import net.minecraft.block.BlockRedstoneWire;
import net.minecraft.block.BlockReed;
import net.minecraft.block.BlockRotatedPillar;
import net.minecraft.block.BlockSand;
import net.minecraft.block.BlockSandStone;
import net.minecraft.block.BlockSapling;
import net.minecraft.block.BlockShulkerBox;
import net.minecraft.block.BlockSilverfish;
import net.minecraft.block.BlockSkull;
import net.minecraft.block.BlockSlab;
import net.minecraft.block.BlockSnow;
import net.minecraft.block.BlockSponge;
import net.minecraft.block.BlockStainedGlass;
import net.minecraft.block.BlockStainedGlassPane;
import net.minecraft.block.BlockStairs;
import net.minecraft.block.BlockStandingSign;
import net.minecraft.block.BlockStem;
import net.minecraft.block.BlockStone;
import net.minecraft.block.BlockStoneBrick;
import net.minecraft.block.BlockStoneSlab;
import net.minecraft.block.BlockStoneSlabNew;
import net.minecraft.block.BlockStructure;
import net.minecraft.block.BlockTNT;
import net.minecraft.block.BlockTallGrass;
import net.minecraft.block.BlockTorch;
import net.minecraft.block.BlockTrapDoor;
import net.minecraft.block.BlockTripWire;
import net.minecraft.block.BlockTripWireHook;
import net.minecraft.block.BlockVine;
import net.minecraft.block.BlockWall;
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

	private static final List<Pair<Predicate<Block>, IProperty<?>>> variants = Lists.newArrayList();
	private static final Map<IProperty<?>, String> unlocalizedNames = Maps.newIdentityHashMap();
	private static final Map<PropertyValueId, String> valueUnlocalizedNames = Maps.newHashMap();

	public static void init() {
		registerVanillaVariantProps();
		registerVanillaUnlocalizedNames();
	}

	private static void registerVanillaVariantProps() {
		// TODO: omit similar blocks
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
		registerVariantProperty(BlockQuartz.VARIANT);
		registerVariantProperty(BlockCarpet.COLOR);
		registerVariantProperty(BlockDoublePlant.VARIANT);
		registerVariantProperty(BlockStainedGlass.COLOR);
		registerVariantProperty(BlockStainedGlassPane.COLOR);
		registerVariantProperty(BlockPrismarine.VARIANT);
		registerVariantProperty(BlockRedSandstone.TYPE);
		registerVariantProperty(BlockStoneSlabNew.VARIANT);
	}

	private static void registerVanillaUnlocalizedNames() {
		setUnlocalizedNamesForProp(BlockDirectional.FACING, "directional.facing", null);

		setUnlocalizedNamesForProp(BlockHorizontal.FACING, "horizontal.facing", "directional.facing");

		setUnlocalizedNamesForProp(BlockRotatedPillar.AXIS, "pillar.axis", null);

		setUnlocalizedNamesForProp(BlockColored.COLOR, "colored.color", null);

		setUnlocalizedNamesForProp(BlockSlab.HALF, "slab.half", null);

		setUnlocalizedNamesForProp(BlockStone.VARIANT, "stone.variant", null);
		setUnlocalizedNameAlias("stone.variant", "variant");

		setUnlocalizedName(BlockGrass.SNOWY, "grass.snowy");

		setUnlocalizedNamesForProp(BlockDirt.VARIANT, "dirt.variant", null);
		setUnlocalizedNameAlias("dirt.variant", "variant");
		setUnlocalizedName(BlockDirt.SNOWY, "dirt.snowy");
		setUnlocalizedNameAlias("dirt.snowy", "grass.snowy");

		setUnlocalizedNamesForProp(BlockPlanks.VARIANT, "planks.variant", null);
		setUnlocalizedNameAlias("planks.variant", "variant");

		setUnlocalizedNamesForProp(BlockSapling.TYPE, "sapling.type", "variant");
		setUnlocalizedName(BlockSapling.STAGE, "sapling.stage");
		setUnlocalizedNameAlias("sapling.stage", "plant.age");

		setUnlocalizedName(BlockLiquid.LEVEL, "liquid.level");

		setUnlocalizedNamesForProp(BlockSand.VARIANT, "sand.variant", null);
		setUnlocalizedNameAlias("sand.variant", "variant");

		setUnlocalizedNamesForProp(BlockOldLog.VARIANT, "log.variant", "variant");
		setUnlocalizedNamesForProp(BlockLog.LOG_AXIS, "log.axis", null);
		setUnlocalizedNameAlias("log.axis", "pillar.axis");
		setUnlocalizedNameAlias("log.axis.x", "pillar.axis.x");
		setUnlocalizedNameAlias("log.axis.y", "pillar.axis.y");
		setUnlocalizedNameAlias("log.axis.z", "pillar.axis.z");

		setUnlocalizedNamesForProp(BlockOldLeaf.VARIANT, "leaves.variant", "variant");
		setUnlocalizedName(BlockLeaves.CHECK_DECAY, "leaves.check_decay");
		setUnlocalizedName(BlockLeaves.DECAYABLE, "leaves.decayable");

		setUnlocalizedName(BlockSponge.WET, "sponge.wet");

		setUnlocalizedName(BlockDispenser.TRIGGERED, "dispenser.triggered");

		setUnlocalizedNamesForProp(BlockSandStone.TYPE, "sandstone.type", null);

		setUnlocalizedNamesForProp(BlockBed.PART, "bed.part", null);
		setUnlocalizedName(BlockBed.OCCUPIED, "bed.occupied");

		setUnlocalizedName(BlockRailPowered.POWERED, "powered_rail.powered");
		setUnlocalizedNameAlias("powered_rail.powered", "rail.powered");
		setUnlocalizedNamesForProp(BlockRailPowered.SHAPE, "powered_rail.shape", "rail.shape");

		setUnlocalizedName(BlockRailDetector.POWERED, "detector_rail.powered");
		setUnlocalizedNameAlias("detector_rail.powered", "rail.powered");
		setUnlocalizedNamesForProp(BlockRailDetector.SHAPE, "detector_rail.shape", "rail.shape");

		setUnlocalizedName(BlockPistonBase.EXTENDED, "piston.extended");

		setUnlocalizedNamesForProp(BlockTallGrass.TYPE, "tallgrass.type", null);

		setUnlocalizedNamesForProp(BlockPistonExtension.TYPE, "piston_head.type", null);
		setUnlocalizedName(BlockPistonExtension.SHORT, "piston_head.short");

		setUnlocalizedNamesForProp(Blocks.YELLOW_FLOWER.getTypeProperty(), "yellow_flower.type", null);

		setUnlocalizedNamesForProp(Blocks.RED_FLOWER.getTypeProperty(), "red_flower.type", null);

		setUnlocalizedName(BlockStoneSlab.SEAMLESS, "stone_slab.seamless");
		setUnlocalizedNamesForProp(BlockStoneSlab.VARIANT, "stone_slab.variant", null);
		setUnlocalizedNameAlias("stone_slab.variant", "variant");

		setUnlocalizedName(BlockTNT.EXPLODE, "tnt.explode");

		setUnlocalizedNamesForProp(BlockTorch.FACING, "torch.facing", "directional.facing");

		setUnlocalizedName(BlockFire.AGE, "fire.age");
		setUnlocalizedNameAlias("fire.age", "plant.age");
		setUnlocalizedNamesForBooleanFacingProps("fire", null, BlockFire.UPPER, BlockFire.WEST, BlockFire.EAST,
				BlockFire.NORTH, BlockFire.SOUTH);

		setUnlocalizedNamesForProp(BlockStairs.HALF, "stairs.half", "slab.half");
		setUnlocalizedNamesForProp(BlockStairs.SHAPE, "stairs.shape", null);

		setUnlocalizedName(BlockRedstoneWire.POWER, "redstone.power");
		// Darn BlockRedstoneWire.EnumAttachPosition being private causes
		// problems which I am forced to overcome
		for (IProperty<?> attachPosProp : new IProperty[] { BlockRedstoneWire.WEST, BlockRedstoneWire.EAST,
				BlockRedstoneWire.NORTH, BlockRedstoneWire.SOUTH }) {
			setUnlocalizedName(attachPosProp, "redstone." + attachPosProp.getName());
			setUnlocalizedNameAlias("redstone." + attachPosProp.getName(),
					"directional.facing." + attachPosProp.getName());
			setRedstoneWireFacingValueNames(attachPosProp);
		}

		setUnlocalizedName(BlockCrops.AGE, "crops.age");
		setUnlocalizedNameAlias("crops.age", "plant.age");

		setUnlocalizedName(BlockFarmland.MOISTURE, "farmland.moisture");

		String[] signRotations = { "s", "ssw", "sw", "wsw", "w", "wnw", "nw", "nnw", "n", "nne", "ne", "ene", "e",
				"ese", "se", "sse" };
		setUnlocalizedName(BlockStandingSign.ROTATION, "sign.rotation");
		for (int i = 0; i < 16; i++) {
			setValueUnlocalizedName(BlockStandingSign.ROTATION, i, "sign.rotation." + signRotations[i]);
		}

		setUnlocalizedName(BlockDoor.OPEN, "door.open");
		setUnlocalizedNamesForProp(BlockDoor.HINGE, "door.hinge", null);
		setUnlocalizedName(BlockDoor.POWERED, "door.powered");
		setUnlocalizedNameAlias("door.powered", "redstone.powered");
		setUnlocalizedNamesForProp(BlockDoor.HALF, "door.half", null);
		setUnlocalizedNameAlias("door.half", "slab.half");
		setUnlocalizedNameAlias("door.half.lower", "slab.half.bottom");
		setUnlocalizedNameAlias("door.half.upper", "slab.half.top");

		setUnlocalizedNamesForProp(BlockRail.SHAPE, "rail.shape", null);

		setUnlocalizedNamesForProp(BlockLever.FACING, "lever.facing", null);
		setUnlocalizedName(BlockLever.POWERED, "lever.powered");
		setUnlocalizedNameAlias("lever.powered", "redstone.powered");

		setUnlocalizedName(BlockPressurePlate.POWERED, "pressure_plate.powered");
		setUnlocalizedNameAlias("pressure_plate.powered", "redstone.powered");

		setUnlocalizedName(BlockButton.POWERED, "button.powered");
		setUnlocalizedNameAlias("button.powered", "redstone.powered");

		setUnlocalizedName(BlockSnow.LAYERS, "snow_layer.layers");

		setUnlocalizedName(BlockCactus.AGE, "cactus.age");
		setUnlocalizedNameAlias("cactus.age", "plant.age");

		setUnlocalizedName(BlockReed.AGE, "reeds.age");
		setUnlocalizedNameAlias("reeds.age", "plant.age");

		setUnlocalizedName(BlockJukebox.HAS_RECORD, "jukebox.has_record");

		setUnlocalizedNamesForBooleanFacingProps("fence", null, null, BlockFence.WEST, BlockFence.EAST,
				BlockFence.NORTH, BlockFence.SOUTH);

		setUnlocalizedNamesForProp(BlockPortal.AXIS, "portal.axis", "pillar.axis");

		setUnlocalizedName(BlockCake.BITES, "cake.bites");

		setUnlocalizedName(BlockRedstoneRepeater.LOCKED, "repeater.locked");
		setUnlocalizedName(BlockRedstoneRepeater.DELAY, "repeater.delay");

		setUnlocalizedNamesForProp(BlockStainedGlass.COLOR, "stained_glass.color", "colored.color");

		setUnlocalizedName(BlockTrapDoor.OPEN, "trapdoor.open");
		setUnlocalizedNameAlias("trapdoor.open", "door.open");
		setUnlocalizedNamesForProp(BlockTrapDoor.HALF, "trapdoor.half", "slab.half");

		setUnlocalizedNamesForProp(BlockSilverfish.VARIANT, "monster_egg.variant", null);
		setUnlocalizedNameAlias("monster_egg.variant", "variant");

		setUnlocalizedNamesForProp(BlockStoneBrick.VARIANT, "stonebrick.variant", null);
		setUnlocalizedNameAlias("stonebrick.variant", "variant");

		setUnlocalizedNamesForProp(BlockHugeMushroom.VARIANT, "mushroom_block.variant", null);
		setUnlocalizedNameAlias("mushroom_block.variant", "variant");

		setUnlocalizedNamesForBooleanFacingProps("pane", null, null, BlockPane.WEST, BlockPane.EAST, BlockPane.NORTH,
				BlockPane.SOUTH);

		setUnlocalizedName(BlockStem.AGE, "stem.age");
		setUnlocalizedNameAlias("stem.age", "plant.age");

		setUnlocalizedNamesForBooleanFacingProps("vine", null, BlockVine.UP, BlockVine.WEST, BlockVine.EAST,
				BlockVine.NORTH, BlockVine.SOUTH);

		setUnlocalizedName(BlockFenceGate.OPEN, "fence_gate.open");
		setUnlocalizedNameAlias("fence_gate.open", "door.open");
		setUnlocalizedName(BlockFenceGate.POWERED, "fence_gate.powered");
		setUnlocalizedNameAlias("fence_gate.powered", "redstone.powered");
		setUnlocalizedName(BlockFenceGate.IN_WALL, "fence_gate.in_wall");

		setUnlocalizedName(BlockMycelium.SNOWY, "mycelium.snowy");
		setUnlocalizedNameAlias("mycelium.snowy", "grass.snowy");

		setUnlocalizedName(BlockNetherWart.AGE, "nether_wart.age");
		setUnlocalizedNameAlias("nether_wart.age", "plant.age");

		for (int i = 0; i < 3; i++) {
			setUnlocalizedName(BlockBrewingStand.HAS_BOTTLE[i], "brewing_stand.has_bottle_" + i);
		}

		setUnlocalizedName(BlockCauldron.LEVEL, "cauldron.level");

		setUnlocalizedName(BlockEndPortalFrame.EYE, "end_portal_frame.eye");

		setUnlocalizedNamesForProp(BlockWoodSlab.VARIANT, "wooden_slab.variant", "variant");

		setUnlocalizedName(BlockCocoa.AGE, "cocoa.age");
		setUnlocalizedNameAlias("cocoa.age", "plant.age");

		setUnlocalizedName(BlockTripWireHook.POWERED, "tripwire_hook.powered");
		setUnlocalizedNameAlias("tripwire_hook.powered", "redstone.powered");
		setUnlocalizedName(BlockTripWireHook.ATTACHED, "tripwire_hook.attached");

		setUnlocalizedName(BlockTripWire.POWERED, "tripwire.powered");
		setUnlocalizedNameAlias("tripwire.powered", "redstone.powered");
		setUnlocalizedName(BlockTripWire.ATTACHED, "tripwire.attached");
		setUnlocalizedNameAlias("tripwire.attached", "tripwire_hook.attached");
		setUnlocalizedName(BlockTripWire.DISARMED, "tripwire.disarmed");
		setUnlocalizedNamesForBooleanFacingProps("tripwire", null, null, BlockTripWire.WEST, BlockTripWire.EAST,
				BlockTripWire.NORTH, BlockTripWire.SOUTH);

		setUnlocalizedName(BlockCommandBlock.CONDITIONAL, "command_block.conditional");

		setUnlocalizedNamesForBooleanFacingProps("wall", null, BlockWall.UP, BlockWall.WEST, BlockWall.EAST,
				BlockWall.NORTH, BlockWall.SOUTH);
		setUnlocalizedNamesForProp(BlockWall.VARIANT, "wall.variant", null);
		setUnlocalizedNameAlias("wall.variant", "variant");

		setUnlocalizedName(BlockFlowerPot.LEGACY_DATA, "flower_pot.legacy_data");
		setUnlocalizedNamesForProp(BlockFlowerPot.CONTENTS, "flower_pot.contents", null);

		setUnlocalizedName(BlockSkull.NODROP, "skull.nodrop");

		setUnlocalizedName(BlockAnvil.DAMAGE, "anvil.damage");
		setValueUnlocalizedName(BlockAnvil.DAMAGE, 0, "anvil.damage.none");
		setValueUnlocalizedName(BlockAnvil.DAMAGE, 1, "anvil.damage.slightly_damaged");
		setValueUnlocalizedName(BlockAnvil.DAMAGE, 2, "anvil.damage.very_damaged");

		setUnlocalizedName(BlockPressurePlateWeighted.POWER, "pressure_plate.power");
		setUnlocalizedNameAlias("pressure_plate.power", "redstone.power");

		setUnlocalizedName(BlockRedstoneComparator.POWERED, "comparator.powered");
		setUnlocalizedNameAlias("comparator.powered", "redstone.powered");
		setUnlocalizedNamesForProp(BlockRedstoneComparator.MODE, "comparator.mode", null);

		setUnlocalizedName(BlockDaylightDetector.POWER, "daylight_detector.power");
		setUnlocalizedNameAlias("daylight_detector.power", "redstone.power");

		setUnlocalizedNamesForProp(BlockHopper.FACING, "hopper.facing", "directional.facing");
		setUnlocalizedName(BlockHopper.ENABLED, "hopper.enabled");

		setUnlocalizedNamesForProp(BlockQuartz.VARIANT, "quartz.variant", null);
		setUnlocalizedNameAlias("quartz.variant", "variant");

		setUnlocalizedNamesForProp(BlockStainedGlassPane.COLOR, "stained_glass_pane.color", "colored.color");

		setUnlocalizedNamesForProp(BlockNewLeaf.VARIANT, "leaves2.variant", "variant");

		setUnlocalizedNamesForProp(BlockNewLog.VARIANT, "log2.variant", "variant");

		setUnlocalizedNamesForProp(BlockPrismarine.VARIANT, "prismarine.variant", null);
		setUnlocalizedNameAlias("prismarine.variant", "variant");

		setUnlocalizedNamesForProp(BlockCarpet.COLOR, "carpet.color", "colored.color");

		setUnlocalizedNamesForProp(BlockDoublePlant.VARIANT, "double_plant.variant", null);
		setUnlocalizedNameAlias("double_plant.variant", "variant");
		setUnlocalizedNamesForProp(BlockDoublePlant.HALF, "double_plant.half", null);
		setUnlocalizedNameAlias("double_plant.half", "slab.half");
		setUnlocalizedNameAlias("double_plant.half.lower", "slab.half.bottom");
		setUnlocalizedNameAlias("double_plant.half.upper", "slab.half.top");

		setUnlocalizedName(BlockBanner.ROTATION, "banner.rotation");
		setUnlocalizedNameAlias("banner.rotation", "sign.rotation");
		for (int i = 0; i < 16; i++) {
			setValueUnlocalizedName(BlockBanner.ROTATION, i, "banner.rotation." + signRotations[i]);
			setUnlocalizedNameAlias("banner.rotation." + signRotations[i], "sign.rotation." + signRotations[i]);
		}

		setUnlocalizedNamesForProp(BlockRedSandstone.TYPE, "red_sandstone.type", null);
		setUnlocalizedNameAlias("red_sandstone.type", "sandstone.type");
		setUnlocalizedNameAlias("red_sandstone.type.red_sandstone", "sandstone.type.sandstone");
		setUnlocalizedNameAlias("red_sandstone.type.chiseled_red_sandstone", "sandstone.type.chiseled_sandstone");
		setUnlocalizedNameAlias("red_sandstone.type.smooth_red_sandstone", "sandstone.type.smooth_sandstone");

		setUnlocalizedName(BlockStoneSlabNew.SEAMLESS, "stone_slab2.seamless");
		setUnlocalizedNameAlias("stone_slab2.seamless", "stone_slab.seamless");
		setUnlocalizedNamesForProp(BlockStoneSlabNew.VARIANT, "stone_slab2.variant", null);
		setUnlocalizedNameAlias("stone_slab2.variant", "variant");

		setUnlocalizedNamesForBooleanFacingProps("chorus_plant", BlockChorusPlant.DOWN, BlockChorusPlant.UP,
				BlockChorusPlant.WEST, BlockChorusPlant.EAST, BlockChorusPlant.NORTH, BlockChorusPlant.SOUTH);

		setUnlocalizedName(BlockChorusFlower.AGE, "chorus_flower.age");
		setUnlocalizedNameAlias("chorus_flower.age", "plant.age");

		setUnlocalizedNamesForProp(BlockPurpurSlab.VARIANT, "purpur_slab.variant", null);
		setUnlocalizedNameAlias("purpur_slab.variant", "variant");

		setUnlocalizedName(BlockBeetroot.BEETROOT_AGE, "beetroot.age");
		setUnlocalizedNameAlias("beetroot.age", "plant.age");

		setUnlocalizedName(BlockFrostedIce.AGE, "frosted_ice.age");
		setUnlocalizedNameAlias("frosted_ice.age", "plant.age");

		setUnlocalizedName(BlockObserver.POWERED, "observer.powered");
		setUnlocalizedNameAlias("observer.powered", "redstone.powered");

		setUnlocalizedNamesForProp(BlockShulkerBox.FACING, "shulker_box.facing", "directional.facing");

		setUnlocalizedNamesForProp(BlockStructure.MODE, "structure_block.mode", null);
	}

	private static <T extends Comparable<T>> void setRedstoneWireFacingValueNames(IProperty<T> prop) {
		for (T value : prop.getAllowedValues()) {
			setValueUnlocalizedName(prop, value, "redstone." + prop.getName() + "." + prop.getName(value));
			setUnlocalizedNameAlias("redstone." + prop.getName() + "." + prop.getName(value),
					"redstone.attach_pos." + prop.getName(value));
		}
	}

	/**
	 * Registers an IProperty to be counted as a variant property. More about
	 * variant properties {@link #registerVariantProperty(Predicate, IProperty)
	 * here}
	 * 
	 * @param property
	 */
	public static <T extends Comparable<T>> void registerVariantProperty(IProperty<T> property) {
		registerVariantProperty(Predicates.<Block>alwaysTrue(), property);
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
	@SuppressWarnings("unchecked")
	public static void registerVariantProperty(Predicate<Block> blockPredicate, IProperty<?> property) {
		// Gotta love Java Generics
		((List<Pair<Predicate<Block>, Object>>) (List<?>) variants).add(Pair.of(blockPredicate, (Object) property));
	}

	/**
	 * Returns whether property is a variant property when in a block state of
	 * the given block
	 * 
	 * @param block
	 * @param property
	 * @return
	 */
	public static boolean isVariantProperty(Block block, IProperty<?> property) {
		for (Pair<Predicate<Block>, IProperty<?>> entry : variants) {
			if (entry.getLeft().apply(block)) {
				if (entry.getRight() == property) {
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
	public static List<IProperty<?>> getVariantProperties(Block block) {
		List<IProperty<?>> variantProps = Lists.newArrayList();
		for (IProperty<?> prop : block.getDefaultState().getPropertyKeys()) {
			if (isVariantProperty(block, prop))
				variantProps.add(prop);
		}
		return variantProps;
	}

	/**
	 * Registers an alias from name to alias
	 * 
	 * @param name
	 * @param alias
	 */
	public static void setUnlocalizedNameAlias(String name, String alias) {
		SmartTranslationRegistry.registerAlias("property." + name, "property." + alias);
	}

	/**
	 * Sets the unlocalized name of a property
	 * 
	 * @param prop
	 * @param unlocalizedName
	 */
	public static void setUnlocalizedName(IProperty<?> prop, String unlocalizedName) {
		unlocalizedNames.put(prop, unlocalizedName);
	}

	/**
	 * Gets the unlocalized name of a property, or <code>null</code> if none has
	 * been registered
	 * 
	 * @param prop
	 * @return
	 */
	public static String getUnlocalizedName(IProperty<?> prop) {
		String unlocalizedName = unlocalizedNames.get(prop);
		if (unlocalizedName == null) {
			return null;
		}
		return "property." + unlocalizedName;
	}

	/**
	 * Automatically fills in unlocalized names for the given property
	 * 
	 * @param prop
	 * @param unlocalizedName
	 * @param alias
	 */
	public static <T extends Comparable<T>> void setUnlocalizedNamesForProp(IProperty<T> prop, String unlocalizedName,
			String alias) {
		setUnlocalizedName(prop, unlocalizedName);
		if (alias != null) {
			setUnlocalizedNameAlias(unlocalizedName, alias);
		}
		for (T value : prop.getAllowedValues()) {
			setValueUnlocalizedName(prop, value, unlocalizedName + "." + prop.getName(value));
			if (alias != null) {
				setUnlocalizedNameAlias(unlocalizedName + "." + prop.getName(value), alias + "." + prop.getName(value));
			}
		}
	}

	public static void setUnlocalizedNamesForBooleanFacingProps(String unlocalizedNamePrefix, IProperty<Boolean> down,
			IProperty<Boolean> up, IProperty<Boolean> west, IProperty<Boolean> east, IProperty<Boolean> north,
			IProperty<Boolean> south) {
		if (down != null) {
			setUnlocalizedName(down, unlocalizedNamePrefix + ".down");
			setUnlocalizedNameAlias(unlocalizedNamePrefix + ".down", "directional.facing.down");
		}
		if (up != null) {
			setUnlocalizedName(up, unlocalizedNamePrefix + ".up");
			setUnlocalizedNameAlias(unlocalizedNamePrefix + ".up", "directional.facing.up");
		}
		if (west != null) {
			setUnlocalizedName(west, unlocalizedNamePrefix + ".west");
			setUnlocalizedNameAlias(unlocalizedNamePrefix + ".west", "directional.facing.west");
		}
		if (east != null) {
			setUnlocalizedName(east, unlocalizedNamePrefix + ".east");
			setUnlocalizedNameAlias(unlocalizedNamePrefix + ".east", "directional.facing.east");
		}
		if (north != null) {
			setUnlocalizedName(north, unlocalizedNamePrefix + ".north");
			setUnlocalizedNameAlias(unlocalizedNamePrefix + ".north", "directional.facing.north");
		}
		if (south != null) {
			setUnlocalizedName(south, unlocalizedNamePrefix + ".south");
			setUnlocalizedNameAlias(unlocalizedNamePrefix + ".south", "directional.facing.south");
		}
	}

	/**
	 * Sets the unlocalized name of a property value
	 * 
	 * @param prop
	 * @param value
	 * @param unlocalizedName
	 */
	public static <T extends Comparable<T>> void setValueUnlocalizedName(IProperty<T> prop, T value,
			String unlocalizedName) {
		valueUnlocalizedNames.put(new PropertyValueId(prop, value), unlocalizedName);
	}

	/**
	 * Gets the unlocalized name of a property value, or <code>null</code> if
	 * none has been registered
	 * 
	 * @param prop
	 * @param value
	 * @return
	 */
	public static <T extends Comparable<T>> String getValueUnlocalizedName(IProperty<T> prop, T value) {
		String unlocalizedName = valueUnlocalizedNames.get(new PropertyValueId(prop, value));
		if (unlocalizedName != null) {
			return "property." + unlocalizedName;
		}
		return null;
	}

	private static class PropertyValueId {
		private IProperty<?> prop;
		private Object value;

		public PropertyValueId(IProperty<?> prop, Object value) {
			this.prop = prop;
			this.value = value;
		}

		@Override
		public int hashCode() {
			return System.identityHashCode(prop) + 31 * value.hashCode();
		}

		@Override
		public boolean equals(Object other) {
			if (other == this) {
				return true;
			} else if (!(other instanceof PropertyValueId)) {
				return false;
			} else {
				PropertyValueId otherId = (PropertyValueId) other;
				return prop == otherId.prop && value.equals(otherId.value);
			}
		}
	}

}
