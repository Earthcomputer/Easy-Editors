package net.earthcomputer.easyeditors;

import java.util.Map;
import java.util.regex.Pattern;

import net.earthcomputer.easyeditors.api.EasyEditorsApi;
import net.earthcomputer.easyeditors.api.GuiReplacementRegistry;
import net.earthcomputer.easyeditors.api.ISmartTranslation;
import net.earthcomputer.easyeditors.api.SmartTranslationRegistry;
import net.earthcomputer.easyeditors.api.util.ChatBlocker;
import net.earthcomputer.easyeditors.api.util.Colors;
import net.earthcomputer.easyeditors.api.util.Colors.Color;
import net.earthcomputer.easyeditors.api.util.GeneralUtils;
import net.earthcomputer.easyeditors.gui.GuiNewCommandBlock;
import net.earthcomputer.easyeditors.util.Translate;
import net.earthcomputer.easyeditors.util.TranslateKeys;
import net.minecraft.client.gui.GuiCommandBlock;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.NetworkCheckHandler;
import net.minecraftforge.fml.relauncher.Side;

/**
 * The main mod class of Easy Editors
 * 
 * @author Earthcomputer
 *
 */
@Mod(modid = EasyEditors.ID, name = EasyEditors.NAME, version = EasyEditors.VERSION, clientSideOnly = true, guiFactory = "net.earthcomputer.easyeditors.GuiFactory")
public class EasyEditors {

	/**
	 * The modid of Easy Editors
	 */
	public static final String ID = "easyeditors";
	/**
	 * The name of Easy Editors
	 */
	public static final String NAME = "Easy Editors";
	/**
	 * The version of Easy Editors
	 */
	public static final String VERSION = "alphatests";

	/**
	 * The singleton instance of Easy Editors
	 */
	@Instance(ID)
	public static EasyEditors instance;

	/**
	 * The Easy Editors configuration
	 */
	public Configuration config;

	public CreativeTabs tablessTab;

	/**
	 * A client-side mod must always accept remote clients and servers
	 * 
	 * @param mods
	 * @param remoteSide
	 * @return
	 */
	@NetworkCheckHandler
	public boolean acceptsRemote(Map<String, String> mods, Side remoteSide) {
		return true;
	}

	@EventHandler
	public void preinit(FMLPreInitializationEvent e) {
		MinecraftForge.EVENT_BUS.register(Translate.class);

		config = new Configuration(e.getSuggestedConfigurationFile());
		config.load();

		tablessTab = new CreativeTabs("tabless") {
			@Override
			public ItemStack getTabIconItem() {
				return new ItemStack(Blocks.COMMAND_BLOCK);
			}
		};
		Blocks.MOB_SPAWNER.setCreativeTab(tablessTab);
		Blocks.FARMLAND.setCreativeTab(tablessTab);
		Blocks.LIT_FURNACE.setCreativeTab(tablessTab);
		Blocks.BROWN_MUSHROOM_BLOCK.setCreativeTab(tablessTab);
		Blocks.RED_MUSHROOM_BLOCK.setCreativeTab(tablessTab);
		Blocks.DRAGON_EGG.setCreativeTab(tablessTab);
		Blocks.COMMAND_BLOCK.setCreativeTab(tablessTab);
		Blocks.REPEATING_COMMAND_BLOCK.setCreativeTab(tablessTab);
		Blocks.CHAIN_COMMAND_BLOCK.setCreativeTab(tablessTab);
		Blocks.BARRIER.setCreativeTab(tablessTab);
		Items.FIREWORKS.setCreativeTab(tablessTab);
		Items.COMMAND_BLOCK_MINECART.setCreativeTab(tablessTab);

		SmartTranslationRegistry.registerAlias("entity.MinecartRideable.name", "item.minecart.name");
		SmartTranslationRegistry.registerAlias("entity.MinecartChest.name", "item.minecartChest.name");
		SmartTranslationRegistry.registerAlias("entity.MinecartCommandBlock.name", "item.minecartCommandBlock.name");
		SmartTranslationRegistry.registerAlias("entity.MinecartTNT.name", "item.minecartTnt.name");
		SmartTranslationRegistry.registerAlias("entity.MinecartHopper.name", "item.minecartHopper.name");
		SmartTranslationRegistry.registerAlias("entity.MinecartFurnace.name", "item.minecartFurnace.name");

		SmartTranslationRegistry.registerAlias("entity.LeashKnot.name", "item.leash.name");
		SmartTranslationRegistry.registerAlias("entity.ItemFrame.name", "item.frame.name");
		SmartTranslationRegistry.registerAlias("entity.FireworksRocketEntity.name", "item.fireworks.name");
		SmartTranslationRegistry.registerAlias("entity.WitherSkull.name", "item.skull.wither.name");

		SmartTranslationRegistry.registerTranslation("entity.ThrownEnderpearl.name",
				new ThrownTranslation("item.enderPearl.name"));
		SmartTranslationRegistry.registerTranslation("entity.EyeOfEnderSignal.name",
				new ThrownTranslation("item.eyeOfEnder.name"));
		SmartTranslationRegistry.registerTranslation("entity.ThrownExpBottle.name",
				new ThrownTranslation("item.expBottle.name"));
		SmartTranslationRegistry.registerTranslation("entity.ThrownEgg.name", new ThrownTranslation("item.egg.name"));
	}

	@EventHandler
	public void init(FMLInitializationEvent e) {
		MinecraftForge.EVENT_BUS.register(this);

		GuiReplacementRegistry.getInstance().registerReplacement(GuiCommandBlock.class, GuiNewCommandBlock.class);

		readFromConfig();
	}

	@SubscribeEvent
	public void configChanged(ConfigChangedEvent.PostConfigChangedEvent e) {
		if (ID.equals(e.getModID()))
			readFromConfig();
	}

	/**
	 * Reads from {@link #config}, then saves it if it has changed
	 */
	public void readFromConfig() {
		Property prop = config.get("general", "active", true, "Whether Easy Editors should replace GUIs");
		EasyEditorsApi.isEasyEditorsActive = prop.getBoolean();
		prop.setLanguageKey(TranslateKeys.GUI_EASYEDITORSCONFIG_ACTIVE);

		prop = config.get("general", "obtainDataFrom", "client",
				"Where to obtain data on the server from. Set to server for servers that are likely to lie about their data (e.g. large public servers such as Hypixel)");
		prop.setValidValues(new String[] { "client", "server" });
		ChatBlocker.obtainDataFromServer = prop.getString().equals("server");
		prop.setLanguageKey(TranslateKeys.GUI_EASYEDITORSCONFIG_OBTAINDATAFROM);
		prop.setConfigEntryClass(GuiFactory.EasyEditorsConfigGui.TranslatedCycleValueEntry.class);

		config.addCustomCategoryComment("colors",
				"This changes all the colors in things like the command editor.\n"
						+ "For six-digit colors, the format is RRGGBB, where R is the red\n"
						+ "component of the color, G is the green component of the color\n"
						+ "and B is the blue component of the color. Where 8 digits appear,\n"
						+ "the format is AARRGGBB, where A is the alpha (opacity) component of the color");
		for (Color color : Colors.allColors()) {
			prop = config.get("colors", color.name,
					String.format(color.includesAlpha ? "%08X" : "%06X", color.defaultColor), color.description,
					Pattern.compile(color.includesAlpha ? "[0-9a-fA-F]{8}" : "[0-9a-fA-F]{6}"));
			color.color = GeneralUtils.hexToInt(prop.getString());
			prop.setLanguageKey("gui.easyeditorsconfig.colors." + color.name);
			prop.setConfigEntryClass(GuiFactory.EasyEditorsConfigGui.ColorEntry.class);
		}

		if (config.hasChanged())
			config.save();
	}

	private static class ThrownTranslation implements ISmartTranslation {
		private String thrownObject;

		public ThrownTranslation(String thrownObject) {
			this.thrownObject = thrownObject;
		}

		@Override
		public String translateToLocal(String language, Map<String, String> translations) {
			return String.format(translations.get(TranslateKeys.SMARTTRANSLATIONS_ENTITY_THROWN),
					translations.get(thrownObject));
		}
	}

}
