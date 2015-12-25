package net.earthcomputer.easyeditors;

import java.util.Map;
import java.util.regex.Pattern;

import net.earthcomputer.easyeditors.gui.GuiNewCommandBlock;
import net.earthcomputer.easyeditors.util.Colors;
import net.earthcomputer.easyeditors.util.Colors.Color;
import net.earthcomputer.easyeditors.util.GeneralUtils;
import net.minecraft.client.gui.GuiCommandBlock;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.NetworkCheckHandler;
import net.minecraftforge.fml.relauncher.Side;

@Mod(modid = EasyEditors.ID, name = EasyEditors.NAME, version = EasyEditors.VERSION, clientSideOnly = true, canBeDeactivated = true, guiFactory = "net.earthcomputer.easyeditors.GuiFactory")
public class EasyEditors {

	public static final String ID = "easyeditors";
	public static final String NAME = "Easy Editors";
	public static final String VERSION = "1.0";

	@Instance(ID)
	public static EasyEditors instance;

	public static final GuiReplacementRegistry GUI_REPLACEMENT_REGISTRY = new GuiReplacementRegistry();

	public Configuration config;
	public boolean active;

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
		FMLCommonHandler.instance().bus().register(this);
		MinecraftForge.EVENT_BUS.register(GUI_REPLACEMENT_REGISTRY);
		GUI_REPLACEMENT_REGISTRY.registerReplacement(GuiCommandBlock.class, GuiNewCommandBlock.class);

		readFromConfig();
	}

	@SubscribeEvent
	public void configChanged(ConfigChangedEvent.PostConfigChangedEvent e) {
		if (ID.equals(e.modID))
			readFromConfig();
	}

	public void readFromConfig() {
		Property prop = config.get("general", "active", true, "Whether Easy Editors should replace GUIs");
		active = prop.getBoolean();
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
