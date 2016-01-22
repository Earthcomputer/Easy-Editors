package net.earthcomputer.easyeditors;

import java.util.Map;
import java.util.regex.Pattern;

import net.earthcomputer.easyeditors.api.Colors;
import net.earthcomputer.easyeditors.api.Colors.Color;
import net.earthcomputer.easyeditors.api.EasyEditorsApi;
import net.earthcomputer.easyeditors.api.GeneralUtils;
import net.earthcomputer.easyeditors.api.GuiReplacementRegistry;
import net.earthcomputer.easyeditors.gui.GuiNewCommandBlock;
import net.minecraft.client.gui.GuiCommandBlock;
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
	public static final String VERSION = "1.0";

	/**
	 * The singleton instance of Easy Editors
	 */
	@Instance(ID)
	public static EasyEditors instance;

	/**
	 * The Easy Editors configuration
	 */
	public Configuration config;

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
		config = new Configuration(e.getSuggestedConfigurationFile());
		config.load();
	}

	@EventHandler
	public void init(FMLInitializationEvent e) {
		//FMLCommonHandler.instance().bus().register(this);
		MinecraftForge.EVENT_BUS.register(this);

		GuiReplacementRegistry.getInstance().registerReplacement(GuiCommandBlock.class, GuiNewCommandBlock.class);

		readFromConfig();
	}

	@SubscribeEvent
	public void configChanged(ConfigChangedEvent.PostConfigChangedEvent e) {
		if (ID.equals(e.modID))
			readFromConfig();
	}

	/**
	 * Reads from {@link #config}, then saves it if it has changed
	 */
	public void readFromConfig() {
		Property prop = config.get("general", "active", true, "Whether Easy Editors should replace GUIs");
		EasyEditorsApi.isEasyEditorsActive = prop.getBoolean();
		prop.setLanguageKey("gui.easyeditorsconfig.active");
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

}
